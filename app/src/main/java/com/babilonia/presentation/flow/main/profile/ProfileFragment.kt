package com.babilonia.presentation.flow.main.profile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.babilonia.Constants
import com.babilonia.R
import com.babilonia.databinding.ProfileFragmentBinding
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.extension.withGlide
import com.babilonia.presentation.utils.RealPathUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.marchinram.rxgallery.RxGallery
import com.tbruyelle.rxpermissions2.RxPermissions
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


private const val PICK_IMAGE_FROM_GALLERY = 999
private const val REQUEST_TAKE_PHOTO = 500
private const val SAMPLE_CROPPED_IMAGE_NAME = "cropped_image.jpg"

class ProfileFragment : BaseFragment<ProfileFragmentBinding, ProfileViewModel>() {

    private var progressDialog: AlertDialog? = null
    private var currentPhotoPath: String? = null
    override fun viewCreated() {
        viewModel.getUser()
        binding.model = viewModel
        setupClicks()
        viewModel.checkDeeplinks()
    }

    override fun startListenToEvents() {
        super.startListenToEvents()
        viewModel.userLiveData.observe(this, Observer {
            binding.ivProfileAvatar.withGlide(it.avatar, R.drawable.ic_profile_placeholder)
            val manager = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            binding.tvPhoneValue.text =
                PhoneNumberUtils.formatNumber(it.phoneNumber, manager.simCountryIso.toUpperCase(Locale.ROOT))
        })
        viewModel.photoUploadProgressLiveData.observe(this, Observer {
            if (it) {
                showProgress()
            } else {
                hideProgress()
            }
        })
    }

    override fun stopListenToEvents() {
        super.stopListenToEvents()
        viewModel.userLiveData.removeObservers(this)
        viewModel.photoUploadProgressLiveData.removeObservers(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_GALLERY) {
            data?.data?.let {
                cropImage(RealPathUtil.getRealPathFromURI(requireContext(), it))
            }
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            currentPhotoPath?.let {
                cropImage(it)
            }
        } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = data?.let { UCrop.getOutput(it) }
            context?.let {
                if (resultUri != null) {
                    viewModel.compressImageAndUpload(it, resultUri)
                }
            }
        }
    }

    private fun cropImage(uri: String?) {
        val sourceUri = Uri.fromFile(File(uri))
        val options = UCrop.Options().apply {
            setRootViewBackgroundColor(Color.WHITE)
            setActiveControlsWidgetColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
            setHideBottomControls(true)
            setFreeStyleCropEnabled(true)
            setMaxBitmapSize(Constants.MAX_IMAGE_RESOLUTION)
        }
        UCrop.of(sourceUri, Uri.fromFile(File(requireContext().cacheDir, SAMPLE_CROPPED_IMAGE_NAME)))
            .withOptions(options)
            .start(requireContext(), this)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.babilonia.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun pickImageFromGallery() {
        RxGallery.gallery(requireActivity(), false, RxGallery.MimeType.IMAGE)
            .subscribe(
                { uris ->
                    cropImage(RealPathUtil.getRealPathFromURI(requireContext(), uris.first()))
                },
                { throwable -> Toast.makeText(requireContext(), throwable.message, Toast.LENGTH_LONG).show() })
    }

    @SuppressLint("CheckResult")
    private fun requestStoragePermission() {
        RxPermissions(this)
            .request(Manifest.permission.READ_EXTERNAL_STORAGE)
            .subscribe {
                if (it) {
                    pickImageFromGallery()
                }
            }
    }

    private fun setupClicks() {
        binding.fabUploadImage.setOnClickListener {
            showUploadImagesDialog()
        }
    }

    private fun showUploadImagesDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val v = LayoutInflater.from(requireContext())
            .inflate(R.layout.list_upload_dialog, binding.root as ViewGroup, false)
        val tvGallery = v.findViewById<TextView>(R.id.tvGallery)

        tvGallery.setOnClickListener {
            requestStoragePermission()
            dialog.dismiss()
        }
        v.findViewById<TextView>(R.id.tvCamera)
            .setOnClickListener {
                withRequestCamraPermission {  isGranted ->
                    if (isGranted) dispatchTakePictureIntent()
                }
                dialog.dismiss()
            }
        dialog.setContentView(v)
        dialog.show()

    }

    @SuppressLint("CheckResult")
    private fun withRequestCamraPermission(oncComplete: (isGranted: Boolean) -> Unit) {
        RxPermissions(this).request(android.Manifest.permission.CAMERA)
            .subscribe {
                oncComplete(it)
            }
    }

    private fun showProgress() {
        if (progressDialog == null) {
            createProgressDialog()
        }
        progressDialog?.show()
    }

    private fun hideProgress() {
        progressDialog?.dismiss()
    }

    private fun createProgressDialog() {
        context?.let {
            progressDialog = AlertDialog.Builder(it)
                .setView(R.layout.dialog_profile_photo_progress)
                .setCancelable(false)
                .create()
                .apply {
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
        }
    }
}

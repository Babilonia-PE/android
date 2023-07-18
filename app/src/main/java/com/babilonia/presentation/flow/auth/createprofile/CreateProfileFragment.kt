package com.babilonia.presentation.flow.auth.createprofile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.babilonia.Constants
import com.babilonia.R
import com.babilonia.databinding.CreateProfileFragmentBinding
import com.babilonia.presentation.base.BaseFragment
import com.babilonia.presentation.flow.main.MainActivity
import com.babilonia.presentation.utils.RealPathUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.signature.ObjectKey
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

class CreateProfileFragment : BaseFragment<CreateProfileFragmentBinding, CreateProfileViewModel>() {

    private var progressDialog: AlertDialog? = null
    private var currentPhotoPath: String? = null
    override fun viewCreated() {
        viewModel.getUser()
        binding.model = viewModel
        setupClicks()
        setErrorListeners()
        setToolbar()
    }

    override fun startListenToEvents() {
        super.startListenToEvents()
        viewModel.emailAlreadyTakenLiveData.observe(this, Observer {
            binding.tyEmail.error = getString(R.string.email_already_taken)
            binding.btChange.apply {
                isEnabled = false
                alpha = 0.5f
            }
        })
        viewModel.photoUploadProgressLiveData.observe(this, Observer {
            if (it) {
                showProgress()
            } else {
                hideProgress()
            }
        })
        viewModel.navigateToRootLiveData.observe(this, Observer {
            val intent = Intent(activity?.applicationContext, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            activity?.finish()
        })}

    override fun stopListenToEvents() {
        super.stopListenToEvents()
        viewModel.emailAlreadyTakenLiveData.removeObservers(this)
        viewModel.userLiveData.removeObservers(this)
        viewModel.photoUploadProgressLiveData.removeObservers(this)
        viewModel.navigateToRootLiveData.removeObservers(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_GALLERY) {
            data?.data?.let {
                cropImage(RealPathUtil.getRealPathFromURI(requireContext(), it))
            }
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            currentPhotoPath?.let {
                cropImage(it)
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = data?.let { UCrop.getOutput(it) }
            resultUri?.let {
                viewModel.avatarUri = it
                setAvatar(it.path)
            }

        }
    }

    private fun setAvatar(avatarPath: String?) {
        Glide.get(binding.ivProfileAvatar.context).clearMemory()
        Glide.with(binding.ivProfileAvatar.context)
            .load(avatarPath)
            .skipMemoryCache(true)
            .placeholder(R.drawable.ic_profile_placeholder)
            .transform(CenterCrop(), RoundedCorners(Constants.CORNER_RADIUS))
            .signature(ObjectKey(System.currentTimeMillis()))
            .into(binding.ivProfileAvatar)
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

    private fun setErrorListeners() {
        binding.etFirstName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.trim().isNullOrEmpty()) {
                    binding.tyFirstName.error = getString(R.string.first_name_empty)
                } else {
                    binding.tyFirstName.error = null
                }
                viewModel.updateCreateProfileValidator.notifyChange()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        binding.etLastName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.trim().isNullOrEmpty()) {
                    binding.tyLastName.error = getString(R.string.last_name_empty)
                } else {
                    binding.tyLastName.error = null
                }
                viewModel.updateCreateProfileValidator.notifyChange()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                when {
                    binding.etEmail.text?.isEmpty() == true -> binding.tyEmail.error = null
                    Patterns.EMAIL_ADDRESS.matcher(s).matches().not() -> binding.tyEmail.error =
                        getString(R.string.invalid_email)
                    else -> binding.tyEmail.error = null
                }
                viewModel.updateCreateProfileValidator.notifyChange()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.tyEmail.error = null
            } else if (binding.etEmail.text.isNullOrEmpty()) {
                binding.tyEmail.error = getString(R.string.field_should_not_be_empty)
            }
        }
    }

    private fun setToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_close_listing)
        binding.toolbar.setNavigationOnClickListener {
            viewModel.signOut(true)
        }
    }

    override fun setOnBackPressedDispatcher() {
        requireActivity().onBackPressedDispatcher.addCallback {
            viewModel.signOut(true)
        }
    }

    private fun setupClicks() {
        binding.btUploadPhoto.setOnClickListener {
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
                withRequestCameraPermission {
                    dispatchTakePictureIntent()
                }
                dialog.dismiss()
            }

        dialog.setContentView(v)
        dialog.show()

    }

    @SuppressLint("CheckResult")
    private fun withRequestCameraPermission(oncComplete: (isGranted: Boolean) -> Unit) {
        RxPermissions(this).request(Manifest.permission.CAMERA)
            .subscribe {
                if (it) {
                    oncComplete(it)
                }
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

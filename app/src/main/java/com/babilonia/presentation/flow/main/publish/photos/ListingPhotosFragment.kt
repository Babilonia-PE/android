package com.babilonia.presentation.flow.main.publish.photos

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.babilonia.Constants.TYPE_IMAGE
import com.babilonia.R
import com.babilonia.databinding.ListingPhotosFragmentBinding
import com.babilonia.domain.model.ListingImage
import com.babilonia.presentation.extension.invisible
import com.babilonia.presentation.extension.visible
import com.babilonia.presentation.flow.main.publish.common.BaseCreateListingFragment
import com.babilonia.presentation.flow.main.publish.createlisting.CreateListingContainerViewModel
import com.babilonia.presentation.flow.main.publish.photos.common.ListingPhotosListener
import com.babilonia.presentation.flow.main.publish.photos.common.ListingPhotosRecyclerAdapter
import com.babilonia.presentation.utils.PathFinder
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tbruyelle.rxpermissions2.RxPermissions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val PICK_IMAGE_FROM_GALLERY = 999
private const val REQUEST_TAKE_PHOTO = 500
private const val MAX_IMAGE_SIZE = 50

class ListingPhotosFragment :
    BaseCreateListingFragment<ListingPhotosFragmentBinding, CreateListingContainerViewModel>(),
    ListingPhotosListener {

    private val adapter = ListingPhotosRecyclerAdapter()
    private var currentPhotoPath: Uri? = null
    override fun viewCreated() {
        initClicks()
        initRecycler()
    }

    override fun startListenToEvents() {
        super.startListenToEvents()
        viewModel.imageUploadedEvent.observe(this, Observer {
            adapter.add(it)
        })
        sharedViewModel.editListingImagesEvent.observe(this, Observer {
            if (it.isNullOrEmpty().not()) {
                adapter.add(it)
            }
            sharedViewModel.draftSetEvent.removeObservers(this)

        })
        viewModel.loadingEvent.observe(this, Observer {
            binding.btEmptyPhotos.isEnabled = it.not()
            binding.btUploadMore.isEnabled = it.not()
        })
    }

    override fun stopListenToEvents() {
        super.stopListenToEvents()
        viewModel.imageUploadedEvent.removeObservers(this)
        sharedViewModel.mediator.removeObservers(this)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when(requestCode){
                REQUEST_TAKE_PHOTO -> {
                    currentPhotoPath?.let {
                        viewModel.compressImageAndUpload(requireContext(), it)
                    }
                }
                PICK_IMAGE_FROM_GALLERY -> {
                    if(data != null) {
                        data.data?.let { mUri ->
                            var realPath = PathFinder.getFilePathV1(requireActivity(), mUri)
                            if(realPath==null) realPath = PathFinder.getFilePathV2(requireActivity(), mUri)
                            realPath?.let{ path ->
                                viewModel.compressImageAndUpload(requireContext(), Uri.parse(path))
                            }?:run{
                                FirebaseCrashlytics.getInstance().setCustomKey("ERROR:", "ERROR PATH")
                                showSnackbar(R.string.image_could_not_be_obtained)
                            }
                        }?:run{
                            FirebaseCrashlytics.getInstance().setCustomKey("ERROR:", "ERROR DATA DATA")
                            showSnackbar(R.string.image_could_not_be_obtained)
                        }
                    }else{
                        FirebaseCrashlytics.getInstance().setCustomKey("ERROR:", "ERROR DATA")
                        showSnackbar(R.string.image_could_not_be_obtained)
                    }
                }
            }
        }
    }

    override fun onClick(position: Int, primary: Boolean) {
        showPickerDialog(position, primary)
    }

    override fun onSizeChanged(newList: List<ListingImage>) {
        binding.tvUploadMore.text = getString(R.string.upload_more_photos, newList.size, MAX_IMAGE_SIZE)
        sharedViewModel.images.value = newList
        if (newList.isEmpty()) {
            binding.emptyGroup.visible()
            binding.photosGroup.invisible()
        } else {
            binding.emptyGroup.invisible()
            binding.photosGroup.visible()
        }
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
            currentPhotoPath = Uri.fromFile(this)
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    FirebaseCrashlytics.getInstance().recordException(ex)
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(requireContext(), "com.babilonia.fileprovider", it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    private fun initClicks() {
        binding.btEmptyPhotos.setOnClickListener {
            showUploadImagesDialog()
        }
        binding.btUploadMore.setOnClickListener {
            if (adapter.itemCount < MAX_IMAGE_SIZE)
                showUploadImagesDialog()
        }
    }

    private fun initRecycler() {
        adapter.listingPhotosListener = this
        binding.rcListingPhotosContainer.adapter = adapter
        onSizeChanged(sharedViewModel.images.value ?: emptyList())
    }

    @SuppressLint("CheckResult")
    private fun requestStoragePermission() {
        RxPermissions(this)
            .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe {
                if(it){
                    openFiles()
                }else{
                    showSnackbar(R.string.permission_not_granted)
                }
            }
    }

    private fun openFiles(){
        val intent = Intent()
        intent.action = Intent.ACTION_OPEN_DOCUMENT //ACTION_GET_CONTENT
        intent.type = TYPE_IMAGE
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        val chooser = Intent.createChooser(intent, getString(R.string.select_an_image))
        startActivityForResult(chooser, PICK_IMAGE_FROM_GALLERY)
    }

    private fun showPickerDialog(position: Int, primary: Boolean) {
        val dialog = BottomSheetDialog(requireContext())
        val v = LayoutInflater.from(requireContext())
            .inflate(R.layout.listing_photos_dialog, binding.root as ViewGroup, false)
        val tvSetMain = v.findViewById<TextView>(R.id.tvSetMain)
        if (primary) {
            tvSetMain.invisible()
        } else {
            tvSetMain.setOnClickListener {
                adapter.setMainPicture(position)
                dialog.dismiss()
            }
        }
        v.findViewById<TextView>(R.id.tvDelete)
            .setOnClickListener {
                adapter.remove(position)
                dialog.dismiss()
            }
        dialog.setContentView(v)
        dialog.show()

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
                withRequestCameraPermission { isGranted ->
                    if (isGranted) dispatchTakePictureIntent()
                }
                dialog.dismiss()
            }
        dialog.setContentView(v)
        dialog.show()

    }

    companion object {
        fun newInstance() = ListingPhotosFragment()
    }

    @SuppressLint("CheckResult")
    private fun withRequestCameraPermission(oncComplete: (isGranted: Boolean) -> Unit) {
        RxPermissions(this).request(android.Manifest.permission.CAMERA)
            .subscribe {
                oncComplete(it)
            }
    }
}

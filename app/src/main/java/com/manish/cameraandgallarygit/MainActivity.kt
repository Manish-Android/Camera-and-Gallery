package com.manish.cameraandgallarygit

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import android.Manifest
import java.util.Locale

class MainActivity : AppCompatActivity() {
    lateinit var camera: Button
    lateinit var gallery: Button
    lateinit var image: ImageView
    val REQUEST_IMAGE_CAPTURE = 4
    val PICK_IMAGE_REQUEST = 3
    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1
    private var currentPhotoPath: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        camera = findViewById(R.id.camera)
        gallery = findViewById(R.id.gallary)
        image = findViewById(R.id.img_main)

        camera.setOnClickListener {
            pickImageFromCamera()
        }

        gallery.setOnClickListener {
            pickImageFromGallery()
        }

    }


    // check result code and set image in Imageview
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("TAG", "<>requestCode :: $requestCode ,resultCode :: $resultCode")


        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Get the file path of the captured image
            val imageFile = File(currentPhotoPath)

            // Check if the file exists
            if (imageFile.exists()) {
                // Decode the file into a Bitmap with orientation adjustment
                val imageBitmap = decodeFile(imageFile)

                // Set the Bitmap to the ImageView
                image.setImageBitmap(imageBitmap)
            } else {
                Toast.makeText(this@MainActivity, "Failed to retrieve captured image", Toast.LENGTH_SHORT).show()
            }
        }

        if (requestCode == PICK_IMAGE_REQUEST) {

            if (resultCode == RESULT_OK) {
                // Use the content URI to display the image
                val galleryBitmapUri = data?.data
                if (galleryBitmapUri != null) {
                    Log.d("TAG", "galleryBitmapUri :: $galleryBitmapUri")
                    image.setImageURI(galleryBitmapUri)
                } else {
                    // Handle the case where data or data URI is null
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to retrieve gallery image",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else
            {
                Toast.makeText(this@MainActivity, "Gallery selection canceled", Toast.LENGTH_SHORT).show()
            }
        }

    }


    // camera open after gallery
    private fun pickImageFromGallery() {
        val cameraPermission = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (cameraPermission != PackageManager.PERMISSION_GRANTED && storagePermission != PackageManager.PERMISSION_GRANTED) {
            // Request both camera and storage permissions
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
            )
        } else {
            // Both permissions are granted, proceed with image picking logic
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }


    // camera open after permission
// camera open after permission
    private fun pickImageFromCamera() {
        val cameraPermission = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (cameraPermission != PackageManager.PERMISSION_GRANTED && storagePermission != PackageManager.PERMISSION_GRANTED) {
            // Request both camera and storage permissions
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
            )
        } else {
            // Both permissions are granted, proceed with image picking logic

            // Create the file where the photo should go
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                // Handle errors creating the File
                null
            }

            // Continue only if the File was successfully created
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this@MainActivity,
                    "com.manish.cameraandgallarygit.fileprovider",
                    it
                )

                // Save the photo path for later use
                currentPhotoPath = it.absolutePath

                // Start the camera intent
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    // Create a file for the photo to be saved
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    // after permission for opening gallary
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with image picking logic
                    pickImageFromGallery()
                } else {
                    // Permission denied, handle accordingly (e.g., show a message to the user)

                    Toast.makeText(
                        this@MainActivity,
                        "Permission denied. Cannot pick image from gallery.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            // Handle other permission requests if needed
        }
    }

    private fun decodeFile(file: File): Bitmap {
        val options = BitmapFactory.Options()
        options.inSampleSize = 2 // You can adjust the inSampleSize for quality

        // Decode the file into a Bitmap
        var bitmap = BitmapFactory.decodeFile(file.absolutePath, options)

        // Read the orientation information from the file
        val exif = ExifInterface(file.absolutePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

        // Rotate the Bitmap according to the orientation information
        bitmap = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }

        return bitmap
    }
    // Function to rotate the Bitmap
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
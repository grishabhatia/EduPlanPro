package com.eduplanpro.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.eduplanpro.database.AppDatabase
import com.eduplanpro.databinding.ActivityRegistrationBinding
import com.eduplanpro.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class RegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    private var selectedImageUri: Uri? = null
    private lateinit var db: AppDatabase

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            selectedImageUri = result.data?.data
            Glide.with(this)
                .load(selectedImageUri)
                .circleCrop()
                .into(binding.profileImageView)
            Toast.makeText(this, "Photo selected!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        binding.profileImageView.setOnClickListener {
            checkPermissionAndOpenGallery()
        }

        binding.registerButton.setOnClickListener {
            validateAndRegister()
        }

        binding.loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun checkPermissionAndOpenGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 101)
        } else {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        }
    }

    private fun saveImageToInternalStorage(uri: Uri?): String {
        if (uri == null) return ""

        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = "profile_${System.currentTimeMillis()}.jpg"
            val directory = File(filesDir, "profile_images")

            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun validateAndRegister() {
        val name = binding.nameInput.text.toString().trim()
        val email = binding.emailInput.text.toString().trim()
        val mobile = binding.mobileInput.text.toString().trim()
        val age = binding.ageInput.text.toString().trim()

        if (name.isEmpty()) {
            binding.nameInput.error = "Name required"
            return
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInput.error = "Valid email required"
            return
        }
        if (mobile.isEmpty() || mobile.length != 10) {
            binding.mobileInput.error = "10 digit mobile required"
            return
        }
        if (age.isEmpty() || age.toIntOrNull() == null || age.toInt() !in 5..100) {
            binding.ageInput.error = "Age 5-100 required"
            return
        }

        val imagePath = saveImageToInternalStorage(selectedImageUri)

        val user = User(
            name = name,
            email = email,
            mobile = mobile,
            age = age.toInt(),
            profileImagePath = imagePath
        )

        CoroutineScope(Dispatchers.IO).launch {
            db.userDao().insertUser(user)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@RegistrationActivity, "Registration Successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegistrationActivity, LoginActivity::class.java))
                finish()
            }
        }
    }
}
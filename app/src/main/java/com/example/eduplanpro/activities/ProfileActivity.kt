package com.eduplanpro.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.eduplanpro.database.AppDatabase
import com.eduplanpro.databinding.ActivityProfileBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var db: AppDatabase
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getIntExtra("user_id", 0)
        db = AppDatabase.getDatabase(this)

        loadUserData()

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadUserData() {
        CoroutineScope(Dispatchers.IO).launch {
            val user = db.userDao().getUserById(userId)
            withContext(Dispatchers.Main) {
                if (user != null) {
                    binding.nameValue.text = user.name
                    binding.emailValue.text = user.email
                    binding.mobileValue.text = user.mobile
                    binding.ageValue.text = user.age.toString()

                    // Load profile photo if exists
                    if (user.profileImagePath.isNotEmpty()) {
                        try {
                            val file = File(user.profileImagePath)
                            if (file.exists()) {
                                Glide.with(this@ProfileActivity)
                                    .load(file)
                                    .circleCrop()
                                    .placeholder(android.R.drawable.ic_menu_camera)
                                    .error(android.R.drawable.ic_menu_camera)
                                    .into(binding.profileImage)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }
}
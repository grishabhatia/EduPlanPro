package com.eduplanpro.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.eduplanpro.database.AppDatabase
import com.eduplanpro.databinding.ActivityLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        binding.loginButton.setOnClickListener {
            validateLogin()
        }

        binding.registerLink.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
            finish()
        }
    }

    private fun validateLogin() {
        val email = binding.emailInput.text.toString().trim()
        val name = binding.nameInput.text.toString().trim()

        if (email.isEmpty()) {
            binding.emailInput.error = "Email is required"
            return
        }

        if (name.isEmpty()) {
            binding.nameInput.error = "Name is required"
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val user = db.userDao().getUserByEmailAndName(email, name)
            withContext(Dispatchers.Main) {
                if (user != null) {
                    Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.putExtra("user_id", user.id)
                    intent.putExtra("user_name", user.name)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid credentials!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
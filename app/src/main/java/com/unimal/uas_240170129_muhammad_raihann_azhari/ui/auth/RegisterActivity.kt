package com.unimal.uas_240170129_muhammad_raihann_azhari.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.unimal.uas_240170129_muhammad_raihann_azhari.R
import com.unimal.uas_240170129_muhammad_raihann_azhari.databinding.ActivityRegisterBinding
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main.MainActivity
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.Validators

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.getInsetsController(window, binding.root).isAppearanceLightStatusBars = false

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.tvLogin.setOnClickListener { finish() }
        binding.etName.doOnTextChanged { _, _, _, _ -> binding.tilName.error = null }
        binding.etEmail.doOnTextChanged { _, _, _, _ -> binding.tilEmail.error = null }
        binding.etPassword.doOnTextChanged { _, _, _, _ -> binding.tilPassword.error = null }
        binding.etConfirm.doOnTextChanged { _, _, _, _ -> binding.tilConfirm.error = null }
        binding.btnRegister.setOnClickListener { attemptRegister() }

        viewModel.state.observe(this) { render(it) }
    }

    private fun attemptRegister() {
        binding.tvError.isVisible = false
        val name = binding.etName.text?.toString().orEmpty().trim()
        val email = binding.etEmail.text?.toString().orEmpty().trim()
        val password = binding.etPassword.text?.toString().orEmpty()
        val confirm = binding.etConfirm.text?.toString().orEmpty()

        var valid = true
        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.error_name_empty)
            valid = false
        } else if (name.length < 3) {
            binding.tilName.error = getString(R.string.error_name_short)
            valid = false
        }
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_email_empty)
            valid = false
        } else if (!Validators.isValidEmail(email)) {
            binding.tilEmail.error = getString(R.string.error_email_invalid)
            valid = false
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_password_empty)
            valid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.error_password_short)
            valid = false
        }
        if (confirm != password) {
            binding.tilConfirm.error = getString(R.string.error_confirm_mismatch)
            valid = false
        }
        if (valid) viewModel.register(name, email, password)
    }

    private fun render(state: AuthState) {
        when (state) {
            AuthState.Idle -> setLoading(false)
            AuthState.Loading -> setLoading(true)
            is AuthState.Success -> {
                viewModel.reset()
                val intent = Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            }
            is AuthState.Error -> {
                setLoading(false)
                if (state.emailError) {
                    binding.tilEmail.error = getString(state.messageRes)
                } else {
                    binding.tvError.text = getString(state.messageRes) +
                            (state.detail?.let { "\n($it)" } ?: "")
                    binding.tvError.isVisible = true
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.isVisible = loading
        binding.btnRegister.isEnabled = !loading
        binding.tvLogin.isEnabled = !loading
    }
}
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
import com.unimal.uas_240170129_muhammad_raihann_azhari.databinding.ActivityLoginBinding
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main.MainActivity
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.SessionManager
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.Validators

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sesi tersimpan: langsung ke halaman utama.
        if (SessionManager(this).isLoggedIn) {
            openMain()
            return
        }

        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.getInsetsController(window, binding.root).isAppearanceLightStatusBars = false

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.etEmail.doOnTextChanged { _, _, _, _ -> binding.tilEmail.error = null }
        binding.etPassword.doOnTextChanged { _, _, _, _ -> binding.tilPassword.error = null }
        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        viewModel.state.observe(this) { render(it) }
    }

    private fun attemptLogin() {
        binding.tvError.isVisible = false
        val email = binding.etEmail.text?.toString().orEmpty().trim()
        val password = binding.etPassword.text?.toString().orEmpty()

        var valid = true
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
        }
        if (valid) viewModel.login(email, password)
    }

    private fun render(state: AuthState) {
        when (state) {
            AuthState.Idle -> setLoading(false)
            AuthState.Loading -> setLoading(true)
            is AuthState.Success -> {
                viewModel.reset()
                openMain()
            }
            is AuthState.Error -> {
                setLoading(false)
                binding.tvError.text = getString(state.messageRes) +
                        (state.detail?.let { "\n($it)" } ?: "")
                binding.tvError.isVisible = true
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.isVisible = loading
        binding.btnLogin.isEnabled = !loading
        binding.tvRegister.isEnabled = !loading
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
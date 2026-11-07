package com.unimal.uas_240170129_muhammad_raihann_azhari.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.unimal.uas_240170129_muhammad_raihann_azhari.R
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.model.BekalMenu
import com.unimal.uas_240170129_muhammad_raihann_azhari.databinding.ActivityDetailBinding
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common.CategoryStyle
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.form.FormActivity
import java.io.File

/** Halaman detail menu bekal: bahan, catatan, tandai sudah dibawa, edit, hapus. */
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val viewModel: DetailViewModel by viewModels()
    private var menuId = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.getInsetsController(window, binding.root).isAppearanceLightStatusBars = false

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        menuId = intent.getLongExtra(EXTRA_ID, -1L)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnEdit.setOnClickListener {
            startActivity(FormActivity.intent(this, menuId = menuId))
        }
        binding.btnDelete.setOnClickListener { confirmDelete() }
        binding.btnToggle.setOnClickListener { viewModel.togglePacked() }

        viewModel.menu.observe(this) { menu ->
            if (menu == null) {
                Toast.makeText(this, R.string.not_found, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                bind(menu)
            }
        }
        viewModel.deleted.observe(this) { deleted ->
            if (deleted) {
                Toast.makeText(this, R.string.deleted_toast, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Muat ulang setiap kembali dari layar edit.
        viewModel.load(menuId)
    }

    private fun bind(menu: BekalMenu) {
        val categories = resources.getStringArray(R.array.categories)
        val days = resources.getStringArray(R.array.days)

        binding.tvName.text = menu.name
        binding.tvCategory.text = categories.getOrElse(menu.category) { "" }
        binding.tvStatus.setText(if (menu.packed) R.string.status_packed else R.string.status_not_packed)
        binding.tvDayValue.text = days.getOrElse(menu.day) { "" }
        binding.tvCaloriesValue.text = getString(R.string.kcal_format, menu.calories)
        binding.tvPrepValue.text = getString(R.string.min_format, menu.prepMinutes)

        binding.tvIngredients.text = menu.ingredients.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n") { "•  $it" }
        binding.tvNotes.text = menu.notes.ifBlank { getString(R.string.no_notes) }

        binding.photoBg.setBackgroundResource(CategoryStyle.gradient(menu.category))
        val photo = menu.photoPath?.let { File(it) }?.takeIf { it.exists() }
        if (photo != null) {
            binding.ivPhoto.isVisible = true
            binding.ivDeco.isVisible = false
            Glide.with(this).load(photo).centerCrop().into(binding.ivPhoto)
        } else {
            binding.ivPhoto.isVisible = false
            binding.ivDeco.isVisible = true
        }

        binding.btnToggle.setImageResource(if (menu.packed) R.drawable.ic_undo else R.drawable.ic_check)
        binding.tvToggleLabel.setText(if (menu.packed) R.string.mark_unpacked else R.string.mark_packed)
    }

    private fun confirmDelete() {
        val name = viewModel.menu.value?.name ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_title)
            .setMessage(getString(R.string.delete_message, name))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.delete() }
            .show()
    }

    companion object {
        private const val EXTRA_ID = "extra_id"

        fun intent(context: Context, id: Long): Intent =
            Intent(context, DetailActivity::class.java).putExtra(EXTRA_ID, id)
    }
}

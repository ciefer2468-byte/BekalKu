package com.unimal.uas_240170129_muhammad_raihann_azhari.ui.form

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.bumptech.glide.Glide
import com.unimal.uas_240170129_muhammad_raihann_azhari.R
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.model.BekalMenu
import com.unimal.uas_240170129_muhammad_raihann_azhari.databinding.ActivityFormBinding
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.AppExecutors
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.ImageUtils
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.isNightMode
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.todayIndex
import java.io.File

/** Form tambah / edit menu bekal (dengan foto dari kamera atau galeri). */
class FormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormBinding
    private val viewModel: FormViewModel by viewModels()

    private var editId = -1L
    private var selectedCategory = 1
    private var selectedDay = 0
    private var photoPath: String? = null
    private var originalPhotoPath: String? = null
    private var pendingCameraFile: File? = null
    private var prefilled = false
    private var saved = false
    private var existing: BekalMenu? = null

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val file = pendingCameraFile
            pendingCameraFile = null
            if (success && file != null && file.exists()) {
                importPhoto { ImageUtils.importFromFile(this, file) }
            } else {
                file?.delete()
            }
        }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) importPhoto { ImageUtils.importFromUri(this, uri) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        WindowCompat.getInsetsController(window, binding.root).isAppearanceLightStatusBars = !isNightMode()

        editId = intent.getLongExtra(EXTRA_MENU_ID, -1L)
        val presetDay = intent.getIntExtra(EXTRA_DAY, -1)

        if (savedInstanceState != null) {
            selectedCategory = savedInstanceState.getInt(KEY_CATEGORY, 1)
            selectedDay = savedInstanceState.getInt(KEY_DAY, 0)
            photoPath = savedInstanceState.getString(KEY_PHOTO)
            originalPhotoPath = savedInstanceState.getString(KEY_ORIGINAL_PHOTO)
            pendingCameraFile = savedInstanceState.getString(KEY_PENDING)?.let { File(it) }
            prefilled = savedInstanceState.getBoolean(KEY_PREFILLED)
        } else {
            selectedDay = if (presetDay in 0..6) presetDay else todayIndex()
            // Prefill dari fitur Inspirasi Bekal (API eksternal).
            intent.getStringExtra(EXTRA_PREFILL_NAME)?.let { binding.etName.setText(it) }
            intent.getStringExtra(EXTRA_PREFILL_INGREDIENTS)?.let { binding.etIngredients.setText(it) }
            intent.getStringExtra(EXTRA_PREFILL_NOTES)?.let { binding.etNotes.setText(it) }
        }

        binding.tvTitle.setText(if (editId > 0) R.string.form_edit_title else R.string.form_add_title)
        binding.btnBack.setOnClickListener { finish() }

        setupDropdowns()
        setupFieldWatchers()
        updatePhotoPreview()

        binding.btnCamera.setOnClickListener { launchCamera() }
        binding.btnGallery.setOnClickListener {
            pickImage.launch(
                PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    .build()
            )
        }
        binding.btnRemovePhoto.setOnClickListener { setPhoto(null) }
        binding.btnSave.setOnClickListener { attemptSave() }

        if (editId > 0) {
            viewModel.loadForEdit(editId)
            viewModel.existing.observe(this) { menu ->
                if (menu == null) {
                    Toast.makeText(this, R.string.not_found, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    existing = menu
                    if (!prefilled) prefill(menu)
                }
            }
        }

        viewModel.saveState.observe(this) { state ->
            val saving = state == SaveState.SAVING
            binding.progressSave.isVisible = saving
            binding.btnSave.isEnabled = !saving
            when (state) {
                SaveState.SAVED -> {
                    saved = true
                    val original = originalPhotoPath
                    if (original != null && original != photoPath) File(original).delete()
                    Toast.makeText(this, R.string.saved_toast, Toast.LENGTH_SHORT).show()
                    finish()
                }
                SaveState.FAILED -> {
                    Toast.makeText(this, R.string.error_generic, Toast.LENGTH_LONG).show()
                    viewModel.consumeFailure()
                }
                else -> Unit
            }
        }
    }

    private fun setupDropdowns() {
        val categories = resources.getStringArray(R.array.categories)
        val days = resources.getStringArray(R.array.days)

        binding.actCategory.setAdapter(ArrayAdapter(this, R.layout.item_dropdown, categories))
        binding.actCategory.setText(categories[selectedCategory], false)
        binding.actCategory.setOnItemClickListener { _, _, position, _ ->
            selectedCategory = position
            binding.tilCategory.error = null
        }

        binding.actDay.setAdapter(ArrayAdapter(this, R.layout.item_dropdown, days))
        binding.actDay.setText(days[selectedDay], false)
        binding.actDay.setOnItemClickListener { _, _, position, _ ->
            selectedDay = position
            binding.tilDay.error = null
        }
    }

    private fun setupFieldWatchers() {
        binding.etName.doOnTextChanged { _, _, _, _ -> binding.tilName.error = null }
        binding.etIngredients.doOnTextChanged { _, _, _, _ -> binding.tilIngredients.error = null }
        binding.etCalories.doOnTextChanged { _, _, _, _ -> binding.tilCalories.error = null }
        binding.etPrep.doOnTextChanged { _, _, _, _ -> binding.tilPrep.error = null }
    }

    private fun prefill(menu: BekalMenu) {
        prefilled = true
        binding.etName.setText(menu.name)
        binding.etIngredients.setText(menu.ingredients)
        binding.etCalories.setText(menu.calories.toString())
        binding.etPrep.setText(menu.prepMinutes.toString())
        binding.etNotes.setText(menu.notes)

        selectedCategory = menu.category.coerceIn(0, 3)
        selectedDay = menu.day.coerceIn(0, 6)
        binding.actCategory.setText(resources.getStringArray(R.array.categories)[selectedCategory], false)
        binding.actDay.setText(resources.getStringArray(R.array.days)[selectedDay], false)

        originalPhotoPath = menu.photoPath
        photoPath = menu.photoPath
        updatePhotoPreview()
    }

    // ---------- Foto (kamera / galeri) ----------

    private fun launchCamera() {
        val tmp = ImageUtils.newPhotoFile(this, "tmp")
        pendingCameraFile = tmp
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", tmp)
        try {
            takePicture.launch(uri)
        } catch (e: ActivityNotFoundException) {
            pendingCameraFile = null
            Toast.makeText(this, R.string.error_camera_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun importPhoto(work: () -> File?) {
        binding.progressPhoto.isVisible = true
        AppExecutors.io.execute {
            val file = try {
                work()
            } catch (e: Exception) {
                null
            }
            runOnUiThread {
                if (isDestroyed) return@runOnUiThread
                binding.progressPhoto.isVisible = false
                if (file == null) {
                    Toast.makeText(this, R.string.error_photo_failed, Toast.LENGTH_SHORT).show()
                } else {
                    setPhoto(file.absolutePath)
                }
            }
        }
    }

    private fun setPhoto(path: String?) {
        // Hapus foto baru sebelumnya yang belum tersimpan.
        val old = photoPath
        if (old != null && old != originalPhotoPath && old != path) File(old).delete()
        photoPath = path
        updatePhotoPreview()
    }

    private fun updatePhotoPreview() {
        val file = photoPath?.let { File(it) }?.takeIf { it.exists() }
        binding.ivPhoto.isVisible = file != null
        binding.ivPhotoPlaceholder.isVisible = file == null
        binding.btnRemovePhoto.isVisible = file != null
        if (file != null) Glide.with(this).load(file).centerCrop().into(binding.ivPhoto)
    }

    // ---------- Validasi & simpan ----------

    private fun attemptSave() {
        val name = binding.etName.text?.toString().orEmpty().trim()
        val ingredients = binding.etIngredients.text?.toString().orEmpty().trim()
        val caloriesText = binding.etCalories.text?.toString().orEmpty().trim()
        val prepText = binding.etPrep.text?.toString().orEmpty().trim()
        val notes = binding.etNotes.text?.toString().orEmpty().trim()

        var valid = true
        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.error_menu_name_empty)
            valid = false
        } else if (name.length < 3) {
            binding.tilName.error = getString(R.string.error_menu_name_short)
            valid = false
        }
        if (ingredients.isEmpty()) {
            binding.tilIngredients.error = getString(R.string.error_ingredients_empty)
            valid = false
        }

        val calories = caloriesText.toIntOrNull()
        if (caloriesText.isEmpty()) {
            binding.tilCalories.error = getString(R.string.error_calories_empty)
            valid = false
        } else if (calories == null || calories !in 0..5000) {
            binding.tilCalories.error = getString(R.string.error_calories_range)
            valid = false
        }

        val prep = prepText.toIntOrNull()
        if (prepText.isEmpty()) {
            binding.tilPrep.error = getString(R.string.error_prep_empty)
            valid = false
        } else if (prep == null || prep !in 1..480) {
            binding.tilPrep.error = getString(R.string.error_prep_range)
            valid = false
        }

        if (!valid || calories == null || prep == null) return

        val current = existing
        viewModel.save(
            BekalMenu(
                id = if (editId > 0) editId else 0,
                userId = viewModel.userId,
                name = name,
                category = selectedCategory,
                day = selectedDay,
                ingredients = ingredients,
                calories = calories,
                prepMinutes = prep,
                notes = notes,
                photoPath = photoPath,
                packed = current?.packed ?: false,
                createdAt = current?.createdAt ?: System.currentTimeMillis()
            )
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CATEGORY, selectedCategory)
        outState.putInt(KEY_DAY, selectedDay)
        outState.putString(KEY_PHOTO, photoPath)
        outState.putString(KEY_ORIGINAL_PHOTO, originalPhotoPath)
        outState.putString(KEY_PENDING, pendingCameraFile?.absolutePath)
        outState.putBoolean(KEY_PREFILLED, prefilled)
    }

    override fun onDestroy() {
        // Dibatalkan tanpa menyimpan: buang foto baru yang sudah terlanjur disalin.
        if (isFinishing && !saved) {
            val path = photoPath
            if (path != null && path != originalPhotoPath) File(path).delete()
        }
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_MENU_ID = "extra_menu_id"
        private const val EXTRA_DAY = "extra_day"
        private const val EXTRA_PREFILL_NAME = "extra_prefill_name"
        private const val EXTRA_PREFILL_INGREDIENTS = "extra_prefill_ingredients"
        private const val EXTRA_PREFILL_NOTES = "extra_prefill_notes"
        private const val KEY_CATEGORY = "key_category"
        private const val KEY_DAY = "key_day"
        private const val KEY_PHOTO = "key_photo"
        private const val KEY_ORIGINAL_PHOTO = "key_original_photo"
        private const val KEY_PENDING = "key_pending"
        private const val KEY_PREFILLED = "key_prefilled"

        /** @param menuId diisi untuk mode edit; @param day indeks hari bawaan untuk menu baru. */
        fun intent(
            context: Context,
            day: Int = -1,
            menuId: Long = -1L,
            prefillName: String? = null,
            prefillIngredients: String? = null,
            prefillNotes: String? = null
        ): Intent =
            Intent(context, FormActivity::class.java)
                .putExtra(EXTRA_DAY, day)
                .putExtra(EXTRA_MENU_ID, menuId)
                .putExtra(EXTRA_PREFILL_NAME, prefillName)
                .putExtra(EXTRA_PREFILL_INGREDIENTS, prefillIngredients)
                .putExtra(EXTRA_PREFILL_NOTES, prefillNotes)
    }
}

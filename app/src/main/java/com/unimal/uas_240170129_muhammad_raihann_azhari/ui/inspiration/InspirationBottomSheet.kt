package com.unimal.uas_240170129_muhammad_raihann_azhari.ui.inspiration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.unimal.uas_240170129_muhammad_raihann_azhari.R
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.remote.Inspiration
import com.unimal.uas_240170129_muhammad_raihann_azhari.databinding.SheetInspirationBinding
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common.StateController
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common.UiState
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.form.FormActivity
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.todayIndex

/** Ide menu bekal acak dari API eksternal (TheMealDB), dengan state loading / error jaringan. */
class InspirationBottomSheet : BottomSheetDialogFragment() {

    private var _binding: SheetInspirationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InspirationViewModel by viewModels()
    private lateinit var stateController: StateController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SheetInspirationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stateController = StateController(binding.stateView)
        binding.btnShuffle.setOnClickListener { viewModel.load(force = true) }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Loading -> {
                    binding.content.isVisible = false
                    stateController.loading()
                }
                is UiState.Error -> {
                    binding.content.isVisible = false
                    val message = if (state.message == InspirationViewModel.ERROR_NETWORK) {
                        R.string.error_network
                    } else {
                        R.string.error_server
                    }
                    stateController.show(
                        R.drawable.ic_error, R.string.error_title, getString(message), R.string.retry
                    ) { viewModel.load(force = true) }
                }
                is UiState.Success -> {
                    stateController.hide()
                    binding.content.isVisible = true
                    bind(state.data)
                }
            }
        }
        viewModel.load()
    }

    private fun bind(item: Inspiration) {
        binding.tvName.text = item.name
        binding.tvMeta.text = listOf(item.category, item.area).filter { it.isNotBlank() }.joinToString(" • ")
        binding.tvIngredients.text = item.ingredients.joinToString("\n") { "•  $it" }
        Glide.with(this).load(item.thumbUrl).centerCrop().into(binding.ivThumb)

        binding.btnUse.setOnClickListener {
            startActivity(
                FormActivity.intent(
                    requireContext(),
                    day = todayIndex(),
                    prefillName = item.name,
                    prefillIngredients = item.ingredients.joinToString("\n"),
                    prefillNotes = item.instructions.take(400)
                )
            )
            dismiss()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

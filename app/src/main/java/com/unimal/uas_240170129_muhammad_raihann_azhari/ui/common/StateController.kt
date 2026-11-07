package com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.unimal.uas_240170129_muhammad_raihann_azhari.databinding.ViewStateBinding

/** Mengatur tampilan loading / empty / error pada layout `view_state`. */
class StateController(private val binding: ViewStateBinding) {

    fun loading() {
        binding.root.isVisible = true
        binding.progress.isVisible = true
        binding.ivIcon.isVisible = false
        binding.tvTitle.isVisible = false
        binding.tvMessage.isVisible = false
        binding.btnAction.isVisible = false
    }

    fun show(
        @DrawableRes icon: Int,
        @StringRes title: Int,
        message: String,
        @StringRes actionText: Int? = null,
        onAction: (() -> Unit)? = null
    ) {
        binding.root.isVisible = true
        binding.progress.isVisible = false
        binding.ivIcon.isVisible = true
        binding.ivIcon.setImageResource(icon)
        binding.tvTitle.isVisible = true
        binding.tvTitle.setText(title)
        binding.tvMessage.isVisible = true
        binding.tvMessage.text = message
        if (actionText != null && onAction != null) {
            binding.btnAction.isVisible = true
            binding.btnAction.setText(actionText)
            binding.btnAction.setOnClickListener { onAction() }
        } else {
            binding.btnAction.isVisible = false
        }
    }

    fun hide() {
        binding.root.isVisible = false
    }
}

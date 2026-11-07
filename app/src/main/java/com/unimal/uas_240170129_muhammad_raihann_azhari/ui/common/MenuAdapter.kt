package com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.unimal.uas_240170129_muhammad_raihann_azhari.R
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.model.BekalMenu
import com.unimal.uas_240170129_muhammad_raihann_azhari.databinding.ItemMenuBinding
import java.io.File

/** Kartu menu bekal untuk RecyclerView. */
class MenuAdapter(
    private val onClick: (BekalMenu) -> Unit,
    private val onTogglePacked: (BekalMenu) -> Unit
) : ListAdapter<BekalMenu, MenuAdapter.ViewHolder>(Diff) {

    inner class ViewHolder(private val b: ItemMenuBinding) : RecyclerView.ViewHolder(b.root) {

        fun bind(menu: BekalMenu) {
            val res = b.root.resources
            val categories = res.getStringArray(R.array.categories)
            val days = res.getStringArray(R.array.days)

            b.tvTitle.text = menu.name
            b.tvTag.text = categories.getOrElse(menu.category) { "" }
            b.tvMeta.text = res.getString(
                R.string.meta_format,
                days.getOrElse(menu.day) { "" }, menu.prepMinutes, menu.calories
            )
            b.bg.setBackgroundResource(CategoryStyle.gradient(menu.category))

            val photo = menu.photoPath?.let { File(it) }?.takeIf { it.exists() }
            if (photo != null) {
                b.ivPhoto.isVisible = true
                b.scrim.isVisible = true
                b.ivDeco.isVisible = false
                Glide.with(b.ivPhoto).load(photo).centerCrop().into(b.ivPhoto)
            } else {
                Glide.with(b.ivPhoto).clear(b.ivPhoto)
                b.ivPhoto.isVisible = false
                b.scrim.isVisible = false
                b.ivDeco.isVisible = true
            }

            b.btnToggle.setImageResource(
                if (menu.packed) R.drawable.ic_check_circle else R.drawable.ic_radio_unchecked
            )
            b.btnToggle.contentDescription = b.root.context.getString(R.string.cd_toggle_packed)
            b.btnToggle.setOnClickListener { onTogglePacked(menu) }
            b.root.setOnClickListener { onClick(menu) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    private object Diff : DiffUtil.ItemCallback<BekalMenu>() {
        override fun areItemsTheSame(old: BekalMenu, new: BekalMenu) = old.id == new.id
        override fun areContentsTheSame(old: BekalMenu, new: BekalMenu) = old == new
    }
}

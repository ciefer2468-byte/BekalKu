package com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.unimal.uas_240170129_muhammad_raihann_azhari.R
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.model.BekalMenu
import com.unimal.uas_240170129_muhammad_raihann_azhari.databinding.FragmentStatsBinding
import com.unimal.uas_240170129_muhammad_raihann_azhari.databinding.ItemStatBarBinding
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common.StateController
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common.UiState
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main.MenuViewModel
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.applyTopInsetPadding

/** Tab Statistik: ringkasan perencanaan bekal. */
class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MenuViewModel by activityViewModels()
    private lateinit var stateController: StateController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.content.applyTopInsetPadding()
        stateController = StateController(binding.stateView)

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Loading -> {
                    binding.content.isVisible = false
                    stateController.loading()
                }
                is UiState.Error -> {
                    binding.content.isVisible = false
                    stateController.show(
                        R.drawable.ic_error, R.string.error_title, getString(R.string.error_load), R.string.retry
                    ) { viewModel.load() }
                }
                is UiState.Success -> {
                    stateController.hide()
                    binding.content.isVisible = true
                    bind(state.data)
                }
            }
        }
    }

    private fun bind(list: List<BekalMenu>) {
        val total = list.size
        val packed = list.count { it.packed }
        val calories = list.sumOf { it.calories }
        val avgPrep = if (total == 0) 0 else list.sumOf { it.prepMinutes } / total
        val daysCovered = list.map { it.day }.distinct().size

        binding.progressCoverage.setProgressCompat(daysCovered * 100 / 7, true)
        binding.tvCoverageValue.text = getString(R.string.coverage_format, daysCovered)

        binding.tvTotalValue.text = total.toString()
        binding.tvPackedValue.text = getString(R.string.ratio_format, packed, total)
        binding.tvCaloriesValue.text = getString(R.string.kcal_format, calories)
        binding.tvPrepValue.text = getString(R.string.min_format, avgPrep)

        val categories = resources.getStringArray(R.array.categories)
        binding.categoryContainer.removeAllViews()
        categories.forEachIndexed { index, name ->
            val count = list.count { it.category == index }
            val row = ItemStatBarBinding.inflate(layoutInflater, binding.categoryContainer, false)
            row.tvName.text = name
            row.tvCount.text = count.toString()
            row.progress.setProgressCompat(if (total == 0) 0 else count * 100 / total, true)
            binding.categoryContainer.addView(row.root)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

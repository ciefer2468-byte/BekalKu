package com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main.tabs

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.unimal.uas_240170129_muhammad_raihann_azhari.R
import com.unimal.uas_240170129_muhammad_raihann_azhari.databinding.FragmentMenuBinding
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common.ChipUtil
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common.MenuAdapter
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common.StateController
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common.UiState
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.detail.DetailActivity
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.form.FormActivity
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.inspiration.InspirationBottomSheet
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main.MenuViewModel
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main.filterBy
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.applyTopInsetPadding

/** Tab Menu: daftar semua menu bekal + pencarian + filter kategori. */
class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MenuViewModel by activityViewModels()
    private lateinit var adapter: MenuAdapter
    private lateinit var stateController: StateController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.applyTopInsetPadding()
        binding.tvTitle.text = buildTitle()

        adapter = MenuAdapter(
            onClick = { startActivity(DetailActivity.intent(requireContext(), it.id)) },
            onTogglePacked = { viewModel.togglePacked(it) }
        )
        binding.rvMenu.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMenu.adapter = adapter
        stateController = StateController(binding.stateView)

        binding.btnAdd.setOnClickListener { startActivity(FormActivity.intent(requireContext())) }
        binding.btnInspire.setOnClickListener {
            InspirationBottomSheet().show(childFragmentManager, "inspiration")
        }

        binding.etSearch.setText(viewModel.searchQuery)
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.searchQuery = text?.toString().orEmpty()
            render()
        }

        binding.chips.chipGroup.check(ChipUtil.chipIdOf(viewModel.menuCategory))
        binding.chips.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            viewModel.menuCategory = ChipUtil.categoryOf(checkedIds.firstOrNull() ?: R.id.chipAll)
            render()
        }

        viewModel.state.observe(viewLifecycleOwner) { render() }
    }

    private fun buildTitle(): CharSequence {
        val prefix = getString(R.string.menu_title_prefix)
        val highlight = getString(R.string.menu_title_highlight)
        val suffix = getString(R.string.menu_title_suffix)
        return SpannableStringBuilder().apply {
            append(prefix)
            val start = length
            append(highlight)
            setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.primary)),
                start, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            append(suffix)
        }
    }

    private fun render() {
        when (val state = viewModel.state.value) {
            null, UiState.Loading -> {
                binding.rvMenu.isVisible = false
                stateController.loading()
            }
            is UiState.Error -> {
                binding.rvMenu.isVisible = false
                stateController.show(
                    R.drawable.ic_error, R.string.error_title, getString(R.string.error_load), R.string.retry
                ) { viewModel.load() }
            }
            is UiState.Success -> {
                val all = state.data
                val filtered = all.filterBy(viewModel.searchQuery, viewModel.menuCategory)
                adapter.submitList(filtered)
                when {
                    all.isEmpty() -> {
                        binding.rvMenu.isVisible = false
                        stateController.show(
                            R.drawable.ic_restaurant, R.string.empty_title, getString(R.string.empty_message),
                            R.string.empty_action
                        ) { startActivity(FormActivity.intent(requireContext())) }
                    }
                    filtered.isEmpty() -> {
                        binding.rvMenu.isVisible = false
                        stateController.show(
                            R.drawable.ic_search, R.string.search_empty_title, getString(R.string.search_empty_message)
                        )
                    }
                    else -> {
                        binding.rvMenu.isVisible = true
                        stateController.hide()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.rvMenu.adapter = null
        _binding = null
        super.onDestroyView()
    }
}

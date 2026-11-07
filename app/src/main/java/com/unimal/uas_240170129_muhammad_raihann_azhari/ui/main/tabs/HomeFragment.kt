package com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main.tabs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.unimal.uas_240170129_muhammad_raihann_azhari.R
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.model.BekalMenu
import com.unimal.uas_240170129_muhammad_raihann_azhari.databinding.FragmentHomeBinding
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common.ChipUtil
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common.MenuAdapter
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common.StateController
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common.UiState
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.detail.DetailActivity
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.form.FormActivity
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main.MainActivity
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main.MenuViewModel
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main.filterBy
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.SessionManager
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.applyTopInsetPadding
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.todayIndex

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MenuViewModel by activityViewModels()
    private lateinit var adapter: MenuAdapter
    private lateinit var stateController: StateController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.headerContainer.applyTopInsetPadding()

        val session = SessionManager(requireContext())
        val firstName = session.userName.trim().substringBefore(' ').ifEmpty { session.userName }
        binding.tvGreeting.text = getString(R.string.greeting_format, firstName)

        adapter = MenuAdapter(
            onClick = { openDetail(it) },
            onTogglePacked = { viewModel.togglePacked(it) }
        )
        binding.rvToday.layoutManager = LinearLayoutManager(requireContext())
        binding.rvToday.adapter = adapter
        stateController = StateController(binding.stateView)

        binding.btnNotif.setOnClickListener { (activity as? MainActivity)?.selectTab(4) }

        binding.chips.chipGroup.check(ChipUtil.chipIdOf(viewModel.homeCategory))
        binding.chips.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            viewModel.homeCategory = ChipUtil.categoryOf(checkedIds.firstOrNull() ?: R.id.chipAll)
            render()
        }

        viewModel.state.observe(viewLifecycleOwner) { render() }
    }

    private fun render() {
        when (val state = viewModel.state.value) {
            null, UiState.Loading -> {
                binding.rvToday.isVisible = false
                stateController.loading()
            }
            is UiState.Error -> {
                binding.rvToday.isVisible = false
                stateController.show(
                    R.drawable.ic_error, R.string.error_title, getString(R.string.error_load),
                    R.string.retry
                ) { viewModel.load() }
            }
            is UiState.Success -> renderSuccess(state.data.filter { it.day == todayIndex() })
        }
    }

    private fun renderSuccess(today: List<BekalMenu>) {
        renderHero(today)

        val packed = today.count { it.packed }
        if (today.isEmpty()) {
            binding.tvProgress.setText(R.string.progress_none)
            binding.progressBar.setProgressCompat(0, true)
        } else {
            binding.tvProgress.text = getString(R.string.progress_format, packed, today.size)
            binding.progressBar.setProgressCompat(packed * 100 / today.size, true)
        }
        binding.tvSectionCount.text = today.size.toString()

        val filtered = today.filterBy("", viewModel.homeCategory)
        adapter.submitList(filtered)

        when {
            today.isEmpty() -> {
                binding.rvToday.isVisible = false
                stateController.show(
                    R.drawable.ic_restaurant, R.string.empty_today_title, getString(R.string.empty_today_message),
                    R.string.empty_action
                ) { startActivity(FormActivity.intent(requireContext(), todayIndex())) }
            }
            filtered.isEmpty() -> {
                binding.rvToday.isVisible = false
                stateController.show(
                    R.drawable.ic_search, R.string.search_empty_title, getString(R.string.category_empty_message)
                )
            }
            else -> {
                binding.rvToday.isVisible = true
                stateController.hide()
            }
        }
    }

    private fun renderHero(today: List<BekalMenu>) {
        val days = resources.getStringArray(R.array.days)
        val categories = resources.getStringArray(R.array.categories)
        val featured = today.firstOrNull { !it.packed } ?: today.firstOrNull()

        if (featured == null) {
            binding.tvHeroTag.text = getString(R.string.hero_tag_day_format, days[todayIndex()])
            binding.tvHeroTitle.setText(R.string.hero_empty_title)
            binding.tvHeroDesc.setText(R.string.hero_empty_desc)
            binding.btnHero.setText(R.string.hero_btn_plan)
            binding.btnHero.setOnClickListener {
                startActivity(FormActivity.intent(requireContext(), todayIndex()))
            }
        } else {
            binding.tvHeroTag.text = getString(
                R.string.hero_tag_menu_format, days[featured.day], categories.getOrElse(featured.category) { "" }
            )
            binding.tvHeroTitle.text = featured.name
            binding.tvHeroDesc.text = featured.ingredients.lines()
                .map { it.trim() }.filter { it.isNotEmpty() }.joinToString(", ")
            binding.btnHero.setText(R.string.hero_btn_view)
            binding.btnHero.setOnClickListener { openDetail(featured) }
        }
    }

    private fun openDetail(menu: BekalMenu) {
        startActivity(DetailActivity.intent(requireContext(), menu.id))
    }

    override fun onDestroyView() {
        binding.rvToday.adapter = null
        _binding = null
        super.onDestroyView()
    }
}

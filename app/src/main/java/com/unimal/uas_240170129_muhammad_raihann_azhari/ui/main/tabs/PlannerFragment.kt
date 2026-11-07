package com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.unimal.uas_240170129_muhammad_raihann_azhari.R
import com.unimal.uas_240170129_muhammad_raihann_azhari.databinding.FragmentPlannerBinding
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common.MenuAdapter
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common.StateController
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.common.UiState
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.detail.DetailActivity
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.form.FormActivity
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main.MenuViewModel
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.applyTopInsetPadding

/** Tab Rencana: perencanaan menu bekal per hari dalam seminggu (TabLayout Senin-Minggu). */
class PlannerFragment : Fragment() {

    private var _binding: FragmentPlannerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MenuViewModel by activityViewModels()
    private lateinit var adapter: MenuAdapter
    private lateinit var stateController: StateController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.applyTopInsetPadding()

        adapter = MenuAdapter(
            onClick = { startActivity(DetailActivity.intent(requireContext(), it.id)) },
            onTogglePacked = { viewModel.togglePacked(it) }
        )
        binding.rvPlan.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPlan.adapter = adapter
        stateController = StateController(binding.stateView)

        val shortDays = resources.getStringArray(R.array.days_short)
        shortDays.forEach { binding.tabDays.addTab(binding.tabDays.newTab().setText(it), false) }
        binding.tabDays.getTabAt(viewModel.plannerDay)?.select()
        binding.tabDays.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.plannerDay = tab.position
                render()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })

        binding.btnAdd.setOnClickListener {
            startActivity(FormActivity.intent(requireContext(), viewModel.plannerDay))
        }

        viewModel.state.observe(viewLifecycleOwner) { render() }
    }

    private fun render() {
        val days = resources.getStringArray(R.array.days)
        val dayName = days[viewModel.plannerDay]
        binding.tvDay.text = dayName

        when (val state = viewModel.state.value) {
            null, UiState.Loading -> {
                binding.rvPlan.isVisible = false
                binding.tvSummary.text = ""
                stateController.loading()
            }
            is UiState.Error -> {
                binding.rvPlan.isVisible = false
                binding.tvSummary.text = ""
                stateController.show(
                    R.drawable.ic_error, R.string.error_title, getString(R.string.error_load), R.string.retry
                ) { viewModel.load() }
            }
            is UiState.Success -> {
                val list = state.data.filter { it.day == viewModel.plannerDay }
                adapter.submitList(list)
                binding.tvSummary.text = getString(R.string.planner_summary_format, list.size, list.sumOf { it.calories })
                if (list.isEmpty()) {
                    binding.rvPlan.isVisible = false
                    stateController.show(
                        R.drawable.ic_calendar, R.string.day_empty_title,
                        getString(R.string.day_empty_message, dayName), R.string.empty_action
                    ) { startActivity(FormActivity.intent(requireContext(), viewModel.plannerDay)) }
                } else {
                    binding.rvPlan.isVisible = true
                    stateController.hide()
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.rvPlan.adapter = null
        _binding = null
        super.onDestroyView()
    }
}

package com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.unimal.uas_240170129_muhammad_raihann_azhari.R
import com.unimal.uas_240170129_muhammad_raihann_azhari.databinding.ActivityMainBinding
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.auth.LoginActivity
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main.tabs.HomeFragment
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main.tabs.MenuFragment
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main.tabs.PlannerFragment
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main.tabs.ProfileFragment
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main.tabs.StatsFragment
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.SessionManager
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.isNightMode
import kotlin.math.abs

/**
 * Host 5 tab: Beranda, Menu, Rencana, Statistik, Profil.
 * ViewPager2 + BottomNavigationView saling tersinkron sehingga bisa digeser (swipe) maupun diketuk.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MenuViewModel by viewModels()

    private val navIcons = intArrayOf(
        R.drawable.ic_home, R.drawable.ic_restaurant, R.drawable.ic_calendar,
        R.drawable.ic_bar_chart, R.drawable.ic_person
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SessionManager(this).isLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Kartu navigasi mengambang: naikkan sesuai tinggi navigation bar sistem.
        val baseMargin = resources.getDimensionPixelSize(R.dimen.nav_margin)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.navCard.updateLayoutParams<androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams> {
                bottomMargin = baseMargin + bars.bottom
                leftMargin = baseMargin + bars.left
                rightMargin = baseMargin + bars.right
            }
            insets
        }

        setupPager()
        setupBottomNav()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.pager.currentItem != 0) {
                    binding.pager.setCurrentItem(0, true)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        // Muat ulang data setiap kembali dari layar detail / form.
        viewModel.load(silent = true)
    }

    private fun setupPager() {
        binding.pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = navIcons.size
            override fun createFragment(position: Int): Fragment = when (position) {
                0 -> HomeFragment()
                1 -> MenuFragment()
                2 -> PlannerFragment()
                3 -> StatsFragment()
                else -> ProfileFragment()
            }
        }
        binding.pager.offscreenPageLimit = navIcons.size - 1

        // Transisi mulus: halaman memudar & mengecil sedikit saat digeser.
        binding.pager.setPageTransformer { page, position ->
            val distance = abs(position).coerceAtMost(1f)
            page.alpha = 1f - 0.5f * distance
            val scale = 1f - 0.06f * distance
            page.scaleX = scale
            page.scaleY = scale
        }

        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // Gelembung liquid mengikuti geseran halaman secara kontinu.
                binding.liquidNav.setProgress(position + positionOffset)
            }

            override fun onPageSelected(position: Int) {
                if (binding.pager.scrollState == ViewPager2.SCROLL_STATE_IDLE) {
                    binding.liquidNav.setProgress(position.toFloat())
                }
                // Beranda & Profil berlatar oranye (dan semua tab saat mode gelap): ikon status bar putih.
                val lightIcons = position != 0 && position != 4 && !isNightMode()
                WindowCompat.getInsetsController(window, binding.root).isAppearanceLightStatusBars = lightIcons
            }
        })
        WindowCompat.getInsetsController(window, binding.root).isAppearanceLightStatusBars = false
    }

    private fun setupBottomNav() {
        binding.liquidNav.setItems(
            navIcons,
            arrayOf(
                getString(R.string.nav_home), getString(R.string.nav_menu), getString(R.string.nav_planner),
                getString(R.string.nav_stats), getString(R.string.nav_profile)
            )
        )
        binding.liquidNav.setProgress(binding.pager.currentItem.toFloat())
        binding.liquidNav.onItemSelected = { index -> binding.pager.setCurrentItem(index, true) }
    }

    /** Dipakai fragment untuk berpindah tab secara terprogram. */
    fun selectTab(index: Int) {
        binding.pager.setCurrentItem(index, true)
    }
}

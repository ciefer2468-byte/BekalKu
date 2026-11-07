package com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main.tabs

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.unimal.uas_240170129_muhammad_raihann_azhari.R
import com.unimal.uas_240170129_muhammad_raihann_azhari.databinding.FragmentProfileBinding
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.auth.LoginActivity
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.repository.AuthRepository
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.AppExecutors
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.ReminderScheduler
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.SessionManager
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.ThemeManager
import com.unimal.uas_240170129_muhammad_raihann_azhari.util.applyTopInsetPadding

/** Tab Profil: info akun, pengingat harian (notifikasi terjadwal), dan logout. */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var session: SessionManager
    private var updatingSwitch = false

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                enableReminder()
            } else {
                // Izin ditolak: aplikasi tetap berjalan normal, pengingat tidak diaktifkan.
                setSwitch(false)
                showSnack(getString(R.string.permission_denied))
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.header.applyTopInsetPadding()
        session = SessionManager(requireContext())

        binding.tvAvatar.text = session.userName.trim().firstOrNull()?.uppercase() ?: "?"
        binding.tvName.text = session.userName
        binding.tvEmail.text = session.userEmail
        binding.tvVersion.text = getString(R.string.version_format, appVersion())

        setSwitch(session.reminderEnabled && hasNotificationPermission())
        updateTimeLabel()

        binding.switchReminder.setOnCheckedChangeListener { _, checked ->
            if (!updatingSwitch) onReminderToggled(checked)
        }
        binding.rowTime.setOnClickListener { pickTime() }

        val currentTheme = ThemeManager.get(requireContext())
        binding.toggleTheme.check(
            when (currentTheme) {
                ThemeManager.LIGHT -> R.id.btnThemeLight
                ThemeManager.DARK -> R.id.btnThemeDark
                else -> R.id.btnThemeSystem
            }
        )
        binding.toggleTheme.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val mode = when (checkedId) {
                R.id.btnThemeLight -> ThemeManager.LIGHT
                R.id.btnThemeDark -> ThemeManager.DARK
                else -> ThemeManager.SYSTEM
            }
            if (mode != ThemeManager.get(requireContext())) ThemeManager.set(requireContext(), mode)
        }
        binding.btnLogout.setOnClickListener { confirmLogout() }
    }

    private fun appVersion(): String = try {
        requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName ?: "1.0"
    } catch (e: Exception) {
        "1.0"
    }

    private fun hasNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun onReminderToggled(checked: Boolean) {
        if (!checked) {
            session.reminderEnabled = false
            ReminderScheduler.cancel(requireContext())
            showSnack(getString(R.string.reminder_off))
        } else if (hasNotificationPermission()) {
            enableReminder()
        } else {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun enableReminder() {
        session.reminderEnabled = true
        ReminderScheduler.createChannel(requireContext())
        ReminderScheduler.schedule(requireContext(), session.reminderHour, session.reminderMinute)
        setSwitch(true)
        showSnack(getString(R.string.reminder_on, timeText()))
    }

    private fun pickTime() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(session.reminderHour)
            .setMinute(session.reminderMinute)
            .setTitleText(R.string.pick_time_title)
            .build()
        picker.addOnPositiveButtonClickListener {
            session.reminderHour = picker.hour
            session.reminderMinute = picker.minute
            updateTimeLabel()
            if (session.reminderEnabled) {
                ReminderScheduler.schedule(requireContext(), picker.hour, picker.minute)
                showSnack(getString(R.string.reminder_on, timeText()))
            }
        }
        picker.show(childFragmentManager, "time_picker")
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout_title)
            .setMessage(R.string.logout_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.btn_logout) { _, _ ->
                ReminderScheduler.cancel(requireContext())
                val appContext = requireContext().applicationContext
                AppExecutors.io.execute { AuthRepository(appContext).signOut() }
                session.clear()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                requireActivity().finish()
            }
            .show()
    }

    private fun setSwitch(checked: Boolean) {
        updatingSwitch = true
        binding.switchReminder.isChecked = checked
        updatingSwitch = false
    }

    private fun timeText() = getString(R.string.time_format, session.reminderHour, session.reminderMinute)

    private fun updateTimeLabel() {
        binding.tvTime.text = timeText()
    }

    private fun showSnack(message: String) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        requireActivity().findViewById<View>(R.id.navCard)?.let { snackbar.setAnchorView(it) }
        snackbar.show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

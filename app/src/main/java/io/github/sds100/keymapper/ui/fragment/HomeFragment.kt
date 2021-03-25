package io.github.sds100.keymapper.ui.fragment

import android.Manifest
import android.content.*
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.addRepeatingJob
import androidx.navigation.fragment.findNavController
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.viewpager2.widget.ViewPager2
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.tabs.TabLayoutMediator
import io.github.sds100.keymapper.*
import io.github.sds100.keymapper.data.model.ChooseAppStoreModel
import io.github.sds100.keymapper.data.viewmodel.*
import io.github.sds100.keymapper.databinding.DialogChooseAppStoreBinding
import io.github.sds100.keymapper.databinding.FragmentHomeBinding
import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.service.MyAccessibilityService
import io.github.sds100.keymapper.ui.adapter.HomePagerAdapter
import io.github.sds100.keymapper.ui.view.StatusLayout
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.delegate.RecoverFailureDelegate
import io.github.sds100.keymapper.util.result.getFullMessage
import io.github.sds100.keymapper.worker.SeedDatabaseWorker
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import splitties.alertdialog.appcompat.alertDialog
import splitties.alertdialog.appcompat.cancelButton
import splitties.alertdialog.appcompat.messageResource
import splitties.systemservices.powerManager
import splitties.toast.longToast
import splitties.toast.toast
import java.util.*

class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by activityViewModels {
        InjectorUtils.provideHomeViewModel(requireContext())
    }

    private val fingerprintMapListViewModel: FingerprintMapListViewModel by activityViewModels {
        InjectorUtils.provideFingerprintMapListViewModel(requireContext())
    }

    /**
     * Scoped to the lifecycle of the fragment's view (between onCreateView and onDestroyView)
     */
    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding
        get() = _binding!!

    private val expandedHeader = MutableLiveData(false)
    private val collapsedStatusState = MutableLiveData(StatusLayout.State.ERROR)
    private val accessibilityServiceStatusState = MutableLiveData(StatusLayout.State.ERROR)
    private val imeServiceStatusState = MutableLiveData(StatusLayout.State.ERROR)
    private val dndAccessStatusState = MutableLiveData(StatusLayout.State.ERROR)
    private val writeSettingsStatusState = MutableLiveData(StatusLayout.State.ERROR)
    private val batteryOptimisationState = MutableLiveData(StatusLayout.State.ERROR)

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return

            when (intent.action) {

                MyAccessibilityService.ACTION_ON_START -> {
                    accessibilityServiceStatusState.value = StatusLayout.State.POSITIVE
                }

                MyAccessibilityService.ACTION_ON_STOP -> {
                    accessibilityServiceStatusState.value = StatusLayout.State.ERROR
                }
            }
        }
    }

    private val backupAllKeymapsLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) {
            it ?: return@registerForActivityResult

            backupRestoreViewModel
                .backupAll(requireContext().contentResolver.openOutputStream(it))
        }

    private val restoreLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            it ?: return@registerForActivityResult

            backupRestoreViewModel.restore(requireContext().contentResolver.openInputStream(it))
        }

    private val backupRestoreViewModel: BackupRestoreViewModel by activityViewModels {
        InjectorUtils.provideBackupRestoreViewModel(requireContext())
    }

    private val requestAccessNotificationPolicy =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            updateStatusLayouts()
        }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            if (position == 0) {
                fab.show()
            } else {
                fab.hide()
            }
        }
    }

    private lateinit var recoverFailureDelegate: RecoverFailureDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        IntentFilter().apply {
            addAction(Intent.ACTION_INPUT_METHOD_CHANGED)
            addAction(MyAccessibilityService.ACTION_ON_START)
            addAction(MyAccessibilityService.ACTION_ON_STOP)

            requireContext().registerReceiver(broadcastReceiver, this)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        recoverFailureDelegate = RecoverFailureDelegate(
            "HomeFragment",
            requireActivity().activityResultRegistry,
            viewLifecycleOwner
        ) {
            homeViewModel.rebuildUiState()
        }

        FragmentHomeBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            _binding = this
            return this.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            viewModel = this@HomeFragment.homeViewModel

            val pagerAdapter = HomePagerAdapter(
                this@HomeFragment,
                fingerprintMapListViewModel.fingerprintGesturesAvailable.value ?: false
            )

            viewPager.adapter = pagerAdapter

            fingerprintMapListViewModel.fingerprintGesturesAvailable.observe(viewLifecycleOwner, {
                pagerAdapter.invalidateFragments(it ?: false)
                isFingerprintGestureDetectionAvailable = it ?: false
            })

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = strArray(R.array.home_tab_titles)[position]
            }.apply {
                attach()
            }

            viewPager.registerOnPageChangeCallback(onPageChangeCallback)

            appBar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_help -> {
                        UrlUtils.launchCustomTab(
                            requireContext(),
                            str(R.string.url_quick_start_guide)
                        )
                        true
                    }

                    R.id.action_seed_database -> {
                        val request = OneTimeWorkRequestBuilder<SeedDatabaseWorker>().build()
                        WorkManager.getInstance(requireContext()).enqueue(request)
                        true
                    }

                    R.id.action_select_all -> {
                        homeViewModel.onSelectAllClick()
                        true
                    }

                    R.id.action_enable -> {
                        homeViewModel.onEnableSelectedKeymapsClick()
                        true
                    }

                    R.id.action_disable -> {
                        homeViewModel.onDisableSelectedKeymapsClick()
                        true
                    }

                    R.id.action_duplicate_keymap -> {
                        homeViewModel.onDuplicateSelectedKeymapsClick()
                        true
                    }

                    R.id.action_backup -> {
                        homeViewModel.onBackupSelectedKeymapsClick()
                        true
                    }

                    else -> false
                }
            }

            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                homeViewModel.onBackPressed()
            }

            appBar.setNavigationOnClickListener {
                homeViewModel.onAppBarNavigationButtonClick()
            }

            backupRestoreViewModel.eventStream.observe(viewLifecycleOwner, {
                when (it) {
                    is MessageEvent -> toast(it.textRes)
                    is ShowErrorMessage -> toast(it.error.getFullMessage(requireContext()))
                    is RequestRestore -> restoreLauncher.launch(FileUtils.MIME_TYPE_ALL)
                    is RequestBackupAll ->
                        backupAllKeymapsLauncher.launch(BackupUtils.createFileName())
                }
            })

            //TODO move all this to view model
            expanded = expandedHeader
            collapsedStatusLayoutState = this@HomeFragment.collapsedStatusState
            accessibilityServiceStatusState = this@HomeFragment.accessibilityServiceStatusState
            imeServiceStatusState = this@HomeFragment.imeServiceStatusState
            dndAccessStatusState = this@HomeFragment.dndAccessStatusState
            writeSettingsStatusState = this@HomeFragment.writeSettingsStatusState
            batteryOptimisationState = this@HomeFragment.batteryOptimisationState

            buttonCollapse.setOnClickListener {
                expandedHeader.value = false
            }

            layoutCollapsed.setOnClickListener {
                expandedHeader.value = true
            }

            setEnableAccessibilityService {
                AccessibilityUtils.enableService(requireContext())
            }

            setEnableImeService {
                viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {

                    KeyboardUtils.enableCompatibleInputMethods(requireContext())

                    delay(3000)

                    updateStatusLayouts()
                }
            }

            setGrantWriteSecureSettingsPermission {
                PermissionUtils.requestWriteSecureSettingsPermission(
                    requireContext(),
                    findNavController()
                )
            }

            setGrantDndAccess {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PermissionUtils.requestAccessNotificationPolicy(requestAccessNotificationPolicy)
                }
            }

            setDisableBatteryOptimisation {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        longToast(R.string.error_battery_optimisation_activity_not_found)
                    }
                }
            }

            expandedHeader.observe(viewLifecycleOwner, {
                if (it == true) {
                    expandableLayout.expand()
                } else {
                    expandableLayout.collapse()

                    val transition = Fade()
                    TransitionManager.beginDelayedTransition(layoutCollapsed, transition)
                }
            })

            updateStatusLayouts()

            setGetNewGuiKeyboard {
                requireContext().alertDialog {
                    messageResource = R.string.dialog_message_select_app_store_gui_keyboard

                    DialogChooseAppStoreBinding.inflate(layoutInflater).apply {
                        model = ChooseAppStoreModel(
                            playStoreLink = str(R.string.url_play_store_keymapper_gui_keyboard),
                            githubLink = str(R.string.url_github_keymapper_gui_keyboard),
                            fdroidLink = str(R.string.url_fdroid_keymapper_gui_keyboard)
                        )

                        setView(this.root)
                    }

                    cancelButton()

                    show()
                }
            }

            viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
                homeViewModel.fixFailure.collectLatest {
                    coordinatorLayout.showFixErrorSnackBar(
                        requireContext(),
                        it,
                        recoverFailureDelegate,
                        findNavController()
                    )
                }
            }
        }

        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
            homeViewModel.onboardingState.collectLatest {
                if (it.showQuickStartGuideTapTarget) {
                    QuickStartGuideTapTarget().show(this@HomeFragment, R.id.action_help) {
                        homeViewModel.approvedQuickStartGuideTapTarget()
                    }
                }

                if (it.showWhatsNew) {
                    val direction = NavAppDirections.actionGlobalOnlineFileFragment(
                        R.string.whats_new,
                        R.string.url_changelog
                    )
                    findNavController().navigate(direction)

                    homeViewModel.approvedWhatsNew()
                }
            }
        }

        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
            homeViewModel.tabsState.collectLatest {
                binding.viewPager.isUserInputEnabled = it.enableViewPagerSwiping
            }
        }

        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
            homeViewModel.appBarState.collectLatest {
                if (it == HomeAppBarState.MULTI_SELECTING) {
                    binding.appBar.replaceMenu(R.menu.menu_multi_select)
                } else {
                    binding.appBar.replaceMenu(R.menu.menu_home)
                }
            }
        }

        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
            homeViewModel.navigateToCreateKeymapScreen.collectLatest {
                val direction = HomeFragmentDirections.actionToConfigKeymap()
                findNavController().navigate(direction)
            }
        }

        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
            homeViewModel.showMenu.collectLatest {
                findNavController().navigate(R.id.action_global_menuFragment)
            }
        }

        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
            homeViewModel.closeKeyMapper.collectLatest {
                requireActivity().finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        homeViewModel.rebuildUiState()

        updateStatusLayouts()

        if (PackageUtils.isAppInstalled(
                requireContext(),
                KeyboardUtils.KEY_MAPPER_GUI_IME_PACKAGE
            )
            || Build.VERSION.SDK_INT < KeyboardUtils.KEY_MAPPER_GUI_IME_MIN_API
        ) {
            ServiceLocator.preferenceRepository(requireContext()).set(Keys.showGuiKeyboardAd, false)
        }

        ServiceLocator.notificationController(requireContext())
            .onEvent(DismissFingerprintFeatureNotification)
    }

    override fun onDestroyView() {
        binding.viewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
        _binding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        requireContext().unregisterReceiver(broadcastReceiver)

        super.onDestroy()
    }

    private fun updateStatusLayouts() {
        if (AccessibilityUtils.isServiceEnabled(requireContext())) {
            accessibilityServiceStatusState.value = StatusLayout.State.POSITIVE

        } else {
            accessibilityServiceStatusState.value = StatusLayout.State.ERROR
        }

        if (PermissionUtils.haveWriteSecureSettingsPermission(requireContext())) {
            writeSettingsStatusState.value = StatusLayout.State.POSITIVE
        } else {
            writeSettingsStatusState.value = StatusLayout.State.WARN
        }

//        if (KeyboardUtils.isCompatibleImeEnabled()) {
//            imeServiceStatusState.value = StatusLayout.State.POSITIVE
//
//        } else if (keymapListViewModel.model.value is Data) {
//
//            if ((keymapListViewModel.model.value as Data<List<KeymapListItemModel>>)
//                    .data.any { keymap ->
//                        keymap.actionList.any { it.error is RecoverableError.NoCompatibleImeEnabled }
//                    }
//            ) {
//
//                imeServiceStatusState.value = StatusLayout.State.ERROR
//            }
//
//        } else {
//            imeServiceStatusState.value = StatusLayout.State.WARN
//        } //TODO

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionUtils.isPermissionGranted(
                    requireContext(),
                    Manifest.permission.ACCESS_NOTIFICATION_POLICY
                )
            ) {

                dndAccessStatusState.value = StatusLayout.State.POSITIVE
            } else {
                dndAccessStatusState.value = StatusLayout.State.WARN
            }

            if (powerManager.isIgnoringBatteryOptimizations(Constants.PACKAGE_NAME)) {
                batteryOptimisationState.value = StatusLayout.State.POSITIVE
            } else {
                batteryOptimisationState.value = StatusLayout.State.WARN
            }
        }

        val states = listOf(
            accessibilityServiceStatusState,
            writeSettingsStatusState,
            imeServiceStatusState,
            dndAccessStatusState,
            batteryOptimisationState
        )

        when {
            states.all { it.value == StatusLayout.State.POSITIVE } -> {
                expandedHeader.value = false
                collapsedStatusState.value = StatusLayout.State.POSITIVE
            }

            states.any { it.value == StatusLayout.State.ERROR } -> {
                expandedHeader.value = true
                collapsedStatusState.value = StatusLayout.State.ERROR
            }

            states.any { it.value == StatusLayout.State.WARN } -> {
                expandedHeader.value = false
                collapsedStatusState.value = StatusLayout.State.WARN
            }
        }
    }
}
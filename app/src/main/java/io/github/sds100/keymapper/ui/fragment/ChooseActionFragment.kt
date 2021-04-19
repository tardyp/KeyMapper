package io.github.sds100.keymapper.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.actions.ChooseActionViewModel
import io.github.sds100.keymapper.keyevents.ConfigKeyEventViewModel
import io.github.sds100.keymapper.databinding.FragmentChooseActionBinding
import io.github.sds100.keymapper.domain.actions.*
import io.github.sds100.keymapper.apps.ChooseAppShortcutResult
import io.github.sds100.keymapper.ui.adapter.ChooseActionPagerAdapter
import io.github.sds100.keymapper.ui.keyevent.ConfigKeyEventResult
import io.github.sds100.keymapper.ui.keyevent.KeyCodeListFragment
import io.github.sds100.keymapper.ui.shortcuts.ChooseAppShortcutFragment
import io.github.sds100.keymapper.ui.utils.getJsonSerializable
import io.github.sds100.keymapper.ui.utils.putJsonSerializable
import io.github.sds100.keymapper.util.*

/**
 * A placeholder fragment containing a simple view.
 */
class ChooseActionFragment : Fragment() {

    companion object {
        const val EXTRA_ACTION = "extra_action"
    }

    private val viewModel by activityViewModels<ChooseActionViewModel> { ChooseActionViewModel.Factory() }

    private val mArgs by navArgs<ChooseActionFragmentArgs>()

    /**
     * Scoped to the lifecycle of the fragment's view (between onCreateView and onDestroyView)
     */
    private var _binding: FragmentChooseActionBinding? = null
    val binding: FragmentChooseActionBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        createActionOnResult(ChooseAppFragment.REQUEST_KEY) {
            val packageName = it.getString(ChooseAppFragment.EXTRA_PACKAGE_NAME)
            OpenAppAction(packageName!!)
        }

        createActionOnResult(ChooseAppShortcutFragment.REQUEST_KEY) {
            val result =
                it.getJsonSerializable<ChooseAppShortcutResult>(ChooseAppShortcutFragment.EXTRA_RESULT)

            OpenAppShortcutAction(
                result!!.packageName,
                result.shortcutName,
                result.uri
            )
        }

        createActionOnResult(KeyActionTypeFragment.REQUEST_KEY) {
            val keyCode = it.getInt(KeyActionTypeFragment.EXTRA_KEYCODE)

            KeyEventAction(keyCode)
        }

        createActionOnResult(ConfigKeyEventFragment.REQUEST_KEY) { bundle ->
            val result =
                bundle.getJsonSerializable<ConfigKeyEventResult>(ConfigKeyEventFragment.EXTRA_RESULT)

            result!!

            KeyEventAction(result.keyCode, result.metaState, result.useShell, result.device)
        }

        createActionOnResult(TextBlockActionTypeFragment.REQUEST_KEY) {
            val text = it.getString(TextBlockActionTypeFragment.EXTRA_TEXT_BLOCK)

            TextAction(text!!)
        }

        createActionOnResult(UrlActionTypeFragment.REQUEST_KEY) {
            val url = it.getString(UrlActionTypeFragment.EXTRA_URL)

            UrlAction(url!!)
        }

        createActionOnResult(SystemActionListFragment.REQUEST_KEY) {
            it.getJsonSerializable<SystemAction>(SystemActionListFragment.EXTRA_SYSTEM_ACTION)!!
        }

        createActionOnResult(KeyCodeListFragment.REQUEST_KEY) {
            val keyCode = it.getInt(KeyCodeListFragment.EXTRA_KEYCODE)

            KeyEventAction(keyCode)
        }

        createActionOnResult(PickDisplayCoordinateFragment.REQUEST_KEY) {
            val x = it.getInt(PickDisplayCoordinateFragment.EXTRA_X)
            val y = it.getInt(PickDisplayCoordinateFragment.EXTRA_Y)
            val description = it.getString(PickDisplayCoordinateFragment.EXTRA_DESCRIPTION)

            TapCoordinateAction(x, y, description)
        }

        createActionOnResult(IntentActionTypeFragment.REQUEST_KEY) { bundle ->
            val description = bundle.getString(IntentActionTypeFragment.EXTRA_DESCRIPTION)!!
            val target = bundle.getString(IntentActionTypeFragment.EXTRA_TARGET)!!.let {
                IntentTarget.valueOf(it)
            }
            val uri = bundle.getString(IntentActionTypeFragment.EXTRA_URI)!!

            IntentAction(description, target, uri)
        }

        createActionOnResult(PhoneCallActionTypeFragment.REQUEST_KEY) {
            val number = it.getString(PhoneCallActionTypeFragment.EXTRA_PHONE_NUMBER)

            PhoneCallAction(number!!)
        }

        setFragmentResultListener(KeyCodeListFragment.REQUEST_KEY) { _, result ->
            val keyEventViewModel by activityViewModels<ConfigKeyEventViewModel> {
                Inject.configKeyEventViewModel(requireContext())
            }

            result.getInt(KeyCodeListFragment.EXTRA_KEYCODE).let {
                keyEventViewModel.setKeyCode(it)
            }
        }

        FragmentChooseActionBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            _binding = this

            return this.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            val pagerAdapter = ChooseActionPagerAdapter(this@ChooseActionFragment)
            viewPager.adapter = pagerAdapter

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = str(pagerAdapter.tabFragmentCreators[position].tabTitle)
            }.attach()

            appBar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            viewModel.currentTabPosition.let {
                if (it > pagerAdapter.itemCount - 1) return@let

                viewPager.setCurrentItem(it, false)
            }

            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    viewModel.currentTabPosition = position
                }
            })

            subscribeSearchView(pagerAdapter)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    @Suppress("UNCHECKED_CAST")
    private fun createActionOnResult(
        requestKey: String,
        createAction: (bundle: Bundle) -> ActionData
    ) {
        childFragmentManager.setFragmentResultListener(
            requestKey,
            viewLifecycleOwner
        ) { _, result ->
            val action = createAction(result)

            setFragmentResult(
                this.mArgs.chooseActionRequestKey,
                Bundle().apply { putJsonSerializable(EXTRA_ACTION, action) }
            )
        }
    }

    private fun FragmentChooseActionBinding.subscribeSearchView(
        pagerAdapter: ChooseActionPagerAdapter
    ) {
        val searchViewMenuItem = appBar.menu.findItem(R.id.action_search)
        val searchView = searchViewMenuItem.actionView as SearchView

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                searchViewMenuItem.isVisible =
                    pagerAdapter.tabFragmentCreators[position].searchStateKey != null
            }
        })

        searchViewMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                //don't allow the user to change the tab when searching
                viewPager.isUserInputEnabled = false
                tabLayout.isVisible = false

                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                viewPager.isUserInputEnabled = true
                tabLayout.isVisible = true

                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String?): Boolean {

                pagerAdapter.tabFragmentCreators[viewPager.currentItem].searchStateKey?.let {
                    findNavController().setCurrentDestinationLiveData(it, newText)
                }

                return false
            }

            override fun onQueryTextSubmit(query: String?) = onQueryTextChange(query)
        })
    }
}
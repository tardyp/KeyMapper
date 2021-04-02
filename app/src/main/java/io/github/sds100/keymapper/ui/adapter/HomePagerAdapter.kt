package io.github.sds100.keymapper.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.sds100.keymapper.ui.fragment.fingerprint.FingerprintMapListFragment
import io.github.sds100.keymapper.ui.fragment.keymap.KeymapListFragment
import io.github.sds100.keymapper.ui.home.HomeTab

/**
 * Created by sds100 on 26/01/2020.
 */

class HomePagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    private var tabs: Set<HomeTab> = emptySet()
    private val tabFragmentsCreators: List<() -> Fragment>
        get() = tabs.map { tab ->
            when (tab) {
                HomeTab.KEY_EVENTS -> {
                    {
                        KeymapListFragment().apply {
                            isAppBarVisible = false
                        }
                    }
                }
                HomeTab.FINGERPRINT_MAPS -> {
                    {
                        FingerprintMapListFragment().apply {
                            isAppBarVisible = false
                        }
                    }
                }
            }
        }


    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int) = tabFragmentsCreators[position].invoke()

    fun invalidateFragments(tabs: Set<HomeTab>) {
        if (this.tabs == tabs) return

        this.tabs = tabs
        notifyDataSetChanged()
    }
}
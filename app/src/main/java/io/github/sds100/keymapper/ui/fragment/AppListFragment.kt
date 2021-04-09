package io.github.sds100.keymapper.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.airbnb.epoxy.EpoxyRecyclerView
import io.github.sds100.keymapper.data.model.AppListItem
import io.github.sds100.keymapper.data.viewmodel.AppListViewModel
import io.github.sds100.keymapper.databinding.FragmentAppListBinding
import io.github.sds100.keymapper.simple
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.util.InjectorUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 22/02/2020.
 */
class AppListFragment : RecyclerViewFragment<AppListItem, FragmentAppListBinding>() {

    companion object {
        const val REQUEST_KEY = "request_app"
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val SEARCH_STATE_KEY = "key_app_search_state"
    }

    override var searchStateKey: String? = SEARCH_STATE_KEY
    override var requestKey: String? = REQUEST_KEY

    private val viewModel: AppListViewModel by activityViewModels {
        InjectorUtils.provideAppListViewModel(requireContext())
    }

    override val listItems: Flow<ListUiState<AppListItem>>
        get() = viewModel.state.map { it.listItems }

    override fun subscribeUi(binding: FragmentAppListBinding) {
        binding.viewModel = viewModel
    }

    override fun onSearchQuery(query: String?) {
        viewModel.searchQuery.value = query
    }

    override fun bind(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentAppListBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }

    override fun getBottomAppBar(binding: FragmentAppListBinding) = binding.appBar

    override fun populateList(recyclerView: EpoxyRecyclerView, listItems: List<AppListItem>) {
        binding.epoxyRecyclerView.withModels {
            listItems.forEach {
                simple {
                    id(it.packageName)
                    primaryText(it.appName)
                    icon(it.icon)

                    onClick { _ ->
                        returnResult(EXTRA_PACKAGE_NAME to it.packageName)
                    }
                }
            }
        }
    }

    override fun getRecyclerView(binding: FragmentAppListBinding) = binding.epoxyRecyclerView
    override fun getProgressBar(binding: FragmentAppListBinding) = binding.progressBar
    override fun getEmptyListPlaceHolder(binding: FragmentAppListBinding) =
        binding.emptyListPlaceHolder
}
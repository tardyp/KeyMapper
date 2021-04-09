package io.github.sds100.keymapper.ui.fragment

import android.os.Bundle
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.airbnb.epoxy.EpoxyRecyclerView
import io.github.sds100.keymapper.NavAppDirections
import io.github.sds100.keymapper.data.model.ChooseConstraintListItem
import io.github.sds100.keymapper.data.viewmodel.ChooseConstraintViewModel
import io.github.sds100.keymapper.databinding.FragmentSimpleRecyclerviewBinding
import io.github.sds100.keymapper.simple
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.showUserResponseRequests
import io.github.sds100.keymapper.util.InjectorUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * A placeholder fragment containing a simple view.
 */
class ChooseConstraintFragment
    : SimpleRecyclerViewFragment<ChooseConstraintListItem>() {

    companion object {
        const val EXTRA_CONSTRAINT = "extra_constraint"
    }

    private val navArgs by navArgs<ChooseConstraintFragmentArgs>()

    private val viewModel: ChooseConstraintViewModel by viewModels {
        InjectorUtils.provideChooseConstraintListViewModel(requireContext())
    }

    override val listItems: Flow<ListUiState<ChooseConstraintListItem>>
        get() = viewModel.state

    @Suppress("SuspiciousVarProperty")
    override var requestKey: String? = null
        get() = navArgs<ChooseConstraintFragmentArgs>().value.chooseConstraintRequestKey

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.setSupportedConstraints(Json.decodeFromString(navArgs.supportedConstraintList))

        setFragmentResultListener(AppListFragment.REQUEST_KEY) { _, result ->
            val packageName = result.getString(AppListFragment.EXTRA_PACKAGE_NAME)

            viewModel.onChooseApp(packageName!!)
        }

        setFragmentResultListener(ChooseBluetoothDeviceFragment.REQUEST_KEY) { _, result ->
            val address = result.getString(ChooseBluetoothDeviceFragment.EXTRA_ADDRESS)
            val name = result.getString(ChooseBluetoothDeviceFragment.EXTRA_NAME)

            viewModel.onChooseBluetoothDevice(address!!, name!!)
        }
    }

    override fun subscribeUi(binding: FragmentSimpleRecyclerviewBinding) {
        super.subscribeUi(binding)

        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.CREATED) {
            viewModel.returnResult.collectLatest {
                returnResult(EXTRA_CONSTRAINT to Json.encodeToString(it))
            }
        }

        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
            viewModel.chooseApp.collectLatest {
                findNavController().navigate(NavAppDirections.chooseApp())
            }
        }

        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
            viewModel.chooseBluetoothDevice.collectLatest {
                findNavController().navigate(NavAppDirections.chooseBluetoothDevice())
            }
        }

        viewModel.showUserResponseRequests(this, binding)
    }

    override fun populateList(
        recyclerView: EpoxyRecyclerView,
        listItems: List<ChooseConstraintListItem>
    ) {
        recyclerView.withModels {
            listItems.forEach { listItem ->
                simple {
                    id(listItem.id.toString())
                    primaryText(listItem.title)

                    onClick { _ ->
                        viewModel.chooseConstraint(listItem.id)
                    }
                }
            }
        }
    }
}
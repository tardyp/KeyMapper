package io.github.sds100.keymapper.ui.fragment.keymap

import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import androidx.navigation.fragment.findNavController
import com.airbnb.epoxy.EpoxyRecyclerView
import io.github.sds100.keymapper.data.viewmodel.BackupRestoreViewModel
import io.github.sds100.keymapper.data.viewmodel.HomeViewModel
import io.github.sds100.keymapper.data.viewmodel.KeymapListViewModel
import io.github.sds100.keymapper.databinding.FragmentSimpleRecyclerviewBinding
import io.github.sds100.keymapper.keymap
import io.github.sds100.keymapper.ui.ChipUi
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.callback.OnChipClickCallback
import io.github.sds100.keymapper.ui.fragment.HomeFragmentDirections
import io.github.sds100.keymapper.ui.fragment.SimpleRecyclerViewFragment
import io.github.sds100.keymapper.ui.mappings.keymap.KeyMapListItem
import io.github.sds100.keymapper.util.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

/**
 * Created by sds100 on 22/02/2020.
 */
class KeymapListFragment : SimpleRecyclerViewFragment<KeyMapListItem>() {

    private val homeViewModel: HomeViewModel by activityViewModels {
        InjectorUtils.provideHomeViewModel(requireContext())
    }

    private val viewModel: KeymapListViewModel
        get() = homeViewModel.keymapListViewModel

    private val backupRestoreViewModel: BackupRestoreViewModel by activityViewModels {
        InjectorUtils.provideBackupRestoreViewModel(requireContext())
    }

    private val backupLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) {
            it ?: return@registerForActivityResult

            //TODO
//            backupRestoreViewModel.backupKeymaps(
//                requireContext().contentResolver.openOutputStream(it),
//            )
        }

    override val listItems: Flow<ListUiState<KeyMapListItem>>
        get() = viewModel.state

    override fun subscribeUi(binding: FragmentSimpleRecyclerviewBinding) {

        viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
            viewModel.launchConfigKeymap.collectLatest {
                val direction = HomeFragmentDirections.actionToConfigKeymap(it)
                findNavController().navigate(direction)
            }
        }
    }

    override fun populateList(
        recyclerView: EpoxyRecyclerView,
        listItems: List<KeyMapListItem>
    ) {
        recyclerView.withModels {
            listItems.forEach { listItem ->
                keymap {
                    id(listItem.keyMapUiState.uid)
                    keyMapUiState(listItem.keyMapUiState)

                    selectionState(listItem.selectionUiState)

                    onActionChipClick(object : OnChipClickCallback {
                        override fun onChipClick(chipModel: ChipUi) {
                            viewModel.onActionChipClick(chipModel)
                        }
                    })

                    onConstraintChipClick(object : OnChipClickCallback {
                        override fun onChipClick(chipModel: ChipUi) {
                            viewModel.onConstraintsChipClick(chipModel)
                        }
                    })

                    onCardClick { _ ->
                        viewModel.onKeymapCardClick(listItem.keyMapUiState.uid)
                    }

                    onCardLongClick { _ ->
                        viewModel.onKeymapCardLongClick(listItem.keyMapUiState.uid)
                        true
                    }
                }
            }
        }
    }
}
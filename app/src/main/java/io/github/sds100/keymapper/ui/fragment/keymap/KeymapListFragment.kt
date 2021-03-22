package io.github.sds100.keymapper.ui.fragment.keymap

import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import io.github.sds100.keymapper.data.viewmodel.BackupRestoreViewModel
import io.github.sds100.keymapper.data.viewmodel.KeymapListViewModel
import io.github.sds100.keymapper.databinding.FragmentSimpleRecyclerviewBinding
import io.github.sds100.keymapper.keymap
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.fragment.HomeFragmentDirections
import io.github.sds100.keymapper.ui.fragment.SimpleRecyclerViewFragment
import io.github.sds100.keymapper.ui.mappings.keymap.KeymapListItemModel
import io.github.sds100.keymapper.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

/**
 * Created by sds100 on 22/02/2020.
 */
class KeymapListFragment : SimpleRecyclerViewFragment<KeymapListItemModel>() {

    private val viewModel: KeymapListViewModel by activityViewModels {
        InjectorUtils.provideKeymapListViewModel(requireContext())
    }

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

    private val controller = KeymapController()

    override val listItems: Flow<ListUiState<KeymapListItemModel>>
        get() = viewModel.state

    override fun subscribeUi(binding: FragmentSimpleRecyclerviewBinding) {
        binding.epoxyRecyclerView.adapter = controller.adapter

        viewLifecycleScope.launchWhenResumed {
            viewModel.launchConfigKeymap.collectLatest {
                val direction = HomeFragmentDirections.actionToConfigKeymap(it)
                findNavController().navigate(direction)
            }
        }
    }

    override fun populateList(
        recyclerView: EpoxyRecyclerView,
        listItems: List<KeymapListItemModel>
    ) {
        controller.keymapList = listItems
    }

    override fun rebuildUiState() = viewModel.rebuildUiState()

    inner class KeymapController : EpoxyController() {
        var keymapList: List<KeymapListItemModel> = listOf()
            set(value) {
                field = value
                requestModelBuild()
            }

        override fun buildModels() {
            keymapList.forEach {
                keymap {
                    id(it.uid)
                    model(it)

                    onChipClick(viewModel)

                    onCardClick { _ ->
                        viewModel.onKeymapCardClick(it.uid)
                    }

                    onCardLongClick { _ ->
                        viewModel.onKeymapCardLongClick(it.uid)
                        true
                    }
                }
            }
        }
    }
}
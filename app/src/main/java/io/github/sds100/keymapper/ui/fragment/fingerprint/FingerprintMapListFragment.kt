package io.github.sds100.keymapper.ui.fragment.fingerprint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.airbnb.epoxy.EpoxyRecyclerView
import io.github.sds100.keymapper.*
import io.github.sds100.keymapper.data.viewmodel.BackupRestoreViewModel
import io.github.sds100.keymapper.data.viewmodel.HomeViewModel
import io.github.sds100.keymapper.databinding.FragmentFingerprintMapListBinding
import io.github.sds100.keymapper.ui.ChipUi
import io.github.sds100.keymapper.ui.ListUiState
import io.github.sds100.keymapper.ui.callback.OnChipClickCallback
import io.github.sds100.keymapper.ui.fragment.RecyclerViewFragment
import io.github.sds100.keymapper.ui.mappings.fingerprintmap.FingerprintMapListItem
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.delegate.FixErrorDelegate
import kotlinx.coroutines.flow.Flow
import splitties.alertdialog.appcompat.*

/**
 * Created by sds100 on 11/12/2020.
 */
class FingerprintMapListFragment :
    RecyclerViewFragment<FingerprintMapListItem, FragmentFingerprintMapListBinding>() {

    private val homeViewModel: HomeViewModel by activityViewModels {
        InjectorUtils.provideHomeViewModel(requireContext())
    }

    private val viewModel by lazy { homeViewModel.fingerprintMapListViewModel }

    private val backupLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) {
            it ?: return@registerForActivityResult

            backupRestoreViewModel
                .backupFingerprintMaps(requireContext().contentResolver.openOutputStream(it))
        }

    private val backupRestoreViewModel: BackupRestoreViewModel by activityViewModels {
        InjectorUtils.provideBackupRestoreViewModel(requireContext())
    }

    private lateinit var fixErrorDelegate: FixErrorDelegate

    override val listItems: Flow<ListUiState<FingerprintMapListItem>>
        get() = viewModel.state

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fixErrorDelegate = FixErrorDelegate(
            "FingerprintGestureFragment",
            requireActivity().activityResultRegistry,
            viewLifecycleOwner
        ) {

            viewModel.rebuildUiState()
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun bind(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentFingerprintMapListBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }

    override fun subscribeUi(binding: FragmentFingerprintMapListBinding) {

    }

    override fun populateList(
        recyclerView: EpoxyRecyclerView,
        listItems: List<FingerprintMapListItem>
    ) {
        recyclerView.withModels {
            listItems.forEach { listItem ->
                fingerprintMap {
                    id(listItem.id.toString())

                    model(listItem)

                    onEnabledSwitchChangeListener { _, isChecked ->
                        viewModel.onEnabledSwitchChange(listItem.id, isChecked)
                    }

                    onChipClick(object : OnChipClickCallback {
                        override fun onChipClick(chipModel: ChipUi) {
                            viewModel.onChipClick(listItem.id, chipModel)
                        }
                    })
                }
            }
        }
    }

    override fun rebuildUiState() = viewModel.rebuildUiState()
    override fun getRecyclerView(binding: FragmentFingerprintMapListBinding) =
        binding.epoxyRecyclerView

    override fun getProgressBar(binding: FragmentFingerprintMapListBinding) = binding.progressBar
    override fun getEmptyListPlaceHolder(binding: FragmentFingerprintMapListBinding) =
        binding.emptyListPlaceHolder
}
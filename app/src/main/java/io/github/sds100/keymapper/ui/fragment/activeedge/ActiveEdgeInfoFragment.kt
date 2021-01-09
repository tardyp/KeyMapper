package io.github.sds100.keymapper.ui.fragment.activeedge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.switchmaterial.SwitchMaterial
import io.github.sds100.keymapper.*
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.ActiveEdgeListItemModel
import io.github.sds100.keymapper.data.model.ActiveEdgeMap
import io.github.sds100.keymapper.data.viewmodel.ActiveEdgeInfoViewModel
import io.github.sds100.keymapper.data.viewmodel.BackupRestoreViewModel
import io.github.sds100.keymapper.databinding.FragmentMapDefaultListBinding
import io.github.sds100.keymapper.ui.callback.ErrorClickCallback
import io.github.sds100.keymapper.ui.fragment.RecyclerViewFragment
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.delegate.RecoverFailureDelegate
import io.github.sds100.keymapper.util.result.Failure
import splitties.alertdialog.appcompat.*

/**
 * Created by sds100 on 11/12/2020.
 */
class ActiveEdgeInfoFragment : RecyclerViewFragment<FragmentMapDefaultListBinding>() {

    private val mViewModel: ActiveEdgeInfoViewModel by activityViewModels {
        InjectorUtils.provideActiveEdgeInfoViewModel(requireContext())
    }

    private val mBackupLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument()) {
            it ?: return@registerForActivityResult

            mBackupRestoreViewModel
                .backupActiveEdgeMap(requireActivity().contentResolver.openOutputStream(it))
        }

    private val mBackupRestoreViewModel: BackupRestoreViewModel by activityViewModels {
        InjectorUtils.provideBackupRestoreViewModel(requireContext())
    }

    private lateinit var mRecoverFailureDelegate: RecoverFailureDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mRecoverFailureDelegate = RecoverFailureDelegate(
            "FingerprintGestureFragment",
            requireActivity().activityResultRegistry,
            this) {

            mViewModel.rebuildModels()
        }
    }

    override fun subscribeUi(binding: FragmentMapDefaultListBinding) {
        mViewModel.model.observe(viewLifecycleOwner, { model ->

            binding.state = model

            if (model !is Data) return@observe

            binding.epoxyRecyclerView.withModels {
                model.data.let {
                    activeEdge {
                        id("active_edge")
                        model(it)

                        onEnabledSwitchClick { view ->
                            mViewModel.setEnabled((view as SwitchMaterial).isChecked)
                        }

                        onErrorClick(object : ErrorClickCallback {
                            override fun onErrorClick(failure: Failure) {
                                mViewModel.fixError(failure)
                            }
                        })

                        onClick { _ ->
                            val direction = NavAppDirections.actionToConfigActiveEdge()
                            findNavController().navigate(direction)
                        }
                    }
                }
            }
        })

        mViewModel.eventStream.observe(viewLifecycleOwner,
            {
                when (it) {
                    is BuildActiveEdgeListModel -> {
                        viewLifecycleScope.launchWhenStarted {
                            mViewModel.setModels(buildModels(it.map))
                        }
                    }

                    is RequestActiveEdgeMapReset -> {
                        requireActivity().alertDialog {
                            messageResource = R.string.dialog_title_are_you_sure

                            positiveButton(R.string.pos_yes) {
                                mViewModel.reset()
                            }

                            cancelButton()

                            show()
                        }
                    }

                    is BackupActiveEdgeMap -> mBackupLauncher.launch(BackupUtils.createFileName())
                }
            })

        mViewModel.rebuildModels()
    }

    private suspend fun buildModels(map: ActiveEdgeMap) =
        ActiveEdgeListItemModel(

            actionModels = map.actionList.map { action ->
                action.buildChipModel(requireContext(), mViewModel.getDeviceInfoList())
            },

            constraintModels = map.constraintList.map { constraint ->
                constraint.buildModel(requireContext())
            },

            constraintMode = map.constraintMode,

            isEnabled = map.isEnabled,

            optionsDescription = map.buildOptionsDescription(requireContext())
        )

    override fun bind(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentMapDefaultListBinding.inflate(inflater, container)
}
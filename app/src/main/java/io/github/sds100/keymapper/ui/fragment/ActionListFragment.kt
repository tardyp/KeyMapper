package io.github.sds100.keymapper.ui.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyTouchHelper
import com.google.android.material.card.MaterialCardView
import io.github.sds100.keymapper.ActionBindingModel_
import io.github.sds100.keymapper.NavAppDirections
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.action
import io.github.sds100.keymapper.data.model.ActionEntity
import io.github.sds100.keymapper.data.model.options.BaseOptions
import io.github.sds100.keymapper.data.viewmodel.ActionListViewModel
import io.github.sds100.keymapper.databinding.FragmentActionListBinding
import io.github.sds100.keymapper.domain.actions.Action
import io.github.sds100.keymapper.ui.actions.ActionListItemModel
import io.github.sds100.keymapper.util.delegate.ModelState
import io.github.sds100.keymapper.util.ifIsData

/**
 * Created by sds100 on 22/11/20.
 */
abstract class ActionListFragment<O : BaseOptions<ActionEntity>, A : Action>
    : RecyclerViewFragment<List<ActionListItemModel>, FragmentActionListBinding>() {

    companion object {
        const val CHOOSE_ACTION_REQUEST_KEY = "request_choose_action"
    }

    abstract val actionListViewModel: ActionListViewModel<A>

    override val modelState: ModelState<List<ActionListItemModel>>
        get() = actionListViewModel

    private val actionListController = ActionListController()

    override fun onResume() {
        super.onResume()

        actionListViewModel.rebuildModels()
    }

    override fun bind(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentActionListBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }

    abstract fun openActionOptionsFragment(options: O)

    override fun populateList(
        binding: FragmentActionListBinding,
        model: List<ActionListItemModel>?
    ) {
        binding.enableActionDragging(actionListController)

        actionListController.modelList = model ?: emptyList()
    }

    override fun subscribeUi(binding: FragmentActionListBinding) {
        binding.viewModel = actionListViewModel

        binding.epoxyRecyclerViewActions.adapter = actionListController.adapter

        actionListViewModel.openEditOptions.observe(viewLifecycleOwner, {
            //TODO
//            openActionOptionsFragment(it)
        })

        binding.setOnAddActionClick {
            val direction =
                NavAppDirections.actionGlobalChooseActionFragment(CHOOSE_ACTION_REQUEST_KEY)
            findNavController().navigate(direction)
        }
    }

    private fun FragmentActionListBinding.enableActionDragging(
        controller: EpoxyController
    ): ItemTouchHelper {

        return EpoxyTouchHelper.initDragging(controller)
            .withRecyclerView(epoxyRecyclerViewActions)
            .forVerticalList()
            .withTarget(ActionBindingModel_::class.java)
            .andCallbacks(object : EpoxyTouchHelper.DragCallbacks<ActionBindingModel_>() {

                override fun isDragEnabledForModel(model: ActionBindingModel_?): Boolean {
                    modelState.model.value?.ifIsData {
                        if (it.size > 1) return true
                    }

                    return false
                }

                override fun onModelMoved(
                    fromPosition: Int,
                    toPosition: Int,
                    modelBeingMoved: ActionBindingModel_?,
                    itemView: View?
                ) {
                    actionListViewModel.moveAction(fromPosition, toPosition)
                }

                override fun onDragStarted(
                    model: ActionBindingModel_?,
                    itemView: View?,
                    adapterPosition: Int
                ) {
                    itemView?.findViewById<MaterialCardView>(R.id.cardView)?.isDragged = true
                }

                override fun onDragReleased(model: ActionBindingModel_?, itemView: View?) {
                    itemView?.findViewById<MaterialCardView>(R.id.cardView)?.isDragged = false
                }
            })
    }

    private inner class ActionListController : EpoxyController() {
        var modelList: List<ActionListItemModel> = listOf()
            set(value) {
                field = value
                requestModelBuild()
            }

        override fun buildModels() {
            modelList.forEach { model ->
                action {
                    id(model.id)
                    model(model)
                    actionCount(modelList.size)

                    onRemoveClick { _ ->
                        actionListViewModel.removeAction(model.id)
                    }

                    onMoreClick { _ ->
                        //TODO
//                        actionListViewModel.editOptions(model.id)
                    }

                    onClick { _ ->
                        actionListViewModel.onModelClick(model.id)
                    }
                }
            }
        }
    }
}
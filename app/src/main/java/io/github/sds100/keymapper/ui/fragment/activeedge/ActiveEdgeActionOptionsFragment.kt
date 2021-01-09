package io.github.sds100.keymapper.ui.fragment.activeedge

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.airbnb.epoxy.EpoxyControllerAdapter
import io.github.sds100.keymapper.data.model.options.ActiveEdgeActionOptions
import io.github.sds100.keymapper.data.viewmodel.ActiveEdgeActionOptionsViewModel
import io.github.sds100.keymapper.databinding.DialogActionOptionsBinding
import io.github.sds100.keymapper.ui.fragment.BaseOptionsDialogFragment
import io.github.sds100.keymapper.util.InjectorUtils

/**
 * Created by sds100 on 27/06/2020.
 */
class ActiveEdgeActionOptionsFragment
    : BaseOptionsDialogFragment<DialogActionOptionsBinding, ActiveEdgeActionOptions>() {

    companion object {
        const val REQUEST_KEY = "request_choose_active_edge_action_options"
    }

    override val optionsViewModel: ActiveEdgeActionOptionsViewModel by viewModels {
        InjectorUtils.provideFingerprintActionOptionsViewModel()
    }

    override val requestKey = REQUEST_KEY

    override val initialOptions: ActiveEdgeActionOptions
        get() = navArgs<ActiveEdgeActionOptionsFragmentArgs>().value
            .StringNavArgActiveEdgeActionOptions

    override fun subscribeCustomUi(binding: DialogActionOptionsBinding) {
        binding.apply {
            viewModel = optionsViewModel
        }
    }

    override fun setRecyclerViewAdapter(
        binding: DialogActionOptionsBinding,
        adapter: EpoxyControllerAdapter
    ) {
        binding.epoxyRecyclerView.adapter = adapter
    }

    override fun bind(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogActionOptionsBinding {
        return DialogActionOptionsBinding.inflate(inflater, container, false)
    }
}
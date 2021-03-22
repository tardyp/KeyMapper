package io.github.sds100.keymapper.ui.fragment

import io.github.sds100.keymapper.data.model.options.BaseOptions
import io.github.sds100.keymapper.data.model.options.OptionsListModel
import io.github.sds100.keymapper.data.viewmodel.BaseOptionsViewModel
import io.github.sds100.keymapper.databinding.FragmentRecyclerviewBinding
import io.github.sds100.keymapper.util.delegate.ModelState

/**
 * Created by sds100 on 15/01/21.
 */
//TODO delete
abstract class OptionsFragment<O : BaseOptions<*>>
    : DefaultRecyclerViewFragment<OptionsListModel>() {
    abstract val optionsViewModel: BaseOptionsViewModel<O>

    override val modelState: ModelState<OptionsListModel>
        get() = optionsViewModel

    override fun populateList(binding: FragmentRecyclerviewBinding, model: OptionsListModel?) {
    }
}
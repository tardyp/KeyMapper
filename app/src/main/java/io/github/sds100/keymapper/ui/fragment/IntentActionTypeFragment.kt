package io.github.sds100.keymapper.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import androidx.navigation.fragment.findNavController
import com.airbnb.epoxy.EpoxyController
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.intents.ConfigIntentViewModel
import io.github.sds100.keymapper.databinding.FragmentIntentActionTypeBinding
import io.github.sds100.keymapper.databinding.ListItemIntentExtraBoolBinding
import io.github.sds100.keymapper.intentExtraBool
import io.github.sds100.keymapper.intentExtraGeneric
import io.github.sds100.keymapper.intents.*
import io.github.sds100.keymapper.util.*
import splitties.alertdialog.appcompat.alertDialog
import splitties.alertdialog.appcompat.message
import splitties.alertdialog.appcompat.messageResource
import splitties.alertdialog.appcompat.okButton

/**
 * Created by sds100 on 30/03/2020.
 */

class IntentActionTypeFragment : Fragment() {
    companion object {
        const val REQUEST_KEY = "request_intent"
        const val EXTRA_DESCRIPTION = "extra_intent_description"
        const val EXTRA_TARGET = "extra_target"
        const val EXTRA_URI = "extra_uri"

        private val EXTRA_TYPES = arrayOf(
            BoolExtraType(),
            BoolArrayExtraType(),
            IntExtraType(),
            IntArrayExtraType(),
            StringExtraType(),
            StringArrayExtraType(),
            LongExtraType(),
            LongArrayExtraType(),
            ByteExtraType(),
            ByteArrayExtraType(),
            DoubleExtraType(),
            DoubleArrayExtraType(),
            CharExtraType(),
            CharArrayExtraType(),
            FloatExtraType(),
            FloatArrayExtraType(),
            ShortExtraType(),
            ShortArrayExtraType()
        )
    }

    private val viewModel: ConfigIntentViewModel by activityViewModels {
        Inject.configIntentViewModel()
    }

    /**
     * Scoped to the lifecycle of the fragment's view (between onCreateView and onDestroyView)
     */
    private var _binding: FragmentIntentActionTypeBinding? = null
    val binding: FragmentIntentActionTypeBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        FragmentIntentActionTypeBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            _binding = this

            return this.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        binding.setOnDoneClick {
            val intent = Intent().apply {
                if (viewModel.action.value?.isNotEmpty() == true) {
                    this.action = viewModel.action.value
                }

                viewModel.categoriesList.value?.forEach {
                    this.addCategory(it)
                }

                if (viewModel.data.value?.isNotEmpty() == true) {
                    this.data = viewModel.data.value?.toUri()
                }

                if (viewModel.targetPackage.value?.isNotEmpty() == true) {
                    this.`package` = viewModel.targetPackage.value

                    if (viewModel.targetClass.value?.isNotEmpty() == true) {
                        this.setClassName(
                            viewModel.targetPackage.value!!,
                            viewModel.targetClass.value!!
                        )
                    }
                }

                viewModel.extras.value?.forEach { model ->
                    if (model.name.isEmpty()) return@forEach
                    if (model.parsedValue == null) return@forEach

                    model.type.putInIntent(this, model.name, model.value)
                }
            }

            val uri = intent.toUri(0)

            setFragmentResult(REQUEST_KEY,
                bundleOf(
                    EXTRA_DESCRIPTION to viewModel.description.value,
                    EXTRA_TARGET to viewModel.getTarget().toString(),
                    EXTRA_URI to uri
                )
            )

            findNavController().navigateUp()
        }

        binding.setOnAddExtraClick {
            requireContext().alertDialog {
                val labels = EXTRA_TYPES.map { str(it.labelStringRes) }.toTypedArray()

                setItems(labels) { _, position ->
                    viewModel.addExtra(EXTRA_TYPES[position])
                }

                show()
            }
        }

        binding.setOnShowCategoriesExampleClick {
            requireContext().alertDialog {
                messageResource = R.string.intent_categories_example
                okButton()
                show()
            }
        }

        //TODO
//        viewModel.eventStream.observe(viewLifecycleOwner, { event ->
//            when (event) {
//                is BuildIntentExtraListItemModels -> viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
//                    val models = event.extraModels.map { it.toListItemModel() }
//                    viewModel.setListItemModels(models)
//                }
//            }
//        })

        subscribeExtrasList()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun subscribeExtrasList() {
        viewModel.extrasListItemModels.observe(viewLifecycleOwner, { state ->
            viewLifecycleOwner.addRepeatingJob(Lifecycle.State.RESUMED) {
                binding.epoxyRecyclerViewExtras.withModels {

                    val models = if (state is Data) {
                        state.data
                    } else {
                        emptyList()
                    }

                    models.forEach {
                        bindExtra(it)
                    }
                }
            }
        })
    }

    private fun EpoxyController.bindExtra(model: IntentExtraListItemModel) {
        when (model) {
            is GenericIntentExtraListItemModel -> intentExtraGeneric {

                id(model.uid)

                model(model)

                onRemoveClick { _ ->
                    viewModel.removeExtra(model.uid)
                }

                onShowExampleClick { _ ->
                    requireContext().alertDialog {
                        message = model.exampleString
                        okButton()

                        show()
                    }
                }

                valueTextWatcher(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        viewModel.setExtraValue(model.uid, s.toString())
                    }
                })

                nameTextWatcher(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    }

                    override fun afterTextChanged(s: Editable?) {
                        viewModel.setExtraName(model.uid, s.toString())
                    }
                })
            }

            is BoolIntentExtraListItemModel -> intentExtraBool {
                id(model.uid)

                model(model)

                onRemoveClick { _ ->
                    viewModel.removeExtra(model.uid)
                }

                onBind { model, view, _ ->
                    (view.dataBinding as ListItemIntentExtraBoolBinding).apply {
                        radioButtonTrue.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) viewModel.setExtraValue(model.model().uid, "true")
                        }

                        radioButtonFalse.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) viewModel.setExtraValue(model.model().uid, "false")
                        }
                    }
                }
            }
        }
    }

    private fun IntentExtraModel.toListItemModel(): IntentExtraListItemModel {
        return when (type) {
            is BoolExtraType -> BoolIntentExtraListItemModel(
                uid,
                name,
                parsedValue?.let { it as Boolean } ?: true,
                isValidValue
            )

            else -> {
                val inputType = when (type) {
                    is IntExtraType ->
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED

                    is IntArrayExtraType -> InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_CLASS_TEXT

                    is LongExtraType ->
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED

                    is LongArrayExtraType -> InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_CLASS_TEXT

                    is ByteExtraType ->
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED

                    is ByteArrayExtraType -> InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_CLASS_TEXT

                    is DoubleExtraType ->
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or
                            InputType.TYPE_NUMBER_FLAG_DECIMAL

                    is DoubleArrayExtraType -> InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_DECIMAL or
                        InputType.TYPE_NUMBER_FLAG_SIGNED or
                        InputType.TYPE_CLASS_TEXT

                    is FloatExtraType ->
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or
                            InputType.TYPE_NUMBER_FLAG_DECIMAL

                    is FloatArrayExtraType -> InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_DECIMAL or
                        InputType.TYPE_NUMBER_FLAG_SIGNED or
                        InputType.TYPE_CLASS_TEXT

                    is ShortExtraType ->
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED

                    is ShortArrayExtraType -> InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_CLASS_TEXT

                    else -> InputType.TYPE_CLASS_TEXT
                }

                GenericIntentExtraListItemModel(
                    uid,
                    str(type.labelStringRes),
                    name,
                    value,
                    isValidValue,
                    str(type.exampleStringRes),
                    inputType
                )
            }
        }
    }
}
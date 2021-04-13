package io.github.sds100.keymapper.data.viewmodel

import android.graphics.Bitmap
import android.graphics.Point
import androidx.lifecycle.*
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.*
import io.github.sds100.keymapper.ui.dialogs.GetUserResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Created by sds100 on 03/08/20.
 */

class PickDisplayCoordinateViewModel(
    resourceProvider: ResourceProvider
) : ViewModel(), ResourceProvider by resourceProvider, UserResponseViewModel by UserResponseViewModelImpl() {

    private val x = MutableStateFlow<Int?>(null)
    private val y = MutableStateFlow<Int?>(null)

    val xString = x.map {
        it ?: return@map ""

        it.toString()
    }.stateIn(viewModelScope, SharingStarted.Lazily, "")

    val yString = y.map {
        it ?: return@map ""

        it.toString()
    }.stateIn(viewModelScope, SharingStarted.Lazily, "")

    val isDoneButtonEnabled: StateFlow<Boolean> = combine(x, y) { x, y ->
        x ?: return@combine false
        y ?: return@combine false

        x >= 0 && y >= 0
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    val bitmap = _bitmap.asStateFlow()

    private val _returnResult = MutableSharedFlow<PickCoordinateResult>()
    val returnResult = _returnResult.asSharedFlow()

    private val _showSnackBar = MutableSharedFlow<SnackBarUi>()
    val showSnackBar = _showSnackBar.asSharedFlow()

    private var createResultJob: Job? = null

    fun selectedScreenshot(newBitmap: Bitmap, displaySize: Point) {
        //check whether the height and width of the bitmap match the display size, even when it is rotated.
        if (
            (displaySize.x != newBitmap.width
                && displaySize.y != newBitmap.height) &&

            (displaySize.y != newBitmap.width
                && displaySize.x != newBitmap.height)
        ) {
            viewModelScope.launch {
                _showSnackBar.emit(
                    SnackBarUi(getString(R.string.toast_incorrect_screenshot_resolution))
                )
            }

            return
        }

        _bitmap.value = newBitmap
    }

    fun setX(x: String) {
        this.x.value = x.toIntOrNull()
    }

    fun setY(y: String) {
        this.y.value = y.toIntOrNull()
    }

    /**
     * [screenshotXRatio] The ratio between the point where the user pressed to the width of the image.
     * [screenshotYRatio] The ratio between the point where the user pressed to the height of the image.
     */
    fun onScreenshotTouch(screenshotXRatio: Float, screenshotYRatio: Float) {
        bitmap.value?.let {

            val displayX = it.width * screenshotXRatio
            val displayY = it.height * screenshotYRatio

            x.value = displayX.roundToInt()
            y.value = displayY.roundToInt()
        }
    }

    fun onDoneClick() {
        createResultJob?.cancel()
        createResultJob = viewModelScope.launch {
            val x = x.value ?: return@launch
            val y = y.value ?: return@launch

            val response  = getUserResponse(
                "coordinate_description",
                GetUserResponse.Text(
                    getString(R.string.hint_tap_coordinate_title),
                    allowEmpty = true
                )
            )?: return@launch

            val description = response.text

            _returnResult.emit(PickCoordinateResult(x, y, description))
        }
    }

    override fun onCleared() {
        bitmap.value?.recycle()
        _bitmap.value = null

        createResultJob?.cancel()
        createResultJob = null

        super.onCleared()
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PickDisplayCoordinateViewModel(resourceProvider) as T
        }
    }
}

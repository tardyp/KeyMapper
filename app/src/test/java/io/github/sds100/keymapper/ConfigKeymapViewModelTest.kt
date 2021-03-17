package io.github.sds100.keymapper

import android.view.KeyEvent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.github.sds100.keymapper.data.model.ActionEntity
import io.github.sds100.keymapper.data.model.KeyMapEntity
import io.github.sds100.keymapper.data.model.TriggerEntity
import io.github.sds100.keymapper.data.repository.DeviceInfoCache
import io.github.sds100.keymapper.data.usecase.ConfigKeymapUseCase
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.ConfigKeymapTriggerUseCase
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCase
import io.github.sds100.keymapper.ui.mappings.keymap.ConfigKeymapViewModel
import io.github.sds100.keymapper.util.KeyEventUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

/**
 * Created by sds100 on 30/01/21.
 */

@ExperimentalCoroutinesApi
class ConfigKeymapViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()
    private val coroutineScope = TestCoroutineScope(testDispatcher)

    private lateinit var mockDeviceInfoRepository: DeviceInfoCache
    private lateinit var mockKeymapRepository: ConfigKeymapUseCase

    private lateinit var viewModel: ConfigKeymapViewModel

    @Before
    fun init() {
        mockKeymapRepository = mock(ConfigKeymapUseCase::class.java)
        mockDeviceInfoRepository = mock(DeviceInfoCache::class.java)

        Dispatchers.setMain(testDispatcher)

        viewModel = ConfigKeymapViewModel(
            mockKeymapRepository,
            mock(GetActionErrorUseCase::class.java),
            mock(ConfigKeymapTriggerUseCase::class.java),
            mock(OnboardingUseCase::class.java)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    /**
     * issue #593
     */
    @Test
    fun `key map with hold down action, load key map, hold down flag shouldn't disappear`() =
        coroutineScope.runBlockingTest {
            //given
            val action = ActionEntity.tapCoordinateAction(100, 100, null)
                .copy(flags = ActionEntity.ACTION_FLAG_HOLD_DOWN)

            val keymap = KeyMapEntity(
                0,
                trigger = TriggerEntity(keys = listOf(TriggerEntity.KeyEntity(KeyEvent.KEYCODE_0))),
                actionList = listOf(action)
            )

            //when
            `when`(mockKeymapRepository.getKeymap(0)).then { keymap }

            viewModel.loadKeymap(0)

            advanceUntilIdle()

            //then
            assertThat(viewModel.actionListViewModel.actionList.value, `is`(listOf(action)))
        }

    @Test
    fun `add modifier key event action, enable hold down option and disable repeat option`() =
        coroutineScope.runBlockingTest {
            KeyEventUtils.MODIFIER_KEYCODES.forEach { keyCode ->
                val action = ActionEntity.keyCodeAction(keyCode)
                viewModel.actionListViewModel.addAction(action)

                viewModel.actionListViewModel.actionList.value!!
                    .single { it.uid == action.uid }
                    .let {
                        assertThat(
                            "action doesn't have hold down flag",
                            it.flags.hasFlag(ActionEntity.ACTION_FLAG_HOLD_DOWN)
                        )
                        assertThat(
                            "action has repeat flag",
                            !it.flags.hasFlag(ActionEntity.ACTION_FLAG_REPEAT)
                        )
                    }
            }
        }
}
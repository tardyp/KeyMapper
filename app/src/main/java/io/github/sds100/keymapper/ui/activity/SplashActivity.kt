package io.github.sds100.keymapper.ui.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import io.github.sds100.keymapper.ServiceLocator
import io.github.sds100.keymapper.UseCases
import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.onboarding.AppIntroSlide
import io.github.sds100.keymapper.util.firstBlocking
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 20/01/21.
 */
class SplashActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shownAppIntro = UseCases.onboarding(this).shownAppIntro

        val approvedFingerprintGestureFeaturePrompt =
            UseCases.onboarding(this).approvedFingerprintFeaturePrompt

        val appIntroSlides: List<AppIntroSlide> = when {
            !shownAppIntro -> listOf(
                AppIntroSlide.NOTE_FROM_DEV,
                AppIntroSlide.ACCESSIBILITY_SERVICE,
                AppIntroSlide.BATTERY_OPTIMISATION,
                AppIntroSlide.FINGERPRINT_GESTURE_SUPPORT,
                AppIntroSlide.DO_NOT_DISTURB,
                AppIntroSlide.CONTRIBUTING,
            )

            !approvedFingerprintGestureFeaturePrompt
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                listOf(AppIntroSlide.FINGERPRINT_GESTURE_SUPPORT)
            }

            else -> emptyList()
        }

        if (appIntroSlides.isEmpty()) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            Intent(this, AppIntroActivity::class.java).apply {
                val slidesToStringArray = appIntroSlides.map { it.toString() }.toTypedArray()

                putExtra(AppIntroActivity.EXTRA_SLIDES, slidesToStringArray)
                startActivity(this)
            }
        }

        finish()
    }
}
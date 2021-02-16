package io.github.sds100.keymapper.ui.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro2
import io.github.sds100.keymapper.data.viewmodel.FingerprintGestureMapIntroViewModel
import io.github.sds100.keymapper.util.InjectorUtils

/**
 * Created by sds100 on 07/07/2019.
 */

@RequiresApi(Build.VERSION_CODES.O)
class FingerprintGestureIntroActivity : AppIntro2() {

    private val viewModel by viewModels<FingerprintGestureMapIntroViewModel> {
        InjectorUtils.provideFingerprintGestureIntroViewModel(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isSkipButtonEnabled = false

        addSlide(FingerprintGestureSupportSlide())
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)

        viewModel.shownIntro()

        startActivity(Intent(this, MainActivity::class.java))

        finish()
    }
}
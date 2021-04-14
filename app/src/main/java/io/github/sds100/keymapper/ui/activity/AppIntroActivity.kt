package io.github.sds100.keymapper.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import androidx.navigation.findNavController
import com.github.appintro.AppIntro2
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.ServiceLocator
import io.github.sds100.keymapper.data.viewmodel.AppIntroViewModel
import io.github.sds100.keymapper.onboarding.AppIntroScrollableFragment
import io.github.sds100.keymapper.onboarding.AppIntroSlide
import io.github.sds100.keymapper.permissions.RequestPermissionDelegate
import io.github.sds100.keymapper.util.InjectorUtils
import io.github.sds100.keymapper.util.UrlUtils
import kotlinx.coroutines.flow.collectLatest

/**
 * Created by sds100 on 07/07/2019.
 */

class AppIntroActivity : AppIntro2() {

    companion object {
        const val EXTRA_SLIDES = "extra_slides"
    }

    private val viewModel by viewModels<AppIntroViewModel> {
        val slides = intent.getStringArrayExtra(EXTRA_SLIDES)?.map { AppIntroSlide.valueOf(it) }

        InjectorUtils.provideAppIntroViewModel(this, slides!!)
    }

    private lateinit var requestPermissionDelegate: RequestPermissionDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isSkipButtonEnabled = false

        requestPermissionDelegate = RequestPermissionDelegate(this, showDialogs = false)

        addRepeatingJob(Lifecycle.State.RESUMED) {
            ServiceLocator.permissionAdapter(this@AppIntroActivity).request.collectLatest { permission ->
                requestPermissionDelegate.requestPermission(
                    permission,
                    null
                )
            }
        }

        addRepeatingJob(Lifecycle.State.RESUMED){
            viewModel.openUrl.collectLatest {
                UrlUtils.openUrl(this@AppIntroActivity, it)
            }
        }

        viewModel.slidesToShow.forEach {
            val args = bundleOf(AppIntroScrollableFragment.KEY_SLIDE to it.toString())

            AppIntroScrollableFragment().apply {
                arguments = args
                addSlide(this)
            }
        }
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)

        viewModel.shownAppIntro()

        startActivity(Intent(this, MainActivity::class.java))

        finish()
    }
}
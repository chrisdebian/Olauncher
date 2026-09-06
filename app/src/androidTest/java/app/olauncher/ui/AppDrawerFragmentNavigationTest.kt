package app.olauncher.ui

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.olauncher.R
import app.olauncher.data.Constants
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers the app-drawer open/close navigation path (roadmap Phase 1 safety net, item 4.1) --
 * this codebase had zero instrumented test coverage before this, and issue #713 ("Can't get out
 * of the app drawer") was closed 2026-08-30 with mixed field confirmation, so this exit path is a
 * real regression risk, not a hypothetical one.
 *
 * Uses the device's own real installed apps via the real [MainViewModel] (this app has no DI
 * seam to fake the app list) -- the test only asserts on navigation *behaviour*, never on which
 * specific app was clicked, so this holds on any device/emulator with at least one launchable app.
 */
@RunWith(AndroidJUnit4::class)
class AppDrawerFragmentNavigationTest {

    private fun attachTestNavController(): TestNavHostController {
        val navController =
            TestNavHostController(InstrumentationRegistry.getInstrumentation().targetContext)
        navController.setGraph(R.navigation.nav_graph)
        navController.setCurrentDestination(R.id.appListFragment)
        return navController
    }

    @Test
    fun tappingAnAppInLaunchModeReturnsToHome() {
        val navController = attachTestNavController()
        val args = bundleOf(Constants.Key.FLAG to Constants.FLAG_LAUNCH_APP)

        val scenario = launchFragmentInContainer<AppDrawerFragment>(
            fragmentArgs = args,
            themeResId = R.style.AppTheme,
        )
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
        scenario.moveToState(Lifecycle.State.RESUMED)

        onView(withId(R.id.recyclerView))
            .perform(RecyclerViewActions.actionOnItemAtPosition<AppDrawerAdapter.ViewHolder>(0, click()))

        assertEquals(R.id.mainFragment, navController.currentDestination?.id)
    }

}

package com.anhprgm.deviceinfo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * One instrumented test that walks every tab.
 *
 * Cheap insurance against nav-graph breakage: a wrong start destination or a
 * route that fails to resolve shows up here immediately, and the graph is the
 * thing most likely to break as destinations keep being added.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationSmokeTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun string(id: Int): String =
        composeRule.activity.getString(id)

    @Test
    fun everyTabOpensItsStartDestination() {
        composeRule.waitForIdle()

        // Info tab is the start destination.
        composeRule.onNodeWithText(string(R.string.dashboard_title)).assertIsDisplayed()

        composeRule.onNodeWithText(string(R.string.tab_monitor)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(string(R.string.screen_monitoring)).assertIsDisplayed()

        composeRule.onNodeWithText(string(R.string.tab_test)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(string(R.string.test_hub_title)).assertIsDisplayed()

        composeRule.onNodeWithText(string(R.string.tab_tools)).performClick()
        composeRule.waitForIdle()
        // Asserting on the screen title would match twice: the Tools tab label
        // and the Tools app-bar title are the same word. Use a unique element.
        composeRule.onNodeWithText(string(R.string.screen_apps)).assertIsDisplayed()

        // Returning to Info must restore its own stack, not reset the app.
        composeRule.onNodeWithText(string(R.string.tab_info)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(string(R.string.dashboard_title)).assertIsDisplayed()
    }

    @Test
    fun drillingIntoADetailScreenAndBackWorks() {
        composeRule.waitForIdle()

        composeRule.onNodeWithText(string(R.string.tab_tools)).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(string(R.string.screen_apps)).performClick()
        composeRule.waitForIdle()

        // The installed-apps screen is a leaf inside the Tools graph.
        composeRule.onNodeWithText(string(R.string.apps_search_hint)).assertIsDisplayed()
    }
}

package com.athebapps.android.list;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.not;

/**
 * Tests that when the first item of the history is clicked and thus selected, the Floating Button
 * Buttons appears. Then, when the same item is clicked again and thus no item is selected, the FAB
 * disappears. Also, tests under the same conditions that the delete button is displayed.
 * Warning: It only works if there is at least one element in the history.
 */
@RunWith(AndroidJUnit4.class)
public class HistoryActivityClickDisplayTest {

    @Rule
    public ActivityTestRule<HistoryActivity> mActivityTestRule =
            new ActivityTestRule<>(HistoryActivity.class);

    @Test
    public void clickUnclickFirstItemCheckFAB() {

        // First checks that FAB is initially invisible
        onView(withId(R.id.floatingActionButtonHistory)).check(matches(not(isDisplayed())));

        // Perform a click on the first element of the history
        onView(withId(R.id.history_recycler_view))
                .perform(RecyclerViewActions.<HistoryAdapter.ViewHolder>actionOnItemAtPosition(0, click()));

        // Check that FAB is now displayed
        onView(withId(R.id.floatingActionButtonHistory)).check(matches(isDisplayed()));

        // Perform a click again on the same element
        onView(withId(R.id.history_recycler_view))
                .perform(RecyclerViewActions.<HistoryAdapter.ViewHolder>actionOnItemAtPosition(0, click()));

        // Finally checks that FAB is back to invisible
        onView(withId(R.id.floatingActionButtonHistory)).check(matches(not(isDisplayed())));
    }

    @Test
    public void clickUnclickFirstItemCheckDeleteButton() {

        // First check that the delete button is invisible
        onView(withId(R.id.action_delete)).check(doesNotExist());

        // Perform a click on the first element of the history
        onView(withId(R.id.history_recycler_view))
                .perform(RecyclerViewActions.<HistoryAdapter.ViewHolder>actionOnItemAtPosition(0, click()));

        // Check that the delete button is now displayed
        onView(withId(R.id.action_delete)).check(matches(isDisplayed()));

        // Perform a click again on the same element
        onView(withId(R.id.history_recycler_view))
                .perform(RecyclerViewActions.<HistoryAdapter.ViewHolder>actionOnItemAtPosition(0, click()));

        // Check again that the delete button is invisible
        onView(withId(R.id.action_delete)).check(doesNotExist());
    }


}

package com.athebapps.android.list;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


/**
 * Tests if long-pressing a product of the list displays the edition dialog, then close the dialog.
 * Warning : This test does not work if the list is empty.
 */
@RunWith(AndroidJUnit4.class)
public class ListActivityLongPressTest {

    @Rule
    public ActivityTestRule<ListActivity> mActivityTestRule =
            new ActivityTestRule<>(ListActivity.class);

    @Test
    public void longPressFirstProduct_checkDialog_closeDialog() {

        // Perform a long click on the first element of the list
        onView(withId(R.id.list_recycler_view))
                .perform(RecyclerViewActions.<ListAdapter.ViewHolder>actionOnItemAtPosition(0, longClick()));

        // Check that the Edition Dialog layout is displayed
        onView(withId(R.id.edition_dialog_root)).check(matches(isDisplayed()));

        // Perform a click on the "OK" to close the dialog
        onView(withText("OK")).perform(click());

        // Check that the Edition Dialog layout is not displayed anymore
        onView(withId(R.id.edition_dialog_root)).check(doesNotExist());
    }

}

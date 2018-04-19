package com.athebapps.android.list;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.KeyEvent;
import android.widget.EditText;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


/**
 * Tests that using the "Create" button on the App Bar of entering product names actually adds a
 * View in the list with the name of the product. Warning: the checked product name should not be
 * in the list already! (Also if the list is long, and the product is not visible, I'm not sure it
 * would work...
 */
@RunWith(AndroidJUnit4.class)
public class ListActivityCreateTest {

    @Rule
    public ActivityTestRule<ListActivity> mActivityTestRule =
            new ActivityTestRule<>(ListActivity.class);

    @Test
    public void clickCreateButton_addProducts_checkTextDisplayed() {

        // Click on the "add" button
        onView(withId(R.id.action_add)).perform(click());

        // Type "Bread" then type ENTER
        onView(isAssignableFrom(EditText.class)).perform(typeText("Bread"), pressKey(KeyEvent.KEYCODE_ENTER));

        // Type "Milk" then type ENTER
        onView(isAssignableFrom(EditText.class)).perform(typeText("Milk"), pressKey(KeyEvent.KEYCODE_ENTER));

        // Check the text "Bread" is Displayed on a View
        onView(withText("Bread")).check(matches(isDisplayed()));
    }
}

package com.athebapps.android.list;

import android.content.Intent;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


/**
 * Tests if an intent is sent when the user clicks the email menu button.
 */
@RunWith(AndroidJUnit4.class)
public class ListActivityEmailClickTest {

    @Rule
    public IntentsTestRule<ListActivity> mIntentsTestRule =
            new IntentsTestRule<>(ListActivity.class);

    @Test
    public void clickOnEmailButton() {


        // Open the menu
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Click on the menu button that is now visible
        onView(withText(R.string.list_menu_email)).perform(click());

        // Assert that an Intent with a 'send' action has been sent
        intended(hasAction(Intent.ACTION_SENDTO));
    }
}

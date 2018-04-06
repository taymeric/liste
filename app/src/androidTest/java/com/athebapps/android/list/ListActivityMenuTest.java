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
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;


/**
 * Performs several simples tests on the ListActivity menu.
 */
@RunWith(AndroidJUnit4.class)
public class ListActivityMenuTest {

    @Rule
    public IntentsTestRule<ListActivity> mIntentsTestRule =
            new IntentsTestRule<>(ListActivity.class);

    // Tests if an intent is sent when the user clicks the email menu button.
    @Test
    public void clickOnEmailButton_checkIntent() {

        // Open the menu.
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Click on the email menu button that is now visible.
        onView(withText(R.string.list_menu_email)).perform(click());

        // Assert that an Intent with a 'send' action has been sent.
        intended(allOf(
                hasAction(Intent.ACTION_SENDTO),
                hasData("mailto:"))
        );
    }

    // Tests if the preferences activity is launched when clicking on the corresponding menu button.
    @Test
    public void clickOnPrefencesButton_checkIntent() {

        // Open the menu.
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Click on the layout change menu button.
        onView(withText(R.string.list_menu_preferences)).perform(click());

        // Assert that an intent for PreferencesActivity has been sent.
        intended(hasComponent(PreferencesActivity.class.getName()));
    }


}

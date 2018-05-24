package org.projects.shoppinglist;


import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class UndoDeletedProduct {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void clear() {
        ClearShoppingList.clearList();
    }

    @Test
    public void undoDeletedProduct() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.addText),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.layout),
                                        1),
                                1),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("Carrot"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.addQty), withText("1"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.layout),
                                        1),
                                2),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText("4"));

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.addQty), withText("4"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.layout),
                                        1),
                                2),
                        isDisplayed()));
        appCompatEditText3.perform(closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.addButton), withText("Add to shopping list"),
                        childAtPosition(
                                allOf(withId(R.id.layout),
                                        childAtPosition(
                                                withClassName(is("android.support.design.widget.CoordinatorLayout")),
                                                1)),
                                2),
                        isDisplayed()));
        appCompatButton.perform(click());

        DataInteraction appCompatCheckedTextView = onData(anything())
                .inAdapterView(allOf(withId(R.id.list),
                        childAtPosition(
                                withId(R.id.layout),
                                3)))
                .atPosition(0);
        appCompatCheckedTextView.perform(click());

        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.action_delete), withText("Delete Item"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.toolbar),
                                        1),
                                0),
                        isDisplayed()));
        actionMenuItemView.perform(click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.snackbar_action), withText("UNDO"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.design.widget.Snackbar$SnackbarLayout")),
                                        0),
                                1),
                        isDisplayed()));
        appCompatButton2.perform(click());

        ViewInteraction checkedTextView = onView(
                allOf(withId(android.R.id.text1),
                        childAtPosition(
                                allOf(withId(R.id.list),
                                        childAtPosition(
                                                withId(R.id.layout),
                                                3)),
                                0),
                        isDisplayed()));
        checkedTextView.check(matches(isDisplayed()));

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}

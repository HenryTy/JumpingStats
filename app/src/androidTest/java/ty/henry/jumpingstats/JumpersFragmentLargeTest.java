package ty.henry.jumpingstats;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.PickerActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import junit.framework.AssertionFailedError;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Calendar;
import java.util.Map;

import ty.henry.jumpingstats.jumpers.Jumper;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.*;
import static android.support.test.espresso.action.ViewActions.*;
import static java.util.Calendar.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;

@LargeTest
public class JumpersFragmentLargeTest {

    @Rule
    public ActivityTestRule<MainActivity> mainActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void openJumpersFragment() {
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open());
        onData(instanceOf(String.class)).inAdapterView(withId(R.id.drawer)).atPosition(2).perform(click());
    }

    @Test
    public void addDisplayEditAndDeleteJumperTest() {
        Calendar date = getInstance();
        date.set(1970, 7, 24);
        Jumper jumper = new Jumper("Marion", "Moseby", Country.USA, date, 1.68f);
        String countryName = jumper.getCountry().getCountryName(mainActivityTestRule.getActivity());

        addJumper(jumper);
        onView(withId(R.id.jumpersRecycler)).perform(RecyclerViewActions.actionOnHolderItem(withJumper(jumper), click()));

        testDetailsFragment(jumper);

        onView(withId(R.id.edit)).perform(click());
        onView(withId(R.id.nameEditText)).check(matches(withText(jumper.getName())));
        onView(withId(R.id.surnameEditText)).check(matches(withText(jumper.getSurname())));
        onView(withId(R.id.heightEditText)).check(matches(withText(jumper.getHeight()+"")));
        onView(withId(R.id.countrySpinner)).check(matches(withSpinnerCountry(countryName)));
        onView(withId(R.id.datePicker)).check(matches(withPickerDate(date)));

        Jumper updatedJumper = new Jumper("Bob", "Moseby", Country.USA, date, 1.75f);
        onView(withId(R.id.nameEditText)).perform(scrollTo(), replaceText(updatedJumper.getName()));
        onView(withId(R.id.heightEditText)).perform(scrollTo(), replaceText(updatedJumper.getHeight()+""),
                ViewActions.closeSoftKeyboard());
        onView(withId(R.id.saveButton)).perform(scrollTo(), click());

        testDetailsFragment(updatedJumper);

        Espresso.pressBack();
        onView(withId(R.id.jumpersRecycler)).perform(RecyclerViewActions.actionOnHolderItem(withJumper(updatedJumper), click()));

        onView(withId(R.id.delete)).perform(click());
        onView(withText(R.string.delete_jumper_message)).check(matches(isDisplayed()));
        onView(withText(R.string.cancel)).perform(click());

        testDetailsFragment(updatedJumper);

        onView(withId(R.id.delete)).perform(click());
        onView(withText(R.string.ok)).perform(click());

        try {
            onView(withId(R.id.jumpersRecycler)).perform(RecyclerViewActions.scrollToHolder(withJumper(updatedJumper)));
            throw new AssertionFailedError("JumpersRecycler shouldn't contain deleted jumper.");
        } catch(PerformException ex) {

        }
    }

    @Test
    public void add10JumpersAddExistingJumperAndDeleteJumpersTest() {
        Jumper[] jumpers = new Jumper[10];
        Calendar date = getInstance();
        date.set(1970, 7, 24);
        for(int i=0; i<10; i++) {
            jumpers[i] = new Jumper("Marion"+i, "Moseby", Country.FRANCE, date, 1.68f);
            addJumper(jumpers[i]);
        }

        onView(withId(R.id.jumpersRecycler)).perform(RecyclerViewActions
                .actionOnHolderItem(withJumper(jumpers[9]), click()));
        onView(withId(R.id.edit)).perform(click());
        onView(withId(R.id.nameEditText)).perform(scrollTo(), replaceText(jumpers[5].getName()));
        onView(withId(R.id.saveButton)).perform(scrollTo(), click());
        onView(withId(R.id.saveButton)).check(matches(isDisplayed()));
        Espresso.closeSoftKeyboard();
        Espresso.pressBack();
        Espresso.pressBack();
        onView(withId(R.id.jumpersRecycler)).perform(RecyclerViewActions.scrollToHolder(withJumper(jumpers[9])));

        addJumper(jumpers[3]);
        onView(withId(R.id.saveButton)).check(matches(isDisplayed()));
        Espresso.closeSoftKeyboard();
        Espresso.pressBack();

        onView(withId(R.id.delete)).perform(click());
        for(Jumper j : jumpers) {
            onView(withId(R.id.jumpersRecycler)).perform(RecyclerViewActions.actionOnHolderItem(withJumper(j), click()));
        }
        onView(withId(R.id.jumpersRecycler)).perform(RecyclerViewActions
                .actionOnHolderItem(withJumper(jumpers[8]), click()));
        onView(withText(R.string.delete)).perform(click());
        try {
            onView(withId(R.id.jumpersRecycler)).perform(RecyclerViewActions.scrollToHolder(withJumper(jumpers[2])));
            throw new AssertionFailedError("JumpersRecycler shouldn't contain deleted jumper.");
        } catch(PerformException ex) {

        }
        onView(withId(R.id.jumpersRecycler)).perform(RecyclerViewActions.scrollToHolder(withJumper(jumpers[8])));
        onView(withId(R.id.delete)).perform(click());
        onView(withId(R.id.jumpersRecycler)).perform(RecyclerViewActions
                .actionOnHolderItem(withJumper(jumpers[8]), click()));
        onView(withText(R.string.delete)).perform(click());
    }

    private void addJumper(Jumper jumper) {
        Calendar date = jumper.getDateOfBirth();
        onView(withId(R.id.addButton)).perform(click());
        onView(withId(R.id.nameEditText)).perform(scrollTo(), typeText(jumper.getName()));
        onView(withId(R.id.surnameEditText)).perform(scrollTo(), typeText(jumper.getSurname()));
        onView(withId(R.id.heightEditText)).perform(scrollTo(), typeText(jumper.getHeight()+""));
        onView(withId(R.id.datePicker)).perform(scrollTo(),
                PickerActions.setDate(date.get(YEAR), date.get(MONTH)+1, date.get(DAY_OF_MONTH)));
        onView(withId(R.id.countrySpinner)).perform(scrollTo(), click());
        String countryName = jumper.getCountry().getCountryName(mainActivityTestRule.getActivity());
        onData(allOf(is(instanceOf(Map.class)), hasEntry(equalTo("name"), equalTo(countryName))))
                .perform(click());
        onView(withId(R.id.saveButton)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
                .perform(scrollTo(), click());
    }

    private void testDetailsFragment(Jumper jumper) {
        String countryName = jumper.getCountry().getCountryName(mainActivityTestRule.getActivity());
        Calendar date = jumper.getDateOfBirth();
        onView(withId(R.id.nameTextView)).check(matches(withText(containsString(jumper.getName()))));
        onView(withId(R.id.surnameTextView)).check(matches(withText(containsString(jumper.getSurname()))));
        onView(withId(R.id.countryTextView)).check(matches(withText(containsString(countryName))));
        onView(withId(R.id.heightTextView)).check(matches(withText(containsString(jumper.getHeight()+""))));
        String dateString = DBHelper.calendarToString(date);
        onView(withId(R.id.dateTextView)).check(matches(withText(containsString(dateString))));
    }

    private static Matcher<View> withSpinnerCountry(String countryName) {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View view) {
                if(!(view instanceof Spinner)) {
                    return false;
                }
                Spinner spinner = (Spinner) view;
                TextView countryNameTextView = spinner.getSelectedView().findViewById(R.id.text);
                String selectedCountry = countryNameTextView.getText().toString();
                return countryName.equals(selectedCountry);
            }

            @Override
            public void describeTo(Description description) {

            }
        };
    }

    private static Matcher<View> withPickerDate(Calendar calendar) {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View item) {
                if(!(item instanceof DatePicker)) {
                    return false;
                }
                DatePicker picker = (DatePicker) item;
                return picker.getYear()==calendar.get(YEAR) &&
                        picker.getMonth()==calendar.get(MONTH) &&
                        picker.getDayOfMonth()==calendar.get(DAY_OF_MONTH);
            }

            @Override
            public void describeTo(Description description) {

            }
        };
    }

    private static Matcher<TextImageAdapter.ViewHolder> withJumper(Jumper jumper) {
        return new TypeSafeMatcher<TextImageAdapter.ViewHolder>() {
            @Override
            protected boolean matchesSafely(TextImageAdapter.ViewHolder holder) {
                TextView textView = holder.itemView.findViewById(R.id.text1);
                ImageView imageView = holder.itemView.findViewById(R.id.image);
                return textView.getText().equals(jumper.getText()[0])
                        && containsDrawable(imageView, jumper.getImage());
            }

            @Override
            public void describeTo(Description description) {

            }
        };
    }

    public static boolean containsDrawable(ImageView imageView, int drawableId) {
        Drawable expectedDrawable = imageView.getContext().getResources().getDrawable(drawableId);
        Drawable actualDrawable = imageView.getDrawable();
        Bitmap expectedBitmap = getBitmap(expectedDrawable);
        Bitmap actualBitmap = getBitmap(actualDrawable);
        return actualBitmap.sameAs(expectedBitmap);
    }

    private static Bitmap getBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}

package se.juneday.thesystembolaget.Activities;


import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PopupActivityTest {

    @Rule
    public ActivityTestRule<PopupActivity> mActivityTestRule = new ActivityTestRule<>(PopupActivity.class);

    @Test
    public void popupActivityTest() {
    }
}

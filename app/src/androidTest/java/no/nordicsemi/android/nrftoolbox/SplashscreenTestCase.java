package no.nordicsemi.android.nrftoolbox;

import org.junit.Rule;
import org.junit.Before;
import org.junit.runner.RunWith;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.rule.ActivityTestRule;
import junit.framework.TestCase;

/**
 * jUnit Test Case
 * @author Martin Zeitler
**/
@RunWith(AndroidJUnit4.class)
public class SplashscreenTestCase extends TestCase {

        /** Log Tag */
        private static final String LOG_TAG = SplashscreenTestCase.class.getSimpleName();

        /** Target Activity */
        private SplashscreenActivity mActivity;

        @Rule
        public ActivityTestRule<SplashscreenActivity> mActivityRule = new ActivityTestRule<>(SplashscreenActivity.class);

        @Override
        public void setUp() throws Exception {
                super.setUp();
        }

        @Before
        public void setUpTest() {

        /* obtaining the Activity from the ActivityTestRule */
                this.mActivity = mActivityRule.getActivity();
        }
}

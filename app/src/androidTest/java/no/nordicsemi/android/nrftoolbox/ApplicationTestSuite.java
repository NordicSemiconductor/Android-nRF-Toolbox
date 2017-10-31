package no.nordicsemi.android.nrftoolbox;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import android.util.Log;

/**
 * jUnit Test Suite.
 * https://developer.android.com/topic/libraries/testing-support-library/index.html#AndroidJUnitRunner
 * @author Martin Zeitler
**/
@RunWith(Suite.class)
@Suite.SuiteClasses({
	SplashscreenTestCase.class
})

public class ApplicationTestSuite {

	/** {@link Log} Tag */
	private static final String LOG_TAG = ApplicationTestSuite.class.getSimpleName();

}
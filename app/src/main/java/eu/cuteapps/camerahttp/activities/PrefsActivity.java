package eu.cuteapps.camerahttp.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import eu.cuteapps.camerahttp.R;

public class PrefsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}
}
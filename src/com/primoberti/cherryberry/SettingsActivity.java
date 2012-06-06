/*
 * Copyright 2012 Alberto Salmerón Moreno
 * 
 * This file is part of CherryBerry - https://github.com/berti/CherryBerry.
 * 
 * CherryBerry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CherryBerry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CherryBerry.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.primoberti.cherryberry;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Activity for modifying user settings.
 * 
 * @author berti
 */
public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private final static String TAG = "SettingsActivity";

	private Preference pomodoroDurationPreference;
	private Preference breakDurationPreference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);

		setTitle(R.string.activity_title_settings);

		pomodoroDurationPreference = findPreference(R.string.settings_key_pomodoro_duration);
		breakDurationPreference = findPreference(R.string.settings_key_break_duration);

		OnPreferenceChangeListener listener = new CheckNumberOnPreferenceChangeListener();
		pomodoroDurationPreference.setOnPreferenceChangeListener(listener);
		breakDurationPreference.setOnPreferenceChangeListener(listener);
	}

	@Override
	protected void onResume() {
		super.onResume();

		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);

		updatePomodoroDurationSummary();
		updateBreakDurationSummary();
	}

	@Override
	protected void onPause() {
		super.onPause();

		PreferenceManager.getDefaultSharedPreferences(this)
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(getString(R.string.settings_key_pomodoro_duration))) {
			updatePomodoroDurationSummary();
		}
		else if (key.equals(getString(R.string.settings_key_break_duration))) {
			updateBreakDurationSummary();
		}
	}

	/* Private methods ************************* */

	private Preference findPreference(int keyId) {
		return getPreferenceScreen().findPreference(getString(keyId));
	}

	private void updateBreakDurationSummary() {
		setSummary(breakDurationPreference, R.string.settings_summary_duration,
				PreferencesHelper.getBreakDurationMins(this));
	}

	private void updatePomodoroDurationSummary() {
		setSummary(pomodoroDurationPreference,
				R.string.settings_summary_duration,
				PreferencesHelper.getPomodoroDurationMins(this));
	}

	private void setSummary(Preference preference, int summaryId,
			Object... args) {
		preference.setSummary(getString(summaryId, args));
	}

	/* Private inner classes ******************* */

	private class CheckNumberOnPreferenceChangeListener implements
			OnPreferenceChangeListener {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			boolean valid = true;
			try {
				Integer.parseInt(newValue.toString());
			}
			catch (NumberFormatException e) {
				Log.w(TAG, preference.getTitle() + ": " + newValue
						+ " is not a number");
				valid = false;
			}
			return valid;
		}
	}

}

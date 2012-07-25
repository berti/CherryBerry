/*
 * Copyright 2012 Alberto Salmerón Moreno
 * 
 * This file is part of CherryBerry - https://github.com/berti/CherryBerry.
 * 
 * “Pomodoro Technique® is a registered trademark of Francesco Cirillo. This
 * application is not affiliated by, associated with nor endorsed by the
 * Pomodoro Technique® or Francesco Cirillo.
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

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

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
	private Preference longBreakDurationPreference;
	private Preference longBreakIntervalPreference;

	@Override
	@TargetApi(11)
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);

		setTitle(R.string.activity_title_settings);

		if (android.os.Build.VERSION.SDK_INT >= 11) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		pomodoroDurationPreference = findPreference(R.string.settings_key_pomodoro_duration);
		breakDurationPreference = findPreference(R.string.settings_key_break_duration);
		longBreakDurationPreference = findPreference(R.string.settings_key_long_break_duration);
		longBreakIntervalPreference = findPreference(R.string.settings_key_long_break_interval);

		OnPreferenceChangeListener listener = new CheckNumberOnPreferenceChangeListener();
		pomodoroDurationPreference.setOnPreferenceChangeListener(listener);
		breakDurationPreference.setOnPreferenceChangeListener(listener);
		longBreakDurationPreference.setOnPreferenceChangeListener(listener);
		longBreakIntervalPreference.setOnPreferenceChangeListener(listener);
	}

	@Override
	protected void onResume() {
		super.onResume();

		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);

		updatePomodoroDurationSummary();
		updateBreakDurationSummary();
		updateLongBreakDurationSummary();
		updateLongBreakIntervalSummary();
		checkLongBreaksEnabled();
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
		else if (key
				.equals(getString(R.string.settings_key_long_break_duration))) {
			updateLongBreakDurationSummary();
		}
		else if (key
				.equals(getString(R.string.settings_key_long_break_interval))) {
			updateLongBreakIntervalSummary();
			checkLongBreaksEnabled();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// Go up, i.e. to CherryBerrryActivity
			Intent intent = new Intent(this, CherryBerryActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* Private methods ************************* */

	private Preference findPreference(int keyId) {
		return getPreferenceScreen().findPreference(getString(keyId));
	}

	private void updatePomodoroDurationSummary() {
		int duration = PreferencesHelper.getPomodoroDurationMins(this);
		setQuantitySummary(pomodoroDurationPreference,
				R.plurals.settings_summary_duration, duration, duration);
	}

	private void updateBreakDurationSummary() {
		int duration = PreferencesHelper.getBreakDurationMins(this);
		setQuantitySummary(breakDurationPreference,
				R.plurals.settings_summary_duration, duration, duration);
	}

	private void updateLongBreakDurationSummary() {
		int duration = PreferencesHelper.getLongBreakDurationMins(this);
		setQuantitySummary(longBreakDurationPreference,
				R.plurals.settings_summary_duration, duration, duration);
	}

	private void updateLongBreakIntervalSummary() {
		int interval = PreferencesHelper.getLongBreakInterval(this);
		if (interval == 0) {
			// Not "zero", but "disabled"
			setSummary(longBreakIntervalPreference,
					R.string.settings_sumary_long_break_interval_disabled);
		}
		else {
			setQuantitySummary(longBreakIntervalPreference,
					R.plurals.settings_summary_long_break_interval, interval,
					interval);
		}
	}

	private void setSummary(Preference preference, int summaryId,
			Object... args) {
		preference.setSummary(getString(summaryId, args));
	}

	private void setQuantitySummary(Preference preference, int summaryId,
			int quantity, Object... args) {
		Resources resources = getResources();
		String summary = resources.getQuantityString(summaryId, quantity, args);
		preference.setSummary(summary);
	}
	
	private void checkLongBreaksEnabled() {
		if (PreferencesHelper.getLongBreakInterval(this) > 0) {
			longBreakDurationPreference.setEnabled(true);
		}
		else {
			longBreakDurationPreference.setEnabled(false);
		}
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

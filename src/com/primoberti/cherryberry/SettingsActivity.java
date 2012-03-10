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

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/**
 * Activity for modifying user settings.
 * 
 * @author berti
 */
public class SettingsActivity extends PreferenceActivity {

	private Preference pomodoroDurationPreference;
	private Preference breakDurationPreference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);

		pomodoroDurationPreference = findPreference(R.string.settings_key_pomodoro_duration);
		breakDurationPreference = findPreference(R.string.settings_key_break_duration);
	}

	@Override
	protected void onResume() {
		super.onResume();

		updatePomodoroDurationSummary();
		updateBreakDurationSummary();
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

}

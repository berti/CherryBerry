package com.primoberti.cherryberry;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.media.Ringtone;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

public class PreferencesFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	private final static String TAG = PreferencesFragment.class.getName();

	private Preference pomodoroDurationPreference;
	private Preference breakDurationPreference;
	private Preference longBreakDurationPreference;
	private Preference longBreakIntervalPreference;
	private Preference notificationRingtonePreference;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		pomodoroDurationPreference = findPreference(R.string.settings_key_pomodoro_duration);
		breakDurationPreference = findPreference(R.string.settings_key_break_duration);
		longBreakDurationPreference = findPreference(R.string.settings_key_long_break_duration);
		longBreakIntervalPreference = findPreference(R.string.settings_key_long_break_interval);
		notificationRingtonePreference = findPreference(R.string.settings_key_notification_ringtone);

		OnPreferenceChangeListener listener = new CheckNumberOnPreferenceChangeListener();
		pomodoroDurationPreference.setOnPreferenceChangeListener(listener);
		breakDurationPreference.setOnPreferenceChangeListener(listener);
		longBreakDurationPreference.setOnPreferenceChangeListener(listener);
		longBreakIntervalPreference.setOnPreferenceChangeListener(listener);
	}

	@Override
	public void onResume() {
		super.onResume();

		PreferenceManager.getDefaultSharedPreferences(getActivity())
				.registerOnSharedPreferenceChangeListener(this);

		updatePomodoroDurationSummary();
		updateBreakDurationSummary();
		updateLongBreakDurationSummary();
		updateLongBreakIntervalSummary();
		checkLongBreaksEnabled();
		updateNotificationRingtoneSummary();
	}

	@Override
	public void onPause() {
		super.onPause();

		PreferenceManager.getDefaultSharedPreferences(getActivity())
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

	/* Private methods ************************* */

	private Preference findPreference(int keyId) {
		return getPreferenceScreen().findPreference(getString(keyId));
	}

	private void updatePomodoroDurationSummary() {
		int duration = PreferencesHelper.getPomodoroDurationMins(getActivity());
		setQuantitySummary(pomodoroDurationPreference,
				R.plurals.settings_summary_duration, duration, duration);
	}

	private void updateBreakDurationSummary() {
		int duration = PreferencesHelper.getBreakDurationMins(getActivity());
		setQuantitySummary(breakDurationPreference,
				R.plurals.settings_summary_duration, duration, duration);
	}

	private void updateLongBreakDurationSummary() {
		int duration = PreferencesHelper
				.getLongBreakDurationMins(getActivity());
		setQuantitySummary(longBreakDurationPreference,
				R.plurals.settings_summary_duration, duration, duration);
	}

	private void updateLongBreakIntervalSummary() {
		int interval = PreferencesHelper.getLongBreakInterval(getActivity());
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

	private void updateNotificationRingtoneSummary() {
		Ringtone ringtone = PreferencesHelper
				.getNotificationRingtone(getActivity());
		if (ringtone == null) {
			// Silent ringtone, i.e. disabled
			setSummary(notificationRingtonePreference,
					R.string.settings_summary_notification_ringtone_silent);
		}
		else {
			notificationRingtonePreference.setSummary(ringtone
					.getTitle(getActivity()));
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
		if (PreferencesHelper.getLongBreakInterval(getActivity()) > 0) {
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

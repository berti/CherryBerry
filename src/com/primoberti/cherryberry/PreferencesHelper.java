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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Abstract helper class for accessing shared preferences across CherryBerry.
 * 
 * @author berti
 */
public abstract class PreferencesHelper {

	private final static String TAG = "PreferencesHelper";

	/* Public static methods ******************* */

	public static int getPomodoroDurationMins(Context context) {
		return getIntFromString(context,
				R.string.settings_key_pomodoro_duration,
				R.integer.settings_default_pomodoro_duration);
	}

	public static int getBreakDurationMins(Context context) {
		return getIntFromString(context, R.string.settings_key_break_duration,
				R.integer.settings_default_break_duration);
	}

	public static int getLongBreakDurationMins(Context context) {
		return getIntFromString(context,
				R.string.settings_key_long_break_duration, 15);
	}

	public static long getPomodoroDuration(Context context) {
		return getPomodoroDurationMins(context) * 60 * 1000;
	}

	public static long getBreakDuration(Context context) {
		return getBreakDurationMins(context) * 60 * 1000;
	}

	public static long getLongBreakDuration(Context context) {
		return getLongBreakDurationMins(context) * 60 * 1000;
	}

	public static boolean isNotificationLight(Context context) {
		return getBoolean(context, R.string.settings_key_notification_light,
				R.bool.settings_default_notification_light);
	}

	public static boolean isNotificationVibration(Context context) {
		return getBoolean(context,
				R.string.settings_key_notification_vibration,
				R.bool.settings_default_notification_vibration);
	}

	public static boolean isNotificationSound(Context context) {
		return getBoolean(context, R.string.settings_key_notification_sound,
				R.bool.settings_default_notification_sound);
	}

	/* Private static methods ****************** */

	private static int getInt(Context context, int key, int defValue) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		return preferences.getInt(context.getResources().getString(key),
				defValue);
	}

	private static int getIntFromString(Context context, int key, int defValue) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		String stringKey = context.getResources().getString(key);
		String stringValue = preferences.getString(stringKey, null);
		int value = defValue;
		if (stringValue != null) {
			try {
				value = Integer.parseInt(stringValue);
			}
			catch (NumberFormatException e) {
				Log.e(PreferencesHelper.TAG, e.toString());
			}
		}
		return value;
	}

	private static boolean getBoolean(Context context, int key, int defValueKey) {
		boolean defValue = context.getResources().getBoolean(defValueKey);
		return getBoolean(context,
				R.string.settings_key_notification_vibration, defValue);
	}

	private static boolean getBoolean(Context context, int key, boolean defValue) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		return preferences.getBoolean(context.getResources().getString(key),
				defValue);
	}

	private static String getString(Context context, int key, String defValue) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		return preferences.getString(context.getResources().getString(key),
				defValue);
	}

}

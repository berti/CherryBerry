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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Static utility methods for setting and cancelling alarms.
 * 
 * @author berti
 */
public abstract class AlarmHelper {

	/**
	 * Sets an alarm to be notified of a finished pomodoro.
	 * 
	 * @param millis duration of the pomodoro
	 */
	public static void setPomodoroAlarm(Context context, long millis) {
		setAlarm(context, millis, PomodoroService.POMODORO_FINISHED);
	}

	/**
	 * Sets an alarm to be notified of a finished break.
	 * 
	 * @param millis duration of the break
	 */
	public static void setBreakAlarm(Context context, long millis) {
		setAlarm(context, millis, PomodoroService.BREAK_FINISHED);
	}

	/**
	 * Sets an alarm to send the given action to this service.
	 * 
	 * @param millis duration of the pomodoro
	 * @param action action to send
	 */
	public static void setAlarm(Context context, long millis, String action) {
		long finishTime = System.currentTimeMillis() + millis;

		Intent intent = new Intent(context, PomodoroService.class);
		intent.setAction(action);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0,
				intent, 0);

		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, finishTime, pendingIntent);
	}

	public static void cancelAlarms(Context context) {
		cancelAlarm(context, PomodoroService.POMODORO_FINISHED);
		cancelAlarm(context, PomodoroService.BREAK_FINISHED);
	}

	public static void cancelAlarm(Context context, String action) {
		Intent intent = new Intent(context, PomodoroService.class);
		intent.setAction(action);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0,
				intent, 0);

		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
	}

}

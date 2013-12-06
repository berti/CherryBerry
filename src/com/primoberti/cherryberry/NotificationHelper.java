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

import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.text.format.DateFormat;

/**
 * Static utility methods for handling notifications.
 * 
 * @author berti
 */
public abstract class NotificationHelper {

	/* Public constants ************************ */

	public final static int NOTIFICATION_ID = 1;

	/* Public static methods ******************* */

	public static void showPersistentPomodoroNotification(Context context,
			long millis) {
		showPersistentNotification(context, PomodoroService.NOTIFICATION_ID,
				R.string.notification_title_pomodoro_running,
				R.string.app_name, R.string.notification_text_pomodoro_running,
				millis);
	}

	public static void showPersistentBreakNotification(Context context,
			long millis) {
		showPersistentNotification(context, PomodoroService.NOTIFICATION_ID,
				R.string.notification_title_break_running, R.string.app_name,
				R.string.notification_text_break_running, millis);
	}

	public static void showPersistentNotification(Context context, int id,
			int tickerTextId, int contentTitleId, int contentTextId, long millis) {
		long finishTime = System.currentTimeMillis() + millis;

		Resources resources = context.getResources();

		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(ns);

		Date date = new Date(finishTime);
		java.text.DateFormat dateFormat = DateFormat.getTimeFormat(context
				.getApplicationContext());
		String contentTextFormat = resources.getString(contentTextId);
		String contentText2 = String.format(contentTextFormat,
				dateFormat.format(date));

		Context appContext = context.getApplicationContext();
		Intent notificationIntent = new Intent(context,
				CherryBerryActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		@SuppressWarnings("deprecation")
		Notification notification = new Notification.Builder(appContext)
				.setTicker(resources.getString(tickerTextId))
				.setContentTitle(resources.getString(contentTitleId))
				.setContentText(contentText2)
				.setSmallIcon(R.drawable.ic_stat_generic).setOngoing(true)
				.setContentIntent(contentIntent).getNotification();

		mNotificationManager.notify(id, notification);
	}

	public static void hidePersistentNotification(Context context) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(ns);

		mNotificationManager.cancel(PomodoroService.NOTIFICATION_ID);
	}

	public static void showPomodoroNotification(Context context) {
		showNotification(context, NOTIFICATION_ID,
				R.string.notification_title_pomodoro_finished,
				R.string.app_name, R.string.notification_text_pomodoro_finished);
	}

	public static void showBreakNotification(Context context) {
		showNotification(context, NOTIFICATION_ID,
				R.string.notification_title_break_finished, R.string.app_name,
				R.string.notification_text_break_finished);
	}

	public static void showNotification(Context context, int id,
			int tickerText, int contentTitle, int contentText) {
		Resources resources = context.getResources();

		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(ns);

		Context appContext = context.getApplicationContext();
		Notification.Builder builder = new Notification.Builder(appContext)
				.setTicker(resources.getString(tickerText))
				.setSmallIcon(R.drawable.ic_stat_generic).setAutoCancel(true);

		int defaults = 0;

		if (PreferencesHelper.isNotificationVibration(context)) {
			defaults |= Notification.DEFAULT_VIBRATE;
		}

		if (PreferencesHelper.isNotificationRingtoneEnabled(context)) {
			String uri = PreferencesHelper.getNotificationRingtoneUri(context);
			builder.setSound(Uri.parse(uri));
		}

		if (PreferencesHelper.isNotificationLight(context)) {
			defaults |= Notification.DEFAULT_LIGHTS;
			builder.setLights(0xffd60707, 300, 3000);
		}

		builder.setDefaults(defaults);

		Intent notificationIntent = new Intent(context,
				CherryBerryActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		builder.setContentTitle(resources.getString(contentTitle))
				.setContentText(resources.getString(contentText))
				.setContentIntent(contentIntent);

		@SuppressWarnings("deprecation")
		Notification notification = builder.getNotification();

		mNotificationManager.notify(id, notification);
	}
}

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
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.format.DateFormat;

/**
 * Service that shows a persistent notification in the status bar while a
 * pomodoro or a break is running.
 * 
 * @author berti
 */
public class PersistentNotificationService extends Service {

	/* Public constants ************************ */

	public final static String POMODORO_STARTED = "com.primoberti.cherryberry.POMODORO_STARTED";

	public final static String BREAK_STARTED = "com.primoberti.cherryberry.BREAK_STARTED";

	public final static String POMODORO_FINISHED = "com.primoberti.cherryberry.POMODORO_FINISHED";

	public final static String BREAK_FINISHED = "com.primoberti.cherryberry.BREAK_FINISHED";

	public final static String EXTRA_FINISH_TIME = "com.primoberti.cherryberry.EXTRA_FINISH_TIME";

	/* Private constants *********************** */

	private final static int NOTIFICATION_POMODORO_STARTED = 1;

	/* Private fields ************************** */

	private long finishTime;

	/* Public methods ************************** */

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.getAction().equals(POMODORO_STARTED)) {
			onPomodoroStarted(intent);
		}
		else if (intent.getAction().equals(BREAK_STARTED)) {
			onBreakStarted(intent);
		}
		return START_STICKY;
	}

	/* Private methods ************************* */

	private void onPomodoroStarted(Intent intent) {
		finishTime = intent.getExtras().getLong(EXTRA_FINISH_TIME);

		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

		int icon = R.drawable.ic_notification;
		CharSequence tickerText = "Pomodoro started";
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;

		Date date = new Date(finishTime);
		java.text.DateFormat dateFormat = DateFormat
				.getTimeFormat(getApplicationContext());

		Context context = getApplicationContext();
		CharSequence contentTitle = "CherryBerry";
		CharSequence contentText = "Pomodoro running - ends at "
				+ dateFormat.format(date);
		Intent notificationIntent = new Intent(this, CherryBerryActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);

		mNotificationManager
				.notify(NOTIFICATION_POMODORO_STARTED, notification);
	}

	private void onBreakStarted(Intent intent) {
		finishTime = intent.getExtras().getLong(EXTRA_FINISH_TIME);
	}

}

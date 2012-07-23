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

import java.util.Date;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Binder;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;

import com.primoberti.cherryberry.Session.Status;

/**
 * Timer-related functionality to control a pomodoro.
 * 
 * @author berti
 */
public class PomodoroService extends Service {

	/* Public constants ************************ */

	public final static String BREAK_FINISHED = "com.primoberti.cherryberry.BREAK_FINISHED";

	public final static String POMODORO_FINISHED = "com.primoberti.cherryberry.POMODORO_FINISHED";

	public final static int NOTIFICATION_ID = 1;

	/* Private constants *********************** */

	private final static String SHARED_PREFS = PomodoroService.class
			+ "_SHARED_PREFS";

	private final static String PREF_STATUS = "status";

	private final static String PREF_TIMER_START = "timerStart";

	private final static String PREF_TIMER_END = "timerEnd";

	/* Private fields ************************** */

	private SessionImpl session;

	private PomodoroListener listener;

	private IBinder binder = new LocalBinder();

	/* Public methods ************************** */

	@Override
	public IBinder onBind(Intent intent) {
		Log.d("PomodoroTimerService", "onBind");

		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.getAction().equals(PomodoroService.POMODORO_FINISHED)) {
			onPomodoroFinished();
		}
		else if (intent.getAction().equals(PomodoroService.BREAK_FINISHED)) {
			onBreakFinished();
		}

		stopSelf();

		return START_NOT_STICKY;
	}

	@Override
	public void onCreate() {
		Log.d("PomodoroTimerService", "onCreate");

		session = new SessionImpl();

		restoreState();
	}

	@Override
	public void onDestroy() {
		Log.d("PomodoroTimerService", "onDestroy");

		super.onDestroy();
	}

	/**
	 * Start a pomodoro with the default pomodoro duration.
	 * 
	 * @see #setPomodoroDuration(long)
	 */
	public void startPomodoro() {
		startPomodoro(PreferencesHelper.getPomodoroDuration(this));
	}

	/**
	 * Start a pomodoro count down timer with the remaining time, but don't set
	 * any alarm. Use this to continue an existing count down after the timer
	 * was shutdown.
	 * 
	 * @param millis the remaining duration of the pomodoro
	 */
	public void continuePomodoro(long millis) {
		startPomodoroTimer(millis);
	}

	/**
	 * Start a break with the default break duration.
	 * 
	 * @see #setBreakDuration(long)
	 */
	public void startBreak() {
		startBreak(PreferencesHelper.getBreakDuration(this));
	}

	/**
	 * Start a break count down timer with the remaining time, but don't set any
	 * alarm. Use this to continue an existing count down after the timer was
	 * shutdown.
	 * 
	 * @param millis the remaining duration of the break
	 */
	public void continueBreak(long millis) {
		startBreakTimer(millis);
	}

	/**
	 * Cancels the current count down timer.
	 */
	public void stop() {
		Status oldStatus = session.getStatus();

		cancelAlarms();
		hidePersistentNotification();

		setIdle();

		if (listener != null) {
			if (oldStatus == Status.POMODORO_RUNNING) {
				listener.onPomodoroCancel(this);
			}
			else if (oldStatus == Status.BREAK_RUNNING) {
				listener.onBreakCancel(this);
			}
		}
	}

	public void skip() {
		stop();
	}

	public Status getStatus() {
		return session.getStatus();
	}

	public boolean isRunning() {
		return getStatus() == Status.POMODORO_RUNNING
				|| getStatus() == Status.BREAK_RUNNING;
	}

	/**
	 * Returns the start time of the current count down timer.
	 * 
	 * @return start time of the current count down timer
	 */
	public long getTimerStart() {
		return session.getStartTime();
	}

	/**
	 * Returns the end time of the current count down timer.
	 * 
	 * @return start time of the current count down timer
	 */
	public long getTimerEnd() {
		return session.getFinishTime();
	}

	public PomodoroListener getListener() {
		return listener;
	}

	public void setListener(PomodoroListener listener) {
		this.listener = listener;
	}

	/* Private methods ************************* */

	/**
	 * Start a pomodoro count down timer.
	 * 
	 * @param millis the duration of the pomodoro
	 */
	private void startPomodoro(long millis) {
		startPomodoroTimer(millis);
		setPomodoroAlarm(millis);
		showPersistentPomodoroNotification(millis);

		if (listener != null) {
			listener.onPomodoroStart(this);
		}
	}

	/**
	 * Start a break countdown timer.
	 * 
	 * @param millis the duration of the break
	 */
	private void startBreak(long millis) {
		if (getStatus() != Status.POMODORO_FINISHED) {
			throw new IllegalStateException("Can't start break in "
					+ getStatus() + " state");
		}

		startBreakTimer(millis);
		setBreakAlarm(millis);
		showPersistentBreakNotification(millis);

		if (listener != null) {
			listener.onBreakStart(this);
		}
	}

	private void onPomodoroFinished() {
		session.setStatus(Status.POMODORO_FINISHED);
		showPomodoroNotification();
		listener.onPomodoroFinish(this);
	}

	private void onBreakFinished() {
		session.setStatus(Status.BREAK_FINISHED);
		showBreakNotification();
		listener.onBreakFinish(this);
	}

	private void saveState() {
		Log.d("PomodoroTimerService", "saveState " + getStatus());

		SharedPreferences preferences = getSharedPreferences(SHARED_PREFS,
				MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();

		editor.putInt(PREF_STATUS, getStatus().ordinal());
		editor.putLong(PREF_TIMER_START, getTimerStart());
		editor.putLong(PREF_TIMER_END, getTimerEnd());

		editor.commit();
	}

	private void restoreState() {
		SharedPreferences preferences = getSharedPreferences(SHARED_PREFS,
				MODE_PRIVATE);

		session.setStatus(Status.values()[preferences.getInt(PREF_STATUS,
				Status.IDLE.ordinal())]);
		session.setStartTime(preferences.getLong(PREF_TIMER_START, 0));
		session.setFinishTime(preferences.getLong(PREF_TIMER_END, 0));

		Log.d("PomodoroTimerService", "restoreState " + getStatus());

		if (getStatus() == Status.POMODORO_RUNNING) {
			if (getTimerEnd() > System.currentTimeMillis()) {
				continuePomodoro(getTimerEnd() - System.currentTimeMillis());
			}
			else {
				session.setStatus(Status.POMODORO_FINISHED);
			}
		}
		else if (getStatus() == Status.BREAK_RUNNING) {
			if (getTimerEnd() > System.currentTimeMillis()) {
				continueBreak(getTimerEnd() - System.currentTimeMillis());
			}
			else {
				session.setStatus(Status.BREAK_FINISHED);
			}
		}
	}

	private void startPomodoroTimer(long millis) {
		if (millis > 0) {
			startTimer(millis);
			session.setStatus(Status.POMODORO_RUNNING);

			saveState();
		}
	}

	private void startBreakTimer(long millis) {
		if (millis > 0) {
			startTimer(millis);
			session.setStatus(Status.BREAK_RUNNING);

			saveState();
		}
	}

	private void startTimer(long millis) {
		session.setStartTime(System.currentTimeMillis());
		session.setFinishTime(getTimerStart() + millis);
	}

	/**
	 * Sets an alarm to be notified of a finished pomodoro.
	 * 
	 * @param millis duration of the pomodoro
	 */
	private void setPomodoroAlarm(long millis) {
		setAlarm(millis, PomodoroService.POMODORO_FINISHED);
	}

	/**
	 * Sets an alarm to be notified of a finished break.
	 * 
	 * @param millis duration of the break
	 */
	private void setBreakAlarm(long millis) {
		setAlarm(millis, PomodoroService.BREAK_FINISHED);
	}

	/**
	 * Sets an alarm to send the given action to this service.
	 * 
	 * @param millis duration of the pomodoro
	 * @param action action to send
	 */
	private void setAlarm(long millis, String action) {
		long finishTime = System.currentTimeMillis() + millis;

		Intent intent = new Intent(this, PomodoroService.class);
		intent.setAction(action);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
				0);

		AlarmManager alarmManager = (AlarmManager) this
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, finishTime, pendingIntent);
	}

	private void cancelAlarms() {
		cancelAlarm(POMODORO_FINISHED);
		cancelAlarm(BREAK_FINISHED);
	}

	private void cancelAlarm(String action) {
		Intent intent = new Intent(this, PomodoroService.class);
		intent.setAction(action);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
				0);

		AlarmManager alarmManager = (AlarmManager) this
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
	}

	private void showPersistentPomodoroNotification(long millis) {
		showPersistentNotification(PomodoroService.NOTIFICATION_ID,
				R.string.notification_title_pomodoro_running,
				R.string.app_name, R.string.notification_text_pomodoro_running,
				millis);
	}

	private void showPersistentBreakNotification(long millis) {
		showPersistentNotification(PomodoroService.NOTIFICATION_ID,
				R.string.notification_title_break_running, R.string.app_name,
				R.string.notification_text_break_running, millis);
	}

	private void showPersistentNotification(int id, int tickerTextId,
			int contentTitleId, int contentTextId, long millis) {
		long finishTime = System.currentTimeMillis() + millis;

		Resources resources = getResources();

		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

		int icon = R.drawable.ic_stat_generic;
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon,
				resources.getString(tickerTextId), when);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;

		Date date = new Date(finishTime);
		java.text.DateFormat dateFormat = DateFormat
				.getTimeFormat(getApplicationContext());
		String contentTextFormat = getResources().getString(contentTextId);
		String contentText2 = String.format(contentTextFormat,
				dateFormat.format(date));

		Context context = getApplicationContext();
		Intent notificationIntent = new Intent(this, CherryBerryActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(context,
				resources.getString(contentTitleId), contentText2,
				contentIntent);

		mNotificationManager.notify(id, notification);
	}

	private void hidePersistentNotification() {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

		mNotificationManager.cancel(PomodoroService.NOTIFICATION_ID);
	}

	private void setIdle() {
		Log.d("PomodoroTimerService", "setIdle");

		session.setStatus(Status.IDLE);
		session.setStartTime(0);
		session.setFinishTime(0);

		saveState();
	}

	private void showPomodoroNotification() {
		showNotification(NOTIFICATION_ID,
				R.string.notification_title_pomodoro_finished,
				R.string.app_name, R.string.notification_text_pomodoro_finished);
	}

	private void showBreakNotification() {
		showNotification(NOTIFICATION_ID,
				R.string.notification_title_break_finished, R.string.app_name,
				R.string.notification_text_break_finished);
	}

	private void showNotification(int id, int tickerText, int contentTitle,
			int contentText) {
		Resources resources = getResources();

		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

		int icon = R.drawable.ic_stat_generic;
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon,
				resources.getString(tickerText), when);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		if (PreferencesHelper.isNotificationVibration(this)) {
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		}

		if (PreferencesHelper.isNotificationSound(this)) {
			notification.defaults |= Notification.DEFAULT_SOUND;
		}

		if (PreferencesHelper.isNotificationLight(this)) {
			notification.ledARGB = 0xffd60707;
			notification.ledOnMS = 300;
			notification.ledOffMS = 3000;
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		}

		Context context = getApplicationContext();
		Intent notificationIntent = new Intent(this, CherryBerryActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(context,
				resources.getString(contentTitle),
				resources.getString(contentText), contentIntent);

		mNotificationManager.notify(id, notification);
	}

	/**
	 * Returns the time instant in which the period with the given duration will
	 * end. Usefull for setting the timer expire time based on its duration.
	 */
	private long finishTime(long duration) {
		return System.currentTimeMillis() + duration;
	}

	/* Public inner classes ******************** */

	public class LocalBinder extends Binder {

		PomodoroService getService() {
			return PomodoroService.this;
		}

	}

}

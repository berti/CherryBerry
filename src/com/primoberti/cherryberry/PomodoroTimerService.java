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

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.text.format.DateFormat;

/**
 * Timer-related functionality to control a pomodoro.
 * 
 * @author berti
 */
public class PomodoroTimerService extends Service {

	/* Public enumerations ********************* */

	public enum Status {
		IDLE, POMODORO_RUNNING, POMODORO_FINISHED, BREAK_RUNNING, BREAK_FINISHED
	};

	/* Public constants ************************ */

	public final static String BREAK_FINISHED = "com.primoberti.cherryberry.BREAK_FINISHED";

	public final static String POMODORO_FINISHED = "com.primoberti.cherryberry.POMODORO_FINISHED";

	/* Private fields ************************** */

	private Status status;

	private InternalTimer timer;

	private long timerStart;

	private long timerEnd;

	private long pomodoroDuration = 25 * 60 * 1000;

	private long breakDuration = 5 * 60 * 1000;

	private PomodoroTimerListener listener;

	private IBinder binder = new LocalBinder();

	/* Public methods ************************** */

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	/**
	 * Start a pomodoro count down timer.
	 * 
	 * @param millis the duration of the pomodoro
	 */
	public void startPomodoro(long millis) {
		startPomodoroTimer(millis);
		setPomodoroAlarm(millis);
		showPersistentPomodoroNotification(millis);
	}

	/**
	 * Start a pomodoro with the default pomodoro duration.
	 * 
	 * @see #setPomodoroDuration(long)
	 */
	public void startPomodoro() {
		startPomodoro(pomodoroDuration);
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
	 * Start a break countdown timer.
	 * 
	 * @param millis the duration of the break
	 */
	public void startBreak(long millis) {
		if (status != Status.POMODORO_FINISHED) {
			throw new IllegalStateException("Can't start break in " + status
					+ " state");
		}

		startBreakTimer(millis);
		setBreakAlarm(millis);
		showPersistentBreakNotification(millis);
	}

	/**
	 * Start a break with the default break duration.
	 * 
	 * @see #setBreakDuration(long)
	 */
	public void startBreak() {
		startBreak(breakDuration);
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
	public void cancel() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	public Status getStatus() {
		return status;
	}

	public boolean isRunning() {
		return status == Status.POMODORO_RUNNING
				|| status == Status.BREAK_RUNNING;
	}

	/**
	 * Returns the start time of the current count down timer.
	 * 
	 * @return start time of the current count down timer
	 */
	public long getTimerStart() {
		return timerStart;
	}

	/**
	 * Returns the end time of the current count down timer.
	 * 
	 * @return start time of the current count down timer
	 */
	public long getTimerEnd() {
		return timerEnd;
	}

	public long getPomodoroDuration() {
		return pomodoroDuration;
	}

	public void setPomodoroDuration(long pomodoroDuration) {
		this.pomodoroDuration = pomodoroDuration;
	}

	public long getBreakDuration() {
		return breakDuration;
	}

	public void setBreakDuration(long breakDuration) {
		this.breakDuration = breakDuration;
	}

	public PomodoroTimerListener getListener() {
		return listener;
	}

	public void setListener(PomodoroTimerListener listener) {
		this.listener = listener;
	}

	/* Private methods ************************* */

	private void startPomodoroTimer(long millis) {
		startTimer(millis);
		status = Status.POMODORO_RUNNING;
	}

	public void startBreakTimer(long millis) {
		startTimer(millis);
		status = Status.BREAK_RUNNING;
	}

	private void startTimer(long millis) {
		if (isRunning()) {
			cancel();
		}

		timer = new InternalTimer(millis, 1000, listener);
		timer.start();

		timerStart = System.currentTimeMillis();
		timerEnd = timerStart + millis;
	}

	/**
	 * Sets an alarm to notify {@link NotificationService} of a finished
	 * pomodoro.
	 * 
	 * @param millis duration of the pomodoro
	 */
	private void setPomodoroAlarm(long millis) {
		setAlarm(millis, PomodoroTimerService.POMODORO_FINISHED);
	}

	/**
	 * Sets an alarm to notify {@link NotificationService} of a finished break.
	 * 
	 * @param millis duration of the break
	 */
	private void setBreakAlarm(long millis) {
		setAlarm(millis, PomodoroTimerService.BREAK_FINISHED);
	}

	/**
	 * Sets an alarm to send the given action to {@link NotificationService}.
	 * 
	 * @param millis duration of the pomodoro
	 * @param action action to send to NotificationService
	 */
	private void setAlarm(long millis, String action) {
		long finishTime = System.currentTimeMillis() + millis;

		Intent intent = new Intent(this, NotificationService.class);
		intent.setAction(action);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
				0);

		AlarmManager alarmManager = (AlarmManager) this
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, finishTime, pendingIntent);
	}

	private void showPersistentPomodoroNotification(long millis) {
		showPersistentNotification(NotificationService.NOTIFICATION_ID,
				R.string.pomodoro_running_notification_title,
				R.string.app_name,
				R.string.pomodoro_running_notification_content, millis);
	}

	private void showPersistentBreakNotification(long millis) {
		showPersistentNotification(NotificationService.NOTIFICATION_ID,
				R.string.pomodoro_running_notification_title,
				R.string.app_name,
				R.string.pomodoro_running_notification_content, millis);
	}

	private void showPersistentNotification(int id, int tickerText,
			int contentTitle, int contentText, long millis) {
		long finishTime = System.currentTimeMillis() + millis;

		Resources resources = getResources();

		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

		int icon = R.drawable.ic_notification;
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon,
				resources.getString(tickerText), when);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;

		Date date = new Date(finishTime);
		java.text.DateFormat dateFormat = DateFormat
				.getTimeFormat(getApplicationContext());
		String contentTextFormat = getResources().getString(
				R.string.pomodoro_running_notification_content);
		String contentText2 = String.format(contentTextFormat,
				dateFormat.format(date));

		Context context = getApplicationContext();
		Intent notificationIntent = new Intent(this, CherryBerryActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(context,
				resources.getString(contentTitle), contentText2, contentIntent);

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

		PomodoroTimerService getService() {
			return PomodoroTimerService.this;
		}

	}

	/* Private inner classes ******************* */

	private class InternalTimer extends CountDownTimer {

		private PomodoroTimerListener listener;

		public InternalTimer(long millisInFuture, long countDownInterval,
				PomodoroTimerListener listener) {
			super(millisInFuture, countDownInterval);

			this.listener = listener;
		}

		@Override
		public void onFinish() {
			if (status == Status.POMODORO_RUNNING) {
				status = Status.POMODORO_FINISHED;
			}
			else if (status == Status.BREAK_RUNNING) {
				status = Status.BREAK_FINISHED;
			}

			if (listener != null) {
				listener.onFinish(PomodoroTimerService.this);
			}
		}

		@Override
		public void onTick(long millisUntilFinished) {
			if (listener != null) {
				listener.onTick(PomodoroTimerService.this, millisUntilFinished);
			}
		}

	}

}

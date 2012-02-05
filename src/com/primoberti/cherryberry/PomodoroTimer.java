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

import android.os.CountDownTimer;

/**
 * Timer-related functionality to control a pomodoro.
 * 
 * @author berti
 */
public class PomodoroTimer {

	public enum Status {
		IDLE, POMODORO_RUNNING, POMODORO_FINISHED, BREAK_RUNNING, BREAK_FINISHED
	};

	public static String BREAK_FINISHED = "com.primoberti.cherryberry.BREAK_FINISHED";

	public static String POMODORO_FINISHED = "com.primoberti.cherryberry.POMODORO_FINISHED";

	private Status status;

	private InternalTimer timer;

	private long pomodoroDuration = 25 * 60 * 1000;

	private long breakDuration = 5 * 60 * 1000;

	private PomodoroTimerListener listener;

	/**
	 * Start a pomodoro count down timer.
	 * 
	 * @param millis the duration of the pomodoro
	 */
	public void startPomodoro(long millis) {
		if (isRunning()) {
			cancel();
		}

		timer = new InternalTimer(millis, 1000, listener);
		timer.start();
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
	 * Start a break countdown timer.
	 * 
	 * @param millis the duration of the break
	 */
	public void startBreak(long millis) {
		if (status != Status.POMODORO_FINISHED) {
			throw new IllegalStateException("Can't start break in " + status
					+ " state");
		}

		timer = new InternalTimer(millis, 1000, listener);
		timer.start();
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

	/**
	 * Returns the time instant in which the period with the given duration will
	 * end. Usefull for setting the timer expire time based on its duration.
	 */
	private long finishTime(long duration) {
		return System.currentTimeMillis() + duration;
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
				listener.onFinish(PomodoroTimer.this);
			}
		}

		@Override
		public void onTick(long millisUntilFinished) {
			if (listener != null) {
				listener.onTick(PomodoroTimer.this, millisUntilFinished);
			}
		}

	}

}

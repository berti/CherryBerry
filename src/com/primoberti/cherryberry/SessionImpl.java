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

import android.content.SharedPreferences;

/**
 * Implementation of {@link Session} with additional setters and other methods.
 * 
 * @author berti
 */
class SessionImpl implements Session {

	/* Private fields ************************** */

	private BreakType breakType;

	private Status status;

	private long startTime;

	private long finishTime;

	private int count;
	
	/* Private constants *********************** */

	private final static String PREF_STATUS = "status";

	private final static String PREF_TIMER_START = "timerStart";

	private final static String PREF_TIMER_END = "timerEnd";

	private final static String PREF_COUNT = "count";

	/* Constructors **************************** */

	public SessionImpl(BreakType type, Status status, int count,
			long startTime, long finishTime) {
		this.breakType = type;
		this.status = status;
		this.count = count;
		this.startTime = startTime;
		this.finishTime = finishTime;
	}

	public SessionImpl(BreakType type, Status status, int count) {
		this(type, status, count, -1, -1);
	}

	public SessionImpl(BreakType type, int count) {
		this(type, Status.IDLE, count);
	}

	public SessionImpl() {
		this(BreakType.NORMAL, 0);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param session an instance of <code>Session</code> whose information will
	 *            be copied to the new instance
	 */
	public SessionImpl(SessionImpl session) {
		this(session.breakType, session.status, session.count,
				session.startTime, session.finishTime);
	}

	/* Public methods ************************** */

	@Override
	public BreakType getBreakType() {
		return breakType;
	}

	public void setBreakType(BreakType type) {
		this.breakType = type;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * Increments the current session count by one.
	 */
	public void incrementCount() {
		this.count++;
	}

	@Override
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	@Override
	public long getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}

	public void save(SharedPreferences preferences) {
		SharedPreferences.Editor editor = preferences.edit();
		save(preferences, editor);
		editor.commit();
	}

	public void save(SharedPreferences preferences,
			SharedPreferences.Editor editor) {
		editor.putInt(PREF_STATUS, status.ordinal());
		editor.putLong(PREF_TIMER_START, startTime);
		editor.putLong(PREF_TIMER_END, finishTime);
		editor.putInt(PREF_COUNT, count);
	}
	
	/* Public static methods ******************* */
	
	public static SessionImpl restore(SharedPreferences preferences) {
		SessionImpl session = new SessionImpl();
		
		session.setStatus(Status.values()[preferences.getInt(PREF_STATUS,
				Status.IDLE.ordinal())]);
		session.setStartTime(preferences.getLong(PREF_TIMER_START, 0));
		session.setFinishTime(preferences.getLong(PREF_TIMER_END, 0));
		session.setCount(preferences.getInt(PREF_COUNT, 0));
		
		return session;
	}

	/* Methods inherited from Object *********** */

	@Override
	public String toString() {
		return String.format("{%s, %s, %d, %dms}", breakType, status, count,
				finishTime);
	}

}

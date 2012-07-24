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

/**
 * Representation of a session. A session has a type of break (normal or long),
 * a status (idle, running a pomodoro or break, or just finished a pomodoro or
 * break), the count of the current session and a finish time (if running a
 * pomodoro or break).
 * 
 * @author berti
 */
public interface Session {

	/* Public inner enums ********************** */

	public enum BreakType {
		NORMAL, LONG
	}

	public enum Status {
		IDLE, POMODORO_RUNNING, POMODORO_FINISHED, BREAK_RUNNING, BREAK_FINISHED;

		public boolean isRunning() {
			return this == POMODORO_RUNNING || this == BREAK_RUNNING;
		}
	};

	/* Public methods ************************** */

	/**
	 * Returns the break type of the current session.
	 * 
	 * @return the break type of the current session
	 */
	public BreakType getBreakType();

	/**
	 * Returns the status of the current session.
	 * 
	 * @return the status of the current session
	 */
	public Status getStatus();

	/**
	 * Returns the current session count.
	 * 
	 * @return the current session count
	 */
	public int getCount();

	/**
	 * When on a running pomodoro or break, returns the time instant at which
	 * they started. Returns -1 otherwise.
	 * 
	 * @return the start time of the current pomodoro or break; -1 otherwise
	 */
	public long getStartTime();

	/**
	 * When on a running pomodoro or break, returns the time instant at which
	 * they finish. Returns -1 otherwise.
	 * 
	 * @return the finish time of the current pomodoro or break; -1 otherwise
	 */
	public long getFinishTime();

}

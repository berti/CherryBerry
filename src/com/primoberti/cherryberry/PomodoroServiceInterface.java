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
 * Public interface exposed by {@link PomodoroService}. The five exposed action
 * methods cannot be used in any context, i.e. {@link #startPomodoro()} can only
 * be used when idle and {@link #cancelBreak()} can only be used when on a
 * break.
 * 
 * @author berti
 */
public interface PomodoroServiceInterface {

	/**
	 * Returns an instance of <code>Session</code> with information about the
	 * current session.
	 * 
	 * @return information about the current session
	 */
	public Session getSession();

	/**
	 * Start a new pomodoro from idle.
	 */
	public void startPomodoro();

	/**
	 * Cancels the current pomodoro and returns to idle.
	 */
	public void cancelPomodoro();

	/**
	 * Starts a new break, after a pomodoro has finished.
	 */
	public void startBreak();

	/**
	 * Skips the next scheduled break and returns to idle. Skipping a break does
	 * not cancel the session, as the pomodoro phase has already finished.
	 */
	public void skipBreak();

	/**
	 * Cancels the current break and return to idle. Cancelling a break does not
	 * cancel the session, as the pomodoro phase has already finished.
	 */
	public void cancelBreak();

}

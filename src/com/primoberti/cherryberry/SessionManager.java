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

/**
 * Class for managing the lifecycle of the work sessions.
 * 
 * @author berti
 */
public class SessionManager {

	/* Private instance constants ************** */

	private final IdleState idleState = new IdleState();
	private final PomodoroRunningState pomodoroRunningState = new PomodoroRunningState();
	private final PomodoroFinishedState pomodoroFinishedState = new PomodoroFinishedState();
	private final BreakRunningState breakRunningState = new BreakRunningState();

	/* Private fields ************************** */
	
	private State state = idleState;
	
	/* Public methods ************************** */
	
	public void start() {
		state = state.start();
	}
	
	public void cancel() {
		state = state.cancel();
	}

	/* Private inner classes ******************* */

	private abstract class State {

		public State start() {
			return this;
		}

		public State cancel() {
			return this;
		}

	}

	private class IdleState extends State {

		@Override
		public State start() {
			return pomodoroRunningState;
		}

	}

	private class PomodoroRunningState extends State {

		@Override
		public State cancel() {
			return idleState;
		}

	}

	private class PomodoroFinishedState extends State {

		@Override
		public State start() {
			return breakRunningState;
		}

		@Override
		public State cancel() {
			return idleState;
		}

	}

	private class BreakRunningState extends State {

		@Override
		public State cancel() {
			return idleState;
		}

	}

}

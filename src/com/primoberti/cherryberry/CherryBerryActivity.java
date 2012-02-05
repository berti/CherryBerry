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

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class CherryBerryActivity extends Activity {

	private PomodoroTimer timer;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		timer = new PomodoroTimer();
		timer.setPomodoroDuration(25 * 60 * 1000);
		timer.setBreakDuration(5 * 60 * 1000);
		timer.setListener(new MyPomodoroTimerListener());

		Button button = (Button) findViewById(R.id.startButton);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onStartClick();
			}
		});

		button = (Button) findViewById(R.id.stopButton);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onStopClick();
			}
		});
	}

	/* Private methods ************************* */

	private void onStartClick() {
		timer.startPomodoro();
	}

	private void onStopClick() {
		timer.cancel();
	}

	private void updateTimer(long millis) {
		long minutes = millis / 1000 / 60;
		long seconds = millis / 1000 % 60;

		TextView timerTextView = (TextView) findViewById(R.id.timerTextView);
		timerTextView.setText(String.format("%d:%02d", minutes, seconds));
	}

	/* Private inner classes ******************* */

	private class MyPomodoroTimerListener implements PomodoroTimerListener {

		@Override
		public void onFinish(PomodoroTimer timer) {
			updateTimer(0);
		}

		@Override
		public void onTick(PomodoroTimer timer, long millisUntilFinished) {
			updateTimer(millisUntilFinished);
		}

	}
}
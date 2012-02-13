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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.primoberti.cherryberry.PomodoroTimerService.LocalBinder;

public class CherryBerryActivity extends Activity {

	/* Private fields ************************** */

	private PomodoroTimerService timerService;

	private boolean timerServiceBound = false;

	private ServiceConnection timerServiceConnection;

	/* Private constants *********************** */

	private final static int DIALOG_POMODORO_FINISHED = 0;

	/* Public methods ************************** */

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("CherryBerryActivity", "onCreate");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		timerServiceConnection = new PomodoroTimerServiceConnector();

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

	@Override
	protected void onStart() {
		Log.d("CherryBerryActivity", "onStart");

		super.onStart();

		bindPomodoroTimerService();
	}

	@Override
	protected void onStop() {
		Log.d("CherryBerryActivity", "onStop");

		// TODO cancel timer?
		unbindService(timerServiceConnection);
		timerService = null;
		timerServiceBound = false;

		super.onStop();
	}

	@Override
	protected void onRestart() {
		Log.d("CherryBerryActivity", "onRestart");

		super.onRestart();

		bindPomodoroTimerService();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d("CherryBerryActivity", "onNewIntent");

		super.onNewIntent(intent);
	}

	@Override
	protected void onDestroy() {
		Log.d("CherryBerryActivity", "onDestroy");

		super.onDestroy();
	}

	@Override
	protected void onPause() {
		Log.d("CherryBerryActivity", "onPause");

		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.d("CherryBerryActivity", "onResume");

		super.onResume();
	}

	/* Protected methods *********************** */

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;

		switch (id) {
		case DIALOG_POMODORO_FINISHED:
			PomodoroFinishedDialogOnClickListener listener = new PomodoroFinishedDialogOnClickListener();

			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setMessage(R.string.pomodoro_finished_dialog_title);
			builder.setCancelable(false);
			builder.setPositiveButton(R.string.start_break_button, listener);
			// builder.setNegativeButton(R.string.cancel_pomodoro_button,
			// listener);
			builder.setNeutralButton(R.string.skip_break_button, listener);

			dialog = builder.create();
			break;
		}

		return dialog;
	}

	/* Private methods ************************* */

	private boolean bindPomodoroTimerService() {
		Log.d("CherryBerryActivity", "bindPomodoroTimerService");

		Intent intent = new Intent(this, PomodoroTimerService.class);
		bindService(intent, timerServiceConnection, BIND_AUTO_CREATE);

		return timerServiceBound;
	}

	private void checkPomodoroTimerServiceStatus() {
		PomodoroTimerService.Status status = timerService.getStatus();
		switch (status) {
		case POMODORO_RUNNING:
			disableStartButton();
			TextView statusTextView = (TextView) findViewById(R.id.statusTextView);
			statusTextView.setText(R.string.status_pomodoro_running);
			break;
		case POMODORO_FINISHED:
			onPomodoroFinish();
			break;
		case BREAK_RUNNING:
			disableStartButton();
			statusTextView = (TextView) findViewById(R.id.statusTextView);
			statusTextView.setText(R.string.status_break_running);
			break;
		case BREAK_FINISHED:
			onBreakFinish();
			break;
		}
	}

	private void onStartClick() {
		if (timerServiceBound) {
			timerService.startPomodoro();
			disableStartButton();

			TextView statusTextView = (TextView) findViewById(R.id.statusTextView);
			statusTextView.setText(R.string.status_pomodoro_running);
		}
	}

	private void onStopClick() {
		if (timerServiceBound) {
			timerService.stop();
		}
		updateTimer(0);
		enableStartButton();
		
		TextView statusTextView = (TextView) findViewById(R.id.statusTextView);
		statusTextView.setText(R.string.status_idle);
	}

	private void updateTimer(long millis) {
		long minutes = millis / 1000 / 60;
		long seconds = millis / 1000 % 60;

		TextView timerTextView = (TextView) findViewById(R.id.timerTextView);
		timerTextView.setText(String.format("%d:%02d", minutes, seconds));
	}

	private void onPomodoroFinish() {
		updateTimer(0);

		TextView statusTextView = (TextView) findViewById(R.id.statusTextView);
		statusTextView.setText(R.string.status_pomodoro_finished);

		showDialog(DIALOG_POMODORO_FINISHED);
	}

	private void onBreakFinish() {
		updateTimer(0);
		enableStartButton();

		TextView statusTextView = (TextView) findViewById(R.id.statusTextView);
		statusTextView.setText(R.string.status_idle);
	}

	private void enableStartButton() {
		((Button) findViewById(R.id.startButton)).setEnabled(true);
		((Button) findViewById(R.id.stopButton)).setEnabled(false);
	}

	private void disableStartButton() {
		((Button) findViewById(R.id.startButton)).setEnabled(false);
		((Button) findViewById(R.id.stopButton)).setEnabled(true);
	}

	/* Private inner classes ******************* */

	private class PomodoroTimerServiceConnector implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d("CherryBerryActivity.PomodoroTimerServiceConnector",
					"onServiceConnected");

			timerService = ((LocalBinder) service).getService();
			timerServiceBound = true;

			timerService.setPomodoroDuration(25 * 60 * 1000);
			timerService.setBreakDuration(5 * 60 * 1000);
			timerService.setListener(new MyPomodoroTimerListener());

			checkPomodoroTimerServiceStatus();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.e(CherryBerryActivity.class.getName(), "onServiceConnected");

			timerService = null;
			timerServiceBound = false;
		}

	}

	private class MyPomodoroTimerListener implements PomodoroTimerListener {

		@Override
		public void onPomodoroFinish(PomodoroTimerService timer) {
			CherryBerryActivity.this.onPomodoroFinish();
		}

		@Override
		public void onBreakFinish(PomodoroTimerService timer) {
			CherryBerryActivity.this.onBreakFinish();
		}

		@Override
		public void onTick(PomodoroTimerService timer, long millisUntilFinished) {
			updateTimer(millisUntilFinished);
		}

	}

	private class PomodoroFinishedDialogOnClickListener implements
			DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case AlertDialog.BUTTON_POSITIVE:
				// Start break
				if (timerServiceBound) {
					timerService.startBreak();

					TextView statusTextView = (TextView) findViewById(R.id.statusTextView);
					statusTextView.setText(R.string.status_break_running);
				}
				break;
			case AlertDialog.BUTTON_NEGATIVE:
				// Cancel pomodoro
				if (timerServiceBound) {
					timerService.stop();
					enableStartButton();
				}
				break;
			case AlertDialog.BUTTON_NEUTRAL:
				// Skip break
				if (timerServiceBound) {
					timerService.skip();
					enableStartButton();

					TextView statusTextView = (TextView) findViewById(R.id.statusTextView);
					statusTextView.setText(R.string.status_idle);
				}
				break;
			}
		}

	}

}
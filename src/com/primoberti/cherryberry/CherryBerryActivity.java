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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.primoberti.cherryberry.PomodoroService.LocalBinder;

public class CherryBerryActivity extends Activity {

	/* Private fields ************************** */

	private PomodoroService timerService;

	private boolean timerServiceBound = false;

	private ServiceConnection timerServiceConnection;

	private TextView statusTextView;

	private TextView timerTextView;

	/* Private constants *********************** */

	private final static String TAG = "CherryBerryActivity";

	private final static int DIALOG_POMODORO_FINISHED = 0;

	private final static int SHOW_SETTINGS = 0;

	/* Public methods ************************** */

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Load the default values for the user settings
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);

		timerServiceConnection = new PomodoroTimerServiceConnector();

		statusTextView = (TextView) findViewById(R.id.statusTextView);
		timerTextView = (TextView) findViewById(R.id.timerTextView);

		// Show current pomodoro duration preference by default
		updateTimer(PreferencesHelper.getPomodoroDuration(this));

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
		Log.d(TAG, "onStart");

		super.onStart();

		bindPomodoroTimerService();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");

		// TODO cancel timer?
		unbindService(timerServiceConnection);
		timerService = null;
		timerServiceBound = false;

		super.onStop();
	}

	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart");

		super.onRestart();

		bindPomodoroTimerService();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG, "onNewIntent");

		super.onNewIntent(intent);
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");

		super.onDestroy();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");

		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");

		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about:
			showAboutDialog();
			return true;
		case R.id.settings:
			showSettings();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* Protected methods *********************** */

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;

		switch (id) {
		case DIALOG_POMODORO_FINISHED:
			PomodoroFinishedDialogOnClickListener listener = new PomodoroFinishedDialogOnClickListener();

			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setMessage(R.string.dialog_title_pomodoro_finished);
			builder.setCancelable(false);
			builder.setPositiveButton(R.string.button_start_break, listener);
			// builder.setNegativeButton(R.string.cancel_pomodoro_button,
			// listener);
			builder.setNeutralButton(R.string.button_skip_break, listener);

			dialog = builder.create();
			break;
		}

		return dialog;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == SHOW_SETTINGS) {
			if (timerService == null || !timerService.isRunning()) {
				updateTimer(PreferencesHelper.getPomodoroDuration(this));
			}
		}
	}

	/* Private methods ************************* */

	private boolean bindPomodoroTimerService() {
		Log.d(TAG, "bindPomodoroTimerService");

		Intent intent = new Intent(this, PomodoroService.class);
		bindService(intent, timerServiceConnection, BIND_AUTO_CREATE);

		return timerServiceBound;
	}

	private void checkPomodoroTimerServiceStatus() {
		PomodoroService.Status status = timerService.getStatus();
		switch (status) {
		case POMODORO_RUNNING:
			disableStartButton();
			statusTextView.setText(R.string.status_pomodoro_running);
			updateTimer(timerService.getTimerEnd() - System.currentTimeMillis());
			break;
		case POMODORO_FINISHED:
			onPomodoroFinish();
			break;
		case BREAK_RUNNING:
			disableStartButton();
			statusTextView.setText(R.string.status_break_running);
			updateTimer(timerService.getTimerEnd() - System.currentTimeMillis());
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

			statusTextView.setText(R.string.status_pomodoro_running);
		}
	}

	private void onStopClick() {
		if (timerServiceBound) {
			timerService.stop();
		}
		updateTimer(0);
		enableStartButton();

		statusTextView.setText(R.string.status_idle);
	}

	private void updateTimer(long millis) {
		long minutes = millis / 1000 / 60;
		long seconds = millis / 1000 % 60;

		timerTextView.setText(String.format("%d:%02d", minutes, seconds));
	}

	private void onPomodoroFinish() {
		updateTimer(0);

		statusTextView.setText(R.string.status_pomodoro_finished);

		showDialog(DIALOG_POMODORO_FINISHED);
	}

	private void onBreakFinish() {
		updateTimer(0);
		enableStartButton();

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

	private void showAboutDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dialog_title_about);
		builder.setMessage(R.string.dialog_message_about);
		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private void showSettings() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivityForResult(intent, SHOW_SETTINGS);
	}

	/* Private inner classes ******************* */

	private class PomodoroTimerServiceConnector implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "onServiceConnected");

			timerService = ((LocalBinder) service).getService();
			timerServiceBound = true;

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

	private class MyPomodoroTimerListener implements PomodoroListener {

		@Override
		public void onPomodoroFinish(PomodoroService service) {
			CherryBerryActivity.this.onPomodoroFinish();
		}

		@Override
		public void onBreakFinish(PomodoroService service) {
			CherryBerryActivity.this.onBreakFinish();
		}

		@Override
		public void onTick(PomodoroService service, long millisUntilFinished) {
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

					statusTextView.setText(R.string.status_idle);
				}
				break;
			}
		}

	}

}

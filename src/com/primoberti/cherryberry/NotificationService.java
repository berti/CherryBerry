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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

/**
 * Service for displaying notifications related to a pomodoro.
 * 
 * @author berti
 */
public class NotificationService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.getAction().equals(CherryBerryActivity.POMODORO_FINISHED)) {
			Context context = getApplicationContext();
			Toast.makeText(context, "Pomodoro finished", Toast.LENGTH_SHORT)
					.show();
		}
		else if (intent.getAction().equals(CherryBerryActivity.BREAK_FINISHED)) {
			Context context = getApplicationContext();
			Toast.makeText(context, "Break finished", Toast.LENGTH_SHORT)
					.show();
		}

		stopSelf();

		return START_NOT_STICKY;
	}

}

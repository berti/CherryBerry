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
import android.content.Intent;
import android.os.IBinder;

/**
 * Service that shows a persistent notification in the status bar while a
 * pomodoro or a break is running.
 * 
 * @author berti
 */
public class PersistentNotificationService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}

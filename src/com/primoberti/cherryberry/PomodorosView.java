/*
 * Copyright 2012-2013 Alberto Salmerón Moreno
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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * A view for showing the progress of a series of pomodoros. Each pomodoro is
 * represented by a circle. An empty circle means a pomodoro that hasn't yet
 * started. A full circle is a finished pomodoro.
 * 
 * @author berti
 */
public class PomodorosView extends View {

	/* Private constants *********************** */

	private final static int DEFAULT_WIDTH = 600;
	private final static int DEFAULT_HEIGHT = 200;

	/* Private fields ************************** */

	private int numPomodoros;
	private int currentPomodoro;
	private boolean running;

	private Paint emptyCirclePaint;
	private Paint fullCirclePaint;
	private Paint runningCirclePaint;

	/* Constructors **************************** */

	public PomodorosView(Context context) {
		super(context);
		initView();
	}

	public PomodorosView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	public PomodorosView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	/* Public methods ************************** */

	public int getNumPomodoros() {
		return numPomodoros;
	}

	public void setNumPomodoros(int numPomodoros) {
		this.numPomodoros = numPomodoros;
	}

	public int getCurrentPomodoro() {
		return currentPomodoro;
	}

	public void setCurrentPomodoro(int currentPomodoro) {
		this.currentPomodoro = currentPomodoro;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	/* Private and protected methods *********** */

	private void initView() {
		Resources r = getResources();

		emptyCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		emptyCirclePaint.setColor(r.getColor(R.color.pomodoro_circle));
		emptyCirclePaint.setStrokeWidth(2);
		emptyCirclePaint.setStyle(Paint.Style.STROKE);

		fullCirclePaint = new Paint(emptyCirclePaint);
		fullCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

		runningCirclePaint = new Paint(emptyCirclePaint);
		runningCirclePaint
				.setColor(r.getColor(R.color.pomodoro_circle_running));
		runningCirclePaint.setStyle(Paint.Style.FILL);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measuredWidth = measureWidth(widthMeasureSpec);
		int measuredHeight = measureHeight(heightMeasureSpec);

		setMeasuredDimension(measuredWidth, measuredHeight);
	}

	private int measureWidth(int widthMeasureSpec) {
		int specMode = MeasureSpec.getMode(widthMeasureSpec);
		int specSize = MeasureSpec.getSize(widthMeasureSpec);

		int result = DEFAULT_WIDTH;

		if (specMode == MeasureSpec.AT_MOST) {
			// TODO compute ideal width
			result = specSize;
		}
		else if (specMode == MeasureSpec.EXACTLY) {
			result = specSize;
		}

		return result;
	}

	private int measureHeight(int heightMeasureSpec) {
		int specMode = MeasureSpec.getMode(heightMeasureSpec);
		int specSize = MeasureSpec.getSize(heightMeasureSpec);

		int result = DEFAULT_HEIGHT;

		if (specMode == MeasureSpec.AT_MOST) {
			// TODO compute ideal height
			result = specSize;
		}
		else if (specMode == MeasureSpec.EXACTLY) {
			result = specSize;
		}

		return result;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int measuredWidth = getMeasuredWidth();
		int measuredHeight = getMeasuredHeight();

		int width = measuredWidth - getPaddingLeft() - getPaddingRight();
		int height = measuredHeight - getPaddingTop() - getPaddingBottom();

		int px = width / 2 / numPomodoros;
		int py = height / 2;

		int radius = Math.min(px, py);

		int n = numPomodoros - 1;
		int cx0 = getPaddingLeft() + radius;
		int cxn = measuredWidth - getPaddingRight() - radius;
		int cy = py + getPaddingTop();

		for (int i = 0; i < numPomodoros; i++) {
			int cx = (cxn - cx0) / n * i + cx0;
			Paint paint = null;
			if (i < currentPomodoro) {
				paint = fullCirclePaint;
			}
			else if (i > currentPomodoro) {
				paint = emptyCirclePaint;
			}
			else {
				paint = emptyCirclePaint;
				if (running) {
					canvas.drawCircle(cx, cy, radius, runningCirclePaint);
				}
			}
			canvas.drawCircle(cx, cy, radius, paint);
		}
	}

}

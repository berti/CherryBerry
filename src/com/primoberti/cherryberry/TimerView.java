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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * A view for showing a countdown timer for the current pomodoro, including cues
 * of how much time is left in the pomodoro or break. The timer is composed of a
 * text enclosed in a circumference. The length of the circumference represents
 * the total time of a pomodoro, plus its break. There is a mark at the top of
 * the circumference, representing the start/end, and another where the pomodoro
 * finishes and the break starts. Finally, there is a moving mark representing
 * the countdown timer.
 * 
 * The view takes as much space as possible. The radius of the circumference is
 * thus the maximum available, taking into account the height and the width of
 * the view (minus the padding). The timer text is centered inside the
 * circumference.
 * 
 * @author berti
 */
public class TimerView extends View {

	/* Private constants *********************** */

	private final static int DEFAULT_WIDTH = 600;
	private final static int DEFAULT_HEIGHT = 600;

	/* Private fields ************************** */

	private int pomodoroLength = 25;
	private int breakLength = 5;

	private int activeColor;
	private int inactiveColor;

	private Paint activeCircumferencePaint;
	private Paint inactiveCircumferencePaint;
	private Paint emptyMarkPaint;
	private Paint fullMarkPaint;

	private RectF timerRect;

	/* Constructors **************************** */

	public TimerView(Context context) {
		super(context);
		initView();
	}

	public TimerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(attrs);
	}

	public TimerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(attrs);
	}

	/* Public methods ************************** */

	/**
	 * Return the current length of the "work" part of a pomodoro.
	 * 
	 * @return the length of a pomodoro
	 */
	public int getPomodoroLength() {
		return pomodoroLength;
	}

	/**
	 * Set the length of the "work" part of a pomodoro.
	 * 
	 * @param pomodoroLength the new pomodoro length
	 */
	public void setPomodoroLength(int pomodoroLength) {
		this.pomodoroLength = pomodoroLength;
	}

	/**
	 * Return the current length of a break.
	 * 
	 * @return the length of a break
	 */
	public int getBreakLength() {
		return breakLength;
	}

	/**
	 * Set the length of a break.
	 * 
	 * @param breakLength the new break length
	 */
	public void setBreakLength(int breakLength) {
		this.breakLength = breakLength;
	}

	/**
	 * Initialize view resources, taking into account the given attribute
	 * values.
	 */
	private void initView(AttributeSet attrs) {
		Resources r = getResources();
		TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs,
				R.styleable.TimerView, 0, 0);

		try {
			activeColor = a.getColor(R.styleable.TimerView_activeColor,
					r.getColor(R.color.pomodoro_circle));
			inactiveColor = a.getColor(R.styleable.TimerView_inactiveColor,
					r.getColor(R.color.inactive_circle));
		}
		finally {
			a.recycle();
		}

		initView();
	}

	/**
	 * Initialize <code>Paint</code> instances used when drawing and other
	 * resources.
	 */
	private void initView() {
		activeCircumferencePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		activeCircumferencePaint.setColor(activeColor);
		activeCircumferencePaint.setStrokeWidth(6);
		activeCircumferencePaint.setStyle(Paint.Style.STROKE);

		inactiveCircumferencePaint = new Paint(activeCircumferencePaint);
		inactiveCircumferencePaint.setColor(inactiveColor);

		emptyMarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		emptyMarkPaint.setColor(activeColor);
		emptyMarkPaint.setStrokeWidth(2);
		emptyMarkPaint.setStyle(Paint.Style.STROKE);

		fullMarkPaint = new Paint(emptyMarkPaint);
		fullMarkPaint.setStyle(Paint.Style.FILL_AND_STROKE);

		timerRect = new RectF();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measuredWidth = measureDimension(widthMeasureSpec, DEFAULT_WIDTH);
		int measuredHeight = measureDimension(heightMeasureSpec, DEFAULT_HEIGHT);

		setMeasuredDimension(measuredWidth, measuredHeight);
	}

	/**
	 * Returns the size given in the measure spec, unless the
	 * <code>specMode</code> is <code>UNSPECIFIED</code> in which case the given
	 * default value is returned.
	 * 
	 * @param measureSpec the measure spec to measure
	 * @param defaultMeasure the default value to return if
	 *            <code>specMode</code> unspecified
	 * @return the given size if <code>EXACTLY</code> or <code>AT_MOST</code>;
	 *         <code>defaultMeasure</code> otherwise
	 */
	private int measureDimension(int measureSpec, int defaultMeasure) {
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		int result = defaultMeasure;

		if (specMode != MeasureSpec.UNSPECIFIED) {
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

		int px = width / 2;
		int py = height / 2;

		int radius = Math.min(px, py) - 3;

		int totalLength = pomodoroLength + breakLength;
		float pomodoroSweepAngle = 360f * pomodoroLength / totalLength;
		float breakSweepAngle = 360.0f - pomodoroSweepAngle;

		timerRect.set(px - radius, py - radius, px + radius, py + radius);

		canvas.drawArc(timerRect, -90, pomodoroSweepAngle, false,
				activeCircumferencePaint);
		canvas.drawArc(timerRect, -90 + pomodoroSweepAngle, breakSweepAngle,
				false, inactiveCircumferencePaint);
	}

}

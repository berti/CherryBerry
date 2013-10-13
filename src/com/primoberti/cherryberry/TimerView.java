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
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * A view for showing a countdown timer for the current pomodoro, including cues
 * of how much time is left in the pomodoro or break. The timer is composed of a
 * text enclosed in a circumference. The length of the circumference represents
 * the total time of a pomodoro, plus its break. The circumference is divided
 * into segments (arcs) representing the pomodoro, break and elapsed times.
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

	private long elapsed = 0;

	private int elapsedColor;
	private int pomodoroColor;
	private int breakColor;
	private float strokeWidth;
	private int textColor;
	private float textSize;

	private Paint elapsedPaint;
	private Paint pomodoroPaint;
	private Paint breakPaint;
	private Paint textPaint;

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
		invalidate();
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
		invalidate();
	}

	/**
	 * Return the current elapsed time, in milliseconds.
	 * 
	 * @return the current elapsed time, in milliseconds
	 */
	public long getElapsed() {
		return elapsed;
	}

	/**
	 * Set the current elapsed time.
	 * 
	 * @param elapsed the new current elapsed time, in milliseconds
	 */
	public void setElapsed(long elapsed) {
		int totalTime = (pomodoroLength + breakLength) * 1000 * 60;
		this.elapsed = elapsed == totalTime ? elapsed : elapsed % totalTime;
		invalidate();
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
			elapsedColor = a.getColor(R.styleable.TimerView_elapsedColor,
					r.getColor(R.color.elapsed_circle));
			pomodoroColor = a.getColor(R.styleable.TimerView_pomodoroColor,
					r.getColor(R.color.pomodoro_circle));
			breakColor = a.getColor(R.styleable.TimerView_breakColor,
					r.getColor(R.color.break_circle));
			strokeWidth = a.getDimension(R.styleable.TimerView_strokeWidth,
					r.getDimension(R.dimen.timer_circunference_stroke_width));
			textColor = a.getColor(R.styleable.TimerView_textColor,
					r.getColor(R.color.text));
			textSize = a.getDimension(R.styleable.TimerView_textSize,
					r.getDimension(R.dimen.timer_text_size));
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
		elapsedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		elapsedPaint.setColor(elapsedColor);
		elapsedPaint.setStrokeWidth(strokeWidth);
		elapsedPaint.setStyle(Paint.Style.STROKE);

		pomodoroPaint = new Paint(elapsedPaint);
		pomodoroPaint.setColor(pomodoroColor);

		breakPaint = new Paint(elapsedPaint);
		breakPaint.setColor(breakColor);

		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(textColor);
		textPaint.setTextAlign(Align.CENTER);
		textPaint.setTextSize(textSize);

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

		int radius = (int) (Math.min(px, py) - elapsedPaint.getStrokeWidth() / 2);

		int totalLength = (pomodoroLength + breakLength) * 1000 * 60;
		float elapsedSweepAngle = 360f * elapsed / totalLength;
		float pomodoroSweepAngle = Math.max(360f * pomodoroLength * 1000 * 60
				/ totalLength - elapsedSweepAngle, 0);
		float breakSweepAngle = 360.0f - elapsedSweepAngle - pomodoroSweepAngle;

		timerRect.set(px - radius, py - radius, px + radius, py + radius);

		canvas.drawArc(timerRect, -90, elapsedSweepAngle, false, elapsedPaint);
		if (pomodoroSweepAngle > 0) {
			canvas.drawArc(timerRect, -90 + elapsedSweepAngle,
					pomodoroSweepAngle, false, pomodoroPaint);
		}
		canvas.drawArc(timerRect, -90 + pomodoroSweepAngle + elapsedSweepAngle,
				breakSweepAngle, false, breakPaint);

		long millis = 0;
		if (elapsed <= pomodoroLength * 1000 * 60) {
			millis = pomodoroLength * 1000 * 60 - elapsed;
		}
		else {
			millis = totalLength - elapsed;
		}

		long minutes = millis / 1000 / 60;
		long seconds = millis / 1000 % 60;

		canvas.drawText(String.format("%2d:%02d", minutes, seconds), px, py
				- ((textPaint.descent() + textPaint.ascent()) / 2), textPaint);
	}

}

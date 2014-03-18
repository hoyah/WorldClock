package com.tcl.worldclock;

import java.util.Calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class MyAnalogClock extends View {

	private Drawable mMinuteHand = null;

	private Drawable mSecondHand = null;

	private Drawable mDial;

	private int mDialWidth;

	private int mDialHeight;

	private float mMinutes;

	private float mSecond;

	public MyAnalogClock(Context context) {
		super(context);
	}

	public MyAnalogClock(Context context, AttributeSet attrs) {
		super(context, attrs);

		// TODO Auto-generated constructor stub

		TypedArray a = context.getResources().obtainAttributes(attrs,
				R.styleable.pathbar);
		mDial = a.getDrawable(R.styleable.pathbar_dial);
		mMinuteHand = a.getDrawable(R.styleable.pathbar_minutehand);
		mSecondHand = a.getDrawable(R.styleable.pathbar_secondhand);
		a.recycle();

		mDialWidth = mDial.getIntrinsicWidth();
		mDialHeight = mDial.getIntrinsicHeight();

	}

	public void updateTime(float Minutes, float Second) {
		mMinutes = Minutes / 5;
		mSecond = Second;
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int availableWidth = getRight() - getLeft();
		int availableHeight = getBottom() - getTop();

		int x = availableWidth / 2;
		int y = availableHeight / 2;

		final Drawable dial = mDial;
		int w = dial.getIntrinsicWidth();
		int h = dial.getIntrinsicHeight();

		boolean scaled = false;

		if (availableWidth < w || availableHeight < h) {
			scaled = true;
			float scale = Math.min((float) availableWidth / (float) w,
					(float) availableHeight / (float) h);
			canvas.save();
			canvas.scale(scale, scale, x, y);
		}
		dial.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
		dial.draw(canvas);

		canvas.save();
		canvas.rotate(mSecond / 60.0f * 360.0f, x, y);

		final Drawable secondhand = mSecondHand;
		w = secondhand.getIntrinsicWidth();
		h = secondhand.getIntrinsicHeight();
		secondhand
				.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
		secondhand.draw(canvas);

		canvas.restore();
		canvas.save();
		canvas.rotate(mMinutes / 12.0f * 360.0f, x, y);

		final Drawable minuteHand = mMinuteHand;

		w = minuteHand.getIntrinsicWidth();
		h = minuteHand.getIntrinsicHeight();

		minuteHand
				.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));

		minuteHand.draw(canvas);
		canvas.restore();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		float hScale = 1.0f;
		float vScale = 1.0f;

		if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth) {
			hScale = (float) widthSize / (float) mDialWidth;
		}

		if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < mDialHeight) {
			vScale = (float) heightSize / (float) mDialHeight;
		}

		float scale = Math.min(hScale, vScale);

		setMeasuredDimension(
				resolveSize((int) (mDialWidth * scale), widthMeasureSpec),
				resolveSize((int) (mDialHeight * scale), heightMeasureSpec));
	}

	public void setTime(Calendar calendar) {
		int hour = calendar.get(Calendar.HOUR);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);

		mSecond = minute + second / 60.0f;
		mMinutes = hour + mSecond / 60.0f;

		invalidate();
	}
}
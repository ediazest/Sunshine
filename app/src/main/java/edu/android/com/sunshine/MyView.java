package edu.android.com.sunshine;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

/**
 * Created by edu on 25/08/2015.
 */
public class MyView extends View {

    private final int circleSize = 300;
    private final int bordersMargin = 15;
    private final int circleMargin = 3;
    private final int smallCircleRadius = 10;
    public float mDegrees;
    public float mWindSpeed;
    private Paint mIndicatorsPaint;
    private Paint mValuePaint;
    private Paint mCirclePaint;
    private Paint mCenterPaint;
    private Paint mArrowPaint;

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CompassView,
                0, 0);

        try {
            mDegrees = a.getFloat(R.styleable.CompassView_degrees, 0);
            mWindSpeed = a.getFloat(R.styleable.CompassView_windSpeed, 0);
        } finally {
            a.recycle();
        }

    }

    public MyView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);

        init();

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CompassView,
                0, 0);

        try {
            mDegrees = a.getFloat(R.styleable.CompassView_degrees, 0);
            mWindSpeed = a.getFloat(R.styleable.CompassView_windSpeed, 0);
        } finally {
            a.recycle();
        }

    }

    private void init() {

        mIndicatorsPaint = new Paint();
        mIndicatorsPaint.setAntiAlias(true);
        mIndicatorsPaint.setStrokeWidth(1);
        mIndicatorsPaint.setTextSize(25);
        mIndicatorsPaint.setStyle(Paint.Style.FILL);
        mIndicatorsPaint.setColor(getContext().getResources().getColor(R.color.black));

        mValuePaint = new Paint();
        mValuePaint.setAntiAlias(true);
        mValuePaint.setStrokeWidth(3);
        mValuePaint.setTextSize(25);
        mValuePaint.setStyle(Paint.Style.FILL);
        mValuePaint.setColor(getContext().getResources().getColor(R.color.black));

        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStrokeWidth(5);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setColor(getContext().getResources().getColor(R.color.sunshine_dark_blue));

        mCenterPaint = new Paint();
        mCenterPaint.setAntiAlias(true);
        mCenterPaint.setStrokeWidth(5);
        mCenterPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mCenterPaint.setColor(getContext().getResources().getColor(R.color.sunshine_dark_blue));

        mArrowPaint = new Paint();
        mArrowPaint.setAntiAlias(true);
        mArrowPaint.setStrokeWidth(5);
        mArrowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mArrowPaint.setColor(getContext().getResources().getColor(R.color.grey_700));


        AccessibilityManager accessibilityManager =
                (AccessibilityManager) getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager.isEnabled()) {
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int minWidth = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth() + circleSize;
        int minHeight = getSuggestedMinimumHeight() + getPaddingBottom() + getPaddingTop() + circleSize;

        setMeasuredDimension(
                resolveSize(minWidth, widthMeasureSpec),
                resolveSize(minHeight, heightMeasureSpec));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(circleSize / 2, circleSize / 2, (circleSize / 2) - circleMargin, mCirclePaint);

        canvas.drawCircle(circleSize / 2, circleSize / 2, smallCircleRadius, mCenterPaint);

        canvas.drawText("N", (circleSize / 2) - 4 * circleMargin, 2 * bordersMargin, mIndicatorsPaint);
//        canvas.drawText("NE", circleSize - 5 * bordersMargin - circleMargin, circleSize / 4, mIndicatorsPaint);
        canvas.drawText("W", bordersMargin, (circleSize / 2) + 2 * circleMargin, mIndicatorsPaint);
//        canvas.drawText("SW", 3 * bordersMargin, 3 * circleSize / 4, mIndicatorsPaint);
        canvas.drawText("S", (circleSize / 2) - 4 * circleMargin, circleSize - bordersMargin, mIndicatorsPaint);
//        canvas.drawText("SE", circleSize - 5 * bordersMargin - circleMargin, 3 * circleSize / 4, mIndicatorsPaint);
        canvas.drawText("E", circleSize - 2 * bordersMargin, (circleSize / 2) + 2 * circleMargin, mIndicatorsPaint);
//        canvas.drawText("NW", 3 * bordersMargin, circleSize / 4, mIndicatorsPaint);

        paintWindSpeedAndDirection(canvas);

    }

    private void paintWindSpeedAndDirection(Canvas canvas) {

        String windText = Utility.getFormattedWind(getContext(), mWindSpeed, mDegrees);

        windText = windText.substring(windText.indexOf(":") + 2);

        if (windText.contains("South East")) {

            canvas.drawLine((circleSize / 2) + smallCircleRadius, (circleSize / 2) + smallCircleRadius, circleSize - 5 * bordersMargin - circleMargin, 3 * circleSize / 4, mArrowPaint);
            windText = windText.replace("South East", "SE");

        } else if (windText.contains("South West")) {

            canvas.drawLine((circleSize / 2) - smallCircleRadius, (circleSize / 2) + smallCircleRadius, 5 * bordersMargin, 3 * circleSize / 4, mArrowPaint);
            windText = windText.replace("South West", "SW");

        } else if (windText.contains("South")) {

            canvas.drawLine((circleSize / 2), (circleSize / 2) + smallCircleRadius, (circleSize / 2), circleSize - (2 * bordersMargin + smallCircleRadius), mArrowPaint);
            windText = windText.replace("South", "S");

        } else if (windText.contains("North East")) {

            canvas.drawLine((circleSize / 2) + smallCircleRadius, (circleSize / 2) - smallCircleRadius, circleSize - 5 * bordersMargin - circleMargin, circleSize / 4, mArrowPaint);
            windText = windText.replace("North East", "NE");

        } else if (windText.contains("North West")) {

            canvas.drawLine((circleSize / 2), (circleSize / 2) - smallCircleRadius, 5 * bordersMargin, circleSize / 4, mArrowPaint);
            windText = windText.replace("North West", "NW");

        } else if (windText.contains("North")) {

            canvas.drawLine((circleSize / 2), (circleSize / 2) - smallCircleRadius, (circleSize / 2), 2 * bordersMargin + smallCircleRadius, mArrowPaint);
            windText = windText.replace("North", "N");

        } else if (windText.contains("East")) {

            canvas.drawLine((circleSize / 2) + smallCircleRadius, (circleSize / 2), circleSize - (2 * bordersMargin + smallCircleRadius), (circleSize / 2), mArrowPaint);
            windText = windText.replace("East", "E");

        } else if (windText.contains("West")) {

            canvas.drawLine((circleSize / 2) - smallCircleRadius, (circleSize / 2), (2 * bordersMargin + smallCircleRadius), (circleSize / 2), mArrowPaint);
            windText = windText.replace("West", "W");

        }


        if (windText.contains("S")) {

            canvas.drawText(windText, (circleSize / 2) - 4 * bordersMargin, (circleSize / 2) - 3 * bordersMargin, mValuePaint);

        } else {

            canvas.drawText(windText, (circleSize / 2) - 4 * bordersMargin, (circleSize / 2) + 3 * bordersMargin, mValuePaint);

        }


    }

    public void setDegrees(float degrees) {
        this.mDegrees = degrees;
        invalidate();
        requestLayout();
    }

    public void setWindSpeed(float windSpeed) {
        this.mWindSpeed = windSpeed;
        invalidate();
        requestLayout();
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        event.getText().add(Utility.getFormattedWind(getContext(), mWindSpeed, mDegrees));
        return true;
    }
}



package org.hugoandrade.euro2016.predictor.customview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;

import org.hugoandrade.euro2016.predictor.R;

public class RoundedCornerLayout extends LinearLayout {

    @SuppressWarnings("unused")
    private static final String TAG = RoundedCornerLayout.class.getSimpleName();

    private float[] cornerRadius = new float[8];
    private float elevationStart, elevationTop, elevationEnd, elevationBottom;
    private float percentileHeight, percentileWidth;
    private int borderColor;
    private int backgroundColor;
    private int backgroundColorSelected;

    private OnTouchStateChangeListener mOnTouchStateListener;
    private View.OnClickListener mOnClickListener;

    private Rect rect;
    private android.graphics.RectF rect2 = new RectF();
    private boolean wasPressedOutside;
    private Path path = new Path();
    private boolean mIntercept = false;

    public RoundedCornerLayout(Context context) {
        super(context);
    }

    public RoundedCornerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RoundedCornerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundedCornerLayout, 0, 0);

        float cornerRadiusGlobal =
                a.getDimension(R.styleable.RoundedCornerLayout_corner_radius,0f);
        float cornerRadiusTopStart =
                a.getDimension(R.styleable.RoundedCornerLayout_corner_radius_top_start, cornerRadiusGlobal);
        float cornerRadiusTopEnd =
                a.getDimension(R.styleable.RoundedCornerLayout_corner_radius_top_end, cornerRadiusGlobal);
        float cornerRadiusBottomStart =
                a.getDimension(R.styleable.RoundedCornerLayout_corner_radius_bottom_start, cornerRadiusGlobal);
        float cornerRadiusBottomEnd =
                a.getDimension(R.styleable.RoundedCornerLayout_corner_radius_bottom_end, cornerRadiusGlobal);
        float elevation =
                a.getDimension(R.styleable.RoundedCornerLayout_border_elevation, 0f);
        elevationStart =
                a.getDimension(R.styleable.RoundedCornerLayout_border_elevation_start, elevation);
        elevationTop =
                a.getDimension(R.styleable.RoundedCornerLayout_border_elevation_top, elevation);
        elevationEnd =
                a.getDimension(R.styleable.RoundedCornerLayout_border_elevation_end, elevation);
        elevationBottom =
                a.getDimension(R.styleable.RoundedCornerLayout_border_elevation_bottom, elevation);
        borderColor =
                a.getColor(R.styleable.RoundedCornerLayout_border_color, Color.TRANSPARENT);
        backgroundColor =
                a.getColor(R.styleable.RoundedCornerLayout_background_color, Color.TRANSPARENT);
        backgroundColorSelected =
                a.getColor(R.styleable.RoundedCornerLayout_background_color_selected, backgroundColor);
        percentileHeight =
                a.getFloat(R.styleable.RoundedCornerLayout_percentile_height, Float.NaN);
        percentileWidth =
                a.getFloat(R.styleable.RoundedCornerLayout_percentile_width, Float.NaN);
        boolean isClickable =
                attrs.getAttributeBooleanValue("http://schemas.android.com/apk/res/android", "clickable", false);
        a.recycle();

        if (!Float.isNaN(percentileWidth) && !Float.isNaN(percentileHeight)) {
            percentileHeight = Float.NaN;
            percentileWidth = Float.NaN;
        }
        else {
            if (!Float.isNaN(percentileWidth)) {
                if (percentileWidth < 0)
                    percentileWidth = 0;
                //else if (percentileWidth > 100) percentileWidth = 100;
            }
            if (!Float.isNaN(percentileHeight)) {
                if (percentileHeight < 0)
                    percentileHeight = 0;
                //else if (percentileHeight > 100) percentileHeight = 100;
            }
        }

        cornerRadius[0] = cornerRadiusTopStart;
        cornerRadius[1] = cornerRadiusTopStart;
        cornerRadius[2] = cornerRadiusTopEnd;
        cornerRadius[3] = cornerRadiusTopEnd;
        cornerRadius[4] = cornerRadiusBottomEnd;
        cornerRadius[5] = cornerRadiusBottomEnd;
        cornerRadius[6] = cornerRadiusBottomStart;
        cornerRadius[7] = cornerRadiusBottomStart;

        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        setClickable(isClickable);
        setOnTouchListener(mTouchListener);
        setBackground(
                makeSelectorBackgroundDrawable(
                        backgroundColor, backgroundColorSelected, borderColor, cornerRadius,
                        new float[] {elevationStart, elevationTop, elevationEnd, elevationBottom}));

        setWillNotDraw(false);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(
                (int) (left + (elevationStart < 0 ? 0 : elevationStart)),
                (int) (top + (elevationTop < 0 ? 0 : elevationTop)),
                (int) (right + (elevationEnd < 0 ? 0 : elevationEnd)),
                (int) (bottom + (elevationBottom < 0 ? 0 : elevationBottom)));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w != oldw || h != oldh)
            requestLayout();

        // compute the path
        path.reset();
        rect2.set(
                0 + (elevationStart < 0 ? 0 : elevationStart),
                0 + (elevationTop < 0 ? 0 : elevationTop),
                w - (elevationEnd < 0 ? 0 : elevationEnd),
                h - (elevationBottom < 0 ? 0 : elevationBottom));
        path.addRoundRect(rect2, cornerRadius, Path.Direction.CW);
        path.close();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setOutlineProvider(new CustomOutline(w, h, (int) cornerRadius[0]));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (!Float.isNaN(percentileHeight)) {
            super.onMeasure(
                    widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(
                            (int) (width * percentileHeight / 100f),
                            MeasureSpec.EXACTLY
                    ));
        } else if (!Float.isNaN(percentileWidth)) {
            //if (getHeight() == 0 && heightMode == MeasureSpec.AT_MOST) super.onMeasure(widthMeasureSpec, heightMeasureSpec);else
            super.onMeasure(

                    //((heightMode == MeasureSpec.AT_MOST && getHeight() == 0)? getHeight() :
                    MeasureSpec.makeMeasureSpec(
                            (int) (
                                    height
                                            * percentileWidth / 100f)
                            , MeasureSpec.EXACTLY)
                    //)
                    ,
                    heightMeasureSpec);
        }
        else
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);/**/
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(path);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(save);
    }

    @Override
    public void setBackgroundColor(@ColorInt int color) {
        backgroundColor = color;
        setBackground(makeSelectorBackgroundDrawable(
                backgroundColor, backgroundColorSelected, borderColor, cornerRadius,
                new float[] {elevationStart, elevationTop, elevationEnd, elevationBottom}));
    }

    public void setBackgroundSelectedColor(@ColorInt int color) {
        backgroundColorSelected = color;
        setBackground(makeSelectorBackgroundDrawable(
                backgroundColor, backgroundColorSelected, borderColor, cornerRadius,
                new float[] {elevationStart, elevationTop, elevationEnd, elevationBottom}));
    }

    public void setBorderColor(@ColorInt int color) {
        borderColor = color;
        setBackground(makeSelectorBackgroundDrawable(
                backgroundColor, backgroundColorSelected, borderColor, cornerRadius,
                new float[] {elevationStart, elevationTop, elevationEnd, elevationBottom}));
    }

    @Override
    public void setOnClickListener(View.OnClickListener listener) {
        setClickable(true);
        mOnClickListener = listener;
    }

    public void disableHeightPercentile() {
        percentileHeight = Float.NaN;
    }

    public void disableWidthPercentile() {
        percentileWidth = Float.NaN;
    }

    public void setCornerRadius(int size) {
        cornerRadius[0] = size;
        cornerRadius[1] = size;
        cornerRadius[2] = size;
        cornerRadius[3] = size;
        cornerRadius[4] = size;
        cornerRadius[5] = size;
        cornerRadius[6] = size;
        cornerRadius[7] = size;
        invalidate();
    }

    public void setInterceptTouchEvent(boolean intercept) {
        mIntercept = intercept;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mIntercept || super.onInterceptTouchEvent(ev);
    }

    interface OnTouchStateChangeListener {
        void onTouchState(View v, int state);
    }
    @SuppressWarnings("unused")
    public void setOnTouchStateChangeListener(OnTouchStateChangeListener listener) {
        mOnTouchStateListener = listener;
    }
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!isClickable())
                return false;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                setPressed(true);
                wasPressedOutside = false;
                rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                setTouchStateAndSendToListener(v, MotionEvent.ACTION_DOWN);

            } else if (wasPressedOutside) {
                return true;
            }
            else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if(!rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())){
                    // User moved outside bounds
                    wasPressedOutside = true;
                    setPressed(false);
                    setTouchStateAndSendToListener(v, MotionEvent.ACTION_UP);

                    return true;
                }
            }
            else if (event.getAction() == MotionEvent.ACTION_UP) {
                setPressed(false);
                setTouchStateAndSendToListener(v, MotionEvent.ACTION_UP);
                if (mOnClickListener != null && isClickable())
                    mOnClickListener.onClick(v);
            }
            else if (event.getAction() == MotionEvent.ACTION_CANCEL ) {
                setPressed(false);
                setTouchStateAndSendToListener(v, MotionEvent.ACTION_UP);
            }
            return true;
        }

        private void setTouchStateAndSendToListener(View v, int action) {
            if (mOnTouchStateListener != null)
                mOnTouchStateListener.onTouchState(v, action);
        }
    };

    private static Drawable makeSelectorBackgroundDrawable(@ColorInt int backgroundColor,
                                                           @ColorInt int backgroundColorSelected,
                                                           @ColorInt int borderColor,
                                                           float[] cornerRadius,
                                                           float[] elevation) {
        StateListDrawable res = new StateListDrawable();
        res.addState(new int[]{android.R.attr.state_pressed},
                makeBackgroundDrawable(backgroundColorSelected, borderColor, cornerRadius, elevation));
        res.addState(new int[]{android.R.attr.state_selected},
                makeBackgroundDrawable(backgroundColorSelected, borderColor, cornerRadius, elevation));
        res.addState(new int[]{},
                makeBackgroundDrawable(backgroundColor, borderColor, cornerRadius, elevation));
        return res;
    }

    private static Drawable makeBackgroundDrawable(@ColorInt int backgroundColor,
                                                   @ColorInt int borderColor,
                                                   float[] cornerRadius,
                                                   float[] elevation) {

        //ShapeDrawable border = new ShapeDrawable(new RoundRectShape(cornerRadius, null, null));
        //border.getPaint().setColor(borderColor);
        //border.getPaint().setStrokeWidth(max(elevation));

        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.TRANSPARENT); //white background
        border.setStroke((int) max(elevation) + 1, borderColor); //black border with full opacity
        border.setCornerRadii(cornerRadius); //black border with full opacity

        ShapeDrawable background = new ShapeDrawable(new RoundRectShape(cornerRadius, null, null));
        background.getPaint().setColor(backgroundColor);

        Drawable[] drawableArray = {border, background};
        LayerDrawable l = new LayerDrawable(drawableArray);
        l.setLayerInset(0,
                (int) (elevation[0] < 0? -elevation[0] : 0),
                (int) (elevation[1] < 0? -elevation[1] : 0),
                (int) (elevation[2] < 0? -elevation[2] : 0),
                (int) (elevation[3] < 0? -elevation[3] : 0));
        l.setLayerInset(1,
                (int) (elevation[0] < 0? 0 : elevation[0]),
                (int) (elevation[1] < 0? 0 : elevation[1]),
                (int) (elevation[2] < 0? 0 : elevation[2]),
                (int) (elevation[3] < 0? 0 : elevation[3]));

        return l;
    }

    private static float max(float[] elevation) {
        float max = Float.NaN;
        for (float f : elevation)
            if (Float.isNaN(max) || f > max)
                max = f;
        return max;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class CustomOutline extends ViewOutlineProvider {

        int width;
        int height;
        int radius;

        CustomOutline(int width, int height, int radius) {
            this.width = width;
            this.height = height;
            this.radius = radius;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(new Rect(0, 0, width, height), radius);
        }
    }
}

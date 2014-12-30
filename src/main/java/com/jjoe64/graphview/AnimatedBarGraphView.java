package com.jjoe64.graphview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author nick
 */
public class AnimatedBarGraphView extends BarGraphView {

    private float mMaxGraphY = 0;
    private BarAnimationStyle mAnimationStyle = BarAnimationStyle.BAR_AT_A_TIME;
    private int mAnimateBar = 0;
    protected int mMaxSize = 0;
    protected ArrayList<GraphViewDataInterface> mGraphViewDataValues;
    protected boolean mIncreasedData = false;
    protected GraphViewData mDataIncreased = null;
    protected double mIncreasedBy = -1;
    protected double mNewIncreasedValue = -1;

    public enum BarAnimationStyle {
        ALL_AT_ONCE,
        BAR_AT_A_TIME
    }

    public AnimatedBarGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimatedBarGraphView(Context context, String title) {
        super(context, title);
    }

    public void setBarAnimationStyle(BarAnimationStyle animationStyle) {
        mAnimationStyle = animationStyle;
    }

    public void setMaxXSize(int size) {
        mMaxSize = size;
    }

    @Override
    public double increaseData(GraphViewData data) {
        mIncreasedData = true;
        mIncreasedBy = data.getY();
        double oldValY = super.increaseData(data);
        mDataIncreased = new GraphViewData(data.getX(), oldValY);
        //This return value is ignored;
        return 0;
    }

    @Override
    public void drawSeries(Canvas canvas, GraphViewDataInterface[] values, float graphwidth, float graphheight, float border, double minX, double minY, double diffX, double diffY, float horstart, GraphViewSeries.GraphViewSeriesStyle style) {
        double max = 0;


        if(mIncreasedData) {
            for(int i = 0; i < values.length; i++) {
                if(values[i].getX() == mDataIncreased.getX()) {

                    double newY = Math.min(Math.min(mDataIncreased.getY() + 600, values[i].getY()), diffY);
                    mDataIncreased = new GraphViewData(mDataIncreased.getX(), newY);
                    if(mDataIncreased.getY() == values[i].getY() || mDataIncreased.getY() == diffY) {
                        mIncreasedData = false;
                    }
                    values[i] = mDataIncreased;
                }
            }

            drawSeries(canvas, values, mMaxSize, graphwidth, graphheight, border, minX, minY, diffX, diffY, horstart, style);

        } else if(mAnimationStyle == BarAnimationStyle.ALL_AT_ONCE) {
            for (int i = 0; i < values.length; i++) {
                GraphViewDataInterface value = values[i];
                if (value.getY() > mMaxGraphY) {
                    values[i] = new GraphViewData(value.getX(), mMaxGraphY);
                }
                max = Math.max(max, value.getY());
            }
            mMaxGraphY = (float) Math.min(mMaxGraphY + 600, max);
            mMaxSize = Math.max(mMaxSize, values.length);

            drawSeries(canvas, values, mMaxSize, graphwidth, graphheight, border, minX, minY, diffX, diffY, horstart, style);
        } else {
            if(mAnimateBar == values.length) {
                drawSeries(canvas, values, mMaxSize, graphwidth, graphheight, border, minX, minY, diffX, diffY, horstart, style);
                return;
            }

            GraphViewDataInterface newValues[] = new GraphViewDataInterface[mAnimateBar+1];

            for(int i = 0; i <= mAnimateBar; i++) {
                if(i == mAnimateBar) {
                    newValues[i] = new GraphViewData(values[i].getX(), mMaxGraphY);
                } else {
                    newValues[i] = values[i];
                }
            }

            mMaxGraphY += (values[mAnimateBar].getY() / 8);
            GraphViewDataInterface value = values[mAnimateBar];
            if(mMaxGraphY >= value.getY()) {
                mMaxGraphY = 0;
                mAnimateBar = Math.min(++mAnimateBar, values.length);
            }

            if(mGraphViewDataValues == null) {
                mGraphViewDataValues = new ArrayList<GraphViewDataInterface>();
            } else {
                mGraphViewDataValues.clear();
            }

            mGraphViewDataValues.addAll(Arrays.asList(values));


            mMaxSize = Math.max(mMaxSize, values.length);

            drawSeries(canvas, newValues, mMaxSize, graphwidth, graphheight, border, minX, minY, diffX, diffY, horstart, style);

        }



    }

    public void drawSeries(Canvas canvas, GraphViewDataInterface[] values, float totalValues, float graphwidth, float graphheight,
                           float border, double minX, double minY, double diffX, double diffY,
                           float horstart, GraphViewSeries.GraphViewSeriesStyle style) {
//        float widthRatio = totalValues/values.length;
        float colwidth = graphwidth / totalValues;

        paint.setStrokeWidth(style.thickness);

        float offset = 0;

        // draw data
        for (int i = 0; i < values.length; i++) {
            float valY = (float) (values[i].getY() - minY);
            float ratY = (float) (valY / diffY);
            float y = graphheight * ratY;

            // hook for value dependent color
            if (style.getValueDependentColor() != null) {
                paint.setColor(style.getValueDependentColor().get(values[i]));
            } else {
                paint.setColor(style.color);
            }

            float left = (i * colwidth) + horstart -offset;
            float top = (border - y) + graphheight;
            float right = ((i * colwidth) + horstart) + (colwidth - 1) -offset;
            canvas.drawRect(left, top, right, graphheight + border - 1, paint);

            // -----Set values on top of graph---------
            if (drawValuesOnTop) {
                top -= 4;
                if (top<=border) top+=border+4;
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setColor(valuesOnTopColor );
                canvas.drawText(formatLabel(values[i].getY(), false), (left+right)/2, top, paint);
            }
        }
    }

}

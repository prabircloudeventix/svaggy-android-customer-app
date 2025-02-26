package com.svaggy.utils.stepview;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.svaggy.R;
import java.util.List;
import java.util.Map;

public class VerticalStepView extends LinearLayout implements VerticalStepViewIndicator.OnDrawIndicatorListener {
    private RelativeLayout mTextContainer;
    private VerticalStepViewIndicator mStepsViewIndicator;
    private List<String> mTexts;
    private int mComplectingPosition;
    private int mUnComplectedTextColor = ContextCompat.getColor(getContext(), R.color.primaryColor);
    private int mComplectedTextColor = ContextCompat.getColor(getContext(), R.color.green_clr);
    private int mTextSize = 14;
    private TextView mTextView;
    public VerticalStepView(Context context)
    {
        this(context, null);
    }
    public VerticalStepView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }
    public VerticalStepView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.widget_vertical_stepsview, this);
        mStepsViewIndicator = (VerticalStepViewIndicator) rootView.findViewById(R.id.steps_indicator);
        mStepsViewIndicator.setOnDrawListener(this);
        mTextContainer = (RelativeLayout) rootView.findViewById(R.id.rl_text_container);}



    public void setStepViewIcons(Map<String, Integer> icons) {
        refreshStepViews(icons);
    }

    private void refreshStepViews(Map<String, Integer> icons) {
        mTextContainer.removeAllViews();

        for (String text : mTexts) {
            TextView stepView = new TextView(getContext());
            stepView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSize);
            stepView.setText(text);

            // Set icon based on text
            int iconResId = icons.containsKey(text) ? icons.get(text) : R.drawable.ic_track_order_pending;
            stepView.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);

            mTextContainer.addView(stepView);
        }
    }





    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public VerticalStepView setStepViewTexts(List<String> texts)
    {
        mTexts = texts;
        if(texts != null){
            mStepsViewIndicator.setStepNum(mTexts.size());
        }else{
            mStepsViewIndicator.setStepNum(0);
        }
        return this;
    }

    public VerticalStepView setStepsViewIndicatorComplectingPosition(int complectingPosition)
    {
        mComplectingPosition = complectingPosition;
        mStepsViewIndicator.setComplectingPosition(complectingPosition);
        return this;
    }

    public VerticalStepView setStepViewUnComplectedTextColor(int unComplectedTextColor)
    {
        mUnComplectedTextColor = unComplectedTextColor;
        return this;
    }

    public VerticalStepView setStepViewComplectedTextColor(int complectedTextColor)
    {
        this.mComplectedTextColor = complectedTextColor;
        return this;
    }

    public VerticalStepView setStepsViewIndicatorUnCompletedLineColor(int unCompletedLineColor)
    {
        mStepsViewIndicator.setUnCompletedLineColor(unCompletedLineColor);
        return this;
    }

    /**
     * 设置StepsViewIndicator完成线的颜色
     *
     * @param completedLineColor
     * @return
     */
    public VerticalStepView setStepsViewIndicatorCompletedLineColor(int completedLineColor)
    {
        mStepsViewIndicator.setCompletedLineColor(completedLineColor);
        return this;
    }

    /**
     * 设置StepsViewIndicator默认图片
     *
     * @param defaultIcon
     */
    public VerticalStepView setStepsViewIndicatorDefaultIcon(Drawable defaultIcon)
    {
        mStepsViewIndicator.setDefaultIcon(defaultIcon);
        return this;
    }

    /**
     * 设置StepsViewIndicator已完成图片
     *
     * @param completeIcon
     */
    public VerticalStepView setStepsViewIndicatorCompleteIcon(Drawable completeIcon)
    {
        mStepsViewIndicator.setCompleteIcon(completeIcon);
        return this;
    }

    /**
     * 设置StepsViewIndicator正在进行中的图片
     *
     * @param attentionIcon
     */
    public VerticalStepView setStepsViewIndicatorAttentionIcon(Drawable attentionIcon)
    {
        mStepsViewIndicator.setAttentionIcon(attentionIcon);
        return this;
    }

    /**
     * is reverse draw 是否倒序画
     *
     * @param isReverSe default is true
     * @return
     */
    public VerticalStepView reverseDraw(boolean isReverSe)
    {
        this.mStepsViewIndicator.reverseDraw(isReverSe);
        return this;
    }

    /**
     * set linePadding  proportion 设置线间距的比例系数
     *
     * @param linePaddingProportion
     * @return
     */
    public VerticalStepView setLinePaddingProportion(float linePaddingProportion)
    {
        this.mStepsViewIndicator.setIndicatorLinePaddingProportion(linePaddingProportion);
        return this;
    }


    /**
     * set textSize
     *
     * @param textSize
     * @return
     */
    public VerticalStepView setTextSize(int textSize)
    {
        if(textSize > 0)
        {
            mTextSize = textSize;
        }
        return this;
    }

    @Override
    public void ondrawIndicator()
    {
        if(mTextContainer != null)
        {
            mTextContainer.removeAllViews();//clear ViewGroup
            List<Float> complectedXPosition = mStepsViewIndicator.getCircleCenterPointPositionList();
            if(mTexts != null && complectedXPosition != null && complectedXPosition.size() > 0)
            {
                for(int i = 0; i < mTexts.size(); i++)
                {
                    mTextView = new TextView(getContext());
                    mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSize);
                    mTextView.setText(mTexts.get(i));
                    mTextView.setY(complectedXPosition.get(i) - mStepsViewIndicator.getCircleRadius() / 2);
                    mTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    if(i <= mComplectingPosition)
                    {
                        mTextView.setTypeface(null, Typeface.BOLD);
                        mTextView.setTextColor(mComplectedTextColor);
                    } else
                    {
                        mTextView.setTextColor(mUnComplectedTextColor);
                    }

                    mTextContainer.addView(mTextView);
                }
            }
        }
    }
}

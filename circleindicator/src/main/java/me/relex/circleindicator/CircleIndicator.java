package me.relex.circleindicator;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import static android.support.v4.view.ViewPager.OnPageChangeListener;

public class CircleIndicator extends LinearLayout implements OnPageChangeListener {

    private final static int DEFAULT_INDICATOR_WIDTH = 5;
    private ViewPager mViewpager;
    private OnPageChangeListener mViewPagerOnPageChangeListener;
    private int mIndicatorMargin;
    private int mIndicatorWidth;
    private int mIndicatorHeight;
    private int mAnimatorResId = R.animator.scale_with_alpha;
    private int mAnimatorReverseResId = -1;
    private int mIndicatorBackground = R.drawable.white_radius;
    private int mIndicatorUnselectedBackground = R.drawable.gray_radius;
    private int mCurrentPosition = 0;

    public CircleIndicator(Context context) {
        super(context);
        init(context, null);
    }

    public CircleIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER);
        handleTypedArray(context, attrs);
    }

    private void handleTypedArray(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray =
                    context.obtainStyledAttributes(attrs, R.styleable.CircleIndicator);
            mIndicatorWidth =
                    typedArray.getDimensionPixelSize(R.styleable.CircleIndicator_ci_width, -1);
            mIndicatorHeight =
                    typedArray.getDimensionPixelSize(R.styleable.CircleIndicator_ci_height, -1);
            mIndicatorMargin =
                    typedArray.getDimensionPixelSize(R.styleable.CircleIndicator_ci_margin, -1);

            mAnimatorResId = typedArray.getResourceId(R.styleable.CircleIndicator_ci_animator,
                    R.animator.scale_with_alpha);
            mAnimatorReverseResId =
                    typedArray.getResourceId(R.styleable.CircleIndicator_ci_animator_reverse, -1);
            mIndicatorBackground = typedArray.getResourceId(R.styleable.CircleIndicator_ci_drawable,
                    mIndicatorBackground);
            mIndicatorUnselectedBackground =
                    typedArray.getResourceId(R.styleable.CircleIndicator_ci_drawable_unselected,
                            mIndicatorUnselectedBackground);
            typedArray.recycle();
        }
        mIndicatorWidth =
                (mIndicatorWidth == -1) ? dip2px(DEFAULT_INDICATOR_WIDTH) : mIndicatorWidth;
        mIndicatorHeight =
                (mIndicatorHeight == -1) ? dip2px(DEFAULT_INDICATOR_WIDTH) : mIndicatorHeight;
        mIndicatorMargin =
                (mIndicatorMargin == -1) ? dip2px(DEFAULT_INDICATOR_WIDTH) : mIndicatorMargin;
    }

    public void setViewPager(ViewPager viewPager) {
        mViewpager = viewPager;
        mCurrentPosition = mViewpager.getCurrentItem();
        createIndicators(viewPager);
        mViewpager.setOnPageChangeListener(this);
        onPageSelected(mCurrentPosition);
    }

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        if (mViewpager == null) {
            throw new NullPointerException("can not find Viewpager , setViewPager first");
        }
        mViewPagerOnPageChangeListener = onPageChangeListener;
        mViewpager.setOnPageChangeListener(this);
    }

    @Override public void onPageScrolled(int position, float positionOffset,
            int positionOffsetPixels) {
        if (mViewPagerOnPageChangeListener != null) {
            mViewPagerOnPageChangeListener.onPageScrolled(position, positionOffset,
                positionOffsetPixels);
        }
    }

    @Override public void onPageSelected(int position) {
        if (mViewPagerOnPageChangeListener != null) {
            mViewPagerOnPageChangeListener.onPageSelected(position);
        }

        if(mCurrentPosition != position) {
            final View currentIndicator = getChildAt(mCurrentPosition);
            if(currentIndicator != null) {
                final Animator animatorOut = getAnimatorOut();
                animatorOut.setTarget(currentIndicator);
                animatorOut.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (mIndicatorUnselectedBackground != 0) {
                            currentIndicator.setBackgroundResource(mIndicatorUnselectedBackground);
                            currentIndicator.setAlpha(1.f);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animatorOut.start();
            }
        }

        View selectedIndicator = getChildAt(position);
        if(selectedIndicator != null) {
            selectedIndicator.setAlpha(0.5f);
            selectedIndicator.setBackgroundResource(mIndicatorBackground);
            final Animator animatorIn = getAnimatorIn();
            animatorIn.setTarget(selectedIndicator);
            animatorIn.start();
        }

        mCurrentPosition = position;
    }

    public Animator getAnimatorIn() {
        return AnimatorInflater.loadAnimator(getContext(), mAnimatorResId);
    }

    public Animator getAnimatorOut() {
        Animator animationIn;
        if(mAnimatorReverseResId == -1) {
             animationIn = getAnimatorIn();
            animationIn.setInterpolator(new ReverseInterpolator());
        } else {
            animationIn = AnimatorInflater.loadAnimator(getContext(), mAnimatorReverseResId);
        }
        return animationIn;
    }

    @Override public void onPageScrollStateChanged(int state) {
        if (mViewPagerOnPageChangeListener != null) {
            mViewPagerOnPageChangeListener.onPageScrollStateChanged(state);
        }
    }

    private void createIndicators(ViewPager viewPager) {
        removeAllViews();
        int count = viewPager.getAdapter().getCount();
        if (count <= 0) {
            return;
        }

        addIndicator(mIndicatorBackground);

        for (int i = 1; i < count; i++) {
            addIndicator(mIndicatorUnselectedBackground);
        }
    }

    private void addIndicator(@DrawableRes int backgroundDrawableId) {
        View Indicator = new View(getContext());
        Indicator.setBackgroundResource(backgroundDrawableId);
        addView(Indicator, mIndicatorWidth, mIndicatorHeight);
        LayoutParams lp = (LayoutParams) Indicator.getLayoutParams();
        lp.leftMargin = mIndicatorMargin;
        lp.rightMargin = mIndicatorMargin;
        Indicator.setLayoutParams(lp);
    }

    private class ReverseInterpolator implements Interpolator {
        @Override
        public float getInterpolation(float value) {
            return Math.abs(1.0f - value);
        }
    }

    public int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}

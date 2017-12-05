package android.support.v4.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;

public class SwipeRefreshLayout extends ViewGroup
{
  private static final int ALPHA_ANIMATION_DURATION = 300;
  private static final int ANIMATE_TO_START_DURATION = 200;
  private static final int ANIMATE_TO_TRIGGER_DURATION = 200;
  private static final int CIRCLE_BG_LIGHT = -328966;
  private static final int CIRCLE_DIAMETER = 40;
  private static final int CIRCLE_DIAMETER_LARGE = 56;
  private static final float DECELERATE_INTERPOLATION_FACTOR = 2.0F;
  public static final int DEFAULT = 1;
  private static final int DEFAULT_CIRCLE_TARGET = 64;
  private static final float DRAG_RATE = 0.5F;
  private static final int INVALID_POINTER = -1;
  public static final int LARGE = 0;
  private static final int[] LAYOUT_ATTRS = { 16842766 };
  private static final String LOG_TAG = SwipeRefreshLayout.class.getSimpleName();
  private static final int MAX_ALPHA = 255;
  private static final float MAX_PROGRESS_ANGLE = 0.8F;
  private static final int SCALE_DOWN_DURATION = 150;
  private static final int STARTING_PROGRESS_ALPHA = 76;
  private int mActivePointerId = -1;
  private Animation mAlphaMaxAnimation;
  private Animation mAlphaStartAnimation;
  private final Animation mAnimateToCorrectPosition = new Animation()
  {
    public void applyTransformation(float paramAnonymousFloat, Transformation paramAnonymousTransformation)
    {
      if (!SwipeRefreshLayout.this.mUsingCustomStart);
      for (int i = (int)(SwipeRefreshLayout.this.mSpinnerFinalOffset - Math.abs(SwipeRefreshLayout.this.mOriginalOffsetTop)); ; i = (int)SwipeRefreshLayout.this.mSpinnerFinalOffset)
      {
        int j = SwipeRefreshLayout.this.mFrom + (int)(paramAnonymousFloat * (i - SwipeRefreshLayout.this.mFrom)) - SwipeRefreshLayout.this.mCircleView.getTop();
        SwipeRefreshLayout.this.setTargetOffsetTopAndBottom(j, false);
        SwipeRefreshLayout.this.mProgress.setArrowScale(1.0F - paramAnonymousFloat);
        return;
      }
    }
  };
  private final Animation mAnimateToStartPosition = new Animation()
  {
    public void applyTransformation(float paramAnonymousFloat, Transformation paramAnonymousTransformation)
    {
      SwipeRefreshLayout.this.moveToStart(paramAnonymousFloat);
    }
  };
  private int mCircleHeight;
  private CircleImageView mCircleView;
  private int mCircleViewIndex = -1;
  private int mCircleWidth;
  private int mCurrentTargetOffsetTop;
  private final DecelerateInterpolator mDecelerateInterpolator;
  protected int mFrom;
  private float mInitialDownY;
  private float mInitialMotionY;
  private boolean mIsBeingDragged;
  private OnRefreshListener mListener;
  private int mMediumAnimationDuration;
  private boolean mNotify;
  private boolean mOriginalOffsetCalculated = false;
  protected int mOriginalOffsetTop;
  private MaterialProgressDrawable mProgress;
  private Animation.AnimationListener mRefreshListener = new Animation.AnimationListener()
  {
    public void onAnimationEnd(Animation paramAnonymousAnimation)
    {
      if (SwipeRefreshLayout.this.mRefreshing)
      {
        SwipeRefreshLayout.this.mProgress.setAlpha(255);
        SwipeRefreshLayout.this.mProgress.start();
        if ((SwipeRefreshLayout.this.mNotify) && (SwipeRefreshLayout.this.mListener != null))
          SwipeRefreshLayout.this.mListener.onRefresh();
      }
      while (true)
      {
        SwipeRefreshLayout.access$802(SwipeRefreshLayout.this, SwipeRefreshLayout.this.mCircleView.getTop());
        return;
        SwipeRefreshLayout.this.mProgress.stop();
        SwipeRefreshLayout.this.mCircleView.setVisibility(8);
        SwipeRefreshLayout.this.setColorViewAlpha(255);
        if (SwipeRefreshLayout.this.mScale)
          SwipeRefreshLayout.this.setAnimationProgress(0.0F);
        else
          SwipeRefreshLayout.this.setTargetOffsetTopAndBottom(SwipeRefreshLayout.this.mOriginalOffsetTop - SwipeRefreshLayout.this.mCurrentTargetOffsetTop, true);
      }
    }

    public void onAnimationRepeat(Animation paramAnonymousAnimation)
    {
    }

    public void onAnimationStart(Animation paramAnonymousAnimation)
    {
    }
  };
  private boolean mRefreshing = false;
  private boolean mReturningToStart;
  private boolean mScale;
  private Animation mScaleAnimation;
  private Animation mScaleDownAnimation;
  private Animation mScaleDownToStartAnimation;
  private float mSpinnerFinalOffset;
  private float mStartingScale;
  private View mTarget;
  private float mTotalDragDistance = -1.0F;
  private int mTouchSlop;
  private boolean mUsingCustomStart;

  public SwipeRefreshLayout(Context paramContext)
  {
    this(paramContext, null);
  }

  public SwipeRefreshLayout(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
    this.mTouchSlop = ViewConfiguration.get(paramContext).getScaledTouchSlop();
    this.mMediumAnimationDuration = getResources().getInteger(17694721);
    setWillNotDraw(false);
    this.mDecelerateInterpolator = new DecelerateInterpolator(2.0F);
    TypedArray localTypedArray = paramContext.obtainStyledAttributes(paramAttributeSet, LAYOUT_ATTRS);
    setEnabled(localTypedArray.getBoolean(0, true));
    localTypedArray.recycle();
    DisplayMetrics localDisplayMetrics = getResources().getDisplayMetrics();
    this.mCircleWidth = ((int)(40.0F * localDisplayMetrics.density));
    this.mCircleHeight = ((int)(40.0F * localDisplayMetrics.density));
    createProgressView();
    ViewCompat.setChildrenDrawingOrderEnabled(this, true);
    this.mSpinnerFinalOffset = (64.0F * localDisplayMetrics.density);
    this.mTotalDragDistance = this.mSpinnerFinalOffset;
  }

  private void animateOffsetToCorrectPosition(int paramInt, Animation.AnimationListener paramAnimationListener)
  {
    this.mFrom = paramInt;
    this.mAnimateToCorrectPosition.reset();
    this.mAnimateToCorrectPosition.setDuration(200L);
    this.mAnimateToCorrectPosition.setInterpolator(this.mDecelerateInterpolator);
    if (paramAnimationListener != null)
      this.mCircleView.setAnimationListener(paramAnimationListener);
    this.mCircleView.clearAnimation();
    this.mCircleView.startAnimation(this.mAnimateToCorrectPosition);
  }

  private void animateOffsetToStartPosition(int paramInt, Animation.AnimationListener paramAnimationListener)
  {
    if (this.mScale)
    {
      startScaleDownReturnToStartAnimation(paramInt, paramAnimationListener);
      return;
    }
    this.mFrom = paramInt;
    this.mAnimateToStartPosition.reset();
    this.mAnimateToStartPosition.setDuration(200L);
    this.mAnimateToStartPosition.setInterpolator(this.mDecelerateInterpolator);
    if (paramAnimationListener != null)
      this.mCircleView.setAnimationListener(paramAnimationListener);
    this.mCircleView.clearAnimation();
    this.mCircleView.startAnimation(this.mAnimateToStartPosition);
  }

  private void createProgressView()
  {
    this.mCircleView = new CircleImageView(getContext(), -328966, 20.0F);
    this.mProgress = new MaterialProgressDrawable(getContext(), this);
    this.mProgress.setBackgroundColor(-328966);
    this.mCircleView.setImageDrawable(this.mProgress);
    this.mCircleView.setVisibility(8);
    addView(this.mCircleView);
  }

  private void ensureTarget()
  {
    if (this.mTarget == null);
    for (int i = 0; ; i++)
      if (i < getChildCount())
      {
        View localView = getChildAt(i);
        if (!localView.equals(this.mCircleView))
          this.mTarget = localView;
      }
      else
      {
        return;
      }
  }

  private float getMotionEventY(MotionEvent paramMotionEvent, int paramInt)
  {
    int i = MotionEventCompat.findPointerIndex(paramMotionEvent, paramInt);
    if (i < 0)
      return -1.0F;
    return MotionEventCompat.getY(paramMotionEvent, i);
  }

  private boolean isAlphaUsedForScale()
  {
    return Build.VERSION.SDK_INT < 11;
  }

  private boolean isAnimationRunning(Animation paramAnimation)
  {
    return (paramAnimation != null) && (paramAnimation.hasStarted()) && (!paramAnimation.hasEnded());
  }

  private void moveToStart(float paramFloat)
  {
    setTargetOffsetTopAndBottom(this.mFrom + (int)(paramFloat * (this.mOriginalOffsetTop - this.mFrom)) - this.mCircleView.getTop(), false);
  }

  private void onSecondaryPointerUp(MotionEvent paramMotionEvent)
  {
    int i = MotionEventCompat.getActionIndex(paramMotionEvent);
    if (MotionEventCompat.getPointerId(paramMotionEvent, i) == this.mActivePointerId)
      if (i != 0)
        break label33;
    label33: for (int j = 1; ; j = 0)
    {
      this.mActivePointerId = MotionEventCompat.getPointerId(paramMotionEvent, j);
      return;
    }
  }

  private void setAnimationProgress(float paramFloat)
  {
    if (isAlphaUsedForScale())
    {
      setColorViewAlpha((int)(255.0F * paramFloat));
      return;
    }
    ViewCompat.setScaleX(this.mCircleView, paramFloat);
    ViewCompat.setScaleY(this.mCircleView, paramFloat);
  }

  private void setColorViewAlpha(int paramInt)
  {
    this.mCircleView.getBackground().setAlpha(paramInt);
    this.mProgress.setAlpha(paramInt);
  }

  private void setRefreshing(boolean paramBoolean1, boolean paramBoolean2)
  {
    if (this.mRefreshing != paramBoolean1)
    {
      this.mNotify = paramBoolean2;
      ensureTarget();
      this.mRefreshing = paramBoolean1;
      if (this.mRefreshing)
        animateOffsetToCorrectPosition(this.mCurrentTargetOffsetTop, this.mRefreshListener);
    }
    else
    {
      return;
    }
    startScaleDownAnimation(this.mRefreshListener);
  }

  private void setTargetOffsetTopAndBottom(int paramInt, boolean paramBoolean)
  {
    this.mCircleView.bringToFront();
    this.mCircleView.offsetTopAndBottom(paramInt);
    this.mCurrentTargetOffsetTop = this.mCircleView.getTop();
    if ((paramBoolean) && (Build.VERSION.SDK_INT < 11))
      invalidate();
  }

  private Animation startAlphaAnimation(final int paramInt1, final int paramInt2)
  {
    if ((this.mScale) && (isAlphaUsedForScale()))
      return null;
    Animation local4 = new Animation()
    {
      public void applyTransformation(float paramAnonymousFloat, Transformation paramAnonymousTransformation)
      {
        SwipeRefreshLayout.this.mProgress.setAlpha((int)(paramInt1 + paramAnonymousFloat * (paramInt2 - paramInt1)));
      }
    };
    local4.setDuration(300L);
    this.mCircleView.setAnimationListener(null);
    this.mCircleView.clearAnimation();
    this.mCircleView.startAnimation(local4);
    return local4;
  }

  private void startProgressAlphaMaxAnimation()
  {
    this.mAlphaMaxAnimation = startAlphaAnimation(this.mProgress.getAlpha(), 255);
  }

  private void startProgressAlphaStartAnimation()
  {
    this.mAlphaStartAnimation = startAlphaAnimation(this.mProgress.getAlpha(), 76);
  }

  private void startScaleDownAnimation(Animation.AnimationListener paramAnimationListener)
  {
    this.mScaleDownAnimation = new Animation()
    {
      public void applyTransformation(float paramAnonymousFloat, Transformation paramAnonymousTransformation)
      {
        SwipeRefreshLayout.this.setAnimationProgress(1.0F - paramAnonymousFloat);
      }
    };
    this.mScaleDownAnimation.setDuration(150L);
    this.mCircleView.setAnimationListener(paramAnimationListener);
    this.mCircleView.clearAnimation();
    this.mCircleView.startAnimation(this.mScaleDownAnimation);
  }

  private void startScaleDownReturnToStartAnimation(int paramInt, Animation.AnimationListener paramAnimationListener)
  {
    this.mFrom = paramInt;
    if (isAlphaUsedForScale());
    for (this.mStartingScale = this.mProgress.getAlpha(); ; this.mStartingScale = ViewCompat.getScaleX(this.mCircleView))
    {
      this.mScaleDownToStartAnimation = new Animation()
      {
        public void applyTransformation(float paramAnonymousFloat, Transformation paramAnonymousTransformation)
        {
          float f = SwipeRefreshLayout.this.mStartingScale + paramAnonymousFloat * -SwipeRefreshLayout.this.mStartingScale;
          SwipeRefreshLayout.this.setAnimationProgress(f);
          SwipeRefreshLayout.this.moveToStart(paramAnonymousFloat);
        }
      };
      this.mScaleDownToStartAnimation.setDuration(150L);
      if (paramAnimationListener != null)
        this.mCircleView.setAnimationListener(paramAnimationListener);
      this.mCircleView.clearAnimation();
      this.mCircleView.startAnimation(this.mScaleDownToStartAnimation);
      return;
    }
  }

  private void startScaleUpAnimation(Animation.AnimationListener paramAnimationListener)
  {
    this.mCircleView.setVisibility(0);
    if (Build.VERSION.SDK_INT >= 11)
      this.mProgress.setAlpha(255);
    this.mScaleAnimation = new Animation()
    {
      public void applyTransformation(float paramAnonymousFloat, Transformation paramAnonymousTransformation)
      {
        SwipeRefreshLayout.this.setAnimationProgress(paramAnonymousFloat);
      }
    };
    this.mScaleAnimation.setDuration(this.mMediumAnimationDuration);
    if (paramAnimationListener != null)
      this.mCircleView.setAnimationListener(paramAnimationListener);
    this.mCircleView.clearAnimation();
    this.mCircleView.startAnimation(this.mScaleAnimation);
  }

  public boolean canChildScrollUp()
  {
    if (Build.VERSION.SDK_INT < 14)
    {
      if ((this.mTarget instanceof AbsListView))
      {
        AbsListView localAbsListView = (AbsListView)this.mTarget;
        return (localAbsListView.getChildCount() > 0) && ((localAbsListView.getFirstVisiblePosition() > 0) || (localAbsListView.getChildAt(0).getTop() < localAbsListView.getPaddingTop()));
      }
      boolean bool;
      if (!ViewCompat.canScrollVertically(this.mTarget, -1))
      {
        int i = this.mTarget.getScrollY();
        bool = false;
        if (i <= 0);
      }
      else
      {
        bool = true;
      }
      return bool;
    }
    return ViewCompat.canScrollVertically(this.mTarget, -1);
  }

  protected int getChildDrawingOrder(int paramInt1, int paramInt2)
  {
    if (this.mCircleViewIndex < 0);
    do
    {
      return paramInt2;
      if (paramInt2 == paramInt1 - 1)
        return this.mCircleViewIndex;
    }
    while (paramInt2 < this.mCircleViewIndex);
    return paramInt2 + 1;
  }

  public int getProgressCircleDiameter()
  {
    if (this.mCircleView != null)
      return this.mCircleView.getMeasuredHeight();
    return 0;
  }

  public boolean isRefreshing()
  {
    return this.mRefreshing;
  }

  public boolean onInterceptTouchEvent(MotionEvent paramMotionEvent)
  {
    ensureTarget();
    int i = MotionEventCompat.getActionMasked(paramMotionEvent);
    if ((this.mReturningToStart) && (i == 0))
      this.mReturningToStart = false;
    if ((!isEnabled()) || (this.mReturningToStart) || (canChildScrollUp()) || (this.mRefreshing))
      return false;
    switch (i)
    {
    case 4:
    case 5:
    default:
    case 0:
    case 2:
    case 6:
    case 1:
    case 3:
    }
    while (true)
    {
      return this.mIsBeingDragged;
      setTargetOffsetTopAndBottom(this.mOriginalOffsetTop - this.mCircleView.getTop(), true);
      this.mActivePointerId = MotionEventCompat.getPointerId(paramMotionEvent, 0);
      this.mIsBeingDragged = false;
      float f2 = getMotionEventY(paramMotionEvent, this.mActivePointerId);
      if (f2 == -1.0F)
        break;
      this.mInitialDownY = f2;
      continue;
      if (this.mActivePointerId == -1)
      {
        Log.e(LOG_TAG, "Got ACTION_MOVE event but don't have an active pointer id.");
        return false;
      }
      float f1 = getMotionEventY(paramMotionEvent, this.mActivePointerId);
      if (f1 == -1.0F)
        break;
      if ((f1 - this.mInitialDownY > this.mTouchSlop) && (!this.mIsBeingDragged))
      {
        this.mInitialMotionY = (this.mInitialDownY + this.mTouchSlop);
        this.mIsBeingDragged = true;
        this.mProgress.setAlpha(76);
        continue;
        onSecondaryPointerUp(paramMotionEvent);
        continue;
        this.mIsBeingDragged = false;
        this.mActivePointerId = -1;
      }
    }
  }

  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    int i = getMeasuredWidth();
    int j = getMeasuredHeight();
    if (getChildCount() == 0);
    do
    {
      return;
      if (this.mTarget == null)
        ensureTarget();
    }
    while (this.mTarget == null);
    View localView = this.mTarget;
    int k = getPaddingLeft();
    int m = getPaddingTop();
    int n = i - getPaddingLeft() - getPaddingRight();
    int i1 = j - getPaddingTop() - getPaddingBottom();
    localView.layout(k, m, k + n, m + i1);
    int i2 = this.mCircleView.getMeasuredWidth();
    int i3 = this.mCircleView.getMeasuredHeight();
    this.mCircleView.layout(i / 2 - i2 / 2, this.mCurrentTargetOffsetTop, i / 2 + i2 / 2, i3 + this.mCurrentTargetOffsetTop);
  }

  public void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(paramInt1, paramInt2);
    if (this.mTarget == null)
      ensureTarget();
    if (this.mTarget == null);
    while (true)
    {
      return;
      this.mTarget.measure(View.MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), 1073741824), View.MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), 1073741824));
      this.mCircleView.measure(View.MeasureSpec.makeMeasureSpec(this.mCircleWidth, 1073741824), View.MeasureSpec.makeMeasureSpec(this.mCircleHeight, 1073741824));
      if ((!this.mUsingCustomStart) && (!this.mOriginalOffsetCalculated))
      {
        this.mOriginalOffsetCalculated = true;
        int j = -this.mCircleView.getMeasuredHeight();
        this.mOriginalOffsetTop = j;
        this.mCurrentTargetOffsetTop = j;
      }
      this.mCircleViewIndex = -1;
      for (int i = 0; i < getChildCount(); i++)
        if (getChildAt(i) == this.mCircleView)
        {
          this.mCircleViewIndex = i;
          return;
        }
    }
  }

  public boolean onTouchEvent(MotionEvent paramMotionEvent)
  {
    int i = MotionEventCompat.getActionMasked(paramMotionEvent);
    if ((this.mReturningToStart) && (i == 0))
      this.mReturningToStart = false;
    if ((!isEnabled()) || (this.mReturningToStart) || (canChildScrollUp()))
      return false;
    switch (i)
    {
    case 4:
    default:
    case 0:
    case 2:
    case 5:
    case 6:
      while (true)
      {
        return true;
        this.mActivePointerId = MotionEventCompat.getPointerId(paramMotionEvent, 0);
        this.mIsBeingDragged = false;
        continue;
        int j = MotionEventCompat.findPointerIndex(paramMotionEvent, this.mActivePointerId);
        if (j < 0)
        {
          Log.e(LOG_TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
          return false;
        }
        float f2 = 0.5F * (MotionEventCompat.getY(paramMotionEvent, j) - this.mInitialMotionY);
        if (this.mIsBeingDragged)
        {
          this.mProgress.showArrow(true);
          float f3 = f2 / this.mTotalDragDistance;
          if (f3 < 0.0F)
            return false;
          float f4 = Math.min(1.0F, Math.abs(f3));
          float f5 = 5.0F * (float)Math.max(f4 - 0.4D, 0.0D) / 3.0F;
          float f6 = Math.abs(f2) - this.mTotalDragDistance;
          float f7;
          label247: float f9;
          int k;
          if (this.mUsingCustomStart)
          {
            f7 = this.mSpinnerFinalOffset - this.mOriginalOffsetTop;
            float f8 = Math.max(0.0F, Math.min(f6, 2.0F * f7) / f7);
            f9 = 2.0F * (float)(f8 / 4.0F - Math.pow(f8 / 4.0F, 2.0D));
            float f10 = 2.0F * (f7 * f9);
            k = this.mOriginalOffsetTop + (int)(f10 + f7 * f4);
            if (this.mCircleView.getVisibility() != 0)
              this.mCircleView.setVisibility(0);
            if (!this.mScale)
            {
              ViewCompat.setScaleX(this.mCircleView, 1.0F);
              ViewCompat.setScaleY(this.mCircleView, 1.0F);
            }
            if (f2 >= this.mTotalDragDistance)
              break label500;
            if (this.mScale)
              setAnimationProgress(f2 / this.mTotalDragDistance);
            if ((this.mProgress.getAlpha() > 76) && (!isAnimationRunning(this.mAlphaStartAnimation)))
              startProgressAlphaStartAnimation();
            float f12 = f5 * 0.8F;
            this.mProgress.setStartEndTrim(0.0F, Math.min(0.8F, f12));
            this.mProgress.setArrowScale(Math.min(1.0F, f5));
          }
          while (true)
          {
            float f11 = 0.5F * (-0.25F + 0.4F * f5 + 2.0F * f9);
            this.mProgress.setProgressRotation(f11);
            setTargetOffsetTopAndBottom(k - this.mCurrentTargetOffsetTop, true);
            break;
            f7 = this.mSpinnerFinalOffset;
            break label247;
            label500: if ((this.mProgress.getAlpha() < 255) && (!isAnimationRunning(this.mAlphaMaxAnimation)))
              startProgressAlphaMaxAnimation();
          }
          this.mActivePointerId = MotionEventCompat.getPointerId(paramMotionEvent, MotionEventCompat.getActionIndex(paramMotionEvent));
          continue;
          onSecondaryPointerUp(paramMotionEvent);
        }
      }
    case 1:
    case 3:
    }
    if (this.mActivePointerId == -1)
    {
      if (i == 1)
        Log.e(LOG_TAG, "Got ACTION_UP event but don't have an active pointer id.");
      return false;
    }
    float f1 = 0.5F * (MotionEventCompat.getY(paramMotionEvent, MotionEventCompat.findPointerIndex(paramMotionEvent, this.mActivePointerId)) - this.mInitialMotionY);
    this.mIsBeingDragged = false;
    if (f1 > this.mTotalDragDistance)
      setRefreshing(true, true);
    while (true)
    {
      this.mActivePointerId = -1;
      return false;
      this.mRefreshing = false;
      this.mProgress.setStartEndTrim(0.0F, 0.0F);
      boolean bool = this.mScale;
      Animation.AnimationListener local5 = null;
      if (!bool)
        local5 = new Animation.AnimationListener()
        {
          public void onAnimationEnd(Animation paramAnonymousAnimation)
          {
            if (!SwipeRefreshLayout.this.mScale)
              SwipeRefreshLayout.this.startScaleDownAnimation(null);
          }

          public void onAnimationRepeat(Animation paramAnonymousAnimation)
          {
          }

          public void onAnimationStart(Animation paramAnonymousAnimation)
          {
          }
        };
      animateOffsetToStartPosition(this.mCurrentTargetOffsetTop, local5);
      this.mProgress.showArrow(false);
    }
  }

  public void requestDisallowInterceptTouchEvent(boolean paramBoolean)
  {
  }

  @Deprecated
  public void setColorScheme(int[] paramArrayOfInt)
  {
    setColorSchemeResources(paramArrayOfInt);
  }

  public void setColorSchemeColors(int[] paramArrayOfInt)
  {
    ensureTarget();
    this.mProgress.setColorSchemeColors(paramArrayOfInt);
  }

  public void setColorSchemeResources(int[] paramArrayOfInt)
  {
    Resources localResources = getResources();
    int[] arrayOfInt = new int[paramArrayOfInt.length];
    for (int i = 0; i < paramArrayOfInt.length; i++)
      arrayOfInt[i] = localResources.getColor(paramArrayOfInt[i]);
    setColorSchemeColors(arrayOfInt);
  }

  public void setDistanceToTriggerSync(int paramInt)
  {
    this.mTotalDragDistance = paramInt;
  }

  public void setOnRefreshListener(OnRefreshListener paramOnRefreshListener)
  {
    this.mListener = paramOnRefreshListener;
  }

  @Deprecated
  public void setProgressBackgroundColor(int paramInt)
  {
    setProgressBackgroundColorSchemeResource(paramInt);
  }

  public void setProgressBackgroundColorSchemeColor(int paramInt)
  {
    this.mCircleView.setBackgroundColor(paramInt);
    this.mProgress.setBackgroundColor(paramInt);
  }

  public void setProgressBackgroundColorSchemeResource(int paramInt)
  {
    setProgressBackgroundColorSchemeColor(getResources().getColor(paramInt));
  }

  public void setProgressViewEndTarget(boolean paramBoolean, int paramInt)
  {
    this.mSpinnerFinalOffset = paramInt;
    this.mScale = paramBoolean;
    this.mCircleView.invalidate();
  }

  public void setProgressViewOffset(boolean paramBoolean, int paramInt1, int paramInt2)
  {
    this.mScale = paramBoolean;
    this.mCircleView.setVisibility(8);
    this.mCurrentTargetOffsetTop = paramInt1;
    this.mOriginalOffsetTop = paramInt1;
    this.mSpinnerFinalOffset = paramInt2;
    this.mUsingCustomStart = true;
    this.mCircleView.invalidate();
  }

  public void setRefreshing(boolean paramBoolean)
  {
    if ((paramBoolean) && (this.mRefreshing != paramBoolean))
    {
      this.mRefreshing = paramBoolean;
      if (!this.mUsingCustomStart);
      for (int i = (int)(this.mSpinnerFinalOffset + this.mOriginalOffsetTop); ; i = (int)this.mSpinnerFinalOffset)
      {
        setTargetOffsetTopAndBottom(i - this.mCurrentTargetOffsetTop, true);
        this.mNotify = false;
        startScaleUpAnimation(this.mRefreshListener);
        return;
      }
    }
    setRefreshing(paramBoolean, false);
  }

  public void setSize(int paramInt)
  {
    if ((paramInt != 0) && (paramInt != 1))
      return;
    DisplayMetrics localDisplayMetrics = getResources().getDisplayMetrics();
    int j;
    if (paramInt == 0)
    {
      j = (int)(56.0F * localDisplayMetrics.density);
      this.mCircleWidth = j;
    }
    int i;
    for (this.mCircleHeight = j; ; this.mCircleHeight = i)
    {
      this.mCircleView.setImageDrawable(null);
      this.mProgress.updateSizes(paramInt);
      this.mCircleView.setImageDrawable(this.mProgress);
      return;
      i = (int)(40.0F * localDisplayMetrics.density);
      this.mCircleWidth = i;
    }
  }

  public static abstract interface OnRefreshListener
  {
    public abstract void onRefresh();
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.widget.SwipeRefreshLayout
 * JD-Core Version:    0.6.2
 */
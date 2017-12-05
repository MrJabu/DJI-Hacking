package android.support.v4.widget;

import android.content.res.Resources;
import android.os.SystemClock;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

public abstract class AutoScrollHelper
  implements View.OnTouchListener
{
  private static final int DEFAULT_ACTIVATION_DELAY = 0;
  private static final int DEFAULT_EDGE_TYPE = 1;
  private static final float DEFAULT_MAXIMUM_EDGE = 3.4028235E+38F;
  private static final int DEFAULT_MAXIMUM_VELOCITY_DIPS = 1575;
  private static final int DEFAULT_MINIMUM_VELOCITY_DIPS = 315;
  private static final int DEFAULT_RAMP_DOWN_DURATION = 500;
  private static final int DEFAULT_RAMP_UP_DURATION = 500;
  private static final float DEFAULT_RELATIVE_EDGE = 0.2F;
  private static final float DEFAULT_RELATIVE_VELOCITY = 1.0F;
  public static final int EDGE_TYPE_INSIDE = 0;
  public static final int EDGE_TYPE_INSIDE_EXTEND = 1;
  public static final int EDGE_TYPE_OUTSIDE = 2;
  private static final int HORIZONTAL = 0;
  public static final float NO_MAX = 3.4028235E+38F;
  public static final float NO_MIN = 0.0F;
  public static final float RELATIVE_UNSPECIFIED = 0.0F;
  private static final int VERTICAL = 1;
  private int mActivationDelay;
  private boolean mAlreadyDelayed;
  private boolean mAnimating;
  private final Interpolator mEdgeInterpolator = new AccelerateInterpolator();
  private int mEdgeType;
  private boolean mEnabled;
  private boolean mExclusive;
  private float[] mMaximumEdges = { 3.4028235E+38F, 3.4028235E+38F };
  private float[] mMaximumVelocity = { 3.4028235E+38F, 3.4028235E+38F };
  private float[] mMinimumVelocity = { 0.0F, 0.0F };
  private boolean mNeedsCancel;
  private boolean mNeedsReset;
  private float[] mRelativeEdges = { 0.0F, 0.0F };
  private float[] mRelativeVelocity = { 0.0F, 0.0F };
  private Runnable mRunnable;
  private final ClampedScroller mScroller = new ClampedScroller();
  private final View mTarget;

  public AutoScrollHelper(View paramView)
  {
    this.mTarget = paramView;
    DisplayMetrics localDisplayMetrics = Resources.getSystem().getDisplayMetrics();
    int i = (int)(0.5F + 1575.0F * localDisplayMetrics.density);
    int j = (int)(0.5F + 315.0F * localDisplayMetrics.density);
    setMaximumVelocity(i, i);
    setMinimumVelocity(j, j);
    setEdgeType(1);
    setMaximumEdges(3.4028235E+38F, 3.4028235E+38F);
    setRelativeEdges(0.2F, 0.2F);
    setRelativeVelocity(1.0F, 1.0F);
    setActivationDelay(DEFAULT_ACTIVATION_DELAY);
    setRampUpDuration(500);
    setRampDownDuration(500);
  }

  private void cancelTargetTouch()
  {
    long l = SystemClock.uptimeMillis();
    MotionEvent localMotionEvent = MotionEvent.obtain(l, l, 3, 0.0F, 0.0F, 0);
    this.mTarget.onTouchEvent(localMotionEvent);
    localMotionEvent.recycle();
  }

  private float computeTargetVelocity(int paramInt, float paramFloat1, float paramFloat2, float paramFloat3)
  {
    float f1 = getEdgeValue(this.mRelativeEdges[paramInt], paramFloat2, this.mMaximumEdges[paramInt], paramFloat1);
    if (f1 == 0.0F)
      return 0.0F;
    float f2 = this.mRelativeVelocity[paramInt];
    float f3 = this.mMinimumVelocity[paramInt];
    float f4 = this.mMaximumVelocity[paramInt];
    float f5 = f2 * paramFloat3;
    if (f1 > 0.0F)
      return constrain(f1 * f5, f3, f4);
    return -constrain(f5 * -f1, f3, f4);
  }

  private static float constrain(float paramFloat1, float paramFloat2, float paramFloat3)
  {
    if (paramFloat1 > paramFloat3)
      return paramFloat3;
    if (paramFloat1 < paramFloat2)
      return paramFloat2;
    return paramFloat1;
  }

  private static int constrain(int paramInt1, int paramInt2, int paramInt3)
  {
    if (paramInt1 > paramInt3)
      return paramInt3;
    if (paramInt1 < paramInt2)
      return paramInt2;
    return paramInt1;
  }

  private float constrainEdgeValue(float paramFloat1, float paramFloat2)
  {
    if (paramFloat2 == 0.0F);
    do
    {
      do
      {
        do
        {
          return 0.0F;
          switch (this.mEdgeType)
          {
          default:
            return 0.0F;
          case 0:
          case 1:
          case 2:
          }
        }
        while (paramFloat1 >= paramFloat2);
        if (paramFloat1 >= 0.0F)
          return 1.0F - paramFloat1 / paramFloat2;
      }
      while ((!this.mAnimating) || (this.mEdgeType != 1));
      return 1.0F;
    }
    while (paramFloat1 >= 0.0F);
    return paramFloat1 / -paramFloat2;
  }

  private float getEdgeValue(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4)
  {
    float f1 = constrain(paramFloat1 * paramFloat2, 0.0F, paramFloat3);
    float f2 = constrainEdgeValue(paramFloat4, f1);
    float f3 = constrainEdgeValue(paramFloat2 - paramFloat4, f1) - f2;
    if (f3 < 0.0F);
    for (float f5 = -this.mEdgeInterpolator.getInterpolation(-f3); ; f5 = this.mEdgeInterpolator.getInterpolation(f3))
    {
      float f4 = constrain(f5, -1.0F, 1.0F);
      boolean bool;
      do
      {
        return f4;
        bool = f3 < 0.0F;
        f4 = 0.0F;
      }
      while (!bool);
    }
  }

  private void requestStop()
  {
    if (this.mNeedsReset)
    {
      this.mAnimating = false;
      return;
    }
    this.mScroller.requestStop();
  }

  private boolean shouldAnimate()
  {
    ClampedScroller localClampedScroller = this.mScroller;
    int i = localClampedScroller.getVerticalDirection();
    int j = localClampedScroller.getHorizontalDirection();
    return ((i != 0) && (canTargetScrollVertically(i))) || ((j != 0) && (canTargetScrollHorizontally(j)));
  }

  private void startAnimating()
  {
    if (this.mRunnable == null)
      this.mRunnable = new ScrollAnimationRunnable(null);
    this.mAnimating = true;
    this.mNeedsReset = true;
    if ((!this.mAlreadyDelayed) && (this.mActivationDelay > 0))
      ViewCompat.postOnAnimationDelayed(this.mTarget, this.mRunnable, this.mActivationDelay);
    while (true)
    {
      this.mAlreadyDelayed = true;
      return;
      this.mRunnable.run();
    }
  }

  public abstract boolean canTargetScrollHorizontally(int paramInt);

  public abstract boolean canTargetScrollVertically(int paramInt);

  public boolean isEnabled()
  {
    return this.mEnabled;
  }

  public boolean isExclusive()
  {
    return this.mExclusive;
  }

  public boolean onTouch(View paramView, MotionEvent paramMotionEvent)
  {
    int i = 1;
    if (!this.mEnabled)
      return false;
    switch (MotionEventCompat.getActionMasked(paramMotionEvent))
    {
    default:
      if ((!this.mExclusive) || (!this.mAnimating))
        break;
    case 0:
    case 2:
    case 1:
    case 3:
    }
    while (true)
    {
      return i;
      this.mNeedsCancel = i;
      this.mAlreadyDelayed = false;
      float f1 = computeTargetVelocity(0, paramMotionEvent.getX(), paramView.getWidth(), this.mTarget.getWidth());
      float f2 = computeTargetVelocity(i, paramMotionEvent.getY(), paramView.getHeight(), this.mTarget.getHeight());
      this.mScroller.setTargetVelocity(f1, f2);
      if ((this.mAnimating) || (!shouldAnimate()))
        break;
      startAnimating();
      break;
      requestStop();
      break;
      int j = 0;
    }
  }

  public abstract void scrollTargetBy(int paramInt1, int paramInt2);

  public AutoScrollHelper setActivationDelay(int paramInt)
  {
    this.mActivationDelay = paramInt;
    return this;
  }

  public AutoScrollHelper setEdgeType(int paramInt)
  {
    this.mEdgeType = paramInt;
    return this;
  }

  public AutoScrollHelper setEnabled(boolean paramBoolean)
  {
    if ((this.mEnabled) && (!paramBoolean))
      requestStop();
    this.mEnabled = paramBoolean;
    return this;
  }

  public AutoScrollHelper setExclusive(boolean paramBoolean)
  {
    this.mExclusive = paramBoolean;
    return this;
  }

  public AutoScrollHelper setMaximumEdges(float paramFloat1, float paramFloat2)
  {
    this.mMaximumEdges[0] = paramFloat1;
    this.mMaximumEdges[1] = paramFloat2;
    return this;
  }

  public AutoScrollHelper setMaximumVelocity(float paramFloat1, float paramFloat2)
  {
    this.mMaximumVelocity[0] = (paramFloat1 / 1000.0F);
    this.mMaximumVelocity[1] = (paramFloat2 / 1000.0F);
    return this;
  }

  public AutoScrollHelper setMinimumVelocity(float paramFloat1, float paramFloat2)
  {
    this.mMinimumVelocity[0] = (paramFloat1 / 1000.0F);
    this.mMinimumVelocity[1] = (paramFloat2 / 1000.0F);
    return this;
  }

  public AutoScrollHelper setRampDownDuration(int paramInt)
  {
    this.mScroller.setRampDownDuration(paramInt);
    return this;
  }

  public AutoScrollHelper setRampUpDuration(int paramInt)
  {
    this.mScroller.setRampUpDuration(paramInt);
    return this;
  }

  public AutoScrollHelper setRelativeEdges(float paramFloat1, float paramFloat2)
  {
    this.mRelativeEdges[0] = paramFloat1;
    this.mRelativeEdges[1] = paramFloat2;
    return this;
  }

  public AutoScrollHelper setRelativeVelocity(float paramFloat1, float paramFloat2)
  {
    this.mRelativeVelocity[0] = (paramFloat1 / 1000.0F);
    this.mRelativeVelocity[1] = (paramFloat2 / 1000.0F);
    return this;
  }

  private static class ClampedScroller
  {
    private long mDeltaTime = 0L;
    private int mDeltaX = 0;
    private int mDeltaY = 0;
    private int mEffectiveRampDown;
    private int mRampDownDuration;
    private int mRampUpDuration;
    private long mStartTime = -9223372036854775808L;
    private long mStopTime = -1L;
    private float mStopValue;
    private float mTargetVelocityX;
    private float mTargetVelocityY;

    private float getValueAt(long paramLong)
    {
      if (paramLong < this.mStartTime)
        return 0.0F;
      if ((this.mStopTime < 0L) || (paramLong < this.mStopTime))
        return 0.5F * AutoScrollHelper.constrain((float)(paramLong - this.mStartTime) / this.mRampUpDuration, 0.0F, 1.0F);
      long l = paramLong - this.mStopTime;
      return 1.0F - this.mStopValue + this.mStopValue * AutoScrollHelper.constrain((float)l / this.mEffectiveRampDown, 0.0F, 1.0F);
    }

    private float interpolateValue(float paramFloat)
    {
      return paramFloat * (-4.0F * paramFloat) + 4.0F * paramFloat;
    }

    public void computeScrollDelta()
    {
      if (this.mDeltaTime == 0L)
        throw new RuntimeException("Cannot compute scroll delta before calling start()");
      long l1 = AnimationUtils.currentAnimationTimeMillis();
      float f = interpolateValue(getValueAt(l1));
      long l2 = l1 - this.mDeltaTime;
      this.mDeltaTime = l1;
      this.mDeltaX = ((int)(f * (float)l2 * this.mTargetVelocityX));
      this.mDeltaY = ((int)(f * (float)l2 * this.mTargetVelocityY));
    }

    public int getDeltaX()
    {
      return this.mDeltaX;
    }

    public int getDeltaY()
    {
      return this.mDeltaY;
    }

    public int getHorizontalDirection()
    {
      return (int)(this.mTargetVelocityX / Math.abs(this.mTargetVelocityX));
    }

    public int getVerticalDirection()
    {
      return (int)(this.mTargetVelocityY / Math.abs(this.mTargetVelocityY));
    }

    public boolean isFinished()
    {
      return (this.mStopTime > 0L) && (AnimationUtils.currentAnimationTimeMillis() > this.mStopTime + this.mEffectiveRampDown);
    }

    public void requestStop()
    {
      long l = AnimationUtils.currentAnimationTimeMillis();
      this.mEffectiveRampDown = AutoScrollHelper.constrain((int)(l - this.mStartTime), 0, this.mRampDownDuration);
      this.mStopValue = getValueAt(l);
      this.mStopTime = l;
    }

    public void setRampDownDuration(int paramInt)
    {
      this.mRampDownDuration = paramInt;
    }

    public void setRampUpDuration(int paramInt)
    {
      this.mRampUpDuration = paramInt;
    }

    public void setTargetVelocity(float paramFloat1, float paramFloat2)
    {
      this.mTargetVelocityX = paramFloat1;
      this.mTargetVelocityY = paramFloat2;
    }

    public void start()
    {
      this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
      this.mStopTime = -1L;
      this.mDeltaTime = this.mStartTime;
      this.mStopValue = 0.5F;
      this.mDeltaX = 0;
      this.mDeltaY = 0;
    }
  }

  private class ScrollAnimationRunnable
    implements Runnable
  {
    private ScrollAnimationRunnable()
    {
    }

    public void run()
    {
      if (!AutoScrollHelper.this.mAnimating)
        return;
      if (AutoScrollHelper.this.mNeedsReset)
      {
        AutoScrollHelper.access$202(AutoScrollHelper.this, false);
        AutoScrollHelper.this.mScroller.start();
      }
      AutoScrollHelper.ClampedScroller localClampedScroller = AutoScrollHelper.this.mScroller;
      if ((localClampedScroller.isFinished()) || (!AutoScrollHelper.this.shouldAnimate()))
      {
        AutoScrollHelper.access$102(AutoScrollHelper.this, false);
        return;
      }
      if (AutoScrollHelper.this.mNeedsCancel)
      {
        AutoScrollHelper.access$502(AutoScrollHelper.this, false);
        AutoScrollHelper.this.cancelTargetTouch();
      }
      localClampedScroller.computeScrollDelta();
      int i = localClampedScroller.getDeltaX();
      int j = localClampedScroller.getDeltaY();
      AutoScrollHelper.this.scrollTargetBy(i, j);
      ViewCompat.postOnAnimation(AutoScrollHelper.this.mTarget, this);
    }
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.widget.AutoScrollHelper
 * JD-Core Version:    0.6.2
 */
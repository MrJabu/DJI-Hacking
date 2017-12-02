package android.support.v4.widget;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import java.util.Arrays;

public class ViewDragHelper
{
  private static final int BASE_SETTLE_DURATION = 256;
  public static final int DIRECTION_ALL = 3;
  public static final int DIRECTION_HORIZONTAL = 1;
  public static final int DIRECTION_VERTICAL = 2;
  public static final int EDGE_ALL = 15;
  public static final int EDGE_BOTTOM = 8;
  public static final int EDGE_LEFT = 1;
  public static final int EDGE_RIGHT = 2;
  private static final int EDGE_SIZE = 20;
  public static final int EDGE_TOP = 4;
  public static final int INVALID_POINTER = -1;
  private static final int MAX_SETTLE_DURATION = 600;
  public static final int STATE_DRAGGING = 1;
  public static final int STATE_IDLE = 0;
  public static final int STATE_SETTLING = 2;
  private static final String TAG = "ViewDragHelper";
  private static final Interpolator sInterpolator = new Interpolator()
  {
    public float getInterpolation(float paramAnonymousFloat)
    {
      float f = paramAnonymousFloat - 1.0F;
      return 1.0F + f * (f * (f * (f * f)));
    }
  };
  private int mActivePointerId = -1;
  private final Callback mCallback;
  private View mCapturedView;
  private int mDragState;
  private int[] mEdgeDragsInProgress;
  private int[] mEdgeDragsLocked;
  private int mEdgeSize;
  private int[] mInitialEdgesTouched;
  private float[] mInitialMotionX;
  private float[] mInitialMotionY;
  private float[] mLastMotionX;
  private float[] mLastMotionY;
  private float mMaxVelocity;
  private float mMinVelocity;
  private final ViewGroup mParentView;
  private int mPointersDown;
  private boolean mReleaseInProgress;
  private ScrollerCompat mScroller;
  private final Runnable mSetIdleRunnable = new Runnable()
  {
    public void run()
    {
      ViewDragHelper.this.setDragState(0);
    }
  };
  private int mTouchSlop;
  private int mTrackingEdges;
  private VelocityTracker mVelocityTracker;

  private ViewDragHelper(Context paramContext, ViewGroup paramViewGroup, Callback paramCallback)
  {
    if (paramViewGroup == null)
      throw new IllegalArgumentException("Parent view may not be null");
    if (paramCallback == null)
      throw new IllegalArgumentException("Callback may not be null");
    this.mParentView = paramViewGroup;
    this.mCallback = paramCallback;
    ViewConfiguration localViewConfiguration = ViewConfiguration.get(paramContext);
    this.mEdgeSize = ((int)(0.5F + 20.0F * paramContext.getResources().getDisplayMetrics().density));
    this.mTouchSlop = localViewConfiguration.getScaledTouchSlop();
    this.mMaxVelocity = localViewConfiguration.getScaledMaximumFlingVelocity();
    this.mMinVelocity = localViewConfiguration.getScaledMinimumFlingVelocity();
    this.mScroller = ScrollerCompat.create(paramContext, sInterpolator);
  }

  private boolean checkNewEdgeDrag(float paramFloat1, float paramFloat2, int paramInt1, int paramInt2)
  {
    float f1 = Math.abs(paramFloat1);
    float f2 = Math.abs(paramFloat2);
    if (((paramInt2 & this.mInitialEdgesTouched[paramInt1]) != paramInt2) || ((paramInt2 & this.mTrackingEdges) == 0) || ((paramInt2 & this.mEdgeDragsLocked[paramInt1]) == paramInt2) || ((paramInt2 & this.mEdgeDragsInProgress[paramInt1]) == paramInt2) || ((f1 <= this.mTouchSlop) && (f2 <= this.mTouchSlop)));
    do
    {
      return false;
      if ((f1 < 0.5F * f2) && (this.mCallback.onEdgeLock(paramInt2)))
      {
        int[] arrayOfInt = this.mEdgeDragsLocked;
        arrayOfInt[paramInt1] = (paramInt2 | arrayOfInt[paramInt1]);
        return false;
      }
    }
    while (((paramInt2 & this.mEdgeDragsInProgress[paramInt1]) != 0) || (f1 <= this.mTouchSlop));
    return true;
  }

  private boolean checkTouchSlop(View paramView, float paramFloat1, float paramFloat2)
  {
    boolean bool1 = true;
    if (paramView == null)
      bool1 = false;
    label28: label82: label88: 
    do
    {
      boolean bool3;
      do
      {
        return bool1;
        boolean bool2;
        if (this.mCallback.getViewHorizontalDragRange(paramView) > 0)
        {
          bool2 = bool1;
          if (this.mCallback.getViewVerticalDragRange(paramView) <= 0)
            break label82;
        }
        for (bool3 = bool1; ; bool3 = false)
        {
          if ((!bool2) || (!bool3))
            break label88;
          if (paramFloat1 * paramFloat1 + paramFloat2 * paramFloat2 > this.mTouchSlop * this.mTouchSlop)
            break;
          return false;
          bool2 = false;
          break label28;
        }
        if (!bool2)
          break;
      }
      while (Math.abs(paramFloat1) > this.mTouchSlop);
      return false;
      if (!bool3)
        break;
    }
    while (Math.abs(paramFloat2) > this.mTouchSlop);
    return false;
    return false;
  }

  private float clampMag(float paramFloat1, float paramFloat2, float paramFloat3)
  {
    float f = Math.abs(paramFloat1);
    if (f < paramFloat2)
      paramFloat3 = 0.0F;
    do
    {
      return paramFloat3;
      if (f <= paramFloat3)
        break;
    }
    while (paramFloat1 > 0.0F);
    return -paramFloat3;
    return paramFloat1;
  }

  private int clampMag(int paramInt1, int paramInt2, int paramInt3)
  {
    int i = Math.abs(paramInt1);
    if (i < paramInt2)
      paramInt3 = 0;
    do
    {
      return paramInt3;
      if (i <= paramInt3)
        break;
    }
    while (paramInt1 > 0);
    return -paramInt3;
    return paramInt1;
  }

  private void clearMotionHistory()
  {
    if (this.mInitialMotionX == null)
      return;
    Arrays.fill(this.mInitialMotionX, 0.0F);
    Arrays.fill(this.mInitialMotionY, 0.0F);
    Arrays.fill(this.mLastMotionX, 0.0F);
    Arrays.fill(this.mLastMotionY, 0.0F);
    Arrays.fill(this.mInitialEdgesTouched, 0);
    Arrays.fill(this.mEdgeDragsInProgress, 0);
    Arrays.fill(this.mEdgeDragsLocked, 0);
    this.mPointersDown = 0;
  }

  private void clearMotionHistory(int paramInt)
  {
    if (this.mInitialMotionX == null)
      return;
    this.mInitialMotionX[paramInt] = 0.0F;
    this.mInitialMotionY[paramInt] = 0.0F;
    this.mLastMotionX[paramInt] = 0.0F;
    this.mLastMotionY[paramInt] = 0.0F;
    this.mInitialEdgesTouched[paramInt] = 0;
    this.mEdgeDragsInProgress[paramInt] = 0;
    this.mEdgeDragsLocked[paramInt] = 0;
    this.mPointersDown &= (0xFFFFFFFF ^ 1 << paramInt);
  }

  private int computeAxisDuration(int paramInt1, int paramInt2, int paramInt3)
  {
    if (paramInt1 == 0)
      return 0;
    int i = this.mParentView.getWidth();
    int j = i / 2;
    float f1 = Math.min(1.0F, Math.abs(paramInt1) / i);
    float f2 = j + j * distanceInfluenceForSnapDuration(f1);
    int k = Math.abs(paramInt2);
    if (k > 0);
    for (int m = 4 * Math.round(1000.0F * Math.abs(f2 / k)); ; m = (int)(256.0F * (1.0F + Math.abs(paramInt1) / paramInt3)))
      return Math.min(m, 600);
  }

  private int computeSettleDuration(View paramView, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    int i = clampMag(paramInt3, (int)this.mMinVelocity, (int)this.mMaxVelocity);
    int j = clampMag(paramInt4, (int)this.mMinVelocity, (int)this.mMaxVelocity);
    int k = Math.abs(paramInt1);
    int m = Math.abs(paramInt2);
    int n = Math.abs(i);
    int i1 = Math.abs(j);
    int i2 = n + i1;
    int i3 = k + m;
    float f1;
    if (i != 0)
    {
      f1 = n / i2;
      if (j == 0)
        break label165;
    }
    label165: for (float f2 = i1 / i2; ; f2 = m / i3)
    {
      int i4 = computeAxisDuration(paramInt1, i, this.mCallback.getViewHorizontalDragRange(paramView));
      int i5 = computeAxisDuration(paramInt2, j, this.mCallback.getViewVerticalDragRange(paramView));
      return (int)(f1 * i4 + f2 * i5);
      f1 = k / i3;
      break;
    }
  }

  public static ViewDragHelper create(ViewGroup paramViewGroup, float paramFloat, Callback paramCallback)
  {
    ViewDragHelper localViewDragHelper = create(paramViewGroup, paramCallback);
    localViewDragHelper.mTouchSlop = ((int)(localViewDragHelper.mTouchSlop * (1.0F / paramFloat)));
    return localViewDragHelper;
  }

  public static ViewDragHelper create(ViewGroup paramViewGroup, Callback paramCallback)
  {
    return new ViewDragHelper(paramViewGroup.getContext(), paramViewGroup, paramCallback);
  }

  private void dispatchViewReleased(float paramFloat1, float paramFloat2)
  {
    this.mReleaseInProgress = true;
    this.mCallback.onViewReleased(this.mCapturedView, paramFloat1, paramFloat2);
    this.mReleaseInProgress = false;
    if (this.mDragState == 1)
      setDragState(0);
  }

  private float distanceInfluenceForSnapDuration(float paramFloat)
  {
    return (float)Math.sin((float)(0.47123891676382D * (paramFloat - 0.5F)));
  }

  private void dragTo(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    int i = paramInt1;
    int j = paramInt2;
    int k = this.mCapturedView.getLeft();
    int m = this.mCapturedView.getTop();
    if (paramInt3 != 0)
    {
      i = this.mCallback.clampViewPositionHorizontal(this.mCapturedView, paramInt1, paramInt3);
      this.mCapturedView.offsetLeftAndRight(i - k);
    }
    if (paramInt4 != 0)
    {
      j = this.mCallback.clampViewPositionVertical(this.mCapturedView, paramInt2, paramInt4);
      this.mCapturedView.offsetTopAndBottom(j - m);
    }
    if ((paramInt3 != 0) || (paramInt4 != 0))
    {
      int n = i - k;
      int i1 = j - m;
      this.mCallback.onViewPositionChanged(this.mCapturedView, i, j, n, i1);
    }
  }

  private void ensureMotionHistorySizeForId(int paramInt)
  {
    if ((this.mInitialMotionX == null) || (this.mInitialMotionX.length <= paramInt))
    {
      float[] arrayOfFloat1 = new float[paramInt + 1];
      float[] arrayOfFloat2 = new float[paramInt + 1];
      float[] arrayOfFloat3 = new float[paramInt + 1];
      float[] arrayOfFloat4 = new float[paramInt + 1];
      int[] arrayOfInt1 = new int[paramInt + 1];
      int[] arrayOfInt2 = new int[paramInt + 1];
      int[] arrayOfInt3 = new int[paramInt + 1];
      if (this.mInitialMotionX != null)
      {
        System.arraycopy(this.mInitialMotionX, 0, arrayOfFloat1, 0, this.mInitialMotionX.length);
        System.arraycopy(this.mInitialMotionY, 0, arrayOfFloat2, 0, this.mInitialMotionY.length);
        System.arraycopy(this.mLastMotionX, 0, arrayOfFloat3, 0, this.mLastMotionX.length);
        System.arraycopy(this.mLastMotionY, 0, arrayOfFloat4, 0, this.mLastMotionY.length);
        System.arraycopy(this.mInitialEdgesTouched, 0, arrayOfInt1, 0, this.mInitialEdgesTouched.length);
        System.arraycopy(this.mEdgeDragsInProgress, 0, arrayOfInt2, 0, this.mEdgeDragsInProgress.length);
        System.arraycopy(this.mEdgeDragsLocked, 0, arrayOfInt3, 0, this.mEdgeDragsLocked.length);
      }
      this.mInitialMotionX = arrayOfFloat1;
      this.mInitialMotionY = arrayOfFloat2;
      this.mLastMotionX = arrayOfFloat3;
      this.mLastMotionY = arrayOfFloat4;
      this.mInitialEdgesTouched = arrayOfInt1;
      this.mEdgeDragsInProgress = arrayOfInt2;
      this.mEdgeDragsLocked = arrayOfInt3;
    }
  }

  private boolean forceSettleCapturedViewAt(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    int i = this.mCapturedView.getLeft();
    int j = this.mCapturedView.getTop();
    int k = paramInt1 - i;
    int m = paramInt2 - j;
    if ((k == 0) && (m == 0))
    {
      this.mScroller.abortAnimation();
      setDragState(0);
      return false;
    }
    int n = computeSettleDuration(this.mCapturedView, k, m, paramInt3, paramInt4);
    this.mScroller.startScroll(i, j, k, m, n);
    setDragState(2);
    return true;
  }

  private int getEdgesTouched(int paramInt1, int paramInt2)
  {
    int i = this.mParentView.getLeft() + this.mEdgeSize;
    int j = 0;
    if (paramInt1 < i)
      j = 0x0 | 0x1;
    if (paramInt2 < this.mParentView.getTop() + this.mEdgeSize)
      j |= 4;
    if (paramInt1 > this.mParentView.getRight() - this.mEdgeSize)
      j |= 2;
    if (paramInt2 > this.mParentView.getBottom() - this.mEdgeSize)
      j |= 8;
    return j;
  }

  private void releaseViewForPointerUp()
  {
    this.mVelocityTracker.computeCurrentVelocity(1000, this.mMaxVelocity);
    dispatchViewReleased(clampMag(VelocityTrackerCompat.getXVelocity(this.mVelocityTracker, this.mActivePointerId), this.mMinVelocity, this.mMaxVelocity), clampMag(VelocityTrackerCompat.getYVelocity(this.mVelocityTracker, this.mActivePointerId), this.mMinVelocity, this.mMaxVelocity));
  }

  private void reportNewEdgeDrags(float paramFloat1, float paramFloat2, int paramInt)
  {
    boolean bool = checkNewEdgeDrag(paramFloat1, paramFloat2, paramInt, 1);
    int i = 0;
    if (bool)
      i = 0x0 | 0x1;
    if (checkNewEdgeDrag(paramFloat2, paramFloat1, paramInt, 4))
      i |= 4;
    if (checkNewEdgeDrag(paramFloat1, paramFloat2, paramInt, 2))
      i |= 2;
    if (checkNewEdgeDrag(paramFloat2, paramFloat1, paramInt, 8))
      i |= 8;
    if (i != 0)
    {
      int[] arrayOfInt = this.mEdgeDragsInProgress;
      arrayOfInt[paramInt] = (i | arrayOfInt[paramInt]);
      this.mCallback.onEdgeDragStarted(i, paramInt);
    }
  }

  private void saveInitialMotion(float paramFloat1, float paramFloat2, int paramInt)
  {
    ensureMotionHistorySizeForId(paramInt);
    float[] arrayOfFloat1 = this.mInitialMotionX;
    this.mLastMotionX[paramInt] = paramFloat1;
    arrayOfFloat1[paramInt] = paramFloat1;
    float[] arrayOfFloat2 = this.mInitialMotionY;
    this.mLastMotionY[paramInt] = paramFloat2;
    arrayOfFloat2[paramInt] = paramFloat2;
    this.mInitialEdgesTouched[paramInt] = getEdgesTouched((int)paramFloat1, (int)paramFloat2);
    this.mPointersDown |= 1 << paramInt;
  }

  private void saveLastMotion(MotionEvent paramMotionEvent)
  {
    int i = MotionEventCompat.getPointerCount(paramMotionEvent);
    for (int j = 0; j < i; j++)
    {
      int k = MotionEventCompat.getPointerId(paramMotionEvent, j);
      float f1 = MotionEventCompat.getX(paramMotionEvent, j);
      float f2 = MotionEventCompat.getY(paramMotionEvent, j);
      this.mLastMotionX[k] = f1;
      this.mLastMotionY[k] = f2;
    }
  }

  public void abort()
  {
    cancel();
    if (this.mDragState == 2)
    {
      int i = this.mScroller.getCurrX();
      int j = this.mScroller.getCurrY();
      this.mScroller.abortAnimation();
      int k = this.mScroller.getCurrX();
      int m = this.mScroller.getCurrY();
      this.mCallback.onViewPositionChanged(this.mCapturedView, k, m, k - i, m - j);
    }
    setDragState(0);
  }

  protected boolean canScroll(View paramView, boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if ((paramView instanceof ViewGroup))
    {
      ViewGroup localViewGroup = (ViewGroup)paramView;
      int i = paramView.getScrollX();
      int j = paramView.getScrollY();
      for (int k = -1 + localViewGroup.getChildCount(); k >= 0; k--)
      {
        View localView = localViewGroup.getChildAt(k);
        if ((paramInt3 + i >= localView.getLeft()) && (paramInt3 + i < localView.getRight()) && (paramInt4 + j >= localView.getTop()) && (paramInt4 + j < localView.getBottom()) && (canScroll(localView, true, paramInt1, paramInt2, paramInt3 + i - localView.getLeft(), paramInt4 + j - localView.getTop())))
          return true;
      }
    }
    return (paramBoolean) && ((ViewCompat.canScrollHorizontally(paramView, -paramInt1)) || (ViewCompat.canScrollVertically(paramView, -paramInt2)));
  }

  public void cancel()
  {
    this.mActivePointerId = -1;
    clearMotionHistory();
    if (this.mVelocityTracker != null)
    {
      this.mVelocityTracker.recycle();
      this.mVelocityTracker = null;
    }
  }

  public void captureChildView(View paramView, int paramInt)
  {
    if (paramView.getParent() != this.mParentView)
      throw new IllegalArgumentException("captureChildView: parameter must be a descendant of the ViewDragHelper's tracked parent view (" + this.mParentView + ")");
    this.mCapturedView = paramView;
    this.mActivePointerId = paramInt;
    this.mCallback.onViewCaptured(paramView, paramInt);
    setDragState(1);
  }

  public boolean checkTouchSlop(int paramInt)
  {
    int i = this.mInitialMotionX.length;
    for (int j = 0; j < i; j++)
      if (checkTouchSlop(paramInt, j))
        return true;
    return false;
  }

  public boolean checkTouchSlop(int paramInt1, int paramInt2)
  {
    int i = 1;
    if (!isPointerDown(paramInt2))
      i = 0;
    label24: float f2;
    label107: label113: 
    do
    {
      float f1;
      int n;
      do
      {
        return i;
        int j;
        if ((paramInt1 & 0x1) == i)
        {
          j = i;
          if ((paramInt1 & 0x2) != 2)
            break label107;
        }
        int k;
        for (int m = i; ; n = 0)
        {
          f1 = this.mLastMotionX[paramInt2] - this.mInitialMotionX[paramInt2];
          f2 = this.mLastMotionY[paramInt2] - this.mInitialMotionY[paramInt2];
          if ((j == 0) || (m == 0))
            break label113;
          if (f1 * f1 + f2 * f2 > this.mTouchSlop * this.mTouchSlop)
            break;
          return false;
          k = 0;
          break label24;
        }
        if (k == 0)
          break;
      }
      while (Math.abs(f1) > this.mTouchSlop);
      return false;
      if (n == 0)
        break;
    }
    while (Math.abs(f2) > this.mTouchSlop);
    return false;
    return false;
  }

  public boolean continueSettling(boolean paramBoolean)
  {
    if (this.mDragState == 2)
    {
      boolean bool = this.mScroller.computeScrollOffset();
      int i = this.mScroller.getCurrX();
      int j = this.mScroller.getCurrY();
      int k = i - this.mCapturedView.getLeft();
      int m = j - this.mCapturedView.getTop();
      if (k != 0)
        this.mCapturedView.offsetLeftAndRight(k);
      if (m != 0)
        this.mCapturedView.offsetTopAndBottom(m);
      if ((k != 0) || (m != 0))
        this.mCallback.onViewPositionChanged(this.mCapturedView, i, j, k, m);
      if ((bool) && (i == this.mScroller.getFinalX()) && (j == this.mScroller.getFinalY()))
      {
        this.mScroller.abortAnimation();
        bool = false;
      }
      if (!bool)
      {
        if (!paramBoolean)
          break label178;
        this.mParentView.post(this.mSetIdleRunnable);
      }
    }
    while (this.mDragState == 2)
    {
      return true;
      label178: setDragState(0);
    }
    return false;
  }

  public View findTopChildUnder(int paramInt1, int paramInt2)
  {
    for (int i = -1 + this.mParentView.getChildCount(); i >= 0; i--)
    {
      View localView = this.mParentView.getChildAt(this.mCallback.getOrderedChildIndex(i));
      if ((paramInt1 >= localView.getLeft()) && (paramInt1 < localView.getRight()) && (paramInt2 >= localView.getTop()) && (paramInt2 < localView.getBottom()))
        return localView;
    }
    return null;
  }

  public void flingCapturedView(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if (!this.mReleaseInProgress)
      throw new IllegalStateException("Cannot flingCapturedView outside of a call to Callback#onViewReleased");
    this.mScroller.fling(this.mCapturedView.getLeft(), this.mCapturedView.getTop(), (int)VelocityTrackerCompat.getXVelocity(this.mVelocityTracker, this.mActivePointerId), (int)VelocityTrackerCompat.getYVelocity(this.mVelocityTracker, this.mActivePointerId), paramInt1, paramInt3, paramInt2, paramInt4);
    setDragState(2);
  }

  public int getActivePointerId()
  {
    return this.mActivePointerId;
  }

  public View getCapturedView()
  {
    return this.mCapturedView;
  }

  public int getEdgeSize()
  {
    return this.mEdgeSize;
  }

  public float getMinVelocity()
  {
    return this.mMinVelocity;
  }

  public int getTouchSlop()
  {
    return this.mTouchSlop;
  }

  public int getViewDragState()
  {
    return this.mDragState;
  }

  public boolean isCapturedViewUnder(int paramInt1, int paramInt2)
  {
    return isViewUnder(this.mCapturedView, paramInt1, paramInt2);
  }

  public boolean isEdgeTouched(int paramInt)
  {
    int i = this.mInitialEdgesTouched.length;
    for (int j = 0; j < i; j++)
      if (isEdgeTouched(paramInt, j))
        return true;
    return false;
  }

  public boolean isEdgeTouched(int paramInt1, int paramInt2)
  {
    return (isPointerDown(paramInt2)) && ((paramInt1 & this.mInitialEdgesTouched[paramInt2]) != 0);
  }

  public boolean isPointerDown(int paramInt)
  {
    return (this.mPointersDown & 1 << paramInt) != 0;
  }

  public boolean isViewUnder(View paramView, int paramInt1, int paramInt2)
  {
    if (paramView == null);
    while ((paramInt1 < paramView.getLeft()) || (paramInt1 >= paramView.getRight()) || (paramInt2 < paramView.getTop()) || (paramInt2 >= paramView.getBottom()))
      return false;
    return true;
  }

  public void processTouchEvent(MotionEvent paramMotionEvent)
  {
    int i = MotionEventCompat.getActionMasked(paramMotionEvent);
    int j = MotionEventCompat.getActionIndex(paramMotionEvent);
    if (i == 0)
      cancel();
    if (this.mVelocityTracker == null)
      this.mVelocityTracker = VelocityTracker.obtain();
    this.mVelocityTracker.addMovement(paramMotionEvent);
    switch (i)
    {
    case 4:
    default:
    case 0:
    case 5:
      int i9;
      float f9;
      float f10;
      do
      {
        int i10;
        do
        {
          int i11;
          int i12;
          do
          {
            return;
            float f11 = paramMotionEvent.getX();
            float f12 = paramMotionEvent.getY();
            i11 = MotionEventCompat.getPointerId(paramMotionEvent, 0);
            View localView2 = findTopChildUnder((int)f11, (int)f12);
            saveInitialMotion(f11, f12, i11);
            tryCaptureViewForDrag(localView2, i11);
            i12 = this.mInitialEdgesTouched[i11];
          }
          while ((i12 & this.mTrackingEdges) == 0);
          this.mCallback.onEdgeTouched(i12 & this.mTrackingEdges, i11);
          return;
          i9 = MotionEventCompat.getPointerId(paramMotionEvent, j);
          f9 = MotionEventCompat.getX(paramMotionEvent, j);
          f10 = MotionEventCompat.getY(paramMotionEvent, j);
          saveInitialMotion(f9, f10, i9);
          if (this.mDragState != 0)
            break;
          tryCaptureViewForDrag(findTopChildUnder((int)f9, (int)f10), i9);
          i10 = this.mInitialEdgesTouched[i9];
        }
        while ((i10 & this.mTrackingEdges) == 0);
        this.mCallback.onEdgeTouched(i10 & this.mTrackingEdges, i9);
        return;
      }
      while (!isCapturedViewUnder((int)f9, (int)f10));
      tryCaptureViewForDrag(this.mCapturedView, i9);
      return;
    case 2:
      if (this.mDragState == 1)
      {
        int i6 = MotionEventCompat.findPointerIndex(paramMotionEvent, this.mActivePointerId);
        float f7 = MotionEventCompat.getX(paramMotionEvent, i6);
        float f8 = MotionEventCompat.getY(paramMotionEvent, i6);
        int i7 = (int)(f7 - this.mLastMotionX[this.mActivePointerId]);
        int i8 = (int)(f8 - this.mLastMotionY[this.mActivePointerId]);
        dragTo(i7 + this.mCapturedView.getLeft(), i8 + this.mCapturedView.getTop(), i7, i8);
        saveLastMotion(paramMotionEvent);
        return;
      }
      int i3 = MotionEventCompat.getPointerCount(paramMotionEvent);
      for (int i4 = 0; ; i4++)
      {
        int i5;
        float f3;
        float f4;
        float f5;
        float f6;
        if (i4 < i3)
        {
          i5 = MotionEventCompat.getPointerId(paramMotionEvent, i4);
          f3 = MotionEventCompat.getX(paramMotionEvent, i4);
          f4 = MotionEventCompat.getY(paramMotionEvent, i4);
          f5 = f3 - this.mInitialMotionX[i5];
          f6 = f4 - this.mInitialMotionY[i5];
          reportNewEdgeDrags(f5, f6, i5);
          if (this.mDragState != 1)
            break label473;
        }
        View localView1;
        do
        {
          saveLastMotion(paramMotionEvent);
          return;
          localView1 = findTopChildUnder((int)f3, (int)f4);
        }
        while ((checkTouchSlop(localView1, f5, f6)) && (tryCaptureViewForDrag(localView1, i5)));
      }
    case 6:
      int k = MotionEventCompat.getPointerId(paramMotionEvent, j);
      if ((this.mDragState == 1) && (k == this.mActivePointerId))
      {
        int m = -1;
        int n = MotionEventCompat.getPointerCount(paramMotionEvent);
        int i1 = 0;
        if (i1 < n)
        {
          int i2 = MotionEventCompat.getPointerId(paramMotionEvent, i1);
          if (i2 == this.mActivePointerId);
          float f1;
          float f2;
          do
          {
            i1++;
            break;
            f1 = MotionEventCompat.getX(paramMotionEvent, i1);
            f2 = MotionEventCompat.getY(paramMotionEvent, i1);
          }
          while ((findTopChildUnder((int)f1, (int)f2) != this.mCapturedView) || (!tryCaptureViewForDrag(this.mCapturedView, i2)));
          m = this.mActivePointerId;
        }
        if (m == -1)
          releaseViewForPointerUp();
      }
      clearMotionHistory(k);
      return;
    case 1:
      label473: if (this.mDragState == 1)
        releaseViewForPointerUp();
      cancel();
      return;
    case 3:
    }
    if (this.mDragState == 1)
      dispatchViewReleased(0.0F, 0.0F);
    cancel();
  }

  void setDragState(int paramInt)
  {
    this.mParentView.removeCallbacks(this.mSetIdleRunnable);
    if (this.mDragState != paramInt)
    {
      this.mDragState = paramInt;
      this.mCallback.onViewDragStateChanged(paramInt);
      if (this.mDragState == 0)
        this.mCapturedView = null;
    }
  }

  public void setEdgeTrackingEnabled(int paramInt)
  {
    this.mTrackingEdges = paramInt;
  }

  public void setMinVelocity(float paramFloat)
  {
    this.mMinVelocity = paramFloat;
  }

  public boolean settleCapturedViewAt(int paramInt1, int paramInt2)
  {
    if (!this.mReleaseInProgress)
      throw new IllegalStateException("Cannot settleCapturedViewAt outside of a call to Callback#onViewReleased");
    return forceSettleCapturedViewAt(paramInt1, paramInt2, (int)VelocityTrackerCompat.getXVelocity(this.mVelocityTracker, this.mActivePointerId), (int)VelocityTrackerCompat.getYVelocity(this.mVelocityTracker, this.mActivePointerId));
  }

  public boolean shouldInterceptTouchEvent(MotionEvent paramMotionEvent)
  {
    int i = MotionEventCompat.getActionMasked(paramMotionEvent);
    int j = MotionEventCompat.getActionIndex(paramMotionEvent);
    if (i == 0)
      cancel();
    if (this.mVelocityTracker == null)
      this.mVelocityTracker = VelocityTracker.obtain();
    this.mVelocityTracker.addMovement(paramMotionEvent);
    switch (i)
    {
    case 4:
    default:
    case 0:
    case 5:
    case 2:
    case 6:
    case 1:
    case 3:
    }
    while (this.mDragState == 1)
    {
      return true;
      float f7 = paramMotionEvent.getX();
      float f8 = paramMotionEvent.getY();
      int i12 = MotionEventCompat.getPointerId(paramMotionEvent, 0);
      saveInitialMotion(f7, f8, i12);
      View localView3 = findTopChildUnder((int)f7, (int)f8);
      if ((localView3 == this.mCapturedView) && (this.mDragState == 2))
        tryCaptureViewForDrag(localView3, i12);
      int i13 = this.mInitialEdgesTouched[i12];
      if ((i13 & this.mTrackingEdges) != 0)
      {
        this.mCallback.onEdgeTouched(i13 & this.mTrackingEdges, i12);
        continue;
        int i10 = MotionEventCompat.getPointerId(paramMotionEvent, j);
        float f5 = MotionEventCompat.getX(paramMotionEvent, j);
        float f6 = MotionEventCompat.getY(paramMotionEvent, j);
        saveInitialMotion(f5, f6, i10);
        if (this.mDragState == 0)
        {
          int i11 = this.mInitialEdgesTouched[i10];
          if ((i11 & this.mTrackingEdges) != 0)
            this.mCallback.onEdgeTouched(i11 & this.mTrackingEdges, i10);
        }
        else if (this.mDragState == 2)
        {
          View localView2 = findTopChildUnder((int)f5, (int)f6);
          if (localView2 == this.mCapturedView)
          {
            tryCaptureViewForDrag(localView2, i10);
            continue;
            if ((this.mInitialMotionX != null) && (this.mInitialMotionY != null))
            {
              int k = MotionEventCompat.getPointerCount(paramMotionEvent);
              for (int m = 0; ; m++)
              {
                int n;
                float f3;
                float f4;
                View localView1;
                int i1;
                if (m < k)
                {
                  n = MotionEventCompat.getPointerId(paramMotionEvent, m);
                  float f1 = MotionEventCompat.getX(paramMotionEvent, m);
                  float f2 = MotionEventCompat.getY(paramMotionEvent, m);
                  f3 = f1 - this.mInitialMotionX[n];
                  f4 = f2 - this.mInitialMotionY[n];
                  localView1 = findTopChildUnder((int)f1, (int)f2);
                  if ((localView1 == null) || (!checkTouchSlop(localView1, f3, f4)))
                    break label558;
                  i1 = 1;
                  label427: if (i1 == 0)
                    break label564;
                  int i2 = localView1.getLeft();
                  int i3 = i2 + (int)f3;
                  int i4 = this.mCallback.clampViewPositionHorizontal(localView1, i3, (int)f3);
                  int i5 = localView1.getTop();
                  int i6 = i5 + (int)f4;
                  int i7 = this.mCallback.clampViewPositionVertical(localView1, i6, (int)f4);
                  int i8 = this.mCallback.getViewHorizontalDragRange(localView1);
                  int i9 = this.mCallback.getViewVerticalDragRange(localView1);
                  if (((i8 != 0) && ((i8 <= 0) || (i4 != i2))) || ((i9 != 0) && ((i9 <= 0) || (i7 != i5))))
                    break label564;
                }
                label558: label564: 
                do
                {
                  saveLastMotion(paramMotionEvent);
                  break;
                  i1 = 0;
                  break label427;
                  reportNewEdgeDrags(f3, f4, n);
                }
                while ((this.mDragState == 1) || ((i1 != 0) && (tryCaptureViewForDrag(localView1, n))));
              }
              clearMotionHistory(MotionEventCompat.getPointerId(paramMotionEvent, j));
              continue;
              cancel();
            }
          }
        }
      }
    }
    return false;
  }

  public boolean smoothSlideViewTo(View paramView, int paramInt1, int paramInt2)
  {
    this.mCapturedView = paramView;
    this.mActivePointerId = -1;
    boolean bool = forceSettleCapturedViewAt(paramInt1, paramInt2, 0, 0);
    if ((!bool) && (this.mDragState == 0) && (this.mCapturedView != null))
      this.mCapturedView = null;
    return bool;
  }

  boolean tryCaptureViewForDrag(View paramView, int paramInt)
  {
    if ((paramView == this.mCapturedView) && (this.mActivePointerId == paramInt))
      return true;
    if ((paramView != null) && (this.mCallback.tryCaptureView(paramView, paramInt)))
    {
      this.mActivePointerId = paramInt;
      captureChildView(paramView, paramInt);
      return true;
    }
    return false;
  }

  public static abstract class Callback
  {
    public int clampViewPositionHorizontal(View paramView, int paramInt1, int paramInt2)
    {
      return 0;
    }

    public int clampViewPositionVertical(View paramView, int paramInt1, int paramInt2)
    {
      return 0;
    }

    public int getOrderedChildIndex(int paramInt)
    {
      return paramInt;
    }

    public int getViewHorizontalDragRange(View paramView)
    {
      return 0;
    }

    public int getViewVerticalDragRange(View paramView)
    {
      return 0;
    }

    public void onEdgeDragStarted(int paramInt1, int paramInt2)
    {
    }

    public boolean onEdgeLock(int paramInt)
    {
      return false;
    }

    public void onEdgeTouched(int paramInt1, int paramInt2)
    {
    }

    public void onViewCaptured(View paramView, int paramInt)
    {
    }

    public void onViewDragStateChanged(int paramInt)
    {
    }

    public void onViewPositionChanged(View paramView, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
    {
    }

    public void onViewReleased(View paramView, float paramFloat1, float paramFloat2)
    {
    }

    public abstract boolean tryCaptureView(View paramView, int paramInt);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.widget.ViewDragHelper
 * JD-Core Version:    0.6.2
 */
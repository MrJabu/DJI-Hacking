package android.support.v4.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.annotation.DrawableRes;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import java.util.ArrayList;

public class SlidingPaneLayout extends ViewGroup
{
  private static final int DEFAULT_FADE_COLOR = -858993460;
  private static final int DEFAULT_OVERHANG_SIZE = 32;
  static final SlidingPanelLayoutImpl IMPL = new SlidingPaneLayout.SlidingPanelLayoutImplBase();
  private static final int MIN_FLING_VELOCITY = 400;
  private static final String TAG = "SlidingPaneLayout";
  private boolean mCanSlide;
  private int mCoveredFadeColor;
  private final ViewDragHelper mDragHelper;
  private boolean mFirstLayout = true;
  private float mInitialMotionX;
  private float mInitialMotionY;
  private boolean mIsUnableToDrag;
  private final int mOverhangSize;
  private PanelSlideListener mPanelSlideListener;
  private int mParallaxBy;
  private float mParallaxOffset;
  private final ArrayList<DisableLayerRunnable> mPostedRunnables = new ArrayList();
  private boolean mPreservedOpenState;
  private Drawable mShadowDrawableLeft;
  private Drawable mShadowDrawableRight;
  private float mSlideOffset;
  private int mSlideRange;
  private View mSlideableView;
  private int mSliderFadeColor = -858993460;
  private final Rect mTmpRect = new Rect();

  static
  {
    int i = Build.VERSION.SDK_INT;
    if (i >= 17)
    {
      IMPL = new SlidingPaneLayout.SlidingPanelLayoutImplJBMR1();
      return;
    }
    if (i >= 16)
    {
      IMPL = new SlidingPaneLayout.SlidingPanelLayoutImplJB();
      return;
    }
  }

  public SlidingPaneLayout(Context paramContext)
  {
    this(paramContext, null);
  }

  public SlidingPaneLayout(Context paramContext, AttributeSet paramAttributeSet)
  {
    this(paramContext, paramAttributeSet, 0);
  }

  public SlidingPaneLayout(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
  {
    super(paramContext, paramAttributeSet, paramInt);
    float f = paramContext.getResources().getDisplayMetrics().density;
    this.mOverhangSize = ((int)(0.5F + 32.0F * f));
    ViewConfiguration.get(paramContext);
    setWillNotDraw(false);
    ViewCompat.setAccessibilityDelegate(this, new SlidingPaneLayout.AccessibilityDelegate(this));
    ViewCompat.setImportantForAccessibility(this, 1);
    this.mDragHelper = ViewDragHelper.create(this, 0.5F, new SlidingPaneLayout.DragHelperCallback(this, null));
    this.mDragHelper.setMinVelocity(400.0F * f);
  }

  private boolean closePane(View paramView, int paramInt)
  {
    boolean bool1;
    if (!this.mFirstLayout)
    {
      boolean bool2 = smoothSlideTo(0.0F, paramInt);
      bool1 = false;
      if (!bool2);
    }
    else
    {
      this.mPreservedOpenState = false;
      bool1 = true;
    }
    return bool1;
  }

  private void dimChildView(View paramView, float paramFloat, int paramInt)
  {
    LayoutParams localLayoutParams = (LayoutParams)paramView.getLayoutParams();
    if ((paramFloat > 0.0F) && (paramInt != 0))
    {
      i = (int)(paramFloat * ((0xFF000000 & paramInt) >>> 24)) << 24 | 0xFFFFFF & paramInt;
      if (localLayoutParams.dimPaint == null)
        localLayoutParams.dimPaint = new Paint();
      localLayoutParams.dimPaint.setColorFilter(new PorterDuffColorFilter(i, PorterDuff.Mode.SRC_OVER));
      if (ViewCompat.getLayerType(paramView) != 2)
        ViewCompat.setLayerType(paramView, 2, localLayoutParams.dimPaint);
      invalidateChildRegion(paramView);
    }
    while (ViewCompat.getLayerType(paramView) == 0)
    {
      int i;
      return;
    }
    if (localLayoutParams.dimPaint != null)
      localLayoutParams.dimPaint.setColorFilter(null);
    DisableLayerRunnable localDisableLayerRunnable = new DisableLayerRunnable(paramView);
    this.mPostedRunnables.add(localDisableLayerRunnable);
    ViewCompat.postOnAnimation(this, localDisableLayerRunnable);
  }

  private void invalidateChildRegion(View paramView)
  {
    IMPL.invalidateChildRegion(this, paramView);
  }

  private boolean isLayoutRtlSupport()
  {
    return ViewCompat.getLayoutDirection(this) == 1;
  }

  private void onPanelDragged(int paramInt)
  {
    if (this.mSlideableView == null)
    {
      this.mSlideOffset = 0.0F;
      return;
    }
    boolean bool = isLayoutRtlSupport();
    LayoutParams localLayoutParams = (LayoutParams)this.mSlideableView.getLayoutParams();
    int i = this.mSlideableView.getWidth();
    int j;
    int k;
    if (bool)
    {
      j = getWidth() - paramInt - i;
      if (!bool)
        break label145;
      k = getPaddingRight();
      label63: if (!bool)
        break label154;
    }
    label145: label154: for (int m = localLayoutParams.rightMargin; ; m = localLayoutParams.leftMargin)
    {
      this.mSlideOffset = ((j - (k + m)) / this.mSlideRange);
      if (this.mParallaxBy != 0)
        parallaxOtherViews(this.mSlideOffset);
      if (localLayoutParams.dimWhenOffset)
        dimChildView(this.mSlideableView, this.mSlideOffset, this.mSliderFadeColor);
      dispatchOnPanelSlide(this.mSlideableView);
      return;
      j = paramInt;
      break;
      k = getPaddingLeft();
      break label63;
    }
  }

  private boolean openPane(View paramView, int paramInt)
  {
    if ((this.mFirstLayout) || (smoothSlideTo(1.0F, paramInt)))
    {
      this.mPreservedOpenState = true;
      return true;
    }
    return false;
  }

  private void parallaxOtherViews(float paramFloat)
  {
    boolean bool = isLayoutRtlSupport();
    LayoutParams localLayoutParams = (LayoutParams)this.mSlideableView.getLayoutParams();
    int i1;
    int i;
    label41: int k;
    label50: View localView;
    if (localLayoutParams.dimWhenOffset)
      if (bool)
      {
        i1 = localLayoutParams.rightMargin;
        if (i1 > 0)
          break label89;
        i = 1;
        int j = getChildCount();
        k = 0;
        if (k >= j)
          return;
        localView = getChildAt(k);
        if (localView != this.mSlideableView)
          break label95;
      }
    label89: label95: 
    do
    {
      k++;
      break label50;
      i1 = localLayoutParams.leftMargin;
      break;
      i = 0;
      break label41;
      int m = (int)((1.0F - this.mParallaxOffset) * this.mParallaxBy);
      this.mParallaxOffset = paramFloat;
      int n = m - (int)((1.0F - paramFloat) * this.mParallaxBy);
      if (bool)
        n = -n;
      localView.offsetLeftAndRight(n);
    }
    while (i == 0);
    if (bool);
    for (float f = this.mParallaxOffset - 1.0F; ; f = 1.0F - this.mParallaxOffset)
    {
      dimChildView(localView, f, this.mCoveredFadeColor);
      break;
    }
  }

  private static boolean viewIsOpaque(View paramView)
  {
    if (ViewCompat.isOpaque(paramView));
    Drawable localDrawable;
    do
    {
      return true;
      if (Build.VERSION.SDK_INT >= 18)
        return false;
      localDrawable = paramView.getBackground();
      if (localDrawable == null)
        break;
    }
    while (localDrawable.getOpacity() == -1);
    return false;
    return false;
  }

  protected boolean canScroll(View paramView, boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3)
  {
    if ((paramView instanceof ViewGroup))
    {
      ViewGroup localViewGroup = (ViewGroup)paramView;
      int i = paramView.getScrollX();
      int j = paramView.getScrollY();
      for (int k = -1 + localViewGroup.getChildCount(); k >= 0; k--)
      {
        View localView = localViewGroup.getChildAt(k);
        if ((paramInt2 + i >= localView.getLeft()) && (paramInt2 + i < localView.getRight()) && (paramInt3 + j >= localView.getTop()) && (paramInt3 + j < localView.getBottom()))
        {
          int m = paramInt2 + i - localView.getLeft();
          int n = paramInt3 + j - localView.getTop();
          if (canScroll(localView, true, paramInt1, m, n))
            return true;
        }
      }
    }
    if (paramBoolean)
    {
      if (isLayoutRtlSupport());
      while (ViewCompat.canScrollHorizontally(paramView, paramInt1))
      {
        return true;
        paramInt1 = -paramInt1;
      }
    }
    return false;
  }

  @Deprecated
  public boolean canSlide()
  {
    return this.mCanSlide;
  }

  protected boolean checkLayoutParams(ViewGroup.LayoutParams paramLayoutParams)
  {
    return ((paramLayoutParams instanceof LayoutParams)) && (super.checkLayoutParams(paramLayoutParams));
  }

  public boolean closePane()
  {
    return closePane(this.mSlideableView, 0);
  }

  public void computeScroll()
  {
    if (this.mDragHelper.continueSettling(true))
    {
      if (!this.mCanSlide)
        this.mDragHelper.abort();
    }
    else
      return;
    ViewCompat.postInvalidateOnAnimation(this);
  }

  void dispatchOnPanelClosed(View paramView)
  {
    if (this.mPanelSlideListener != null)
      this.mPanelSlideListener.onPanelClosed(paramView);
    sendAccessibilityEvent(32);
  }

  void dispatchOnPanelOpened(View paramView)
  {
    if (this.mPanelSlideListener != null)
      this.mPanelSlideListener.onPanelOpened(paramView);
    sendAccessibilityEvent(32);
  }

  void dispatchOnPanelSlide(View paramView)
  {
    if (this.mPanelSlideListener != null)
      this.mPanelSlideListener.onPanelSlide(paramView, this.mSlideOffset);
  }

  public void draw(Canvas paramCanvas)
  {
    super.draw(paramCanvas);
    Drawable localDrawable;
    if (isLayoutRtlSupport())
    {
      localDrawable = this.mShadowDrawableRight;
      if (getChildCount() <= 1)
        break label48;
    }
    label48: for (View localView = getChildAt(1); ; localView = null)
    {
      if ((localView != null) && (localDrawable != null))
        break label53;
      return;
      localDrawable = this.mShadowDrawableLeft;
      break;
    }
    label53: int i = localView.getTop();
    int j = localView.getBottom();
    int k = localDrawable.getIntrinsicWidth();
    int n;
    int m;
    if (isLayoutRtlSupport())
    {
      n = localView.getRight();
      m = n + k;
    }
    while (true)
    {
      localDrawable.setBounds(n, i, m, j);
      localDrawable.draw(paramCanvas);
      return;
      m = localView.getLeft();
      n = m - k;
    }
  }

  protected boolean drawChild(Canvas paramCanvas, View paramView, long paramLong)
  {
    LayoutParams localLayoutParams = (LayoutParams)paramView.getLayoutParams();
    int i = paramCanvas.save(2);
    boolean bool;
    if ((this.mCanSlide) && (!localLayoutParams.slideable) && (this.mSlideableView != null))
    {
      paramCanvas.getClipBounds(this.mTmpRect);
      if (isLayoutRtlSupport())
      {
        this.mTmpRect.left = Math.max(this.mTmpRect.left, this.mSlideableView.getRight());
        paramCanvas.clipRect(this.mTmpRect);
      }
    }
    else
    {
      if (Build.VERSION.SDK_INT < 11)
        break label140;
      bool = super.drawChild(paramCanvas, paramView, paramLong);
    }
    while (true)
    {
      paramCanvas.restoreToCount(i);
      return bool;
      this.mTmpRect.right = Math.min(this.mTmpRect.right, this.mSlideableView.getLeft());
      break;
      label140: if ((localLayoutParams.dimWhenOffset) && (this.mSlideOffset > 0.0F))
      {
        if (!paramView.isDrawingCacheEnabled())
          paramView.setDrawingCacheEnabled(true);
        Bitmap localBitmap = paramView.getDrawingCache();
        if (localBitmap != null)
        {
          paramCanvas.drawBitmap(localBitmap, paramView.getLeft(), paramView.getTop(), localLayoutParams.dimPaint);
          bool = false;
        }
        else
        {
          Log.e("SlidingPaneLayout", "drawChild: child view " + paramView + " returned null drawing cache");
          bool = super.drawChild(paramCanvas, paramView, paramLong);
        }
      }
      else
      {
        if (paramView.isDrawingCacheEnabled())
          paramView.setDrawingCacheEnabled(false);
        bool = super.drawChild(paramCanvas, paramView, paramLong);
      }
    }
  }

  protected ViewGroup.LayoutParams generateDefaultLayoutParams()
  {
    return new LayoutParams();
  }

  public ViewGroup.LayoutParams generateLayoutParams(AttributeSet paramAttributeSet)
  {
    return new LayoutParams(getContext(), paramAttributeSet);
  }

  protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams paramLayoutParams)
  {
    if ((paramLayoutParams instanceof ViewGroup.MarginLayoutParams))
      return new LayoutParams((ViewGroup.MarginLayoutParams)paramLayoutParams);
    return new LayoutParams(paramLayoutParams);
  }

  public int getCoveredFadeColor()
  {
    return this.mCoveredFadeColor;
  }

  public int getParallaxDistance()
  {
    return this.mParallaxBy;
  }

  public int getSliderFadeColor()
  {
    return this.mSliderFadeColor;
  }

  boolean isDimmed(View paramView)
  {
    if (paramView == null);
    LayoutParams localLayoutParams;
    do
    {
      return false;
      localLayoutParams = (LayoutParams)paramView.getLayoutParams();
    }
    while ((!this.mCanSlide) || (!localLayoutParams.dimWhenOffset) || (this.mSlideOffset <= 0.0F));
    return true;
  }

  public boolean isOpen()
  {
    return (!this.mCanSlide) || (this.mSlideOffset == 1.0F);
  }

  public boolean isSlideable()
  {
    return this.mCanSlide;
  }

  protected void onAttachedToWindow()
  {
    super.onAttachedToWindow();
    this.mFirstLayout = true;
  }

  protected void onDetachedFromWindow()
  {
    super.onDetachedFromWindow();
    this.mFirstLayout = true;
    int i = 0;
    int j = this.mPostedRunnables.size();
    while (i < j)
    {
      ((DisableLayerRunnable)this.mPostedRunnables.get(i)).run();
      i++;
    }
    this.mPostedRunnables.clear();
  }

  public boolean onInterceptTouchEvent(MotionEvent paramMotionEvent)
  {
    int i = MotionEventCompat.getActionMasked(paramMotionEvent);
    if ((!this.mCanSlide) && (i == 0) && (getChildCount() > 1))
    {
      View localView = getChildAt(1);
      if (localView != null)
        if (this.mDragHelper.isViewUnder(localView, (int)paramMotionEvent.getX(), (int)paramMotionEvent.getY()))
          break label98;
    }
    label98: for (boolean bool5 = true; ; bool5 = false)
    {
      this.mPreservedOpenState = bool5;
      if ((this.mCanSlide) && ((!this.mIsUnableToDrag) || (i == 0)))
        break;
      this.mDragHelper.cancel();
      return super.onInterceptTouchEvent(paramMotionEvent);
    }
    if ((i == 3) || (i == 1))
    {
      this.mDragHelper.cancel();
      return false;
    }
    int j = 0;
    switch (i)
    {
    case 1:
    default:
    case 0:
    case 2:
    }
    while ((this.mDragHelper.shouldInterceptTouchEvent(paramMotionEvent)) || (j != 0))
    {
      return true;
      this.mIsUnableToDrag = false;
      float f5 = paramMotionEvent.getX();
      float f6 = paramMotionEvent.getY();
      this.mInitialMotionX = f5;
      this.mInitialMotionY = f6;
      boolean bool3 = this.mDragHelper.isViewUnder(this.mSlideableView, (int)f5, (int)f6);
      j = 0;
      if (bool3)
      {
        boolean bool4 = isDimmed(this.mSlideableView);
        j = 0;
        if (bool4)
        {
          j = 1;
          continue;
          float f1 = paramMotionEvent.getX();
          float f2 = paramMotionEvent.getY();
          float f3 = Math.abs(f1 - this.mInitialMotionX);
          float f4 = Math.abs(f2 - this.mInitialMotionY);
          boolean bool1 = f3 < this.mDragHelper.getTouchSlop();
          j = 0;
          if (bool1)
          {
            boolean bool2 = f4 < f3;
            j = 0;
            if (bool2)
            {
              this.mDragHelper.cancel();
              this.mIsUnableToDrag = true;
              return false;
            }
          }
        }
      }
    }
    return false;
  }

  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    boolean bool1 = isLayoutRtlSupport();
    int i;
    int j;
    label36: int k;
    label47: int m;
    int n;
    int i1;
    int i2;
    if (bool1)
    {
      this.mDragHelper.setEdgeTrackingEnabled(2);
      i = paramInt3 - paramInt1;
      if (!bool1)
        break label142;
      j = getPaddingRight();
      if (!bool1)
        break label151;
      k = getPaddingLeft();
      m = getPaddingTop();
      n = getChildCount();
      i1 = j;
      i2 = i1;
      if (this.mFirstLayout)
        if ((!this.mCanSlide) || (!this.mPreservedOpenState))
          break label160;
    }
    View localView;
    label142: label151: label160: for (float f = 1.0F; ; f = 0.0F)
    {
      this.mSlideOffset = f;
      for (int i3 = 0; ; i3++)
      {
        if (i3 >= n)
          break label450;
        localView = getChildAt(i3);
        if (localView.getVisibility() != 8)
          break;
      }
      this.mDragHelper.setEdgeTrackingEnabled(1);
      break;
      j = getPaddingLeft();
      break label36;
      k = getPaddingRight();
      break label47;
    }
    LayoutParams localLayoutParams = (LayoutParams)localView.getLayoutParams();
    int i5 = localView.getMeasuredWidth();
    int i6 = 0;
    int i12;
    label252: boolean bool2;
    label276: label317: int i8;
    int i7;
    if (localLayoutParams.slideable)
    {
      int i9 = localLayoutParams.leftMargin + localLayoutParams.rightMargin;
      int i10 = i - k - this.mOverhangSize;
      int i11 = Math.min(i2, i10) - i1 - i9;
      this.mSlideRange = i11;
      if (bool1)
      {
        i12 = localLayoutParams.rightMargin;
        if (i11 + (i1 + i12) + i5 / 2 <= i - k)
          break label381;
        bool2 = true;
        localLayoutParams.dimWhenOffset = bool2;
        int i13 = (int)(i11 * this.mSlideOffset);
        i1 += i13 + i12;
        this.mSlideOffset = (i13 / this.mSlideRange);
        if (!bool1)
          break label433;
        i8 = i6 + (i - i1);
        i7 = i8 - i5;
      }
    }
    while (true)
    {
      localView.layout(i7, m, i8, m + localView.getMeasuredHeight());
      i2 += localView.getWidth();
      break;
      i12 = localLayoutParams.leftMargin;
      break label252;
      label381: bool2 = false;
      break label276;
      if ((this.mCanSlide) && (this.mParallaxBy != 0))
      {
        i6 = (int)((1.0F - this.mSlideOffset) * this.mParallaxBy);
        i1 = i2;
        break label317;
      }
      i1 = i2;
      i6 = 0;
      break label317;
      label433: i7 = i1 - i6;
      i8 = i7 + i5;
    }
    label450: if (this.mFirstLayout)
    {
      if (!this.mCanSlide)
        break label525;
      if (this.mParallaxBy != 0)
        parallaxOtherViews(this.mSlideOffset);
      if (((LayoutParams)this.mSlideableView.getLayoutParams()).dimWhenOffset)
        dimChildView(this.mSlideableView, this.mSlideOffset, this.mSliderFadeColor);
    }
    while (true)
    {
      updateObscuredViewsVisibility(this.mSlideableView);
      this.mFirstLayout = false;
      return;
      label525: for (int i4 = 0; i4 < n; i4++)
        dimChildView(getChildAt(i4), 0.0F, this.mSliderFadeColor);
    }
  }

  protected void onMeasure(int paramInt1, int paramInt2)
  {
    int i = View.MeasureSpec.getMode(paramInt1);
    int j = View.MeasureSpec.getSize(paramInt1);
    int k = View.MeasureSpec.getMode(paramInt2);
    int m = View.MeasureSpec.getSize(paramInt2);
    int n;
    int i1;
    label80: float f;
    boolean bool1;
    int i2;
    int i3;
    int i4;
    int i5;
    label133: View localView2;
    LayoutParams localLayoutParams2;
    if (i != 1073741824)
      if (isInEditMode())
        if (i == -2147483648)
        {
          n = -1;
          i1 = 0;
          switch (k)
          {
          default:
            f = 0.0F;
            bool1 = false;
            i2 = j - getPaddingLeft() - getPaddingRight();
            i3 = i2;
            i4 = getChildCount();
            if (i4 > 2)
              Log.e("SlidingPaneLayout", "onMeasure: More than two child views are not supported.");
            this.mSlideableView = null;
            i5 = 0;
            if (i5 >= i4)
              break label547;
            localView2 = getChildAt(i5);
            localLayoutParams2 = (LayoutParams)localView2.getLayoutParams();
            if (localView2.getVisibility() == 8)
              localLayoutParams2.dimWhenOffset = false;
            break;
          case 1073741824:
          case -2147483648:
          }
        }
    do
    {
      i5++;
      break label133;
      if (i != 0)
        break;
      j = 300;
      break;
      throw new IllegalStateException("Width must have an exact value or MATCH_PARENT");
      if (k != 0)
        break;
      if (isInEditMode())
      {
        if (k != 0)
          break;
        k = -2147483648;
        m = 300;
        break;
      }
      throw new IllegalStateException("Height must not be UNSPECIFIED");
      n = m - getPaddingTop() - getPaddingBottom();
      i1 = n;
      break label80;
      n = m - getPaddingTop() - getPaddingBottom();
      i1 = 0;
      break label80;
      if (localLayoutParams2.weight <= 0.0F)
        break label313;
      f += localLayoutParams2.weight;
    }
    while (localLayoutParams2.width == 0);
    label313: int i15 = localLayoutParams2.leftMargin + localLayoutParams2.rightMargin;
    int i16;
    label349: int i17;
    if (localLayoutParams2.width == -2)
    {
      i16 = View.MeasureSpec.makeMeasureSpec(i2 - i15, -2147483648);
      if (localLayoutParams2.height != -2)
        break label503;
      i17 = View.MeasureSpec.makeMeasureSpec(n, -2147483648);
      label369: localView2.measure(i16, i17);
      int i18 = localView2.getMeasuredWidth();
      int i19 = localView2.getMeasuredHeight();
      if ((k == -2147483648) && (i19 > i1))
        i1 = Math.min(i19, n);
      i3 -= i18;
      if (i3 >= 0)
        break label541;
    }
    label541: for (boolean bool2 = true; ; bool2 = false)
    {
      localLayoutParams2.slideable = bool2;
      bool1 |= bool2;
      if (!localLayoutParams2.slideable)
        break;
      this.mSlideableView = localView2;
      break;
      if (localLayoutParams2.width == -1)
      {
        i16 = View.MeasureSpec.makeMeasureSpec(i2 - i15, 1073741824);
        break label349;
      }
      i16 = View.MeasureSpec.makeMeasureSpec(localLayoutParams2.width, 1073741824);
      break label349;
      label503: if (localLayoutParams2.height == -1)
      {
        i17 = View.MeasureSpec.makeMeasureSpec(n, 1073741824);
        break label369;
      }
      i17 = View.MeasureSpec.makeMeasureSpec(localLayoutParams2.height, 1073741824);
      break label369;
    }
    label547: if ((bool1) || (f > 0.0F))
    {
      int i6 = i2 - this.mOverhangSize;
      int i7 = 0;
      if (i7 < i4)
      {
        View localView1 = getChildAt(i7);
        if (localView1.getVisibility() == 8);
        while (true)
        {
          i7++;
          break;
          LayoutParams localLayoutParams1 = (LayoutParams)localView1.getLayoutParams();
          if (localView1.getVisibility() != 8)
          {
            int i8;
            int i9;
            int i14;
            if ((localLayoutParams1.width == 0) && (localLayoutParams1.weight > 0.0F))
            {
              i8 = 1;
              if (i8 != 0)
              {
                i9 = 0;
                if ((!bool1) || (localView1 == this.mSlideableView))
                  break label803;
                if ((localLayoutParams1.width >= 0) || ((i9 <= i6) && (localLayoutParams1.weight <= 0.0F)))
                  continue;
                if (i8 == 0)
                  break label787;
                if (localLayoutParams1.height != -2)
                  break label749;
                i14 = View.MeasureSpec.makeMeasureSpec(n, -2147483648);
              }
            }
            else
            {
              while (true)
              {
                localView1.measure(View.MeasureSpec.makeMeasureSpec(i6, 1073741824), i14);
                break;
                i8 = 0;
                break label643;
                i9 = localView1.getMeasuredWidth();
                break label651;
                label749: if (localLayoutParams1.height == -1)
                {
                  i14 = View.MeasureSpec.makeMeasureSpec(n, 1073741824);
                }
                else
                {
                  i14 = View.MeasureSpec.makeMeasureSpec(localLayoutParams1.height, 1073741824);
                  continue;
                  i14 = View.MeasureSpec.makeMeasureSpec(localView1.getMeasuredHeight(), 1073741824);
                }
              }
              label803: if (localLayoutParams1.weight > 0.0F)
              {
                int i10;
                if (localLayoutParams1.width == 0)
                  if (localLayoutParams1.height == -2)
                    i10 = View.MeasureSpec.makeMeasureSpec(n, -2147483648);
                while (true)
                {
                  if (!bool1)
                    break label945;
                  int i12 = i2 - (localLayoutParams1.leftMargin + localLayoutParams1.rightMargin);
                  int i13 = View.MeasureSpec.makeMeasureSpec(i12, 1073741824);
                  if (i9 == i12)
                    break;
                  localView1.measure(i13, i10);
                  break;
                  if (localLayoutParams1.height == -1)
                  {
                    i10 = View.MeasureSpec.makeMeasureSpec(n, 1073741824);
                  }
                  else
                  {
                    i10 = View.MeasureSpec.makeMeasureSpec(localLayoutParams1.height, 1073741824);
                    continue;
                    i10 = View.MeasureSpec.makeMeasureSpec(localView1.getMeasuredHeight(), 1073741824);
                  }
                }
                label945: int i11 = Math.max(0, i3);
                localView1.measure(View.MeasureSpec.makeMeasureSpec(i9 + (int)(localLayoutParams1.weight * i11 / f), 1073741824), i10);
              }
            }
          }
        }
      }
    }
    label643: label651: label787: setMeasuredDimension(j, i1 + getPaddingTop() + getPaddingBottom());
    this.mCanSlide = bool1;
    if ((this.mDragHelper.getViewDragState() != 0) && (!bool1))
      this.mDragHelper.abort();
  }

  protected void onRestoreInstanceState(Parcelable paramParcelable)
  {
    SavedState localSavedState = (SavedState)paramParcelable;
    super.onRestoreInstanceState(localSavedState.getSuperState());
    if (localSavedState.isOpen)
      openPane();
    while (true)
    {
      this.mPreservedOpenState = localSavedState.isOpen;
      return;
      closePane();
    }
  }

  protected Parcelable onSaveInstanceState()
  {
    SavedState localSavedState = new SavedState(super.onSaveInstanceState());
    if (isSlideable());
    for (boolean bool = isOpen(); ; bool = this.mPreservedOpenState)
    {
      localSavedState.isOpen = bool;
      return localSavedState;
    }
  }

  protected void onSizeChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super.onSizeChanged(paramInt1, paramInt2, paramInt3, paramInt4);
    if (paramInt1 != paramInt3)
      this.mFirstLayout = true;
  }

  public boolean onTouchEvent(MotionEvent paramMotionEvent)
  {
    boolean bool;
    if (!this.mCanSlide)
      bool = super.onTouchEvent(paramMotionEvent);
    float f1;
    float f2;
    float f3;
    float f4;
    int j;
    do
    {
      do
      {
        return bool;
        this.mDragHelper.processTouchEvent(paramMotionEvent);
        int i = paramMotionEvent.getAction();
        bool = true;
        switch (i & 0xFF)
        {
        default:
          return bool;
        case 0:
          float f5 = paramMotionEvent.getX();
          float f6 = paramMotionEvent.getY();
          this.mInitialMotionX = f5;
          this.mInitialMotionY = f6;
          return bool;
        case 1:
        }
      }
      while (!isDimmed(this.mSlideableView));
      f1 = paramMotionEvent.getX();
      f2 = paramMotionEvent.getY();
      f3 = f1 - this.mInitialMotionX;
      f4 = f2 - this.mInitialMotionY;
      j = this.mDragHelper.getTouchSlop();
    }
    while ((f3 * f3 + f4 * f4 >= j * j) || (!this.mDragHelper.isViewUnder(this.mSlideableView, (int)f1, (int)f2)));
    closePane(this.mSlideableView, 0);
    return bool;
  }

  public boolean openPane()
  {
    return openPane(this.mSlideableView, 0);
  }

  public void requestChildFocus(View paramView1, View paramView2)
  {
    super.requestChildFocus(paramView1, paramView2);
    if ((!isInTouchMode()) && (!this.mCanSlide))
      if (paramView1 != this.mSlideableView)
        break label36;
    label36: for (boolean bool = true; ; bool = false)
    {
      this.mPreservedOpenState = bool;
      return;
    }
  }

  void setAllChildrenVisible()
  {
    int i = 0;
    int j = getChildCount();
    while (i < j)
    {
      View localView = getChildAt(i);
      if (localView.getVisibility() == 4)
        localView.setVisibility(0);
      i++;
    }
  }

  public void setCoveredFadeColor(int paramInt)
  {
    this.mCoveredFadeColor = paramInt;
  }

  public void setPanelSlideListener(PanelSlideListener paramPanelSlideListener)
  {
    this.mPanelSlideListener = paramPanelSlideListener;
  }

  public void setParallaxDistance(int paramInt)
  {
    this.mParallaxBy = paramInt;
    requestLayout();
  }

  @Deprecated
  public void setShadowDrawable(Drawable paramDrawable)
  {
    setShadowDrawableLeft(paramDrawable);
  }

  public void setShadowDrawableLeft(Drawable paramDrawable)
  {
    this.mShadowDrawableLeft = paramDrawable;
  }

  public void setShadowDrawableRight(Drawable paramDrawable)
  {
    this.mShadowDrawableRight = paramDrawable;
  }

  @Deprecated
  public void setShadowResource(@DrawableRes int paramInt)
  {
    setShadowDrawable(getResources().getDrawable(paramInt));
  }

  public void setShadowResourceLeft(int paramInt)
  {
    setShadowDrawableLeft(getResources().getDrawable(paramInt));
  }

  public void setShadowResourceRight(int paramInt)
  {
    setShadowDrawableRight(getResources().getDrawable(paramInt));
  }

  public void setSliderFadeColor(int paramInt)
  {
    this.mSliderFadeColor = paramInt;
  }

  @Deprecated
  public void smoothSlideClosed()
  {
    closePane();
  }

  @Deprecated
  public void smoothSlideOpen()
  {
    openPane();
  }

  boolean smoothSlideTo(float paramFloat, int paramInt)
  {
    if (!this.mCanSlide);
    while (true)
    {
      return false;
      boolean bool = isLayoutRtlSupport();
      LayoutParams localLayoutParams = (LayoutParams)this.mSlideableView.getLayoutParams();
      int j;
      int k;
      if (bool)
      {
        j = getPaddingRight() + localLayoutParams.rightMargin;
        k = this.mSlideableView.getWidth();
      }
      for (int i = (int)(getWidth() - (j + paramFloat * this.mSlideRange + k)); this.mDragHelper.smoothSlideViewTo(this.mSlideableView, i, this.mSlideableView.getTop()); i = (int)(getPaddingLeft() + localLayoutParams.leftMargin + paramFloat * this.mSlideRange))
      {
        setAllChildrenVisible();
        ViewCompat.postInvalidateOnAnimation(this);
        return true;
      }
    }
  }

  void updateObscuredViewsVisibility(View paramView)
  {
    boolean bool = isLayoutRtlSupport();
    int i;
    int j;
    label29: int k;
    int m;
    int i1;
    int i2;
    int i3;
    int n;
    if (bool)
    {
      i = getWidth() - getPaddingRight();
      if (!bool)
        break label120;
      j = getPaddingLeft();
      k = getPaddingTop();
      m = getHeight() - getPaddingBottom();
      if ((paramView == null) || (!viewIsOpaque(paramView)))
        break label134;
      i1 = paramView.getLeft();
      i2 = paramView.getRight();
      i3 = paramView.getTop();
      n = paramView.getBottom();
    }
    int i4;
    View localView;
    while (true)
    {
      i4 = 0;
      int i5 = getChildCount();
      if (i4 < i5)
      {
        localView = getChildAt(i4);
        if (localView != paramView)
          break label149;
      }
      return;
      i = getPaddingLeft();
      break;
      label120: j = getWidth() - getPaddingRight();
      break label29;
      label134: n = 0;
      i1 = 0;
      i2 = 0;
      i3 = 0;
    }
    label149: int i6;
    label157: int i9;
    if (bool)
    {
      i6 = j;
      int i7 = Math.max(i6, localView.getLeft());
      int i8 = Math.max(k, localView.getTop());
      if (!bool)
        break label262;
      i9 = i;
      label188: int i10 = Math.min(i9, localView.getRight());
      int i11 = Math.min(m, localView.getBottom());
      if ((i7 < i1) || (i8 < i3) || (i10 > i2) || (i11 > n))
        break label269;
    }
    label262: label269: for (int i12 = 4; ; i12 = 0)
    {
      localView.setVisibility(i12);
      i4++;
      break;
      i6 = i;
      break label157;
      i9 = j;
      break label188;
    }
  }

  private class DisableLayerRunnable
    implements Runnable
  {
    final View mChildView;

    DisableLayerRunnable(View arg2)
    {
      Object localObject;
      this.mChildView = localObject;
    }

    public void run()
    {
      if (this.mChildView.getParent() == SlidingPaneLayout.this)
      {
        ViewCompat.setLayerType(this.mChildView, 0, null);
        SlidingPaneLayout.this.invalidateChildRegion(this.mChildView);
      }
      SlidingPaneLayout.this.mPostedRunnables.remove(this);
    }
  }

  public static class LayoutParams extends ViewGroup.MarginLayoutParams
  {
    private static final int[] ATTRS = { 16843137 };
    Paint dimPaint;
    boolean dimWhenOffset;
    boolean slideable;
    public float weight = 0.0F;

    public LayoutParams()
    {
      super(-1);
    }

    public LayoutParams(int paramInt1, int paramInt2)
    {
      super(paramInt2);
    }

    public LayoutParams(Context paramContext, AttributeSet paramAttributeSet)
    {
      super(paramAttributeSet);
      TypedArray localTypedArray = paramContext.obtainStyledAttributes(paramAttributeSet, ATTRS);
      this.weight = localTypedArray.getFloat(0, 0.0F);
      localTypedArray.recycle();
    }

    public LayoutParams(LayoutParams paramLayoutParams)
    {
      super();
      this.weight = paramLayoutParams.weight;
    }

    public LayoutParams(ViewGroup.LayoutParams paramLayoutParams)
    {
      super();
    }

    public LayoutParams(ViewGroup.MarginLayoutParams paramMarginLayoutParams)
    {
      super();
    }
  }

  public static abstract interface PanelSlideListener
  {
    public abstract void onPanelClosed(View paramView);

    public abstract void onPanelOpened(View paramView);

    public abstract void onPanelSlide(View paramView, float paramFloat);
  }

  static class SavedState extends View.BaseSavedState
  {
    public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator()
    {
      public SlidingPaneLayout.SavedState createFromParcel(Parcel paramAnonymousParcel)
      {
        return new SlidingPaneLayout.SavedState(paramAnonymousParcel, null);
      }

      public SlidingPaneLayout.SavedState[] newArray(int paramAnonymousInt)
      {
        return new SlidingPaneLayout.SavedState[paramAnonymousInt];
      }
    };
    boolean isOpen;

    private SavedState(Parcel paramParcel)
    {
      super();
      if (paramParcel.readInt() != 0);
      for (boolean bool = true; ; bool = false)
      {
        this.isOpen = bool;
        return;
      }
    }

    SavedState(Parcelable paramParcelable)
    {
      super();
    }

    public void writeToParcel(Parcel paramParcel, int paramInt)
    {
      super.writeToParcel(paramParcel, paramInt);
      if (this.isOpen);
      for (int i = 1; ; i = 0)
      {
        paramParcel.writeInt(i);
        return;
      }
    }
  }

  static abstract interface SlidingPanelLayoutImpl
  {
    public abstract void invalidateChildRegion(SlidingPaneLayout paramSlidingPaneLayout, View paramView);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.widget.SlidingPaneLayout
 * JD-Core Version:    0.6.2
 */
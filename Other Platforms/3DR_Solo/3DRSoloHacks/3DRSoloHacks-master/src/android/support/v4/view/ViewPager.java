package android.support.v4.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.widget.EdgeEffectCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ViewPager extends ViewGroup
{
  private static final int CLOSE_ENOUGH = 2;
  private static final Comparator<ItemInfo> COMPARATOR = new Comparator()
  {
    public int compare(ViewPager.ItemInfo paramAnonymousItemInfo1, ViewPager.ItemInfo paramAnonymousItemInfo2)
    {
      return paramAnonymousItemInfo1.position - paramAnonymousItemInfo2.position;
    }
  };
  private static final boolean DEBUG = false;
  private static final int DEFAULT_GUTTER_SIZE = 16;
  private static final int DEFAULT_OFFSCREEN_PAGES = 1;
  private static final int DRAW_ORDER_DEFAULT = 0;
  private static final int DRAW_ORDER_FORWARD = 1;
  private static final int DRAW_ORDER_REVERSE = 2;
  private static final int INVALID_POINTER = -1;
  private static final int[] LAYOUT_ATTRS = { 16842931 };
  private static final int MAX_SETTLE_DURATION = 600;
  private static final int MIN_DISTANCE_FOR_FLING = 25;
  private static final int MIN_FLING_VELOCITY = 400;
  public static final int SCROLL_STATE_DRAGGING = 1;
  public static final int SCROLL_STATE_IDLE = 0;
  public static final int SCROLL_STATE_SETTLING = 2;
  private static final String TAG = "ViewPager";
  private static final boolean USE_CACHE;
  private static final Interpolator sInterpolator = new Interpolator()
  {
    public float getInterpolation(float paramAnonymousFloat)
    {
      float f = paramAnonymousFloat - 1.0F;
      return 1.0F + f * (f * (f * (f * f)));
    }
  };
  private static final ViewPositionComparator sPositionComparator = new ViewPositionComparator();
  private int mActivePointerId = -1;
  private PagerAdapter mAdapter;
  private OnAdapterChangeListener mAdapterChangeListener;
  private int mBottomPageBounds;
  private boolean mCalledSuper;
  private int mChildHeightMeasureSpec;
  private int mChildWidthMeasureSpec;
  private int mCloseEnough;
  private int mCurItem;
  private int mDecorChildCount;
  private int mDefaultGutterSize;
  private int mDrawingOrder;
  private ArrayList<View> mDrawingOrderedChildren;
  private final Runnable mEndScrollRunnable = new Runnable()
  {
    public void run()
    {
      ViewPager.this.setScrollState(0);
      ViewPager.this.populate();
    }
  };
  private int mExpectedAdapterCount;
  private long mFakeDragBeginTime;
  private boolean mFakeDragging;
  private boolean mFirstLayout = true;
  private float mFirstOffset = -3.402824E+38F;
  private int mFlingDistance;
  private int mGutterSize;
  private boolean mInLayout;
  private float mInitialMotionX;
  private float mInitialMotionY;
  private OnPageChangeListener mInternalPageChangeListener;
  private boolean mIsBeingDragged;
  private boolean mIsUnableToDrag;
  private final ArrayList<ItemInfo> mItems = new ArrayList();
  private float mLastMotionX;
  private float mLastMotionY;
  private float mLastOffset = 3.4028235E+38F;
  private EdgeEffectCompat mLeftEdge;
  private Drawable mMarginDrawable;
  private int mMaximumVelocity;
  private int mMinimumVelocity;
  private boolean mNeedCalculatePageOffsets = false;
  private PagerObserver mObserver;
  private int mOffscreenPageLimit = 1;
  private OnPageChangeListener mOnPageChangeListener;
  private List<OnPageChangeListener> mOnPageChangeListeners;
  private int mPageMargin;
  private PageTransformer mPageTransformer;
  private boolean mPopulatePending;
  private Parcelable mRestoredAdapterState = null;
  private ClassLoader mRestoredClassLoader = null;
  private int mRestoredCurItem = -1;
  private EdgeEffectCompat mRightEdge;
  private int mScrollState = 0;
  private Scroller mScroller;
  private boolean mScrollingCacheEnabled;
  private Method mSetChildrenDrawingOrderEnabled;
  private final ItemInfo mTempItem = new ItemInfo();
  private final Rect mTempRect = new Rect();
  private int mTopPageBounds;
  private int mTouchSlop;
  private VelocityTracker mVelocityTracker;

  public ViewPager(Context paramContext)
  {
    super(paramContext);
    initViewPager();
  }

  public ViewPager(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
    initViewPager();
  }

  private void calculatePageOffsets(ItemInfo paramItemInfo1, int paramInt, ItemInfo paramItemInfo2)
  {
    int i = this.mAdapter.getCount();
    int j = getClientWidth();
    float f1;
    int i5;
    int i8;
    float f7;
    if (j > 0)
    {
      f1 = this.mPageMargin / j;
      if (paramItemInfo2 == null)
        break label371;
      i5 = paramItemInfo2.position;
      if (i5 < paramItemInfo1.position)
      {
        i8 = 0;
        f7 = f1 + (paramItemInfo2.offset + paramItemInfo2.widthFactor);
      }
    }
    else
    {
      for (int i9 = i5 + 1; ; i9++)
      {
        if ((i9 > paramItemInfo1.position) || (i8 >= this.mItems.size()))
          break label371;
        ItemInfo localItemInfo4 = (ItemInfo)this.mItems.get(i8);
        while (true)
          if ((i9 > localItemInfo4.position) && (i8 < -1 + this.mItems.size()))
          {
            i8++;
            localItemInfo4 = (ItemInfo)this.mItems.get(i8);
            continue;
            f1 = 0.0F;
            break;
          }
        while (i9 < localItemInfo4.position)
        {
          f7 += f1 + this.mAdapter.getPageWidth(i9);
          i9++;
        }
        localItemInfo4.offset = f7;
        f7 += f1 + localItemInfo4.widthFactor;
      }
    }
    if (i5 > paramItemInfo1.position)
    {
      int i6 = -1 + this.mItems.size();
      float f6 = paramItemInfo2.offset;
      for (int i7 = i5 - 1; (i7 >= paramItemInfo1.position) && (i6 >= 0); i7--)
      {
        for (ItemInfo localItemInfo3 = (ItemInfo)this.mItems.get(i6); (i7 < localItemInfo3.position) && (i6 > 0); localItemInfo3 = (ItemInfo)this.mItems.get(i6))
          i6--;
        while (i7 > localItemInfo3.position)
        {
          f6 -= f1 + this.mAdapter.getPageWidth(i7);
          i7--;
        }
        f6 -= f1 + localItemInfo3.widthFactor;
        localItemInfo3.offset = f6;
      }
    }
    label371: int k = this.mItems.size();
    float f2 = paramItemInfo1.offset;
    int m = -1 + paramItemInfo1.position;
    float f3;
    float f4;
    label437: int n;
    if (paramItemInfo1.position == 0)
    {
      f3 = paramItemInfo1.offset;
      this.mFirstOffset = f3;
      if (paramItemInfo1.position != i - 1)
        break label518;
      f4 = paramItemInfo1.offset + paramItemInfo1.widthFactor - 1.0F;
      this.mLastOffset = f4;
      n = paramInt - 1;
    }
    while (true)
    {
      if (n < 0)
        break label568;
      ItemInfo localItemInfo2 = (ItemInfo)this.mItems.get(n);
      while (true)
        if (m > localItemInfo2.position)
        {
          PagerAdapter localPagerAdapter2 = this.mAdapter;
          int i4 = m - 1;
          f2 -= f1 + localPagerAdapter2.getPageWidth(m);
          m = i4;
          continue;
          f3 = -3.402824E+38F;
          break;
          label518: f4 = 3.4028235E+38F;
          break label437;
        }
      f2 -= f1 + localItemInfo2.widthFactor;
      localItemInfo2.offset = f2;
      if (localItemInfo2.position == 0)
        this.mFirstOffset = f2;
      n--;
      m--;
    }
    label568: float f5 = f1 + (paramItemInfo1.offset + paramItemInfo1.widthFactor);
    int i1 = 1 + paramItemInfo1.position;
    int i2 = paramInt + 1;
    while (i2 < k)
    {
      ItemInfo localItemInfo1 = (ItemInfo)this.mItems.get(i2);
      while (i1 < localItemInfo1.position)
      {
        PagerAdapter localPagerAdapter1 = this.mAdapter;
        int i3 = i1 + 1;
        f5 += f1 + localPagerAdapter1.getPageWidth(i1);
        i1 = i3;
      }
      if (localItemInfo1.position == i - 1)
        this.mLastOffset = (f5 + localItemInfo1.widthFactor - 1.0F);
      localItemInfo1.offset = f5;
      f5 += f1 + localItemInfo1.widthFactor;
      i2++;
      i1++;
    }
    this.mNeedCalculatePageOffsets = false;
  }

  private void completeScroll(boolean paramBoolean)
  {
    if (this.mScrollState == 2);
    for (int i = 1; ; i = 0)
    {
      if (i != 0)
      {
        setScrollingCacheEnabled(false);
        this.mScroller.abortAnimation();
        int k = getScrollX();
        int m = getScrollY();
        int n = this.mScroller.getCurrX();
        int i1 = this.mScroller.getCurrY();
        if ((k != n) || (m != i1))
        {
          scrollTo(n, i1);
          if (n != k)
            pageScrolled(n);
        }
      }
      this.mPopulatePending = false;
      for (int j = 0; j < this.mItems.size(); j++)
      {
        ItemInfo localItemInfo = (ItemInfo)this.mItems.get(j);
        if (localItemInfo.scrolling)
        {
          i = 1;
          localItemInfo.scrolling = false;
        }
      }
    }
    if (i != 0)
    {
      if (paramBoolean)
        ViewCompat.postOnAnimation(this, this.mEndScrollRunnable);
    }
    else
      return;
    this.mEndScrollRunnable.run();
  }

  private int determineTargetPage(int paramInt1, float paramFloat, int paramInt2, int paramInt3)
  {
    int i;
    if ((Math.abs(paramInt3) > this.mFlingDistance) && (Math.abs(paramInt2) > this.mMinimumVelocity))
    {
      if (paramInt2 > 0);
      for (i = paramInt1; ; i = paramInt1 + 1)
      {
        if (this.mItems.size() > 0)
        {
          ItemInfo localItemInfo1 = (ItemInfo)this.mItems.get(0);
          ItemInfo localItemInfo2 = (ItemInfo)this.mItems.get(-1 + this.mItems.size());
          i = Math.max(localItemInfo1.position, Math.min(i, localItemInfo2.position));
        }
        return i;
      }
    }
    if (paramInt1 >= this.mCurItem);
    for (float f = 0.4F; ; f = 0.6F)
    {
      i = (int)(f + (paramFloat + paramInt1));
      break;
    }
  }

  private void dispatchOnPageScrolled(int paramInt1, float paramFloat, int paramInt2)
  {
    if (this.mOnPageChangeListener != null)
      this.mOnPageChangeListener.onPageScrolled(paramInt1, paramFloat, paramInt2);
    if (this.mOnPageChangeListeners != null)
    {
      int i = 0;
      int j = this.mOnPageChangeListeners.size();
      while (i < j)
      {
        OnPageChangeListener localOnPageChangeListener = (OnPageChangeListener)this.mOnPageChangeListeners.get(i);
        if (localOnPageChangeListener != null)
          localOnPageChangeListener.onPageScrolled(paramInt1, paramFloat, paramInt2);
        i++;
      }
    }
    if (this.mInternalPageChangeListener != null)
      this.mInternalPageChangeListener.onPageScrolled(paramInt1, paramFloat, paramInt2);
  }

  private void dispatchOnPageSelected(int paramInt)
  {
    if (this.mOnPageChangeListener != null)
      this.mOnPageChangeListener.onPageSelected(paramInt);
    if (this.mOnPageChangeListeners != null)
    {
      int i = 0;
      int j = this.mOnPageChangeListeners.size();
      while (i < j)
      {
        OnPageChangeListener localOnPageChangeListener = (OnPageChangeListener)this.mOnPageChangeListeners.get(i);
        if (localOnPageChangeListener != null)
          localOnPageChangeListener.onPageSelected(paramInt);
        i++;
      }
    }
    if (this.mInternalPageChangeListener != null)
      this.mInternalPageChangeListener.onPageSelected(paramInt);
  }

  private void dispatchOnScrollStateChanged(int paramInt)
  {
    if (this.mOnPageChangeListener != null)
      this.mOnPageChangeListener.onPageScrollStateChanged(paramInt);
    if (this.mOnPageChangeListeners != null)
    {
      int i = 0;
      int j = this.mOnPageChangeListeners.size();
      while (i < j)
      {
        OnPageChangeListener localOnPageChangeListener = (OnPageChangeListener)this.mOnPageChangeListeners.get(i);
        if (localOnPageChangeListener != null)
          localOnPageChangeListener.onPageScrollStateChanged(paramInt);
        i++;
      }
    }
    if (this.mInternalPageChangeListener != null)
      this.mInternalPageChangeListener.onPageScrollStateChanged(paramInt);
  }

  private void enableLayers(boolean paramBoolean)
  {
    int i = getChildCount();
    int j = 0;
    if (j < i)
    {
      if (paramBoolean);
      for (int k = 2; ; k = 0)
      {
        ViewCompat.setLayerType(getChildAt(j), k, null);
        j++;
        break;
      }
    }
  }

  private void endDrag()
  {
    this.mIsBeingDragged = false;
    this.mIsUnableToDrag = false;
    if (this.mVelocityTracker != null)
    {
      this.mVelocityTracker.recycle();
      this.mVelocityTracker = null;
    }
  }

  private Rect getChildRectInPagerCoordinates(Rect paramRect, View paramView)
  {
    if (paramRect == null)
      paramRect = new Rect();
    if (paramView == null)
      paramRect.set(0, 0, 0, 0);
    while (true)
    {
      return paramRect;
      paramRect.left = paramView.getLeft();
      paramRect.right = paramView.getRight();
      paramRect.top = paramView.getTop();
      paramRect.bottom = paramView.getBottom();
      ViewGroup localViewGroup;
      for (ViewParent localViewParent = paramView.getParent(); ((localViewParent instanceof ViewGroup)) && (localViewParent != this); localViewParent = localViewGroup.getParent())
      {
        localViewGroup = (ViewGroup)localViewParent;
        paramRect.left += localViewGroup.getLeft();
        paramRect.right += localViewGroup.getRight();
        paramRect.top += localViewGroup.getTop();
        paramRect.bottom += localViewGroup.getBottom();
      }
    }
  }

  private int getClientWidth()
  {
    return getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
  }

  private ItemInfo infoForCurrentScrollPosition()
  {
    int i = getClientWidth();
    float f1;
    float f2;
    int j;
    float f3;
    float f4;
    int k;
    Object localObject;
    int m;
    if (i > 0)
    {
      f1 = getScrollX() / i;
      f2 = 0.0F;
      if (i > 0)
        f2 = this.mPageMargin / i;
      j = -1;
      f3 = 0.0F;
      f4 = 0.0F;
      k = 1;
      localObject = null;
      m = 0;
    }
    while (true)
    {
      ItemInfo localItemInfo;
      float f5;
      if (m < this.mItems.size())
      {
        localItemInfo = (ItemInfo)this.mItems.get(m);
        if ((k == 0) && (localItemInfo.position != j + 1))
        {
          localItemInfo = this.mTempItem;
          localItemInfo.offset = (f2 + (f3 + f4));
          localItemInfo.position = (j + 1);
          localItemInfo.widthFactor = this.mAdapter.getPageWidth(localItemInfo.position);
          m--;
        }
        f5 = localItemInfo.offset;
        float f6 = f2 + (f5 + localItemInfo.widthFactor);
        if ((k != 0) || (f1 >= f5))
        {
          if ((f1 >= f6) && (m != -1 + this.mItems.size()))
            break label205;
          localObject = localItemInfo;
        }
      }
      return localObject;
      f1 = 0.0F;
      break;
      label205: j = localItemInfo.position;
      f3 = f5;
      f4 = localItemInfo.widthFactor;
      localObject = localItemInfo;
      m++;
      k = 0;
    }
  }

  private boolean isGutterDrag(float paramFloat1, float paramFloat2)
  {
    return ((paramFloat1 < this.mGutterSize) && (paramFloat2 > 0.0F)) || ((paramFloat1 > getWidth() - this.mGutterSize) && (paramFloat2 < 0.0F));
  }

  private void onSecondaryPointerUp(MotionEvent paramMotionEvent)
  {
    int i = MotionEventCompat.getActionIndex(paramMotionEvent);
    if (MotionEventCompat.getPointerId(paramMotionEvent, i) == this.mActivePointerId)
      if (i != 0)
        break label56;
    label56: for (int j = 1; ; j = 0)
    {
      this.mLastMotionX = MotionEventCompat.getX(paramMotionEvent, j);
      this.mActivePointerId = MotionEventCompat.getPointerId(paramMotionEvent, j);
      if (this.mVelocityTracker != null)
        this.mVelocityTracker.clear();
      return;
    }
  }

  private boolean pageScrolled(int paramInt)
  {
    boolean bool1;
    if (this.mItems.size() == 0)
    {
      this.mCalledSuper = false;
      onPageScrolled(0, 0.0F, 0);
      boolean bool2 = this.mCalledSuper;
      bool1 = false;
      if (!bool2)
        throw new IllegalStateException("onPageScrolled did not call superclass implementation");
    }
    else
    {
      ItemInfo localItemInfo = infoForCurrentScrollPosition();
      int i = getClientWidth();
      int j = i + this.mPageMargin;
      float f1 = this.mPageMargin / i;
      int k = localItemInfo.position;
      float f2 = (paramInt / i - localItemInfo.offset) / (f1 + localItemInfo.widthFactor);
      int m = (int)(f2 * j);
      this.mCalledSuper = false;
      onPageScrolled(k, f2, m);
      if (!this.mCalledSuper)
        throw new IllegalStateException("onPageScrolled did not call superclass implementation");
      bool1 = true;
    }
    return bool1;
  }

  private boolean performDrag(float paramFloat)
  {
    float f1 = this.mLastMotionX - paramFloat;
    this.mLastMotionX = paramFloat;
    float f2 = f1 + getScrollX();
    int i = getClientWidth();
    float f3 = i * this.mFirstOffset;
    float f4 = i * this.mLastOffset;
    int j = 1;
    int k = 1;
    ItemInfo localItemInfo1 = (ItemInfo)this.mItems.get(0);
    ItemInfo localItemInfo2 = (ItemInfo)this.mItems.get(-1 + this.mItems.size());
    if (localItemInfo1.position != 0)
    {
      j = 0;
      f3 = localItemInfo1.offset * i;
    }
    if (localItemInfo2.position != -1 + this.mAdapter.getCount())
    {
      k = 0;
      f4 = localItemInfo2.offset * i;
    }
    boolean bool1;
    if (f2 < f3)
    {
      bool1 = false;
      if (j != 0)
      {
        float f5 = f3 - f2;
        bool1 = this.mLeftEdge.onPull(Math.abs(f5) / i);
      }
      f2 = f3;
    }
    while (true)
    {
      this.mLastMotionX += f2 - (int)f2;
      scrollTo((int)f2, getScrollY());
      pageScrolled((int)f2);
      return bool1;
      boolean bool2 = f2 < f4;
      bool1 = false;
      if (bool2)
      {
        bool1 = false;
        if (k != 0)
        {
          float f6 = f2 - f4;
          bool1 = this.mRightEdge.onPull(Math.abs(f6) / i);
        }
        f2 = f4;
      }
    }
  }

  private void recomputeScrollPosition(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if ((paramInt2 > 0) && (!this.mItems.isEmpty()))
    {
      int j = paramInt3 + (paramInt1 - getPaddingLeft() - getPaddingRight());
      int k = paramInt4 + (paramInt2 - getPaddingLeft() - getPaddingRight());
      int m = (int)(getScrollX() / k * j);
      scrollTo(m, getScrollY());
      if (!this.mScroller.isFinished())
      {
        int n = this.mScroller.getDuration() - this.mScroller.timePassed();
        ItemInfo localItemInfo2 = infoForPosition(this.mCurItem);
        this.mScroller.startScroll(m, 0, (int)(localItemInfo2.offset * paramInt1), 0, n);
      }
      return;
    }
    ItemInfo localItemInfo1 = infoForPosition(this.mCurItem);
    if (localItemInfo1 != null);
    for (float f = Math.min(localItemInfo1.offset, this.mLastOffset); ; f = 0.0F)
    {
      int i = (int)(f * (paramInt1 - getPaddingLeft() - getPaddingRight()));
      if (i == getScrollX())
        break;
      completeScroll(false);
      scrollTo(i, getScrollY());
      return;
    }
  }

  private void removeNonDecorViews()
  {
    for (int i = 0; i < getChildCount(); i++)
      if (!((LayoutParams)getChildAt(i).getLayoutParams()).isDecor)
      {
        removeViewAt(i);
        i--;
      }
  }

  private void requestParentDisallowInterceptTouchEvent(boolean paramBoolean)
  {
    ViewParent localViewParent = getParent();
    if (localViewParent != null)
      localViewParent.requestDisallowInterceptTouchEvent(paramBoolean);
  }

  private void scrollToItem(int paramInt1, boolean paramBoolean1, int paramInt2, boolean paramBoolean2)
  {
    ItemInfo localItemInfo = infoForPosition(paramInt1);
    int i = 0;
    if (localItemInfo != null)
      i = (int)(getClientWidth() * Math.max(this.mFirstOffset, Math.min(localItemInfo.offset, this.mLastOffset)));
    if (paramBoolean1)
    {
      smoothScrollTo(i, 0, paramInt2);
      if (paramBoolean2)
        dispatchOnPageSelected(paramInt1);
      return;
    }
    if (paramBoolean2)
      dispatchOnPageSelected(paramInt1);
    completeScroll(false);
    scrollTo(i, 0);
    pageScrolled(i);
  }

  private void setScrollState(int paramInt)
  {
    if (this.mScrollState == paramInt)
      return;
    this.mScrollState = paramInt;
    if (this.mPageTransformer != null)
      if (paramInt == 0)
        break label38;
    label38: for (boolean bool = true; ; bool = false)
    {
      enableLayers(bool);
      dispatchOnScrollStateChanged(paramInt);
      return;
    }
  }

  private void setScrollingCacheEnabled(boolean paramBoolean)
  {
    if (this.mScrollingCacheEnabled != paramBoolean)
      this.mScrollingCacheEnabled = paramBoolean;
  }

  private void sortChildDrawingOrder()
  {
    if (this.mDrawingOrder != 0)
    {
      if (this.mDrawingOrderedChildren == null)
        this.mDrawingOrderedChildren = new ArrayList();
      while (true)
      {
        int i = getChildCount();
        for (int j = 0; j < i; j++)
        {
          View localView = getChildAt(j);
          this.mDrawingOrderedChildren.add(localView);
        }
        this.mDrawingOrderedChildren.clear();
      }
      Collections.sort(this.mDrawingOrderedChildren, sPositionComparator);
    }
  }

  public void addFocusables(ArrayList<View> paramArrayList, int paramInt1, int paramInt2)
  {
    int i = paramArrayList.size();
    int j = getDescendantFocusability();
    if (j != 393216)
      for (int k = 0; k < getChildCount(); k++)
      {
        View localView = getChildAt(k);
        if (localView.getVisibility() == 0)
        {
          ItemInfo localItemInfo = infoForChild(localView);
          if ((localItemInfo != null) && (localItemInfo.position == this.mCurItem))
            localView.addFocusables(paramArrayList, paramInt1, paramInt2);
        }
      }
    if (((j == 262144) && (i != paramArrayList.size())) || (!isFocusable()));
    while ((((paramInt2 & 0x1) == 1) && (isInTouchMode()) && (!isFocusableInTouchMode())) || (paramArrayList == null))
      return;
    paramArrayList.add(this);
  }

  ItemInfo addNewItem(int paramInt1, int paramInt2)
  {
    ItemInfo localItemInfo = new ItemInfo();
    localItemInfo.position = paramInt1;
    localItemInfo.object = this.mAdapter.instantiateItem(this, paramInt1);
    localItemInfo.widthFactor = this.mAdapter.getPageWidth(paramInt1);
    if ((paramInt2 < 0) || (paramInt2 >= this.mItems.size()))
    {
      this.mItems.add(localItemInfo);
      return localItemInfo;
    }
    this.mItems.add(paramInt2, localItemInfo);
    return localItemInfo;
  }

  public void addOnPageChangeListener(OnPageChangeListener paramOnPageChangeListener)
  {
    if (this.mOnPageChangeListeners == null)
      this.mOnPageChangeListeners = new ArrayList();
    this.mOnPageChangeListeners.add(paramOnPageChangeListener);
  }

  public void addTouchables(ArrayList<View> paramArrayList)
  {
    for (int i = 0; i < getChildCount(); i++)
    {
      View localView = getChildAt(i);
      if (localView.getVisibility() == 0)
      {
        ItemInfo localItemInfo = infoForChild(localView);
        if ((localItemInfo != null) && (localItemInfo.position == this.mCurItem))
          localView.addTouchables(paramArrayList);
      }
    }
  }

  public void addView(View paramView, int paramInt, ViewGroup.LayoutParams paramLayoutParams)
  {
    if (!checkLayoutParams(paramLayoutParams))
      paramLayoutParams = generateLayoutParams(paramLayoutParams);
    LayoutParams localLayoutParams = (LayoutParams)paramLayoutParams;
    localLayoutParams.isDecor |= paramView instanceof Decor;
    if (this.mInLayout)
    {
      if ((localLayoutParams != null) && (localLayoutParams.isDecor))
        throw new IllegalStateException("Cannot add pager decor view during layout");
      localLayoutParams.needsMeasure = true;
      addViewInLayout(paramView, paramInt, paramLayoutParams);
      return;
    }
    super.addView(paramView, paramInt, paramLayoutParams);
  }

  public boolean arrowScroll(int paramInt)
  {
    View localView1 = findFocus();
    View localView2;
    boolean bool2;
    if (localView1 == this)
    {
      localView1 = null;
      localView2 = FocusFinder.getInstance().findNextFocus(this, localView1, paramInt);
      if ((localView2 == null) || (localView2 == localView1))
        break label329;
      if (paramInt != 17)
        break label261;
      int m = getChildRectInPagerCoordinates(this.mTempRect, localView2).left;
      int n = getChildRectInPagerCoordinates(this.mTempRect, localView1).left;
      if ((localView1 == null) || (m < n))
        break label251;
      bool2 = pageLeft();
    }
    while (true)
    {
      if (bool2)
        playSoundEffect(SoundEffectConstants.getContantForFocusDirection(paramInt));
      return bool2;
      if (localView1 == null)
        break;
      StringBuilder localStringBuilder;
      for (ViewParent localViewParent1 = localView1.getParent(); ; localViewParent1 = localViewParent1.getParent())
      {
        boolean bool1 = localViewParent1 instanceof ViewGroup;
        int i = 0;
        if (bool1)
        {
          if (localViewParent1 == this)
            i = 1;
        }
        else
        {
          if (i != 0)
            break;
          localStringBuilder = new StringBuilder();
          localStringBuilder.append(localView1.getClass().getSimpleName());
          for (ViewParent localViewParent2 = localView1.getParent(); (localViewParent2 instanceof ViewGroup); localViewParent2 = localViewParent2.getParent())
            localStringBuilder.append(" => ").append(localViewParent2.getClass().getSimpleName());
        }
      }
      Log.e("ViewPager", "arrowScroll tried to find focus based on non-child current focused view " + localStringBuilder.toString());
      localView1 = null;
      break;
      label251: bool2 = localView2.requestFocus();
      continue;
      label261: bool2 = false;
      if (paramInt == 66)
      {
        int j = getChildRectInPagerCoordinates(this.mTempRect, localView2).left;
        int k = getChildRectInPagerCoordinates(this.mTempRect, localView1).left;
        if ((localView1 != null) && (j <= k))
        {
          bool2 = pageRight();
        }
        else
        {
          bool2 = localView2.requestFocus();
          continue;
          label329: if ((paramInt == 17) || (paramInt == 1))
          {
            bool2 = pageLeft();
          }
          else if (paramInt != 66)
          {
            bool2 = false;
            if (paramInt != 2);
          }
          else
          {
            bool2 = pageRight();
          }
        }
      }
    }
  }

  public boolean beginFakeDrag()
  {
    if (this.mIsBeingDragged)
      return false;
    this.mFakeDragging = true;
    setScrollState(1);
    this.mLastMotionX = 0.0F;
    this.mInitialMotionX = 0.0F;
    if (this.mVelocityTracker == null)
      this.mVelocityTracker = VelocityTracker.obtain();
    while (true)
    {
      long l = SystemClock.uptimeMillis();
      MotionEvent localMotionEvent = MotionEvent.obtain(l, l, 0, 0.0F, 0.0F, 0);
      this.mVelocityTracker.addMovement(localMotionEvent);
      localMotionEvent.recycle();
      this.mFakeDragBeginTime = l;
      return true;
      this.mVelocityTracker.clear();
    }
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
        if ((paramInt2 + i >= localView.getLeft()) && (paramInt2 + i < localView.getRight()) && (paramInt3 + j >= localView.getTop()) && (paramInt3 + j < localView.getBottom()) && (canScroll(localView, true, paramInt1, paramInt2 + i - localView.getLeft(), paramInt3 + j - localView.getTop())))
          return true;
      }
    }
    return (paramBoolean) && (ViewCompat.canScrollHorizontally(paramView, -paramInt1));
  }

  public boolean canScrollHorizontally(int paramInt)
  {
    boolean bool = true;
    if (this.mAdapter == null);
    int i;
    int j;
    do
    {
      return false;
      i = getClientWidth();
      j = getScrollX();
      if (paramInt < 0)
      {
        if (j > (int)(i * this.mFirstOffset));
        while (true)
        {
          return bool;
          bool = false;
        }
      }
    }
    while (paramInt <= 0);
    if (j < (int)(i * this.mLastOffset));
    while (true)
    {
      return bool;
      bool = false;
    }
  }

  protected boolean checkLayoutParams(ViewGroup.LayoutParams paramLayoutParams)
  {
    return ((paramLayoutParams instanceof LayoutParams)) && (super.checkLayoutParams(paramLayoutParams));
  }

  public void clearOnPageChangeListeners()
  {
    if (this.mOnPageChangeListeners != null)
      this.mOnPageChangeListeners.clear();
  }

  public void computeScroll()
  {
    if ((!this.mScroller.isFinished()) && (this.mScroller.computeScrollOffset()))
    {
      int i = getScrollX();
      int j = getScrollY();
      int k = this.mScroller.getCurrX();
      int m = this.mScroller.getCurrY();
      if ((i != k) || (j != m))
      {
        scrollTo(k, m);
        if (!pageScrolled(k))
        {
          this.mScroller.abortAnimation();
          scrollTo(0, m);
        }
      }
      ViewCompat.postInvalidateOnAnimation(this);
      return;
    }
    completeScroll(true);
  }

  void dataSetChanged()
  {
    int i = this.mAdapter.getCount();
    this.mExpectedAdapterCount = i;
    int j;
    int k;
    int m;
    int n;
    label55: ItemInfo localItemInfo;
    int i3;
    if ((this.mItems.size() < 1 + 2 * this.mOffscreenPageLimit) && (this.mItems.size() < i))
    {
      j = 1;
      k = this.mCurItem;
      m = 0;
      n = 0;
      if (n >= this.mItems.size())
        break label237;
      localItemInfo = (ItemInfo)this.mItems.get(n);
      i3 = this.mAdapter.getItemPosition(localItemInfo.object);
      if (i3 != -1)
        break label112;
    }
    while (true)
    {
      n++;
      break label55;
      j = 0;
      break;
      label112: if (i3 == -2)
      {
        this.mItems.remove(n);
        n--;
        if (m == 0)
        {
          this.mAdapter.startUpdate(this);
          m = 1;
        }
        this.mAdapter.destroyItem(this, localItemInfo.position, localItemInfo.object);
        j = 1;
        if (this.mCurItem == localItemInfo.position)
        {
          k = Math.max(0, Math.min(this.mCurItem, i - 1));
          j = 1;
        }
      }
      else if (localItemInfo.position != i3)
      {
        if (localItemInfo.position == this.mCurItem)
          k = i3;
        localItemInfo.position = i3;
        j = 1;
      }
    }
    label237: if (m != 0)
      this.mAdapter.finishUpdate(this);
    Collections.sort(this.mItems, COMPARATOR);
    if (j != 0)
    {
      int i1 = getChildCount();
      for (int i2 = 0; i2 < i1; i2++)
      {
        LayoutParams localLayoutParams = (LayoutParams)getChildAt(i2).getLayoutParams();
        if (!localLayoutParams.isDecor)
          localLayoutParams.widthFactor = 0.0F;
      }
      setCurrentItemInternal(k, false, true);
      requestLayout();
    }
  }

  public boolean dispatchKeyEvent(KeyEvent paramKeyEvent)
  {
    return (super.dispatchKeyEvent(paramKeyEvent)) || (executeKeyEvent(paramKeyEvent));
  }

  public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent paramAccessibilityEvent)
  {
    if (paramAccessibilityEvent.getEventType() == 4096)
      return super.dispatchPopulateAccessibilityEvent(paramAccessibilityEvent);
    int i = getChildCount();
    for (int j = 0; j < i; j++)
    {
      View localView = getChildAt(j);
      if (localView.getVisibility() == 0)
      {
        ItemInfo localItemInfo = infoForChild(localView);
        if ((localItemInfo != null) && (localItemInfo.position == this.mCurItem) && (localView.dispatchPopulateAccessibilityEvent(paramAccessibilityEvent)))
          return true;
      }
    }
    return false;
  }

  float distanceInfluenceForSnapDuration(float paramFloat)
  {
    return (float)Math.sin((float)(0.47123891676382D * (paramFloat - 0.5F)));
  }

  public void draw(Canvas paramCanvas)
  {
    super.draw(paramCanvas);
    int i = ViewCompat.getOverScrollMode(this);
    boolean bool2;
    if ((i == 0) || ((i == 1) && (this.mAdapter != null) && (this.mAdapter.getCount() > 1)))
    {
      boolean bool1 = this.mLeftEdge.isFinished();
      bool2 = false;
      if (!bool1)
      {
        int n = paramCanvas.save();
        int i1 = getHeight() - getPaddingTop() - getPaddingBottom();
        int i2 = getWidth();
        paramCanvas.rotate(270.0F);
        paramCanvas.translate(-i1 + getPaddingTop(), this.mFirstOffset * i2);
        this.mLeftEdge.setSize(i1, i2);
        bool2 = false | this.mLeftEdge.draw(paramCanvas);
        paramCanvas.restoreToCount(n);
      }
      if (!this.mRightEdge.isFinished())
      {
        int j = paramCanvas.save();
        int k = getWidth();
        int m = getHeight() - getPaddingTop() - getPaddingBottom();
        paramCanvas.rotate(90.0F);
        paramCanvas.translate(-getPaddingTop(), -(1.0F + this.mLastOffset) * k);
        this.mRightEdge.setSize(m, k);
        bool2 |= this.mRightEdge.draw(paramCanvas);
        paramCanvas.restoreToCount(j);
      }
    }
    while (true)
    {
      if (bool2)
        ViewCompat.postInvalidateOnAnimation(this);
      return;
      this.mLeftEdge.finish();
      this.mRightEdge.finish();
      bool2 = false;
    }
  }

  protected void drawableStateChanged()
  {
    super.drawableStateChanged();
    Drawable localDrawable = this.mMarginDrawable;
    if ((localDrawable != null) && (localDrawable.isStateful()))
      localDrawable.setState(getDrawableState());
  }

  public void endFakeDrag()
  {
    if (!this.mFakeDragging)
      throw new IllegalStateException("No fake drag in progress. Call beginFakeDrag first.");
    VelocityTracker localVelocityTracker = this.mVelocityTracker;
    localVelocityTracker.computeCurrentVelocity(1000, this.mMaximumVelocity);
    int i = (int)VelocityTrackerCompat.getXVelocity(localVelocityTracker, this.mActivePointerId);
    this.mPopulatePending = true;
    int j = getClientWidth();
    int k = getScrollX();
    ItemInfo localItemInfo = infoForCurrentScrollPosition();
    setCurrentItemInternal(determineTargetPage(localItemInfo.position, (k / j - localItemInfo.offset) / localItemInfo.widthFactor, i, (int)(this.mLastMotionX - this.mInitialMotionX)), true, true, i);
    endDrag();
    this.mFakeDragging = false;
  }

  public boolean executeKeyEvent(KeyEvent paramKeyEvent)
  {
    if (paramKeyEvent.getAction() == 0)
      switch (paramKeyEvent.getKeyCode())
      {
      default:
      case 21:
      case 22:
      case 61:
      }
    do
    {
      do
      {
        return false;
        return arrowScroll(17);
        return arrowScroll(66);
      }
      while (Build.VERSION.SDK_INT < 11);
      if (KeyEventCompat.hasNoModifiers(paramKeyEvent))
        return arrowScroll(2);
    }
    while (!KeyEventCompat.hasModifiers(paramKeyEvent, 1));
    return arrowScroll(1);
  }

  public void fakeDragBy(float paramFloat)
  {
    if (!this.mFakeDragging)
      throw new IllegalStateException("No fake drag in progress. Call beginFakeDrag first.");
    this.mLastMotionX = (paramFloat + this.mLastMotionX);
    float f1 = getScrollX() - paramFloat;
    int i = getClientWidth();
    float f2 = i * this.mFirstOffset;
    float f3 = i * this.mLastOffset;
    ItemInfo localItemInfo1 = (ItemInfo)this.mItems.get(0);
    ItemInfo localItemInfo2 = (ItemInfo)this.mItems.get(-1 + this.mItems.size());
    if (localItemInfo1.position != 0)
      f2 = localItemInfo1.offset * i;
    if (localItemInfo2.position != -1 + this.mAdapter.getCount())
      f3 = localItemInfo2.offset * i;
    if (f1 < f2)
      f1 = f2;
    while (true)
    {
      this.mLastMotionX += f1 - (int)f1;
      scrollTo((int)f1, getScrollY());
      pageScrolled((int)f1);
      long l = SystemClock.uptimeMillis();
      MotionEvent localMotionEvent = MotionEvent.obtain(this.mFakeDragBeginTime, l, 2, this.mLastMotionX, 0.0F, 0);
      this.mVelocityTracker.addMovement(localMotionEvent);
      localMotionEvent.recycle();
      return;
      if (f1 > f3)
        f1 = f3;
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
    return generateDefaultLayoutParams();
  }

  public PagerAdapter getAdapter()
  {
    return this.mAdapter;
  }

  protected int getChildDrawingOrder(int paramInt1, int paramInt2)
  {
    if (this.mDrawingOrder == 2);
    for (int i = paramInt1 - 1 - paramInt2; ; i = paramInt2)
      return ((LayoutParams)((View)this.mDrawingOrderedChildren.get(i)).getLayoutParams()).childIndex;
  }

  public int getCurrentItem()
  {
    return this.mCurItem;
  }

  public int getOffscreenPageLimit()
  {
    return this.mOffscreenPageLimit;
  }

  public int getPageMargin()
  {
    return this.mPageMargin;
  }

  ItemInfo infoForAnyChild(View paramView)
  {
    while (true)
    {
      ViewParent localViewParent = paramView.getParent();
      if (localViewParent == this)
        break;
      if ((localViewParent == null) || (!(localViewParent instanceof View)))
        return null;
      paramView = (View)localViewParent;
    }
    return infoForChild(paramView);
  }

  ItemInfo infoForChild(View paramView)
  {
    for (int i = 0; i < this.mItems.size(); i++)
    {
      ItemInfo localItemInfo = (ItemInfo)this.mItems.get(i);
      if (this.mAdapter.isViewFromObject(paramView, localItemInfo.object))
        return localItemInfo;
    }
    return null;
  }

  ItemInfo infoForPosition(int paramInt)
  {
    for (int i = 0; i < this.mItems.size(); i++)
    {
      ItemInfo localItemInfo = (ItemInfo)this.mItems.get(i);
      if (localItemInfo.position == paramInt)
        return localItemInfo;
    }
    return null;
  }

  void initViewPager()
  {
    setWillNotDraw(false);
    setDescendantFocusability(262144);
    setFocusable(true);
    Context localContext = getContext();
    this.mScroller = new Scroller(localContext, sInterpolator);
    ViewConfiguration localViewConfiguration = ViewConfiguration.get(localContext);
    float f = localContext.getResources().getDisplayMetrics().density;
    this.mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(localViewConfiguration);
    this.mMinimumVelocity = ((int)(400.0F * f));
    this.mMaximumVelocity = localViewConfiguration.getScaledMaximumFlingVelocity();
    this.mLeftEdge = new EdgeEffectCompat(localContext);
    this.mRightEdge = new EdgeEffectCompat(localContext);
    this.mFlingDistance = ((int)(25.0F * f));
    this.mCloseEnough = ((int)(2.0F * f));
    this.mDefaultGutterSize = ((int)(16.0F * f));
    ViewCompat.setAccessibilityDelegate(this, new ViewPager.MyAccessibilityDelegate(this));
    if (ViewCompat.getImportantForAccessibility(this) == 0)
      ViewCompat.setImportantForAccessibility(this, 1);
  }

  public boolean isFakeDragging()
  {
    return this.mFakeDragging;
  }

  protected void onAttachedToWindow()
  {
    super.onAttachedToWindow();
    this.mFirstLayout = true;
  }

  protected void onDetachedFromWindow()
  {
    removeCallbacks(this.mEndScrollRunnable);
    super.onDetachedFromWindow();
  }

  protected void onDraw(Canvas paramCanvas)
  {
    super.onDraw(paramCanvas);
    int i;
    int j;
    float f1;
    int k;
    ItemInfo localItemInfo;
    float f2;
    int m;
    int n;
    int i1;
    if ((this.mPageMargin > 0) && (this.mMarginDrawable != null) && (this.mItems.size() > 0) && (this.mAdapter != null))
    {
      i = getScrollX();
      j = getWidth();
      f1 = this.mPageMargin / j;
      k = 0;
      localItemInfo = (ItemInfo)this.mItems.get(0);
      f2 = localItemInfo.offset;
      m = this.mItems.size();
      n = localItemInfo.position;
      i1 = ((ItemInfo)this.mItems.get(m - 1)).position;
    }
    for (int i2 = n; ; i2++)
    {
      float f4;
      if (i2 < i1)
      {
        while ((i2 > localItemInfo.position) && (k < m))
        {
          ArrayList localArrayList = this.mItems;
          k++;
          localItemInfo = (ItemInfo)localArrayList.get(k);
        }
        if (i2 != localItemInfo.position)
          break label272;
        f4 = (localItemInfo.offset + localItemInfo.widthFactor) * j;
      }
      label272: float f3;
      for (f2 = f1 + (localItemInfo.offset + localItemInfo.widthFactor); ; f2 += f3 + f1)
      {
        if (f4 + this.mPageMargin > i)
        {
          this.mMarginDrawable.setBounds((int)f4, this.mTopPageBounds, (int)(0.5F + (f4 + this.mPageMargin)), this.mBottomPageBounds);
          this.mMarginDrawable.draw(paramCanvas);
        }
        if (f4 <= i + j)
          break;
        return;
        f3 = this.mAdapter.getPageWidth(i2);
        f4 = (f2 + f3) * j;
      }
    }
  }

  public boolean onInterceptTouchEvent(MotionEvent paramMotionEvent)
  {
    int i = 0xFF & paramMotionEvent.getAction();
    if ((i == 3) || (i == 1))
    {
      this.mIsBeingDragged = false;
      this.mIsUnableToDrag = false;
      this.mActivePointerId = -1;
      if (this.mVelocityTracker != null)
      {
        this.mVelocityTracker.recycle();
        this.mVelocityTracker = null;
      }
      return false;
    }
    if (i != 0)
    {
      if (this.mIsBeingDragged)
        return true;
      if (this.mIsUnableToDrag)
        return false;
    }
    switch (i)
    {
    default:
    case 2:
    case 0:
    case 6:
    }
    while (true)
    {
      if (this.mVelocityTracker == null)
        this.mVelocityTracker = VelocityTracker.obtain();
      this.mVelocityTracker.addMovement(paramMotionEvent);
      return this.mIsBeingDragged;
      int j = this.mActivePointerId;
      if (j != -1)
      {
        int k = MotionEventCompat.findPointerIndex(paramMotionEvent, j);
        float f3 = MotionEventCompat.getX(paramMotionEvent, k);
        float f4 = f3 - this.mLastMotionX;
        float f5 = Math.abs(f4);
        float f6 = MotionEventCompat.getY(paramMotionEvent, k);
        float f7 = Math.abs(f6 - this.mInitialMotionY);
        if ((f4 != 0.0F) && (!isGutterDrag(this.mLastMotionX, f4)) && (canScroll(this, false, (int)f4, (int)f3, (int)f6)))
        {
          this.mLastMotionX = f3;
          this.mLastMotionY = f6;
          this.mIsUnableToDrag = true;
          return false;
        }
        float f8;
        if ((f5 > this.mTouchSlop) && (0.5F * f5 > f7))
        {
          this.mIsBeingDragged = true;
          requestParentDisallowInterceptTouchEvent(true);
          setScrollState(1);
          if (f4 > 0.0F)
          {
            f8 = this.mInitialMotionX + this.mTouchSlop;
            label317: this.mLastMotionX = f8;
            this.mLastMotionY = f6;
            setScrollingCacheEnabled(true);
          }
        }
        while ((this.mIsBeingDragged) && (performDrag(f3)))
        {
          ViewCompat.postInvalidateOnAnimation(this);
          break;
          f8 = this.mInitialMotionX - this.mTouchSlop;
          break label317;
          if (f7 > this.mTouchSlop)
            this.mIsUnableToDrag = true;
        }
        float f1 = paramMotionEvent.getX();
        this.mInitialMotionX = f1;
        this.mLastMotionX = f1;
        float f2 = paramMotionEvent.getY();
        this.mInitialMotionY = f2;
        this.mLastMotionY = f2;
        this.mActivePointerId = MotionEventCompat.getPointerId(paramMotionEvent, 0);
        this.mIsUnableToDrag = false;
        this.mScroller.computeScrollOffset();
        if ((this.mScrollState == 2) && (Math.abs(this.mScroller.getFinalX() - this.mScroller.getCurrX()) > this.mCloseEnough))
        {
          this.mScroller.abortAnimation();
          this.mPopulatePending = false;
          populate();
          this.mIsBeingDragged = true;
          requestParentDisallowInterceptTouchEvent(true);
          setScrollState(1);
        }
        else
        {
          completeScroll(false);
          this.mIsBeingDragged = false;
          continue;
          onSecondaryPointerUp(paramMotionEvent);
        }
      }
    }
  }

  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    int i = getChildCount();
    int j = paramInt3 - paramInt1;
    int k = paramInt4 - paramInt2;
    int m = getPaddingLeft();
    int n = getPaddingTop();
    int i1 = getPaddingRight();
    int i2 = getPaddingBottom();
    int i3 = getScrollX();
    int i4 = 0;
    int i5 = 0;
    if (i5 < i)
    {
      View localView2 = getChildAt(i5);
      int i12;
      label156: int i13;
      if (localView2.getVisibility() != 8)
      {
        LayoutParams localLayoutParams2 = (LayoutParams)localView2.getLayoutParams();
        if (localLayoutParams2.isDecor)
        {
          int i10 = 0x7 & localLayoutParams2.gravity;
          int i11 = 0x70 & localLayoutParams2.gravity;
          switch (i10)
          {
          case 2:
          case 4:
          default:
            i12 = m;
            switch (i11)
            {
            default:
              i13 = n;
            case 48:
            case 16:
            case 80:
            }
            break;
          case 3:
          case 1:
          case 5:
          }
        }
      }
      while (true)
      {
        int i14 = i12 + i3;
        localView2.layout(i14, i13, i14 + localView2.getMeasuredWidth(), i13 + localView2.getMeasuredHeight());
        i4++;
        i5++;
        break;
        i12 = m;
        m += localView2.getMeasuredWidth();
        break label156;
        i12 = Math.max((j - localView2.getMeasuredWidth()) / 2, m);
        break label156;
        i12 = j - i1 - localView2.getMeasuredWidth();
        i1 += localView2.getMeasuredWidth();
        break label156;
        i13 = n;
        n += localView2.getMeasuredHeight();
        continue;
        i13 = Math.max((k - localView2.getMeasuredHeight()) / 2, n);
        continue;
        i13 = k - i2 - localView2.getMeasuredHeight();
        i2 += localView2.getMeasuredHeight();
      }
    }
    int i6 = j - m - i1;
    for (int i7 = 0; i7 < i; i7++)
    {
      View localView1 = getChildAt(i7);
      if (localView1.getVisibility() != 8)
      {
        LayoutParams localLayoutParams1 = (LayoutParams)localView1.getLayoutParams();
        if (!localLayoutParams1.isDecor)
        {
          ItemInfo localItemInfo = infoForChild(localView1);
          if (localItemInfo != null)
          {
            int i8 = m + (int)(i6 * localItemInfo.offset);
            int i9 = n;
            if (localLayoutParams1.needsMeasure)
            {
              localLayoutParams1.needsMeasure = false;
              localView1.measure(View.MeasureSpec.makeMeasureSpec((int)(i6 * localLayoutParams1.widthFactor), 1073741824), View.MeasureSpec.makeMeasureSpec(k - n - i2, 1073741824));
            }
            localView1.layout(i8, i9, i8 + localView1.getMeasuredWidth(), i9 + localView1.getMeasuredHeight());
          }
        }
      }
    }
    this.mTopPageBounds = n;
    this.mBottomPageBounds = (k - i2);
    this.mDecorChildCount = i4;
    if (this.mFirstLayout)
      scrollToItem(this.mCurItem, false, 0, false);
    this.mFirstLayout = false;
  }

  protected void onMeasure(int paramInt1, int paramInt2)
  {
    setMeasuredDimension(getDefaultSize(0, paramInt1), getDefaultSize(0, paramInt2));
    int i = getMeasuredWidth();
    this.mGutterSize = Math.min(i / 10, this.mDefaultGutterSize);
    int j = i - getPaddingLeft() - getPaddingRight();
    int k = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
    int m = getChildCount();
    int n = 0;
    if (n < m)
    {
      View localView2 = getChildAt(n);
      int i6;
      int i7;
      label167: int i8;
      if (localView2.getVisibility() != 8)
      {
        LayoutParams localLayoutParams2 = (LayoutParams)localView2.getLayoutParams();
        if ((localLayoutParams2 != null) && (localLayoutParams2.isDecor))
        {
          int i3 = 0x7 & localLayoutParams2.gravity;
          int i4 = 0x70 & localLayoutParams2.gravity;
          int i5 = -2147483648;
          i6 = -2147483648;
          if ((i4 != 48) && (i4 != 80))
            break label302;
          i7 = 1;
          if ((i3 != 3) && (i3 != 5))
            break label308;
          i8 = 1;
          label182: if (i7 == 0)
            break label314;
          i5 = 1073741824;
          label192: int i9 = j;
          int i10 = k;
          if (localLayoutParams2.width != -2)
          {
            i5 = 1073741824;
            if (localLayoutParams2.width != -1)
              i9 = localLayoutParams2.width;
          }
          if (localLayoutParams2.height != -2)
          {
            i6 = 1073741824;
            if (localLayoutParams2.height != -1)
              i10 = localLayoutParams2.height;
          }
          localView2.measure(View.MeasureSpec.makeMeasureSpec(i9, i5), View.MeasureSpec.makeMeasureSpec(i10, i6));
          if (i7 == 0)
            break label327;
          k -= localView2.getMeasuredHeight();
        }
      }
      while (true)
      {
        n++;
        break;
        label302: i7 = 0;
        break label167;
        label308: i8 = 0;
        break label182;
        label314: if (i8 == 0)
          break label192;
        i6 = 1073741824;
        break label192;
        label327: if (i8 != 0)
          j -= localView2.getMeasuredWidth();
      }
    }
    this.mChildWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(j, 1073741824);
    this.mChildHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(k, 1073741824);
    this.mInLayout = true;
    populate();
    this.mInLayout = false;
    int i1 = getChildCount();
    for (int i2 = 0; i2 < i1; i2++)
    {
      View localView1 = getChildAt(i2);
      if (localView1.getVisibility() != 8)
      {
        LayoutParams localLayoutParams1 = (LayoutParams)localView1.getLayoutParams();
        if ((localLayoutParams1 == null) || (!localLayoutParams1.isDecor))
          localView1.measure(View.MeasureSpec.makeMeasureSpec((int)(j * localLayoutParams1.widthFactor), 1073741824), this.mChildHeightMeasureSpec);
      }
    }
  }

  protected void onPageScrolled(int paramInt1, float paramFloat, int paramInt2)
  {
    if (this.mDecorChildCount > 0)
    {
      int m = getScrollX();
      int n = getPaddingLeft();
      int i1 = getPaddingRight();
      int i2 = getWidth();
      int i3 = getChildCount();
      int i4 = 0;
      while (i4 < i3)
      {
        View localView2 = getChildAt(i4);
        LayoutParams localLayoutParams = (LayoutParams)localView2.getLayoutParams();
        if (!localLayoutParams.isDecor)
        {
          i4++;
        }
        else
        {
          int i5;
          switch (0x7 & localLayoutParams.gravity)
          {
          case 2:
          case 4:
          default:
            i5 = n;
          case 3:
          case 1:
          case 5:
          }
          while (true)
          {
            int i6 = i5 + m - localView2.getLeft();
            if (i6 == 0)
              break;
            localView2.offsetLeftAndRight(i6);
            break;
            i5 = n;
            n += localView2.getWidth();
            continue;
            i5 = Math.max((i2 - localView2.getMeasuredWidth()) / 2, n);
            continue;
            i5 = i2 - i1 - localView2.getMeasuredWidth();
            i1 += localView2.getMeasuredWidth();
          }
        }
      }
    }
    dispatchOnPageScrolled(paramInt1, paramFloat, paramInt2);
    if (this.mPageTransformer != null)
    {
      int i = getScrollX();
      int j = getChildCount();
      int k = 0;
      if (k < j)
      {
        View localView1 = getChildAt(k);
        if (((LayoutParams)localView1.getLayoutParams()).isDecor);
        while (true)
        {
          k++;
          break;
          float f = (localView1.getLeft() - i) / getClientWidth();
          this.mPageTransformer.transformPage(localView1, f);
        }
      }
    }
    this.mCalledSuper = true;
  }

  protected boolean onRequestFocusInDescendants(int paramInt, Rect paramRect)
  {
    int i = getChildCount();
    int j;
    int k;
    int m;
    int n;
    if ((paramInt & 0x2) != 0)
    {
      j = 0;
      k = 1;
      m = i;
      n = j;
    }
    while (true)
    {
      if (n == m)
        break label108;
      View localView = getChildAt(n);
      if (localView.getVisibility() == 0)
      {
        ItemInfo localItemInfo = infoForChild(localView);
        if ((localItemInfo != null) && (localItemInfo.position == this.mCurItem) && (localView.requestFocus(paramInt, paramRect)))
        {
          return true;
          j = i - 1;
          k = -1;
          m = -1;
          break;
        }
      }
      n += k;
    }
    label108: return false;
  }

  public void onRestoreInstanceState(Parcelable paramParcelable)
  {
    if (!(paramParcelable instanceof SavedState))
    {
      super.onRestoreInstanceState(paramParcelable);
      return;
    }
    SavedState localSavedState = (SavedState)paramParcelable;
    super.onRestoreInstanceState(localSavedState.getSuperState());
    if (this.mAdapter != null)
    {
      this.mAdapter.restoreState(localSavedState.adapterState, localSavedState.loader);
      setCurrentItemInternal(localSavedState.position, false, true);
      return;
    }
    this.mRestoredCurItem = localSavedState.position;
    this.mRestoredAdapterState = localSavedState.adapterState;
    this.mRestoredClassLoader = localSavedState.loader;
  }

  public Parcelable onSaveInstanceState()
  {
    SavedState localSavedState = new SavedState(super.onSaveInstanceState());
    localSavedState.position = this.mCurItem;
    if (this.mAdapter != null)
      localSavedState.adapterState = this.mAdapter.saveState();
    return localSavedState;
  }

  protected void onSizeChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super.onSizeChanged(paramInt1, paramInt2, paramInt3, paramInt4);
    if (paramInt1 != paramInt3)
      recomputeScrollPosition(paramInt1, paramInt3, this.mPageMargin, this.mPageMargin);
  }

  public boolean onTouchEvent(MotionEvent paramMotionEvent)
  {
    if (this.mFakeDragging)
      return true;
    if ((paramMotionEvent.getAction() == 0) && (paramMotionEvent.getEdgeFlags() != 0))
      return false;
    if ((this.mAdapter == null) || (this.mAdapter.getCount() == 0))
      return false;
    if (this.mVelocityTracker == null)
      this.mVelocityTracker = VelocityTracker.obtain();
    this.mVelocityTracker.addMovement(paramMotionEvent);
    int i = 0xFF & paramMotionEvent.getAction();
    boolean bool1 = false;
    switch (i)
    {
    case 4:
    default:
    case 0:
    case 2:
    case 1:
    case 3:
    case 5:
    case 6:
    }
    while (true)
    {
      if (bool1)
        ViewCompat.postInvalidateOnAnimation(this);
      return true;
      this.mScroller.abortAnimation();
      this.mPopulatePending = false;
      populate();
      float f6 = paramMotionEvent.getX();
      this.mInitialMotionX = f6;
      this.mLastMotionX = f6;
      float f7 = paramMotionEvent.getY();
      this.mInitialMotionY = f7;
      this.mLastMotionY = f7;
      this.mActivePointerId = MotionEventCompat.getPointerId(paramMotionEvent, 0);
      bool1 = false;
      continue;
      float f3;
      if (!this.mIsBeingDragged)
      {
        int i1 = MotionEventCompat.findPointerIndex(paramMotionEvent, this.mActivePointerId);
        float f1 = MotionEventCompat.getX(paramMotionEvent, i1);
        float f2 = Math.abs(f1 - this.mLastMotionX);
        f3 = MotionEventCompat.getY(paramMotionEvent, i1);
        float f4 = Math.abs(f3 - this.mLastMotionY);
        if ((f2 > this.mTouchSlop) && (f2 > f4))
        {
          this.mIsBeingDragged = true;
          requestParentDisallowInterceptTouchEvent(true);
          if (f1 - this.mInitialMotionX <= 0.0F)
            break label382;
        }
      }
      label382: for (float f5 = this.mInitialMotionX + this.mTouchSlop; ; f5 = this.mInitialMotionX - this.mTouchSlop)
      {
        this.mLastMotionX = f5;
        this.mLastMotionY = f3;
        setScrollState(1);
        setScrollingCacheEnabled(true);
        ViewParent localViewParent = getParent();
        if (localViewParent != null)
          localViewParent.requestDisallowInterceptTouchEvent(true);
        boolean bool4 = this.mIsBeingDragged;
        bool1 = false;
        if (!bool4)
          break;
        bool1 = false | performDrag(MotionEventCompat.getX(paramMotionEvent, MotionEventCompat.findPointerIndex(paramMotionEvent, this.mActivePointerId)));
        break;
      }
      boolean bool3 = this.mIsBeingDragged;
      bool1 = false;
      if (bool3)
      {
        VelocityTracker localVelocityTracker = this.mVelocityTracker;
        localVelocityTracker.computeCurrentVelocity(1000, this.mMaximumVelocity);
        int k = (int)VelocityTrackerCompat.getXVelocity(localVelocityTracker, this.mActivePointerId);
        this.mPopulatePending = true;
        int m = getClientWidth();
        int n = getScrollX();
        ItemInfo localItemInfo = infoForCurrentScrollPosition();
        setCurrentItemInternal(determineTargetPage(localItemInfo.position, (n / m - localItemInfo.offset) / localItemInfo.widthFactor, k, (int)(MotionEventCompat.getX(paramMotionEvent, MotionEventCompat.findPointerIndex(paramMotionEvent, this.mActivePointerId)) - this.mInitialMotionX)), true, true, k);
        this.mActivePointerId = -1;
        endDrag();
        bool1 = this.mLeftEdge.onRelease() | this.mRightEdge.onRelease();
        continue;
        boolean bool2 = this.mIsBeingDragged;
        bool1 = false;
        if (bool2)
        {
          scrollToItem(this.mCurItem, true, 0, false);
          this.mActivePointerId = -1;
          endDrag();
          bool1 = this.mLeftEdge.onRelease() | this.mRightEdge.onRelease();
          continue;
          int j = MotionEventCompat.getActionIndex(paramMotionEvent);
          this.mLastMotionX = MotionEventCompat.getX(paramMotionEvent, j);
          this.mActivePointerId = MotionEventCompat.getPointerId(paramMotionEvent, j);
          bool1 = false;
          continue;
          onSecondaryPointerUp(paramMotionEvent);
          this.mLastMotionX = MotionEventCompat.getX(paramMotionEvent, MotionEventCompat.findPointerIndex(paramMotionEvent, this.mActivePointerId));
          bool1 = false;
        }
      }
    }
  }

  boolean pageLeft()
  {
    if (this.mCurItem > 0)
    {
      setCurrentItem(-1 + this.mCurItem, true);
      return true;
    }
    return false;
  }

  boolean pageRight()
  {
    if ((this.mAdapter != null) && (this.mCurItem < -1 + this.mAdapter.getCount()))
    {
      setCurrentItem(1 + this.mCurItem, true);
      return true;
    }
    return false;
  }

  void populate()
  {
    populate(this.mCurItem);
  }

  void populate(int paramInt)
  {
    int i = 2;
    int j = this.mCurItem;
    ItemInfo localItemInfo1 = null;
    if (j != paramInt)
    {
      if (this.mCurItem < paramInt)
      {
        i = 66;
        localItemInfo1 = infoForPosition(this.mCurItem);
        this.mCurItem = paramInt;
      }
    }
    else
    {
      if (this.mAdapter != null)
        break label59;
      sortChildDrawingOrder();
    }
    label1024: label1284: 
    while (true)
    {
      return;
      i = 17;
      break;
      label59: if (this.mPopulatePending)
      {
        sortChildDrawingOrder();
        return;
      }
      if (getWindowToken() != null)
      {
        this.mAdapter.startUpdate(this);
        int k = this.mOffscreenPageLimit;
        int m = Math.max(0, this.mCurItem - k);
        int n = this.mAdapter.getCount();
        int i1 = Math.min(n - 1, k + this.mCurItem);
        if (n != this.mExpectedAdapterCount)
          try
          {
            String str2 = getResources().getResourceName(getId());
            str1 = str2;
            throw new IllegalStateException("The application's PagerAdapter changed the adapter's contents without calling PagerAdapter#notifyDataSetChanged! Expected adapter item count: " + this.mExpectedAdapterCount + ", found: " + n + " Pager id: " + str1 + " Pager class: " + getClass() + " Problematic adapter: " + this.mAdapter.getClass());
          }
          catch (Resources.NotFoundException localNotFoundException)
          {
            while (true)
              String str1 = Integer.toHexString(getId());
          }
        int i2 = 0;
        int i3 = this.mItems.size();
        Object localObject1 = null;
        if (i2 < i3)
        {
          ItemInfo localItemInfo8 = (ItemInfo)this.mItems.get(i2);
          if (localItemInfo8.position < this.mCurItem)
            break label672;
          int i23 = localItemInfo8.position;
          int i24 = this.mCurItem;
          localObject1 = null;
          if (i23 == i24)
            localObject1 = localItemInfo8;
        }
        if ((localObject1 == null) && (n > 0))
          localObject1 = addNewItem(this.mCurItem, i2);
        float f1;
        int i9;
        ItemInfo localItemInfo5;
        label382: int i10;
        float f2;
        label396: int i11;
        float f3;
        int i12;
        ItemInfo localItemInfo6;
        label479: float f4;
        label487: int i14;
        PagerAdapter localPagerAdapter1;
        int i4;
        if (localObject1 != null)
        {
          f1 = 0.0F;
          i9 = i2 - 1;
          if (i9 >= 0)
          {
            localItemInfo5 = (ItemInfo)this.mItems.get(i9);
            i10 = getClientWidth();
            if (i10 > 0)
              break label684;
            f2 = 0.0F;
            i11 = -1 + this.mCurItem;
            if (i11 >= 0)
            {
              if ((f1 < f2) || (i11 >= m))
                break label798;
              if (localItemInfo5 != null)
                break label706;
            }
            f3 = ((ItemInfo)localObject1).widthFactor;
            i12 = i2 + 1;
            if (f3 < 2.0F)
            {
              int i13 = this.mItems.size();
              if (i12 >= i13)
                break label911;
              localItemInfo6 = (ItemInfo)this.mItems.get(i12);
              if (i10 > 0)
                break label917;
              f4 = 0.0F;
              i14 = 1 + this.mCurItem;
              if (i14 < n)
              {
                if ((f3 < f4) || (i14 <= i1))
                  break label1030;
                if (localItemInfo6 != null)
                  break label933;
              }
            }
            calculatePageOffsets((ItemInfo)localObject1, i2, localItemInfo1);
          }
        }
        else
        {
          localPagerAdapter1 = this.mAdapter;
          i4 = this.mCurItem;
          if (localObject1 == null)
            break label1163;
        }
        label1030: label1163: for (Object localObject2 = ((ItemInfo)localObject1).object; ; localObject2 = null)
        {
          localPagerAdapter1.setPrimaryItem(this, i4, localObject2);
          this.mAdapter.finishUpdate(this);
          int i5 = getChildCount();
          for (int i6 = 0; i6 < i5; i6++)
          {
            View localView3 = getChildAt(i6);
            LayoutParams localLayoutParams = (LayoutParams)localView3.getLayoutParams();
            localLayoutParams.childIndex = i6;
            if ((!localLayoutParams.isDecor) && (localLayoutParams.widthFactor == 0.0F))
            {
              ItemInfo localItemInfo4 = infoForChild(localView3);
              if (localItemInfo4 != null)
              {
                localLayoutParams.widthFactor = localItemInfo4.widthFactor;
                localLayoutParams.position = localItemInfo4.position;
              }
            }
          }
          label672: i2++;
          break;
          localItemInfo5 = null;
          break label382;
          label684: f2 = 2.0F - ((ItemInfo)localObject1).widthFactor + getPaddingLeft() / i10;
          break label396;
          label706: int i22 = localItemInfo5.position;
          if ((i11 == i22) && (!localItemInfo5.scrolling))
          {
            this.mItems.remove(i9);
            PagerAdapter localPagerAdapter3 = this.mAdapter;
            Object localObject4 = localItemInfo5.object;
            localPagerAdapter3.destroyItem(this, i11, localObject4);
            i9--;
            i2--;
            if (i9 < 0)
              break label792;
          }
          label792: for (localItemInfo5 = (ItemInfo)this.mItems.get(i9); ; localItemInfo5 = null)
          {
            i11--;
            break;
          }
          label798: if (localItemInfo5 != null)
          {
            int i21 = localItemInfo5.position;
            if (i11 == i21)
            {
              f1 += localItemInfo5.widthFactor;
              i9--;
              if (i9 >= 0);
              for (localItemInfo5 = (ItemInfo)this.mItems.get(i9); ; localItemInfo5 = null)
                break;
            }
          }
          int i20 = i9 + 1;
          f1 += addNewItem(i11, i20).widthFactor;
          i2++;
          if (i9 >= 0);
          for (localItemInfo5 = (ItemInfo)this.mItems.get(i9); ; localItemInfo5 = null)
            break;
          label911: localItemInfo6 = null;
          break label479;
          label917: f4 = 2.0F + getPaddingRight() / i10;
          break label487;
          label933: int i18 = localItemInfo6.position;
          if ((i14 == i18) && (!localItemInfo6.scrolling))
          {
            this.mItems.remove(i12);
            PagerAdapter localPagerAdapter2 = this.mAdapter;
            Object localObject3 = localItemInfo6.object;
            localPagerAdapter2.destroyItem(this, i14, localObject3);
            int i19 = this.mItems.size();
            if (i12 >= i19)
              break label1024;
          }
          for (localItemInfo6 = (ItemInfo)this.mItems.get(i12); ; localItemInfo6 = null)
          {
            i14++;
            break;
          }
          if (localItemInfo6 != null)
          {
            int i16 = localItemInfo6.position;
            if (i14 == i16)
            {
              f3 += localItemInfo6.widthFactor;
              i12++;
              int i17 = this.mItems.size();
              if (i12 < i17);
              for (localItemInfo6 = (ItemInfo)this.mItems.get(i12); ; localItemInfo6 = null)
                break;
            }
          }
          ItemInfo localItemInfo7 = addNewItem(i14, i12);
          i12++;
          f3 += localItemInfo7.widthFactor;
          int i15 = this.mItems.size();
          if (i12 < i15);
          for (localItemInfo6 = (ItemInfo)this.mItems.get(i12); ; localItemInfo6 = null)
            break;
        }
        sortChildDrawingOrder();
        if (hasFocus())
        {
          View localView1 = findFocus();
          if (localView1 != null);
          for (ItemInfo localItemInfo2 = infoForAnyChild(localView1); ; localItemInfo2 = null)
          {
            if ((localItemInfo2 != null) && (localItemInfo2.position == this.mCurItem))
              break label1284;
            for (int i7 = 0; ; i7++)
            {
              int i8 = getChildCount();
              if (i7 >= i8)
                break;
              View localView2 = getChildAt(i7);
              ItemInfo localItemInfo3 = infoForChild(localView2);
              if ((localItemInfo3 != null) && (localItemInfo3.position == this.mCurItem) && (localView2.requestFocus(i)))
                break;
            }
          }
        }
      }
    }
  }

  public void removeOnPageChangeListener(OnPageChangeListener paramOnPageChangeListener)
  {
    if (this.mOnPageChangeListeners != null)
      this.mOnPageChangeListeners.remove(paramOnPageChangeListener);
  }

  public void removeView(View paramView)
  {
    if (this.mInLayout)
    {
      removeViewInLayout(paramView);
      return;
    }
    super.removeView(paramView);
  }

  public void setAdapter(PagerAdapter paramPagerAdapter)
  {
    if (this.mAdapter != null)
    {
      this.mAdapter.unregisterDataSetObserver(this.mObserver);
      this.mAdapter.startUpdate(this);
      for (int i = 0; i < this.mItems.size(); i++)
      {
        ItemInfo localItemInfo = (ItemInfo)this.mItems.get(i);
        this.mAdapter.destroyItem(this, localItemInfo.position, localItemInfo.object);
      }
      this.mAdapter.finishUpdate(this);
      this.mItems.clear();
      removeNonDecorViews();
      this.mCurItem = 0;
      scrollTo(0, 0);
    }
    PagerAdapter localPagerAdapter = this.mAdapter;
    this.mAdapter = paramPagerAdapter;
    this.mExpectedAdapterCount = 0;
    boolean bool;
    if (this.mAdapter != null)
    {
      if (this.mObserver == null)
        this.mObserver = new PagerObserver(null);
      this.mAdapter.registerDataSetObserver(this.mObserver);
      this.mPopulatePending = false;
      bool = this.mFirstLayout;
      this.mFirstLayout = true;
      this.mExpectedAdapterCount = this.mAdapter.getCount();
      if (this.mRestoredCurItem < 0)
        break label259;
      this.mAdapter.restoreState(this.mRestoredAdapterState, this.mRestoredClassLoader);
      setCurrentItemInternal(this.mRestoredCurItem, false, true);
      this.mRestoredCurItem = -1;
      this.mRestoredAdapterState = null;
      this.mRestoredClassLoader = null;
    }
    while (true)
    {
      if ((this.mAdapterChangeListener != null) && (localPagerAdapter != paramPagerAdapter))
        this.mAdapterChangeListener.onAdapterChanged(localPagerAdapter, paramPagerAdapter);
      return;
      label259: if (!bool)
        populate();
      else
        requestLayout();
    }
  }

  void setChildrenDrawingOrderEnabledCompat(boolean paramBoolean)
  {
    if ((Build.VERSION.SDK_INT < 7) || (this.mSetChildrenDrawingOrderEnabled == null));
    try
    {
      Class[] arrayOfClass = new Class[1];
      arrayOfClass[0] = Boolean.TYPE;
      this.mSetChildrenDrawingOrderEnabled = ViewGroup.class.getDeclaredMethod("setChildrenDrawingOrderEnabled", arrayOfClass);
    }
    catch (NoSuchMethodException localNoSuchMethodException)
    {
      try
      {
        while (true)
        {
          Method localMethod = this.mSetChildrenDrawingOrderEnabled;
          Object[] arrayOfObject = new Object[1];
          arrayOfObject[0] = Boolean.valueOf(paramBoolean);
          localMethod.invoke(this, arrayOfObject);
          return;
          localNoSuchMethodException = localNoSuchMethodException;
          Log.e("ViewPager", "Can't find setChildrenDrawingOrderEnabled", localNoSuchMethodException);
        }
      }
      catch (Exception localException)
      {
        Log.e("ViewPager", "Error changing children drawing order", localException);
      }
    }
  }

  public void setCurrentItem(int paramInt)
  {
    this.mPopulatePending = false;
    if (!this.mFirstLayout);
    for (boolean bool = true; ; bool = false)
    {
      setCurrentItemInternal(paramInt, bool, false);
      return;
    }
  }

  public void setCurrentItem(int paramInt, boolean paramBoolean)
  {
    this.mPopulatePending = false;
    setCurrentItemInternal(paramInt, paramBoolean, false);
  }

  void setCurrentItemInternal(int paramInt, boolean paramBoolean1, boolean paramBoolean2)
  {
    setCurrentItemInternal(paramInt, paramBoolean1, paramBoolean2, 0);
  }

  void setCurrentItemInternal(int paramInt1, boolean paramBoolean1, boolean paramBoolean2, int paramInt2)
  {
    boolean bool = true;
    if ((this.mAdapter == null) || (this.mAdapter.getCount() <= 0))
    {
      setScrollingCacheEnabled(false);
      return;
    }
    if ((!paramBoolean2) && (this.mCurItem == paramInt1) && (this.mItems.size() != 0))
    {
      setScrollingCacheEnabled(false);
      return;
    }
    if (paramInt1 < 0)
      paramInt1 = 0;
    while (true)
    {
      int i = this.mOffscreenPageLimit;
      if ((paramInt1 <= i + this.mCurItem) && (paramInt1 >= this.mCurItem - i))
        break;
      for (int j = 0; j < this.mItems.size(); j++)
        ((ItemInfo)this.mItems.get(j)).scrolling = bool;
      if (paramInt1 >= this.mAdapter.getCount())
        paramInt1 = -1 + this.mAdapter.getCount();
    }
    if (this.mCurItem != paramInt1);
    while (this.mFirstLayout)
    {
      this.mCurItem = paramInt1;
      if (bool)
        dispatchOnPageSelected(paramInt1);
      requestLayout();
      return;
      bool = false;
    }
    populate(paramInt1);
    scrollToItem(paramInt1, paramBoolean1, paramInt2, bool);
  }

  OnPageChangeListener setInternalPageChangeListener(OnPageChangeListener paramOnPageChangeListener)
  {
    OnPageChangeListener localOnPageChangeListener = this.mInternalPageChangeListener;
    this.mInternalPageChangeListener = paramOnPageChangeListener;
    return localOnPageChangeListener;
  }

  public void setOffscreenPageLimit(int paramInt)
  {
    if (paramInt < 1)
    {
      Log.w("ViewPager", "Requested offscreen page limit " + paramInt + " too small; defaulting to " + 1);
      paramInt = 1;
    }
    if (paramInt != this.mOffscreenPageLimit)
    {
      this.mOffscreenPageLimit = paramInt;
      populate();
    }
  }

  void setOnAdapterChangeListener(OnAdapterChangeListener paramOnAdapterChangeListener)
  {
    this.mAdapterChangeListener = paramOnAdapterChangeListener;
  }

  @Deprecated
  public void setOnPageChangeListener(OnPageChangeListener paramOnPageChangeListener)
  {
    this.mOnPageChangeListener = paramOnPageChangeListener;
  }

  public void setPageMargin(int paramInt)
  {
    int i = this.mPageMargin;
    this.mPageMargin = paramInt;
    int j = getWidth();
    recomputeScrollPosition(j, j, paramInt, i);
    requestLayout();
  }

  public void setPageMarginDrawable(@DrawableRes int paramInt)
  {
    setPageMarginDrawable(getContext().getResources().getDrawable(paramInt));
  }

  public void setPageMarginDrawable(Drawable paramDrawable)
  {
    this.mMarginDrawable = paramDrawable;
    if (paramDrawable != null)
      refreshDrawableState();
    if (paramDrawable == null);
    for (boolean bool = true; ; bool = false)
    {
      setWillNotDraw(bool);
      invalidate();
      return;
    }
  }

  public void setPageTransformer(boolean paramBoolean, PageTransformer paramPageTransformer)
  {
    int i = 1;
    label27: int i1;
    if (Build.VERSION.SDK_INT >= 11)
    {
      if (paramPageTransformer == null)
        break label74;
      int j = i;
      if (this.mPageTransformer == null)
        break label80;
      int m = i;
      if (j == m)
        break label86;
      i1 = i;
      label37: this.mPageTransformer = paramPageTransformer;
      setChildrenDrawingOrderEnabledCompat(j);
      if (j == 0)
        break label92;
      if (paramBoolean)
        i = 2;
    }
    label74: label80: label86: label92: for (this.mDrawingOrder = i; ; this.mDrawingOrder = 0)
    {
      if (i1 != 0)
        populate();
      return;
      int k = 0;
      break;
      int n = 0;
      break label27;
      i1 = 0;
      break label37;
    }
  }

  void smoothScrollTo(int paramInt1, int paramInt2)
  {
    smoothScrollTo(paramInt1, paramInt2, 0);
  }

  void smoothScrollTo(int paramInt1, int paramInt2, int paramInt3)
  {
    if (getChildCount() == 0)
    {
      setScrollingCacheEnabled(false);
      return;
    }
    int i = getScrollX();
    int j = getScrollY();
    int k = paramInt1 - i;
    int m = paramInt2 - j;
    if ((k == 0) && (m == 0))
    {
      completeScroll(false);
      populate();
      setScrollState(0);
      return;
    }
    setScrollingCacheEnabled(true);
    setScrollState(2);
    int n = getClientWidth();
    int i1 = n / 2;
    float f1 = Math.min(1.0F, 1.0F * Math.abs(k) / n);
    float f2 = i1 + i1 * distanceInfluenceForSnapDuration(f1);
    int i2 = Math.abs(paramInt3);
    if (i2 > 0);
    float f3;
    for (int i3 = 4 * Math.round(1000.0F * Math.abs(f2 / i2)); ; i3 = (int)(100.0F * (1.0F + Math.abs(k) / (f3 + this.mPageMargin))))
    {
      int i4 = Math.min(i3, 600);
      this.mScroller.startScroll(i, j, k, m, i4);
      ViewCompat.postInvalidateOnAnimation(this);
      return;
      f3 = n * this.mAdapter.getPageWidth(this.mCurItem);
    }
  }

  protected boolean verifyDrawable(Drawable paramDrawable)
  {
    return (super.verifyDrawable(paramDrawable)) || (paramDrawable == this.mMarginDrawable);
  }

  static abstract interface Decor
  {
  }

  static class ItemInfo
  {
    Object object;
    float offset;
    int position;
    boolean scrolling;
    float widthFactor;
  }

  public static class LayoutParams extends ViewGroup.LayoutParams
  {
    int childIndex;
    public int gravity;
    public boolean isDecor;
    boolean needsMeasure;
    int position;
    float widthFactor = 0.0F;

    public LayoutParams()
    {
      super(-1);
    }

    public LayoutParams(Context paramContext, AttributeSet paramAttributeSet)
    {
      super(paramAttributeSet);
      TypedArray localTypedArray = paramContext.obtainStyledAttributes(paramAttributeSet, ViewPager.LAYOUT_ATTRS);
      this.gravity = localTypedArray.getInteger(0, 48);
      localTypedArray.recycle();
    }
  }

  static abstract interface OnAdapterChangeListener
  {
    public abstract void onAdapterChanged(PagerAdapter paramPagerAdapter1, PagerAdapter paramPagerAdapter2);
  }

  public static abstract interface OnPageChangeListener
  {
    public abstract void onPageScrollStateChanged(int paramInt);

    public abstract void onPageScrolled(int paramInt1, float paramFloat, int paramInt2);

    public abstract void onPageSelected(int paramInt);
  }

  public static abstract interface PageTransformer
  {
    public abstract void transformPage(View paramView, float paramFloat);
  }

  private class PagerObserver extends DataSetObserver
  {
    private PagerObserver()
    {
    }

    public void onChanged()
    {
      ViewPager.this.dataSetChanged();
    }

    public void onInvalidated()
    {
      ViewPager.this.dataSetChanged();
    }
  }

  public static class SavedState extends View.BaseSavedState
  {
    public static final Parcelable.Creator<SavedState> CREATOR = ParcelableCompat.newCreator(new ViewPager.SavedState.1());
    Parcelable adapterState;
    ClassLoader loader;
    int position;

    SavedState(Parcel paramParcel, ClassLoader paramClassLoader)
    {
      super();
      if (paramClassLoader == null)
        paramClassLoader = getClass().getClassLoader();
      this.position = paramParcel.readInt();
      this.adapterState = paramParcel.readParcelable(paramClassLoader);
      this.loader = paramClassLoader;
    }

    public SavedState(Parcelable paramParcelable)
    {
      super();
    }

    public String toString()
    {
      return "FragmentPager.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " position=" + this.position + "}";
    }

    public void writeToParcel(Parcel paramParcel, int paramInt)
    {
      super.writeToParcel(paramParcel, paramInt);
      paramParcel.writeInt(this.position);
      paramParcel.writeParcelable(this.adapterState, paramInt);
    }
  }

  static class ViewPositionComparator
    implements Comparator<View>
  {
    public int compare(View paramView1, View paramView2)
    {
      ViewPager.LayoutParams localLayoutParams1 = (ViewPager.LayoutParams)paramView1.getLayoutParams();
      ViewPager.LayoutParams localLayoutParams2 = (ViewPager.LayoutParams)paramView2.getLayoutParams();
      if (localLayoutParams1.isDecor != localLayoutParams2.isDecor)
      {
        if (localLayoutParams1.isDecor)
          return 1;
        return -1;
      }
      return localLayoutParams1.position - localLayoutParams2.position;
    }
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.view.ViewPager
 * JD-Core Version:    0.6.2
 */
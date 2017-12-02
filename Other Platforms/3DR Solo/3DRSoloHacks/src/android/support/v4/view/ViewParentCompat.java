package android.support.v4.view;

import android.os.Build.VERSION;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;

public class ViewParentCompat
{
  static final ViewParentCompatImpl IMPL = new ViewParentCompat.ViewParentCompatStubImpl();

  static
  {
    int i = Build.VERSION.SDK_INT;
    if (i >= 21)
    {
      IMPL = new ViewParentCompat.ViewParentCompatLollipopImpl();
      return;
    }
    if (i >= 19)
    {
      IMPL = new ViewParentCompat.ViewParentCompatKitKatImpl();
      return;
    }
    if (i >= 14)
    {
      IMPL = new ViewParentCompat.ViewParentCompatICSImpl();
      return;
    }
  }

  public static void notifySubtreeAccessibilityStateChanged(ViewParent paramViewParent, View paramView1, View paramView2, int paramInt)
  {
    IMPL.notifySubtreeAccessibilityStateChanged(paramViewParent, paramView1, paramView2, paramInt);
  }

  public static boolean onNestedFling(ViewParent paramViewParent, View paramView, float paramFloat1, float paramFloat2, boolean paramBoolean)
  {
    return IMPL.onNestedFling(paramViewParent, paramView, paramFloat1, paramFloat2, paramBoolean);
  }

  public static boolean onNestedPreFling(ViewParent paramViewParent, View paramView, float paramFloat1, float paramFloat2)
  {
    return IMPL.onNestedPreFling(paramViewParent, paramView, paramFloat1, paramFloat2);
  }

  public static void onNestedPreScroll(ViewParent paramViewParent, View paramView, int paramInt1, int paramInt2, int[] paramArrayOfInt)
  {
    IMPL.onNestedPreScroll(paramViewParent, paramView, paramInt1, paramInt2, paramArrayOfInt);
  }

  public static void onNestedScroll(ViewParent paramViewParent, View paramView, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    IMPL.onNestedScroll(paramViewParent, paramView, paramInt1, paramInt2, paramInt3, paramInt4);
  }

  public static void onNestedScrollAccepted(ViewParent paramViewParent, View paramView1, View paramView2, int paramInt)
  {
    IMPL.onNestedScrollAccepted(paramViewParent, paramView1, paramView2, paramInt);
  }

  public static boolean onStartNestedScroll(ViewParent paramViewParent, View paramView1, View paramView2, int paramInt)
  {
    return IMPL.onStartNestedScroll(paramViewParent, paramView1, paramView2, paramInt);
  }

  public static void onStopNestedScroll(ViewParent paramViewParent, View paramView)
  {
    IMPL.onStopNestedScroll(paramViewParent, paramView);
  }

  public static boolean requestSendAccessibilityEvent(ViewParent paramViewParent, View paramView, AccessibilityEvent paramAccessibilityEvent)
  {
    return IMPL.requestSendAccessibilityEvent(paramViewParent, paramView, paramAccessibilityEvent);
  }

  static abstract interface ViewParentCompatImpl
  {
    public abstract void notifySubtreeAccessibilityStateChanged(ViewParent paramViewParent, View paramView1, View paramView2, int paramInt);

    public abstract boolean onNestedFling(ViewParent paramViewParent, View paramView, float paramFloat1, float paramFloat2, boolean paramBoolean);

    public abstract boolean onNestedPreFling(ViewParent paramViewParent, View paramView, float paramFloat1, float paramFloat2);

    public abstract void onNestedPreScroll(ViewParent paramViewParent, View paramView, int paramInt1, int paramInt2, int[] paramArrayOfInt);

    public abstract void onNestedScroll(ViewParent paramViewParent, View paramView, int paramInt1, int paramInt2, int paramInt3, int paramInt4);

    public abstract void onNestedScrollAccepted(ViewParent paramViewParent, View paramView1, View paramView2, int paramInt);

    public abstract boolean onStartNestedScroll(ViewParent paramViewParent, View paramView1, View paramView2, int paramInt);

    public abstract void onStopNestedScroll(ViewParent paramViewParent, View paramView);

    public abstract boolean requestSendAccessibilityEvent(ViewParent paramViewParent, View paramView, AccessibilityEvent paramAccessibilityEvent);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.view.ViewParentCompat
 * JD-Core Version:    0.6.2
 */
package android.support.v4.view;

import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeProviderCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

public class AccessibilityDelegateCompat
{
  private static final Object DEFAULT_DELEGATE;
  private static final AccessibilityDelegateImpl IMPL;
  final Object mBridge = IMPL.newAccessiblityDelegateBridge(this);

  static
  {
    if (Build.VERSION.SDK_INT >= 16)
      IMPL = new AccessibilityDelegateCompat.AccessibilityDelegateJellyBeanImpl();
    while (true)
    {
      DEFAULT_DELEGATE = IMPL.newAccessiblityDelegateDefaultImpl();
      return;
      if (Build.VERSION.SDK_INT >= 14)
        IMPL = new AccessibilityDelegateCompat.AccessibilityDelegateIcsImpl();
      else
        IMPL = new AccessibilityDelegateCompat.AccessibilityDelegateStubImpl();
    }
  }

  public boolean dispatchPopulateAccessibilityEvent(View paramView, AccessibilityEvent paramAccessibilityEvent)
  {
    return IMPL.dispatchPopulateAccessibilityEvent(DEFAULT_DELEGATE, paramView, paramAccessibilityEvent);
  }

  public AccessibilityNodeProviderCompat getAccessibilityNodeProvider(View paramView)
  {
    return IMPL.getAccessibilityNodeProvider(DEFAULT_DELEGATE, paramView);
  }

  Object getBridge()
  {
    return this.mBridge;
  }

  public void onInitializeAccessibilityEvent(View paramView, AccessibilityEvent paramAccessibilityEvent)
  {
    IMPL.onInitializeAccessibilityEvent(DEFAULT_DELEGATE, paramView, paramAccessibilityEvent);
  }

  public void onInitializeAccessibilityNodeInfo(View paramView, AccessibilityNodeInfoCompat paramAccessibilityNodeInfoCompat)
  {
    IMPL.onInitializeAccessibilityNodeInfo(DEFAULT_DELEGATE, paramView, paramAccessibilityNodeInfoCompat);
  }

  public void onPopulateAccessibilityEvent(View paramView, AccessibilityEvent paramAccessibilityEvent)
  {
    IMPL.onPopulateAccessibilityEvent(DEFAULT_DELEGATE, paramView, paramAccessibilityEvent);
  }

  public boolean onRequestSendAccessibilityEvent(ViewGroup paramViewGroup, View paramView, AccessibilityEvent paramAccessibilityEvent)
  {
    return IMPL.onRequestSendAccessibilityEvent(DEFAULT_DELEGATE, paramViewGroup, paramView, paramAccessibilityEvent);
  }

  public boolean performAccessibilityAction(View paramView, int paramInt, Bundle paramBundle)
  {
    return IMPL.performAccessibilityAction(DEFAULT_DELEGATE, paramView, paramInt, paramBundle);
  }

  public void sendAccessibilityEvent(View paramView, int paramInt)
  {
    IMPL.sendAccessibilityEvent(DEFAULT_DELEGATE, paramView, paramInt);
  }

  public void sendAccessibilityEventUnchecked(View paramView, AccessibilityEvent paramAccessibilityEvent)
  {
    IMPL.sendAccessibilityEventUnchecked(DEFAULT_DELEGATE, paramView, paramAccessibilityEvent);
  }

  static abstract interface AccessibilityDelegateImpl
  {
    public abstract boolean dispatchPopulateAccessibilityEvent(Object paramObject, View paramView, AccessibilityEvent paramAccessibilityEvent);

    public abstract AccessibilityNodeProviderCompat getAccessibilityNodeProvider(Object paramObject, View paramView);

    public abstract Object newAccessiblityDelegateBridge(AccessibilityDelegateCompat paramAccessibilityDelegateCompat);

    public abstract Object newAccessiblityDelegateDefaultImpl();

    public abstract void onInitializeAccessibilityEvent(Object paramObject, View paramView, AccessibilityEvent paramAccessibilityEvent);

    public abstract void onInitializeAccessibilityNodeInfo(Object paramObject, View paramView, AccessibilityNodeInfoCompat paramAccessibilityNodeInfoCompat);

    public abstract void onPopulateAccessibilityEvent(Object paramObject, View paramView, AccessibilityEvent paramAccessibilityEvent);

    public abstract boolean onRequestSendAccessibilityEvent(Object paramObject, ViewGroup paramViewGroup, View paramView, AccessibilityEvent paramAccessibilityEvent);

    public abstract boolean performAccessibilityAction(Object paramObject, View paramView, int paramInt, Bundle paramBundle);

    public abstract void sendAccessibilityEvent(Object paramObject, View paramView, int paramInt);

    public abstract void sendAccessibilityEventUnchecked(Object paramObject, View paramView, AccessibilityEvent paramAccessibilityEvent);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.view.AccessibilityDelegateCompat
 * JD-Core Version:    0.6.2
 */
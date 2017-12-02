package android.support.v4.view.accessibility;

import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccessibilityNodeInfoCompat
{
  public static final int ACTION_ACCESSIBILITY_FOCUS = 64;
  public static final String ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN = "ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN";
  public static final String ACTION_ARGUMENT_HTML_ELEMENT_STRING = "ACTION_ARGUMENT_HTML_ELEMENT_STRING";
  public static final String ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT = "ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT";
  public static final String ACTION_ARGUMENT_SELECTION_END_INT = "ACTION_ARGUMENT_SELECTION_END_INT";
  public static final String ACTION_ARGUMENT_SELECTION_START_INT = "ACTION_ARGUMENT_SELECTION_START_INT";
  public static final String ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE = "ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE";
  public static final int ACTION_CLEAR_ACCESSIBILITY_FOCUS = 128;
  public static final int ACTION_CLEAR_FOCUS = 2;
  public static final int ACTION_CLEAR_SELECTION = 8;
  public static final int ACTION_CLICK = 16;
  public static final int ACTION_COPY = 16384;
  public static final int ACTION_CUT = 65536;
  public static final int ACTION_FOCUS = 1;
  public static final int ACTION_LONG_CLICK = 32;
  public static final int ACTION_NEXT_AT_MOVEMENT_GRANULARITY = 256;
  public static final int ACTION_NEXT_HTML_ELEMENT = 1024;
  public static final int ACTION_PASTE = 32768;
  public static final int ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY = 512;
  public static final int ACTION_PREVIOUS_HTML_ELEMENT = 2048;
  public static final int ACTION_SCROLL_BACKWARD = 8192;
  public static final int ACTION_SCROLL_FORWARD = 4096;
  public static final int ACTION_SELECT = 4;
  public static final int ACTION_SET_SELECTION = 131072;
  public static final int ACTION_SET_TEXT = 2097152;
  public static final int FOCUS_ACCESSIBILITY = 2;
  public static final int FOCUS_INPUT = 1;
  private static final AccessibilityNodeInfoImpl IMPL = new AccessibilityNodeInfoCompat.AccessibilityNodeInfoStubImpl();
  public static final int MOVEMENT_GRANULARITY_CHARACTER = 1;
  public static final int MOVEMENT_GRANULARITY_LINE = 4;
  public static final int MOVEMENT_GRANULARITY_PAGE = 16;
  public static final int MOVEMENT_GRANULARITY_PARAGRAPH = 8;
  public static final int MOVEMENT_GRANULARITY_WORD = 2;
  private final Object mInfo;

  static
  {
    if (Build.VERSION.SDK_INT >= 22)
    {
      IMPL = new AccessibilityNodeInfoCompat.AccessibilityNodeInfoApi22Impl();
      return;
    }
    if (Build.VERSION.SDK_INT >= 21)
    {
      IMPL = new AccessibilityNodeInfoCompat.AccessibilityNodeInfoApi21Impl();
      return;
    }
    if (Build.VERSION.SDK_INT >= 19)
    {
      IMPL = new AccessibilityNodeInfoCompat.AccessibilityNodeInfoKitKatImpl();
      return;
    }
    if (Build.VERSION.SDK_INT >= 18)
    {
      IMPL = new AccessibilityNodeInfoCompat.AccessibilityNodeInfoJellybeanMr2Impl();
      return;
    }
    if (Build.VERSION.SDK_INT >= 16)
    {
      IMPL = new AccessibilityNodeInfoCompat.AccessibilityNodeInfoJellybeanImpl();
      return;
    }
    if (Build.VERSION.SDK_INT >= 14)
    {
      IMPL = new AccessibilityNodeInfoCompat.AccessibilityNodeInfoIcsImpl();
      return;
    }
  }

  public AccessibilityNodeInfoCompat(Object paramObject)
  {
    this.mInfo = paramObject;
  }

  private static String getActionSymbolicName(int paramInt)
  {
    switch (paramInt)
    {
    default:
      return "ACTION_UNKNOWN";
    case 1:
      return "ACTION_FOCUS";
    case 2:
      return "ACTION_CLEAR_FOCUS";
    case 4:
      return "ACTION_SELECT";
    case 8:
      return "ACTION_CLEAR_SELECTION";
    case 16:
      return "ACTION_CLICK";
    case 32:
      return "ACTION_LONG_CLICK";
    case 64:
      return "ACTION_ACCESSIBILITY_FOCUS";
    case 128:
      return "ACTION_CLEAR_ACCESSIBILITY_FOCUS";
    case 256:
      return "ACTION_NEXT_AT_MOVEMENT_GRANULARITY";
    case 512:
      return "ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY";
    case 1024:
      return "ACTION_NEXT_HTML_ELEMENT";
    case 2048:
      return "ACTION_PREVIOUS_HTML_ELEMENT";
    case 4096:
      return "ACTION_SCROLL_FORWARD";
    case 8192:
      return "ACTION_SCROLL_BACKWARD";
    case 65536:
      return "ACTION_CUT";
    case 16384:
      return "ACTION_COPY";
    case 32768:
      return "ACTION_PASTE";
    case 131072:
    }
    return "ACTION_SET_SELECTION";
  }

  public static AccessibilityNodeInfoCompat obtain()
  {
    return wrapNonNullInstance(IMPL.obtain());
  }

  public static AccessibilityNodeInfoCompat obtain(AccessibilityNodeInfoCompat paramAccessibilityNodeInfoCompat)
  {
    return wrapNonNullInstance(IMPL.obtain(paramAccessibilityNodeInfoCompat.mInfo));
  }

  public static AccessibilityNodeInfoCompat obtain(View paramView)
  {
    return wrapNonNullInstance(IMPL.obtain(paramView));
  }

  public static AccessibilityNodeInfoCompat obtain(View paramView, int paramInt)
  {
    return wrapNonNullInstance(IMPL.obtain(paramView, paramInt));
  }

  static AccessibilityNodeInfoCompat wrapNonNullInstance(Object paramObject)
  {
    if (paramObject != null)
      return new AccessibilityNodeInfoCompat(paramObject);
    return null;
  }

  public void addAction(int paramInt)
  {
    IMPL.addAction(this.mInfo, paramInt);
  }

  public void addAction(AccessibilityActionCompat paramAccessibilityActionCompat)
  {
    IMPL.addAction(this.mInfo, paramAccessibilityActionCompat.mAction);
  }

  public void addChild(View paramView)
  {
    IMPL.addChild(this.mInfo, paramView);
  }

  public void addChild(View paramView, int paramInt)
  {
    IMPL.addChild(this.mInfo, paramView, paramInt);
  }

  public boolean equals(Object paramObject)
  {
    if (this == paramObject);
    AccessibilityNodeInfoCompat localAccessibilityNodeInfoCompat;
    do
    {
      do
      {
        return true;
        if (paramObject == null)
          return false;
        if (getClass() != paramObject.getClass())
          return false;
        localAccessibilityNodeInfoCompat = (AccessibilityNodeInfoCompat)paramObject;
        if (this.mInfo != null)
          break;
      }
      while (localAccessibilityNodeInfoCompat.mInfo == null);
      return false;
    }
    while (this.mInfo.equals(localAccessibilityNodeInfoCompat.mInfo));
    return false;
  }

  public List<AccessibilityNodeInfoCompat> findAccessibilityNodeInfosByText(String paramString)
  {
    ArrayList localArrayList = new ArrayList();
    List localList = IMPL.findAccessibilityNodeInfosByText(this.mInfo, paramString);
    int i = localList.size();
    for (int j = 0; j < i; j++)
      localArrayList.add(new AccessibilityNodeInfoCompat(localList.get(j)));
    return localArrayList;
  }

  public AccessibilityNodeInfoCompat findFocus(int paramInt)
  {
    return wrapNonNullInstance(IMPL.findFocus(this.mInfo, paramInt));
  }

  public AccessibilityNodeInfoCompat focusSearch(int paramInt)
  {
    return wrapNonNullInstance(IMPL.focusSearch(this.mInfo, paramInt));
  }

  public List<AccessibilityActionCompat> getActionList()
  {
    List localList = IMPL.getActionList(this.mInfo);
    if (localList != null)
    {
      localObject = new ArrayList();
      int i = localList.size();
      for (int j = 0; j < i; j++)
        ((List)localObject).add(new AccessibilityActionCompat(localList.get(j), null));
    }
    Object localObject = Collections.emptyList();
    return localObject;
  }

  public int getActions()
  {
    return IMPL.getActions(this.mInfo);
  }

  public void getBoundsInParent(Rect paramRect)
  {
    IMPL.getBoundsInParent(this.mInfo, paramRect);
  }

  public void getBoundsInScreen(Rect paramRect)
  {
    IMPL.getBoundsInScreen(this.mInfo, paramRect);
  }

  public AccessibilityNodeInfoCompat getChild(int paramInt)
  {
    return wrapNonNullInstance(IMPL.getChild(this.mInfo, paramInt));
  }

  public int getChildCount()
  {
    return IMPL.getChildCount(this.mInfo);
  }

  public CharSequence getClassName()
  {
    return IMPL.getClassName(this.mInfo);
  }

  public CollectionInfoCompat getCollectionInfo()
  {
    Object localObject = IMPL.getCollectionInfo(this.mInfo);
    if (localObject == null)
      return null;
    return new CollectionInfoCompat(localObject, null);
  }

  public CollectionItemInfoCompat getCollectionItemInfo()
  {
    Object localObject = IMPL.getCollectionItemInfo(this.mInfo);
    if (localObject == null)
      return null;
    return new CollectionItemInfoCompat(localObject, null);
  }

  public CharSequence getContentDescription()
  {
    return IMPL.getContentDescription(this.mInfo);
  }

  public CharSequence getError()
  {
    return IMPL.getError(this.mInfo);
  }

  public Object getInfo()
  {
    return this.mInfo;
  }

  public int getLiveRegion()
  {
    return IMPL.getLiveRegion(this.mInfo);
  }

  public int getMovementGranularities()
  {
    return IMPL.getMovementGranularities(this.mInfo);
  }

  public CharSequence getPackageName()
  {
    return IMPL.getPackageName(this.mInfo);
  }

  public AccessibilityNodeInfoCompat getParent()
  {
    return wrapNonNullInstance(IMPL.getParent(this.mInfo));
  }

  public RangeInfoCompat getRangeInfo()
  {
    Object localObject = IMPL.getRangeInfo(this.mInfo);
    if (localObject == null)
      return null;
    return new RangeInfoCompat(localObject, null);
  }

  public CharSequence getText()
  {
    return IMPL.getText(this.mInfo);
  }

  public String getViewIdResourceName()
  {
    return IMPL.getViewIdResourceName(this.mInfo);
  }

  public int getWindowId()
  {
    return IMPL.getWindowId(this.mInfo);
  }

  public int hashCode()
  {
    if (this.mInfo == null)
      return 0;
    return this.mInfo.hashCode();
  }

  public boolean isAccessibilityFocused()
  {
    return IMPL.isAccessibilityFocused(this.mInfo);
  }

  public boolean isCheckable()
  {
    return IMPL.isCheckable(this.mInfo);
  }

  public boolean isChecked()
  {
    return IMPL.isChecked(this.mInfo);
  }

  public boolean isClickable()
  {
    return IMPL.isClickable(this.mInfo);
  }

  public boolean isContentInvalid()
  {
    return IMPL.isContentInvalid(this.mInfo);
  }

  public boolean isEnabled()
  {
    return IMPL.isEnabled(this.mInfo);
  }

  public boolean isFocusable()
  {
    return IMPL.isFocusable(this.mInfo);
  }

  public boolean isFocused()
  {
    return IMPL.isFocused(this.mInfo);
  }

  public boolean isLongClickable()
  {
    return IMPL.isLongClickable(this.mInfo);
  }

  public boolean isPassword()
  {
    return IMPL.isPassword(this.mInfo);
  }

  public boolean isScrollable()
  {
    return IMPL.isScrollable(this.mInfo);
  }

  public boolean isSelected()
  {
    return IMPL.isSelected(this.mInfo);
  }

  public boolean isVisibleToUser()
  {
    return IMPL.isVisibleToUser(this.mInfo);
  }

  public boolean performAction(int paramInt)
  {
    return IMPL.performAction(this.mInfo, paramInt);
  }

  public boolean performAction(int paramInt, Bundle paramBundle)
  {
    return IMPL.performAction(this.mInfo, paramInt, paramBundle);
  }

  public void recycle()
  {
    IMPL.recycle(this.mInfo);
  }

  public void setAccessibilityFocused(boolean paramBoolean)
  {
    IMPL.setAccessibilityFocused(this.mInfo, paramBoolean);
  }

  public void setBoundsInParent(Rect paramRect)
  {
    IMPL.setBoundsInParent(this.mInfo, paramRect);
  }

  public void setBoundsInScreen(Rect paramRect)
  {
    IMPL.setBoundsInScreen(this.mInfo, paramRect);
  }

  public void setCheckable(boolean paramBoolean)
  {
    IMPL.setCheckable(this.mInfo, paramBoolean);
  }

  public void setChecked(boolean paramBoolean)
  {
    IMPL.setChecked(this.mInfo, paramBoolean);
  }

  public void setClassName(CharSequence paramCharSequence)
  {
    IMPL.setClassName(this.mInfo, paramCharSequence);
  }

  public void setClickable(boolean paramBoolean)
  {
    IMPL.setClickable(this.mInfo, paramBoolean);
  }

  public void setCollectionInfo(Object paramObject)
  {
    IMPL.setCollectionInfo(this.mInfo, ((CollectionInfoCompat)paramObject).mInfo);
  }

  public void setCollectionItemInfo(Object paramObject)
  {
    IMPL.setCollectionItemInfo(this.mInfo, ((CollectionItemInfoCompat)paramObject).mInfo);
  }

  public void setContentDescription(CharSequence paramCharSequence)
  {
    IMPL.setContentDescription(this.mInfo, paramCharSequence);
  }

  public void setContentInvalid(boolean paramBoolean)
  {
    IMPL.setContentInvalid(this.mInfo, paramBoolean);
  }

  public void setEnabled(boolean paramBoolean)
  {
    IMPL.setEnabled(this.mInfo, paramBoolean);
  }

  public void setError(CharSequence paramCharSequence)
  {
    IMPL.setError(this.mInfo, paramCharSequence);
  }

  public void setFocusable(boolean paramBoolean)
  {
    IMPL.setFocusable(this.mInfo, paramBoolean);
  }

  public void setFocused(boolean paramBoolean)
  {
    IMPL.setFocused(this.mInfo, paramBoolean);
  }

  public void setLabelFor(View paramView)
  {
    IMPL.setLabelFor(this.mInfo, paramView);
  }

  public void setLabelFor(View paramView, int paramInt)
  {
    IMPL.setLabelFor(this.mInfo, paramView, paramInt);
  }

  public void setLiveRegion(int paramInt)
  {
    IMPL.setLiveRegion(this.mInfo, paramInt);
  }

  public void setLongClickable(boolean paramBoolean)
  {
    IMPL.setLongClickable(this.mInfo, paramBoolean);
  }

  public void setMovementGranularities(int paramInt)
  {
    IMPL.setMovementGranularities(this.mInfo, paramInt);
  }

  public void setPackageName(CharSequence paramCharSequence)
  {
    IMPL.setPackageName(this.mInfo, paramCharSequence);
  }

  public void setParent(View paramView)
  {
    IMPL.setParent(this.mInfo, paramView);
  }

  public void setParent(View paramView, int paramInt)
  {
    IMPL.setParent(this.mInfo, paramView, paramInt);
  }

  public void setPassword(boolean paramBoolean)
  {
    IMPL.setPassword(this.mInfo, paramBoolean);
  }

  public void setScrollable(boolean paramBoolean)
  {
    IMPL.setScrollable(this.mInfo, paramBoolean);
  }

  public void setSelected(boolean paramBoolean)
  {
    IMPL.setSelected(this.mInfo, paramBoolean);
  }

  public void setSource(View paramView)
  {
    IMPL.setSource(this.mInfo, paramView);
  }

  public void setSource(View paramView, int paramInt)
  {
    IMPL.setSource(this.mInfo, paramView, paramInt);
  }

  public void setText(CharSequence paramCharSequence)
  {
    IMPL.setText(this.mInfo, paramCharSequence);
  }

  public void setViewIdResourceName(String paramString)
  {
    IMPL.setViewIdResourceName(this.mInfo, paramString);
  }

  public void setVisibleToUser(boolean paramBoolean)
  {
    IMPL.setVisibleToUser(this.mInfo, paramBoolean);
  }

  public String toString()
  {
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append(super.toString());
    Rect localRect = new Rect();
    getBoundsInParent(localRect);
    localStringBuilder.append("; boundsInParent: " + localRect);
    getBoundsInScreen(localRect);
    localStringBuilder.append("; boundsInScreen: " + localRect);
    localStringBuilder.append("; packageName: ").append(getPackageName());
    localStringBuilder.append("; className: ").append(getClassName());
    localStringBuilder.append("; text: ").append(getText());
    localStringBuilder.append("; contentDescription: ").append(getContentDescription());
    localStringBuilder.append("; viewId: ").append(getViewIdResourceName());
    localStringBuilder.append("; checkable: ").append(isCheckable());
    localStringBuilder.append("; checked: ").append(isChecked());
    localStringBuilder.append("; focusable: ").append(isFocusable());
    localStringBuilder.append("; focused: ").append(isFocused());
    localStringBuilder.append("; selected: ").append(isSelected());
    localStringBuilder.append("; clickable: ").append(isClickable());
    localStringBuilder.append("; longClickable: ").append(isLongClickable());
    localStringBuilder.append("; enabled: ").append(isEnabled());
    localStringBuilder.append("; password: ").append(isPassword());
    localStringBuilder.append("; scrollable: " + isScrollable());
    localStringBuilder.append("; [");
    int i = getActions();
    while (i != 0)
    {
      int j = 1 << Integer.numberOfTrailingZeros(i);
      i &= (j ^ 0xFFFFFFFF);
      localStringBuilder.append(getActionSymbolicName(j));
      if (i != 0)
        localStringBuilder.append(", ");
    }
    localStringBuilder.append("]");
    return localStringBuilder.toString();
  }

  public static class AccessibilityActionCompat
  {
    private final Object mAction;

    public AccessibilityActionCompat(int paramInt, CharSequence paramCharSequence)
    {
      this(AccessibilityNodeInfoCompat.IMPL.newAccessibilityAction(paramInt, paramCharSequence));
    }

    private AccessibilityActionCompat(Object paramObject)
    {
      this.mAction = paramObject;
    }

    public int getId()
    {
      return AccessibilityNodeInfoCompat.IMPL.getAccessibilityActionId(this.mAction);
    }

    public CharSequence getLabel()
    {
      return AccessibilityNodeInfoCompat.IMPL.getAccessibilityActionLabel(this.mAction);
    }
  }

  static abstract interface AccessibilityNodeInfoImpl
  {
    public abstract void addAction(Object paramObject, int paramInt);

    public abstract void addAction(Object paramObject1, Object paramObject2);

    public abstract void addChild(Object paramObject, View paramView);

    public abstract void addChild(Object paramObject, View paramView, int paramInt);

    public abstract List<Object> findAccessibilityNodeInfosByText(Object paramObject, String paramString);

    public abstract Object findFocus(Object paramObject, int paramInt);

    public abstract Object focusSearch(Object paramObject, int paramInt);

    public abstract int getAccessibilityActionId(Object paramObject);

    public abstract CharSequence getAccessibilityActionLabel(Object paramObject);

    public abstract List<Object> getActionList(Object paramObject);

    public abstract int getActions(Object paramObject);

    public abstract void getBoundsInParent(Object paramObject, Rect paramRect);

    public abstract void getBoundsInScreen(Object paramObject, Rect paramRect);

    public abstract Object getChild(Object paramObject, int paramInt);

    public abstract int getChildCount(Object paramObject);

    public abstract CharSequence getClassName(Object paramObject);

    public abstract Object getCollectionInfo(Object paramObject);

    public abstract int getCollectionInfoColumnCount(Object paramObject);

    public abstract int getCollectionInfoRowCount(Object paramObject);

    public abstract int getCollectionItemColumnIndex(Object paramObject);

    public abstract int getCollectionItemColumnSpan(Object paramObject);

    public abstract Object getCollectionItemInfo(Object paramObject);

    public abstract int getCollectionItemRowIndex(Object paramObject);

    public abstract int getCollectionItemRowSpan(Object paramObject);

    public abstract CharSequence getContentDescription(Object paramObject);

    public abstract CharSequence getError(Object paramObject);

    public abstract int getLiveRegion(Object paramObject);

    public abstract int getMovementGranularities(Object paramObject);

    public abstract CharSequence getPackageName(Object paramObject);

    public abstract Object getParent(Object paramObject);

    public abstract Object getRangeInfo(Object paramObject);

    public abstract CharSequence getText(Object paramObject);

    public abstract AccessibilityNodeInfoCompat getTraversalAfter(Object paramObject);

    public abstract AccessibilityNodeInfoCompat getTraversalBefore(Object paramObject);

    public abstract String getViewIdResourceName(Object paramObject);

    public abstract int getWindowId(Object paramObject);

    public abstract boolean isAccessibilityFocused(Object paramObject);

    public abstract boolean isCheckable(Object paramObject);

    public abstract boolean isChecked(Object paramObject);

    public abstract boolean isClickable(Object paramObject);

    public abstract boolean isCollectionInfoHierarchical(Object paramObject);

    public abstract boolean isCollectionItemHeading(Object paramObject);

    public abstract boolean isCollectionItemSelected(Object paramObject);

    public abstract boolean isContentInvalid(Object paramObject);

    public abstract boolean isEnabled(Object paramObject);

    public abstract boolean isFocusable(Object paramObject);

    public abstract boolean isFocused(Object paramObject);

    public abstract boolean isLongClickable(Object paramObject);

    public abstract boolean isPassword(Object paramObject);

    public abstract boolean isScrollable(Object paramObject);

    public abstract boolean isSelected(Object paramObject);

    public abstract boolean isVisibleToUser(Object paramObject);

    public abstract Object newAccessibilityAction(int paramInt, CharSequence paramCharSequence);

    public abstract Object obtain();

    public abstract Object obtain(View paramView);

    public abstract Object obtain(View paramView, int paramInt);

    public abstract Object obtain(Object paramObject);

    public abstract Object obtainCollectionInfo(int paramInt1, int paramInt2, boolean paramBoolean, int paramInt3);

    public abstract Object obtainCollectionItemInfo(int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean1, boolean paramBoolean2);

    public abstract boolean performAction(Object paramObject, int paramInt);

    public abstract boolean performAction(Object paramObject, int paramInt, Bundle paramBundle);

    public abstract void recycle(Object paramObject);

    public abstract void setAccessibilityFocused(Object paramObject, boolean paramBoolean);

    public abstract void setBoundsInParent(Object paramObject, Rect paramRect);

    public abstract void setBoundsInScreen(Object paramObject, Rect paramRect);

    public abstract void setCheckable(Object paramObject, boolean paramBoolean);

    public abstract void setChecked(Object paramObject, boolean paramBoolean);

    public abstract void setClassName(Object paramObject, CharSequence paramCharSequence);

    public abstract void setClickable(Object paramObject, boolean paramBoolean);

    public abstract void setCollectionInfo(Object paramObject1, Object paramObject2);

    public abstract void setCollectionItemInfo(Object paramObject1, Object paramObject2);

    public abstract void setContentDescription(Object paramObject, CharSequence paramCharSequence);

    public abstract void setContentInvalid(Object paramObject, boolean paramBoolean);

    public abstract void setEnabled(Object paramObject, boolean paramBoolean);

    public abstract void setError(Object paramObject, CharSequence paramCharSequence);

    public abstract void setFocusable(Object paramObject, boolean paramBoolean);

    public abstract void setFocused(Object paramObject, boolean paramBoolean);

    public abstract void setLabelFor(Object paramObject, View paramView);

    public abstract void setLabelFor(Object paramObject, View paramView, int paramInt);

    public abstract void setLiveRegion(Object paramObject, int paramInt);

    public abstract void setLongClickable(Object paramObject, boolean paramBoolean);

    public abstract void setMovementGranularities(Object paramObject, int paramInt);

    public abstract void setPackageName(Object paramObject, CharSequence paramCharSequence);

    public abstract void setParent(Object paramObject, View paramView);

    public abstract void setParent(Object paramObject, View paramView, int paramInt);

    public abstract void setPassword(Object paramObject, boolean paramBoolean);

    public abstract void setScrollable(Object paramObject, boolean paramBoolean);

    public abstract void setSelected(Object paramObject, boolean paramBoolean);

    public abstract void setSource(Object paramObject, View paramView);

    public abstract void setSource(Object paramObject, View paramView, int paramInt);

    public abstract void setText(Object paramObject, CharSequence paramCharSequence);

    public abstract void setTraversalAfter(Object paramObject, View paramView);

    public abstract void setTraversalAfter(Object paramObject, View paramView, int paramInt);

    public abstract void setTraversalBefore(Object paramObject, View paramView);

    public abstract void setTraversalBefore(Object paramObject, View paramView, int paramInt);

    public abstract void setViewIdResourceName(Object paramObject, String paramString);

    public abstract void setVisibleToUser(Object paramObject, boolean paramBoolean);
  }

  public static class CollectionInfoCompat
  {
    public static final int SELECTION_MODE_MULTIPLE = 2;
    public static final int SELECTION_MODE_NONE = 0;
    public static final int SELECTION_MODE_SINGLE = 1;
    final Object mInfo;

    private CollectionInfoCompat(Object paramObject)
    {
      this.mInfo = paramObject;
    }

    public static CollectionInfoCompat obtain(int paramInt1, int paramInt2, boolean paramBoolean, int paramInt3)
    {
      return new CollectionInfoCompat(AccessibilityNodeInfoCompat.IMPL.obtainCollectionInfo(paramInt1, paramInt2, paramBoolean, paramInt3));
    }

    public int getColumnCount()
    {
      return AccessibilityNodeInfoCompat.IMPL.getCollectionInfoColumnCount(this.mInfo);
    }

    public int getRowCount()
    {
      return AccessibilityNodeInfoCompat.IMPL.getCollectionInfoRowCount(this.mInfo);
    }

    public boolean isHierarchical()
    {
      return AccessibilityNodeInfoCompat.IMPL.isCollectionInfoHierarchical(this.mInfo);
    }
  }

  public static class CollectionItemInfoCompat
  {
    private final Object mInfo;

    private CollectionItemInfoCompat(Object paramObject)
    {
      this.mInfo = paramObject;
    }

    public static CollectionItemInfoCompat obtain(int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean1, boolean paramBoolean2)
    {
      return new CollectionItemInfoCompat(AccessibilityNodeInfoCompat.IMPL.obtainCollectionItemInfo(paramInt1, paramInt2, paramInt3, paramInt4, paramBoolean1, paramBoolean2));
    }

    public int getColumnIndex()
    {
      return AccessibilityNodeInfoCompat.IMPL.getCollectionItemColumnIndex(this.mInfo);
    }

    public int getColumnSpan()
    {
      return AccessibilityNodeInfoCompat.IMPL.getCollectionItemColumnSpan(this.mInfo);
    }

    public int getRowIndex()
    {
      return AccessibilityNodeInfoCompat.IMPL.getCollectionItemRowIndex(this.mInfo);
    }

    public int getRowSpan()
    {
      return AccessibilityNodeInfoCompat.IMPL.getCollectionItemRowSpan(this.mInfo);
    }

    public boolean isHeading()
    {
      return AccessibilityNodeInfoCompat.IMPL.isCollectionItemHeading(this.mInfo);
    }

    public boolean isSelected()
    {
      return AccessibilityNodeInfoCompat.IMPL.isCollectionItemSelected(this.mInfo);
    }
  }

  public static class RangeInfoCompat
  {
    public static final int RANGE_TYPE_FLOAT = 1;
    public static final int RANGE_TYPE_INT = 0;
    public static final int RANGE_TYPE_PERCENT = 2;
    private final Object mInfo;

    private RangeInfoCompat(Object paramObject)
    {
      this.mInfo = paramObject;
    }

    public float getCurrent()
    {
      return AccessibilityNodeInfoCompatKitKat.RangeInfo.getCurrent(this.mInfo);
    }

    public float getMax()
    {
      return AccessibilityNodeInfoCompatKitKat.RangeInfo.getMax(this.mInfo);
    }

    public float getMin()
    {
      return AccessibilityNodeInfoCompatKitKat.RangeInfo.getMin(this.mInfo);
    }

    public int getType()
    {
      return AccessibilityNodeInfoCompatKitKat.RangeInfo.getType(this.mInfo);
    }
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.view.accessibility.AccessibilityNodeInfoCompat
 * JD-Core Version:    0.6.2
 */
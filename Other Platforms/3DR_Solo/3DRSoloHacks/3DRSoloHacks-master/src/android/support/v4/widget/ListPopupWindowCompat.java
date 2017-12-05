package android.support.v4.widget;

import android.os.Build.VERSION;
import android.view.View;
import android.view.View.OnTouchListener;

public class ListPopupWindowCompat
{
  static final ListPopupWindowImpl IMPL = new ListPopupWindowCompat.BaseListPopupWindowImpl();

  static
  {
    if (Build.VERSION.SDK_INT >= 19)
    {
      IMPL = new ListPopupWindowCompat.KitKatListPopupWindowImpl();
      return;
    }
  }

  public static View.OnTouchListener createDragToOpenListener(Object paramObject, View paramView)
  {
    return IMPL.createDragToOpenListener(paramObject, paramView);
  }

  static abstract interface ListPopupWindowImpl
  {
    public abstract View.OnTouchListener createDragToOpenListener(Object paramObject, View paramView);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.widget.ListPopupWindowCompat
 * JD-Core Version:    0.6.2
 */
package android.support.v4.widget;

import android.os.Build.VERSION;
import android.view.View;
import android.widget.PopupWindow;

public class PopupWindowCompat
{
  static final PopupWindowImpl IMPL = new PopupWindowCompat.BasePopupWindowImpl();

  static
  {
    if (Build.VERSION.SDK_INT >= 19)
    {
      IMPL = new PopupWindowCompat.KitKatPopupWindowImpl();
      return;
    }
  }

  public static void showAsDropDown(PopupWindow paramPopupWindow, View paramView, int paramInt1, int paramInt2, int paramInt3)
  {
    IMPL.showAsDropDown(paramPopupWindow, paramView, paramInt1, paramInt2, paramInt3);
  }

  static abstract interface PopupWindowImpl
  {
    public abstract void showAsDropDown(PopupWindow paramPopupWindow, View paramView, int paramInt1, int paramInt2, int paramInt3);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.widget.PopupWindowCompat
 * JD-Core Version:    0.6.2
 */
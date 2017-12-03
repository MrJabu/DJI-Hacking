package android.support.v4.view;

import android.os.Build.VERSION;
import android.view.ViewConfiguration;

public class ViewConfigurationCompat
{
  static final ViewConfigurationVersionImpl IMPL = new ViewConfigurationCompat.BaseViewConfigurationVersionImpl();

  static
  {
    if (Build.VERSION.SDK_INT >= 14)
    {
      IMPL = new ViewConfigurationCompat.IcsViewConfigurationVersionImpl();
      return;
    }
    if (Build.VERSION.SDK_INT >= 11)
    {
      IMPL = new ViewConfigurationCompat.HoneycombViewConfigurationVersionImpl();
      return;
    }
    if (Build.VERSION.SDK_INT >= 8)
    {
      IMPL = new ViewConfigurationCompat.FroyoViewConfigurationVersionImpl();
      return;
    }
  }

  public static int getScaledPagingTouchSlop(ViewConfiguration paramViewConfiguration)
  {
    return IMPL.getScaledPagingTouchSlop(paramViewConfiguration);
  }

  public static boolean hasPermanentMenuKey(ViewConfiguration paramViewConfiguration)
  {
    return IMPL.hasPermanentMenuKey(paramViewConfiguration);
  }

  static abstract interface ViewConfigurationVersionImpl
  {
    public abstract int getScaledPagingTouchSlop(ViewConfiguration paramViewConfiguration);

    public abstract boolean hasPermanentMenuKey(ViewConfiguration paramViewConfiguration);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.view.ViewConfigurationCompat
 * JD-Core Version:    0.6.2
 */
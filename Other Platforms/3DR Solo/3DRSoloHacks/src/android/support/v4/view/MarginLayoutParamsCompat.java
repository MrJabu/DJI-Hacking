package android.support.v4.view;

import android.os.Build.VERSION;
import android.view.ViewGroup.MarginLayoutParams;

public class MarginLayoutParamsCompat
{
  static final MarginLayoutParamsCompatImpl IMPL = new MarginLayoutParamsCompat.MarginLayoutParamsCompatImplBase();

  static
  {
    if (Build.VERSION.SDK_INT >= 17)
    {
      IMPL = new MarginLayoutParamsCompat.MarginLayoutParamsCompatImplJbMr1();
      return;
    }
  }

  public static int getLayoutDirection(ViewGroup.MarginLayoutParams paramMarginLayoutParams)
  {
    return IMPL.getLayoutDirection(paramMarginLayoutParams);
  }

  public static int getMarginEnd(ViewGroup.MarginLayoutParams paramMarginLayoutParams)
  {
    return IMPL.getMarginEnd(paramMarginLayoutParams);
  }

  public static int getMarginStart(ViewGroup.MarginLayoutParams paramMarginLayoutParams)
  {
    return IMPL.getMarginStart(paramMarginLayoutParams);
  }

  public static boolean isMarginRelative(ViewGroup.MarginLayoutParams paramMarginLayoutParams)
  {
    return IMPL.isMarginRelative(paramMarginLayoutParams);
  }

  public static void resolveLayoutDirection(ViewGroup.MarginLayoutParams paramMarginLayoutParams, int paramInt)
  {
    IMPL.resolveLayoutDirection(paramMarginLayoutParams, paramInt);
  }

  public static void setLayoutDirection(ViewGroup.MarginLayoutParams paramMarginLayoutParams, int paramInt)
  {
    IMPL.setLayoutDirection(paramMarginLayoutParams, paramInt);
  }

  public static void setMarginEnd(ViewGroup.MarginLayoutParams paramMarginLayoutParams, int paramInt)
  {
    IMPL.setMarginEnd(paramMarginLayoutParams, paramInt);
  }

  public static void setMarginStart(ViewGroup.MarginLayoutParams paramMarginLayoutParams, int paramInt)
  {
    IMPL.setMarginStart(paramMarginLayoutParams, paramInt);
  }

  static abstract interface MarginLayoutParamsCompatImpl
  {
    public abstract int getLayoutDirection(ViewGroup.MarginLayoutParams paramMarginLayoutParams);

    public abstract int getMarginEnd(ViewGroup.MarginLayoutParams paramMarginLayoutParams);

    public abstract int getMarginStart(ViewGroup.MarginLayoutParams paramMarginLayoutParams);

    public abstract boolean isMarginRelative(ViewGroup.MarginLayoutParams paramMarginLayoutParams);

    public abstract void resolveLayoutDirection(ViewGroup.MarginLayoutParams paramMarginLayoutParams, int paramInt);

    public abstract void setLayoutDirection(ViewGroup.MarginLayoutParams paramMarginLayoutParams, int paramInt);

    public abstract void setMarginEnd(ViewGroup.MarginLayoutParams paramMarginLayoutParams, int paramInt);

    public abstract void setMarginStart(ViewGroup.MarginLayoutParams paramMarginLayoutParams, int paramInt);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.view.MarginLayoutParamsCompat
 * JD-Core Version:    0.6.2
 */
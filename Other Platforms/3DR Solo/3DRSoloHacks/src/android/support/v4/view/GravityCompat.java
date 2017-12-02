package android.support.v4.view;

import android.graphics.Rect;
import android.os.Build.VERSION;

public class GravityCompat
{
  public static final int END = 8388613;
  static final GravityCompatImpl IMPL = new GravityCompat.GravityCompatImplBase();
  public static final int RELATIVE_HORIZONTAL_GRAVITY_MASK = 8388615;
  public static final int RELATIVE_LAYOUT_DIRECTION = 8388608;
  public static final int START = 8388611;

  static
  {
    if (Build.VERSION.SDK_INT >= 17)
    {
      IMPL = new GravityCompat.GravityCompatImplJellybeanMr1();
      return;
    }
  }

  public static void apply(int paramInt1, int paramInt2, int paramInt3, Rect paramRect1, int paramInt4, int paramInt5, Rect paramRect2, int paramInt6)
  {
    IMPL.apply(paramInt1, paramInt2, paramInt3, paramRect1, paramInt4, paramInt5, paramRect2, paramInt6);
  }

  public static void apply(int paramInt1, int paramInt2, int paramInt3, Rect paramRect1, Rect paramRect2, int paramInt4)
  {
    IMPL.apply(paramInt1, paramInt2, paramInt3, paramRect1, paramRect2, paramInt4);
  }

  public static void applyDisplay(int paramInt1, Rect paramRect1, Rect paramRect2, int paramInt2)
  {
    IMPL.applyDisplay(paramInt1, paramRect1, paramRect2, paramInt2);
  }

  public static int getAbsoluteGravity(int paramInt1, int paramInt2)
  {
    return IMPL.getAbsoluteGravity(paramInt1, paramInt2);
  }

  static abstract interface GravityCompatImpl
  {
    public abstract void apply(int paramInt1, int paramInt2, int paramInt3, Rect paramRect1, int paramInt4, int paramInt5, Rect paramRect2, int paramInt6);

    public abstract void apply(int paramInt1, int paramInt2, int paramInt3, Rect paramRect1, Rect paramRect2, int paramInt4);

    public abstract void applyDisplay(int paramInt1, Rect paramRect1, Rect paramRect2, int paramInt2);

    public abstract int getAbsoluteGravity(int paramInt1, int paramInt2);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.view.GravityCompat
 * JD-Core Version:    0.6.2
 */
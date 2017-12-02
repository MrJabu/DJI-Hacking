package android.support.v4.graphics;

import android.graphics.Bitmap;
import android.os.Build.VERSION;

public class BitmapCompat
{
  static final BitmapImpl IMPL = new BitmapCompat.BaseBitmapImpl();

  static
  {
    int i = Build.VERSION.SDK_INT;
    if (i >= 19)
    {
      IMPL = new BitmapCompat.KitKatBitmapCompatImpl();
      return;
    }
    if (i >= 18)
    {
      IMPL = new BitmapCompat.JbMr2BitmapCompatImpl();
      return;
    }
    if (i >= 12)
    {
      IMPL = new BitmapCompat.HcMr1BitmapCompatImpl();
      return;
    }
  }

  public static int getAllocationByteCount(Bitmap paramBitmap)
  {
    return IMPL.getAllocationByteCount(paramBitmap);
  }

  public static boolean hasMipMap(Bitmap paramBitmap)
  {
    return IMPL.hasMipMap(paramBitmap);
  }

  public static void setHasMipMap(Bitmap paramBitmap, boolean paramBoolean)
  {
    IMPL.setHasMipMap(paramBitmap, paramBoolean);
  }

  static abstract interface BitmapImpl
  {
    public abstract int getAllocationByteCount(Bitmap paramBitmap);

    public abstract boolean hasMipMap(Bitmap paramBitmap);

    public abstract void setHasMipMap(Bitmap paramBitmap, boolean paramBoolean);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.graphics.BitmapCompat
 * JD-Core Version:    0.6.2
 */
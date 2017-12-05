package android.support.v4.graphics.drawable;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;

public class DrawableCompat
{
  static final DrawableImpl IMPL = new DrawableCompat.BaseDrawableImpl();

  static
  {
    int i = Build.VERSION.SDK_INT;
    if (i >= 22)
    {
      IMPL = new DrawableCompat.LollipopMr1DrawableImpl();
      return;
    }
    if (i >= 21)
    {
      IMPL = new DrawableCompat.LollipopDrawableImpl();
      return;
    }
    if (i >= 19)
    {
      IMPL = new DrawableCompat.KitKatDrawableImpl();
      return;
    }
    if (i >= 11)
    {
      IMPL = new DrawableCompat.HoneycombDrawableImpl();
      return;
    }
  }

  public static boolean isAutoMirrored(Drawable paramDrawable)
  {
    return IMPL.isAutoMirrored(paramDrawable);
  }

  public static void jumpToCurrentState(Drawable paramDrawable)
  {
    IMPL.jumpToCurrentState(paramDrawable);
  }

  public static void setAutoMirrored(Drawable paramDrawable, boolean paramBoolean)
  {
    IMPL.setAutoMirrored(paramDrawable, paramBoolean);
  }

  public static void setHotspot(Drawable paramDrawable, float paramFloat1, float paramFloat2)
  {
    IMPL.setHotspot(paramDrawable, paramFloat1, paramFloat2);
  }

  public static void setHotspotBounds(Drawable paramDrawable, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    IMPL.setHotspotBounds(paramDrawable, paramInt1, paramInt2, paramInt3, paramInt4);
  }

  public static void setTint(Drawable paramDrawable, int paramInt)
  {
    IMPL.setTint(paramDrawable, paramInt);
  }

  public static void setTintList(Drawable paramDrawable, ColorStateList paramColorStateList)
  {
    IMPL.setTintList(paramDrawable, paramColorStateList);
  }

  public static void setTintMode(Drawable paramDrawable, PorterDuff.Mode paramMode)
  {
    IMPL.setTintMode(paramDrawable, paramMode);
  }

  public static <T extends Drawable> T unwrap(Drawable paramDrawable)
  {
    if ((paramDrawable instanceof DrawableWrapper))
      paramDrawable = ((DrawableWrapper)paramDrawable).getWrappedDrawable();
    return paramDrawable;
  }

  public static Drawable wrap(Drawable paramDrawable)
  {
    return IMPL.wrap(paramDrawable);
  }

  static abstract interface DrawableImpl
  {
    public abstract boolean isAutoMirrored(Drawable paramDrawable);

    public abstract void jumpToCurrentState(Drawable paramDrawable);

    public abstract void setAutoMirrored(Drawable paramDrawable, boolean paramBoolean);

    public abstract void setHotspot(Drawable paramDrawable, float paramFloat1, float paramFloat2);

    public abstract void setHotspotBounds(Drawable paramDrawable, int paramInt1, int paramInt2, int paramInt3, int paramInt4);

    public abstract void setTint(Drawable paramDrawable, int paramInt);

    public abstract void setTintList(Drawable paramDrawable, ColorStateList paramColorStateList);

    public abstract void setTintMode(Drawable paramDrawable, PorterDuff.Mode paramMode);

    public abstract Drawable wrap(Drawable paramDrawable);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.graphics.drawable.DrawableCompat
 * JD-Core Version:    0.6.2
 */
package android.support.v4.graphics.drawable;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;

public abstract interface DrawableWrapper
{
  public abstract Drawable getWrappedDrawable();

  public abstract void setTint(int paramInt);

  public abstract void setTintList(ColorStateList paramColorStateList);

  public abstract void setTintMode(PorterDuff.Mode paramMode);

  public abstract void setWrappedDrawable(Drawable paramDrawable);
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.graphics.drawable.DrawableWrapper
 * JD-Core Version:    0.6.2
 */
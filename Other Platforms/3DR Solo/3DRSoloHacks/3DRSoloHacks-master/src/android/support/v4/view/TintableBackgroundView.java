package android.support.v4.view;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff.Mode;
import android.support.annotation.Nullable;

public abstract interface TintableBackgroundView
{
  @Nullable
  public abstract ColorStateList getSupportBackgroundTintList();

  @Nullable
  public abstract PorterDuff.Mode getSupportBackgroundTintMode();

  public abstract void setSupportBackgroundTintList(@Nullable ColorStateList paramColorStateList);

  public abstract void setSupportBackgroundTintMode(@Nullable PorterDuff.Mode paramMode);
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.view.TintableBackgroundView
 * JD-Core Version:    0.6.2
 */
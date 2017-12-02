package android.support.v4.widget;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.TextView;

class TextViewCompatJbMr1
{
  public static void setCompoundDrawablesRelative(@NonNull TextView paramTextView, @Nullable Drawable paramDrawable1, @Nullable Drawable paramDrawable2, @Nullable Drawable paramDrawable3, @Nullable Drawable paramDrawable4)
  {
    int i = 1;
    Drawable localDrawable;
    if (paramTextView.getLayoutDirection() == i)
    {
      if (i == 0)
        break label42;
      localDrawable = paramDrawable3;
      label20: if (i == 0)
        break label48;
    }
    while (true)
    {
      paramTextView.setCompoundDrawables(localDrawable, paramDrawable2, paramDrawable1, paramDrawable4);
      return;
      i = 0;
      break;
      label42: localDrawable = paramDrawable1;
      break label20;
      label48: paramDrawable1 = paramDrawable3;
    }
  }

  public static void setCompoundDrawablesRelativeWithIntrinsicBounds(@NonNull TextView paramTextView, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    int i = 1;
    int j;
    if (paramTextView.getLayoutDirection() == i)
    {
      if (i == 0)
        break label42;
      j = paramInt3;
      label20: if (i == 0)
        break label48;
    }
    while (true)
    {
      paramTextView.setCompoundDrawablesWithIntrinsicBounds(j, paramInt2, paramInt1, paramInt4);
      return;
      i = 0;
      break;
      label42: j = paramInt1;
      break label20;
      label48: paramInt1 = paramInt3;
    }
  }

  public static void setCompoundDrawablesRelativeWithIntrinsicBounds(@NonNull TextView paramTextView, @Nullable Drawable paramDrawable1, @Nullable Drawable paramDrawable2, @Nullable Drawable paramDrawable3, @Nullable Drawable paramDrawable4)
  {
    int i = 1;
    Drawable localDrawable;
    if (paramTextView.getLayoutDirection() == i)
    {
      if (i == 0)
        break label42;
      localDrawable = paramDrawable3;
      label20: if (i == 0)
        break label48;
    }
    while (true)
    {
      paramTextView.setCompoundDrawablesWithIntrinsicBounds(localDrawable, paramDrawable2, paramDrawable1, paramDrawable4);
      return;
      i = 0;
      break;
      label42: localDrawable = paramDrawable1;
      break label20;
      label48: paramDrawable1 = paramDrawable3;
    }
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.widget.TextViewCompatJbMr1
 * JD-Core Version:    0.6.2
 */
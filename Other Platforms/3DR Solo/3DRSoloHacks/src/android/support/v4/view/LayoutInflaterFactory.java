package android.support.v4.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public abstract interface LayoutInflaterFactory
{
  public abstract View onCreateView(View paramView, String paramString, Context paramContext, AttributeSet paramAttributeSet);
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.view.LayoutInflaterFactory
 * JD-Core Version:    0.6.2
 */
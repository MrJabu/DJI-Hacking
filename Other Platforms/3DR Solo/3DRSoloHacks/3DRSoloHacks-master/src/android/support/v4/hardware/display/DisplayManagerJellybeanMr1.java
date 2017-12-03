package android.support.v4.hardware.display;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.view.Display;

final class DisplayManagerJellybeanMr1
{
  public static Display getDisplay(Object paramObject, int paramInt)
  {
    return ((DisplayManager)paramObject).getDisplay(paramInt);
  }

  public static Object getDisplayManager(Context paramContext)
  {
    return paramContext.getSystemService("display");
  }

  public static Display[] getDisplays(Object paramObject)
  {
    return ((DisplayManager)paramObject).getDisplays();
  }

  public static Display[] getDisplays(Object paramObject, String paramString)
  {
    return ((DisplayManager)paramObject).getDisplays(paramString);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.hardware.display.DisplayManagerJellybeanMr1
 * JD-Core Version:    0.6.2
 */
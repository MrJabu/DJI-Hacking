package android.support.v4.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.view.View;

public class ActivityOptionsCompat
{
  public static ActivityOptionsCompat makeCustomAnimation(Context paramContext, int paramInt1, int paramInt2)
  {
    if (Build.VERSION.SDK_INT >= 16)
      return new ActivityOptionsCompat.ActivityOptionsImplJB(ActivityOptionsCompatJB.makeCustomAnimation(paramContext, paramInt1, paramInt2));
    return new ActivityOptionsCompat();
  }

  public static ActivityOptionsCompat makeScaleUpAnimation(View paramView, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if (Build.VERSION.SDK_INT >= 16)
      return new ActivityOptionsCompat.ActivityOptionsImplJB(ActivityOptionsCompatJB.makeScaleUpAnimation(paramView, paramInt1, paramInt2, paramInt3, paramInt4));
    return new ActivityOptionsCompat();
  }

  public static ActivityOptionsCompat makeSceneTransitionAnimation(Activity paramActivity, View paramView, String paramString)
  {
    if (Build.VERSION.SDK_INT >= 21)
      return new ActivityOptionsCompat.ActivityOptionsImpl21(ActivityOptionsCompat21.makeSceneTransitionAnimation(paramActivity, paramView, paramString));
    return new ActivityOptionsCompat();
  }

  public static ActivityOptionsCompat makeSceneTransitionAnimation(Activity paramActivity, Pair<View, String>[] paramArrayOfPair)
  {
    if (Build.VERSION.SDK_INT >= 21)
    {
      String[] arrayOfString = null;
      View[] arrayOfView = null;
      if (paramArrayOfPair != null)
      {
        arrayOfView = new View[paramArrayOfPair.length];
        arrayOfString = new String[paramArrayOfPair.length];
        for (int i = 0; i < paramArrayOfPair.length; i++)
        {
          arrayOfView[i] = ((View)paramArrayOfPair[i].first);
          arrayOfString[i] = ((String)paramArrayOfPair[i].second);
        }
      }
      return new ActivityOptionsCompat.ActivityOptionsImpl21(ActivityOptionsCompat21.makeSceneTransitionAnimation(paramActivity, arrayOfView, arrayOfString));
    }
    return new ActivityOptionsCompat();
  }

  public static ActivityOptionsCompat makeThumbnailScaleUpAnimation(View paramView, Bitmap paramBitmap, int paramInt1, int paramInt2)
  {
    if (Build.VERSION.SDK_INT >= 16)
      return new ActivityOptionsCompat.ActivityOptionsImplJB(ActivityOptionsCompatJB.makeThumbnailScaleUpAnimation(paramView, paramBitmap, paramInt1, paramInt2));
    return new ActivityOptionsCompat();
  }

  public Bundle toBundle()
  {
    return null;
  }

  public void update(ActivityOptionsCompat paramActivityOptionsCompat)
  {
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.app.ActivityOptionsCompat
 * JD-Core Version:    0.6.2
 */
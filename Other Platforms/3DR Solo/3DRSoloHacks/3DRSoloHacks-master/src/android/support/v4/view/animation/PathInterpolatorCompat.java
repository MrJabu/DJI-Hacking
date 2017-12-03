package android.support.v4.view.animation;

import android.graphics.Path;
import android.os.Build.VERSION;
import android.view.animation.Interpolator;

public class PathInterpolatorCompat
{
  public static Interpolator create(float paramFloat1, float paramFloat2)
  {
    if (Build.VERSION.SDK_INT >= 21)
      return PathInterpolatorCompatApi21.create(paramFloat1, paramFloat2);
    return PathInterpolatorCompatBase.create(paramFloat1, paramFloat2);
  }

  public static Interpolator create(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4)
  {
    if (Build.VERSION.SDK_INT >= 21)
      return PathInterpolatorCompatApi21.create(paramFloat1, paramFloat2, paramFloat3, paramFloat4);
    return PathInterpolatorCompatBase.create(paramFloat1, paramFloat2, paramFloat3, paramFloat4);
  }

  public static Interpolator create(Path paramPath)
  {
    if (Build.VERSION.SDK_INT >= 21)
      return PathInterpolatorCompatApi21.create(paramPath);
    return PathInterpolatorCompatBase.create(paramPath);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.view.animation.PathInterpolatorCompat
 * JD-Core Version:    0.6.2
 */
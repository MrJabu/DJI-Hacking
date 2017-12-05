package android.support.v4.view;

import android.os.Build.VERSION;
import android.view.VelocityTracker;

public class VelocityTrackerCompat
{
  static final VelocityTrackerVersionImpl IMPL = new VelocityTrackerCompat.BaseVelocityTrackerVersionImpl();

  static
  {
    if (Build.VERSION.SDK_INT >= 11)
    {
      IMPL = new VelocityTrackerCompat.HoneycombVelocityTrackerVersionImpl();
      return;
    }
  }

  public static float getXVelocity(VelocityTracker paramVelocityTracker, int paramInt)
  {
    return IMPL.getXVelocity(paramVelocityTracker, paramInt);
  }

  public static float getYVelocity(VelocityTracker paramVelocityTracker, int paramInt)
  {
    return IMPL.getYVelocity(paramVelocityTracker, paramInt);
  }

  static abstract interface VelocityTrackerVersionImpl
  {
    public abstract float getXVelocity(VelocityTracker paramVelocityTracker, int paramInt);

    public abstract float getYVelocity(VelocityTracker paramVelocityTracker, int paramInt);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.view.VelocityTrackerCompat
 * JD-Core Version:    0.6.2
 */
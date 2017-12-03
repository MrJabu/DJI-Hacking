package android.support.v4.media;

import android.media.VolumeProvider;

class VolumeProviderCompatApi21
{
  public static Object createVolumeProvider(int paramInt1, int paramInt2, int paramInt3, final Delegate paramDelegate)
  {
    return new VolumeProvider(paramInt1, paramInt2, paramInt3)
    {
      public void onAdjustVolume(int paramAnonymousInt)
      {
        paramDelegate.onAdjustVolume(paramAnonymousInt);
      }

      public void onSetVolumeTo(int paramAnonymousInt)
      {
        paramDelegate.onSetVolumeTo(paramAnonymousInt);
      }
    };
  }

  public static void setCurrentVolume(Object paramObject, int paramInt)
  {
    ((VolumeProvider)paramObject).setCurrentVolume(paramInt);
  }

  public static abstract interface Delegate
  {
    public abstract void onAdjustVolume(int paramInt);

    public abstract void onSetVolumeTo(int paramInt);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.media.VolumeProviderCompatApi21
 * JD-Core Version:    0.6.2
 */
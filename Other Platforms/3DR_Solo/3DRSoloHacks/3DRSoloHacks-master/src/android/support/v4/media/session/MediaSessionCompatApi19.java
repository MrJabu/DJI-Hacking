package android.support.v4.media.session;

import android.media.Rating;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.media.RemoteControlClient.OnMetadataUpdateListener;
import android.os.Bundle;

public class MediaSessionCompatApi19
{
  private static final long ACTION_SET_RATING = 128L;
  private static final String METADATA_KEY_RATING = "android.media.metadata.RATING";
  private static final String METADATA_KEY_USER_RATING = "android.media.metadata.USER_RATING";
  private static final String METADATA_KEY_YEAR = "android.media.metadata.YEAR";

  static void addNewMetadata(Bundle paramBundle, RemoteControlClient.MetadataEditor paramMetadataEditor)
  {
    if (paramBundle == null);
    do
    {
      return;
      if (paramBundle.containsKey("android.media.metadata.YEAR"))
        paramMetadataEditor.putLong(8, paramBundle.getLong("android.media.metadata.YEAR"));
      if (paramBundle.containsKey("android.media.metadata.RATING"))
        paramMetadataEditor.putObject(101, paramBundle.getParcelable("android.media.metadata.RATING"));
    }
    while (!paramBundle.containsKey("android.media.metadata.USER_RATING"));
    paramMetadataEditor.putObject(268435457, paramBundle.getParcelable("android.media.metadata.USER_RATING"));
  }

  public static Object createMetadataUpdateListener(MediaSessionCompatApi14.Callback paramCallback)
  {
    return new OnMetadataUpdateListener(paramCallback);
  }

  static int getRccTransportControlFlagsFromActions(long paramLong)
  {
    int i = MediaSessionCompatApi18.getRccTransportControlFlagsFromActions(paramLong);
    if ((0x80 & paramLong) != 0L)
      i |= 512;
    return i;
  }

  public static void setMetadata(Object paramObject, Bundle paramBundle, long paramLong)
  {
    RemoteControlClient.MetadataEditor localMetadataEditor = ((RemoteControlClient)paramObject).editMetadata(true);
    MediaSessionCompatApi14.buildOldMetadata(paramBundle, localMetadataEditor);
    addNewMetadata(paramBundle, localMetadataEditor);
    if ((0x80 & paramLong) != 0L)
      localMetadataEditor.addEditableKey(268435457);
    localMetadataEditor.apply();
  }

  public static void setOnMetadataUpdateListener(Object paramObject1, Object paramObject2)
  {
    ((RemoteControlClient)paramObject1).setMetadataUpdateListener((RemoteControlClient.OnMetadataUpdateListener)paramObject2);
  }

  public static void setTransportControlFlags(Object paramObject, long paramLong)
  {
    ((RemoteControlClient)paramObject).setTransportControlFlags(getRccTransportControlFlagsFromActions(paramLong));
  }

  static class OnMetadataUpdateListener<T extends MediaSessionCompatApi14.Callback>
    implements RemoteControlClient.OnMetadataUpdateListener
  {
    protected final T mCallback;

    public OnMetadataUpdateListener(T paramT)
    {
      this.mCallback = paramT;
    }

    public void onMetadataUpdate(int paramInt, Object paramObject)
    {
      if ((paramInt == 268435457) && ((paramObject instanceof Rating)))
        this.mCallback.onSetRating(paramObject);
    }
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.media.session.MediaSessionCompatApi19
 * JD-Core Version:    0.6.2
 */
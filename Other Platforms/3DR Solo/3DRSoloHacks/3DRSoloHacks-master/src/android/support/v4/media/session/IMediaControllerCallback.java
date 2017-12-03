package android.support.v4.media.session;

import android.os.Bundle;
import android.os.IInterface;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import java.util.List;

public abstract interface IMediaControllerCallback extends IInterface
{
  public abstract void onEvent(String paramString, Bundle paramBundle)
    throws RemoteException;

  public abstract void onExtrasChanged(Bundle paramBundle)
    throws RemoteException;

  public abstract void onMetadataChanged(MediaMetadataCompat paramMediaMetadataCompat)
    throws RemoteException;

  public abstract void onPlaybackStateChanged(PlaybackStateCompat paramPlaybackStateCompat)
    throws RemoteException;

  public abstract void onQueueChanged(List<MediaSessionCompat.QueueItem> paramList)
    throws RemoteException;

  public abstract void onQueueTitleChanged(CharSequence paramCharSequence)
    throws RemoteException;

  public abstract void onSessionDestroyed()
    throws RemoteException;

  public abstract void onVolumeInfoChanged(ParcelableVolumeInfo paramParcelableVolumeInfo)
    throws RemoteException;
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.media.session.IMediaControllerCallback
 * JD-Core Version:    0.6.2
 */
package android.support.v4.media.session;

import android.app.PendingIntent;
import android.os.Bundle;
import android.os.IInterface;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.view.KeyEvent;
import java.util.List;

public abstract interface IMediaSession extends IInterface
{
  public abstract void adjustVolume(int paramInt1, int paramInt2, String paramString)
    throws RemoteException;

  public abstract void fastForward()
    throws RemoteException;

  public abstract Bundle getExtras()
    throws RemoteException;

  public abstract long getFlags()
    throws RemoteException;

  public abstract PendingIntent getLaunchPendingIntent()
    throws RemoteException;

  public abstract MediaMetadataCompat getMetadata()
    throws RemoteException;

  public abstract String getPackageName()
    throws RemoteException;

  public abstract PlaybackStateCompat getPlaybackState()
    throws RemoteException;

  public abstract List<MediaSessionCompat.QueueItem> getQueue()
    throws RemoteException;

  public abstract CharSequence getQueueTitle()
    throws RemoteException;

  public abstract int getRatingType()
    throws RemoteException;

  public abstract String getTag()
    throws RemoteException;

  public abstract ParcelableVolumeInfo getVolumeAttributes()
    throws RemoteException;

  public abstract boolean isTransportControlEnabled()
    throws RemoteException;

  public abstract void next()
    throws RemoteException;

  public abstract void pause()
    throws RemoteException;

  public abstract void play()
    throws RemoteException;

  public abstract void playFromMediaId(String paramString, Bundle paramBundle)
    throws RemoteException;

  public abstract void playFromSearch(String paramString, Bundle paramBundle)
    throws RemoteException;

  public abstract void previous()
    throws RemoteException;

  public abstract void rate(RatingCompat paramRatingCompat)
    throws RemoteException;

  public abstract void registerCallbackListener(IMediaControllerCallback paramIMediaControllerCallback)
    throws RemoteException;

  public abstract void rewind()
    throws RemoteException;

  public abstract void seekTo(long paramLong)
    throws RemoteException;

  public abstract void sendCommand(String paramString, Bundle paramBundle, MediaSessionCompat.ResultReceiverWrapper paramResultReceiverWrapper)
    throws RemoteException;

  public abstract void sendCustomAction(String paramString, Bundle paramBundle)
    throws RemoteException;

  public abstract boolean sendMediaButton(KeyEvent paramKeyEvent)
    throws RemoteException;

  public abstract void setVolumeTo(int paramInt1, int paramInt2, String paramString)
    throws RemoteException;

  public abstract void skipToQueueItem(long paramLong)
    throws RemoteException;

  public abstract void stop()
    throws RemoteException;

  public abstract void unregisterCallbackListener(IMediaControllerCallback paramIMediaControllerCallback)
    throws RemoteException;
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.media.session.IMediaSession
 * JD-Core Version:    0.6.2
 */
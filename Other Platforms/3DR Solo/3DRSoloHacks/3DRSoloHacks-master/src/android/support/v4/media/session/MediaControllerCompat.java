package android.support.v4.media.session;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import java.util.List;

public final class MediaControllerCompat
{
  private static final String TAG = "MediaControllerCompat";
  private final MediaControllerImpl mImpl;
  private final MediaSessionCompat.Token mToken;

  public MediaControllerCompat(Context paramContext, MediaSessionCompat.Token paramToken)
    throws RemoteException
  {
    if (paramToken == null)
      throw new IllegalArgumentException("sessionToken must not be null");
    this.mToken = paramToken;
    if (Build.VERSION.SDK_INT >= 21)
    {
      this.mImpl = new MediaControllerCompat.MediaControllerImplApi21(paramContext, paramToken);
      return;
    }
    this.mImpl = new MediaControllerCompat.MediaControllerImplBase(this.mToken);
  }

  public MediaControllerCompat(Context paramContext, MediaSessionCompat paramMediaSessionCompat)
  {
    if (paramMediaSessionCompat == null)
      throw new IllegalArgumentException("session must not be null");
    this.mToken = paramMediaSessionCompat.getSessionToken();
    if (Build.VERSION.SDK_INT >= 21)
    {
      this.mImpl = new MediaControllerCompat.MediaControllerImplApi21(paramContext, paramMediaSessionCompat);
      return;
    }
    this.mImpl = new MediaControllerCompat.MediaControllerImplBase(this.mToken);
  }

  public void adjustVolume(int paramInt1, int paramInt2)
  {
    this.mImpl.adjustVolume(paramInt1, paramInt2);
  }

  public boolean dispatchMediaButtonEvent(KeyEvent paramKeyEvent)
  {
    if (paramKeyEvent == null)
      throw new IllegalArgumentException("KeyEvent may not be null");
    return this.mImpl.dispatchMediaButtonEvent(paramKeyEvent);
  }

  public Bundle getExtras()
  {
    return this.mImpl.getExtras();
  }

  public long getFlags()
  {
    return this.mImpl.getFlags();
  }

  public Object getMediaController()
  {
    return this.mImpl.getMediaController();
  }

  public MediaMetadataCompat getMetadata()
  {
    return this.mImpl.getMetadata();
  }

  public String getPackageName()
  {
    return this.mImpl.getPackageName();
  }

  public PlaybackInfo getPlaybackInfo()
  {
    return this.mImpl.getPlaybackInfo();
  }

  public PlaybackStateCompat getPlaybackState()
  {
    return this.mImpl.getPlaybackState();
  }

  public List<MediaSessionCompat.QueueItem> getQueue()
  {
    return this.mImpl.getQueue();
  }

  public CharSequence getQueueTitle()
  {
    return this.mImpl.getQueueTitle();
  }

  public int getRatingType()
  {
    return this.mImpl.getRatingType();
  }

  public PendingIntent getSessionActivity()
  {
    return this.mImpl.getSessionActivity();
  }

  public MediaSessionCompat.Token getSessionToken()
  {
    return this.mToken;
  }

  public TransportControls getTransportControls()
  {
    return this.mImpl.getTransportControls();
  }

  public void registerCallback(Callback paramCallback)
  {
    registerCallback(paramCallback, null);
  }

  public void registerCallback(Callback paramCallback, Handler paramHandler)
  {
    if (paramCallback == null)
      throw new IllegalArgumentException("callback cannot be null");
    if (paramHandler == null)
      paramHandler = new Handler();
    this.mImpl.registerCallback(paramCallback, paramHandler);
  }

  public void sendCommand(String paramString, Bundle paramBundle, ResultReceiver paramResultReceiver)
  {
    if (TextUtils.isEmpty(paramString))
      throw new IllegalArgumentException("command cannot be null or empty");
    this.mImpl.sendCommand(paramString, paramBundle, paramResultReceiver);
  }

  public void setVolumeTo(int paramInt1, int paramInt2)
  {
    this.mImpl.setVolumeTo(paramInt1, paramInt2);
  }

  public void unregisterCallback(Callback paramCallback)
  {
    if (paramCallback == null)
      throw new IllegalArgumentException("callback cannot be null");
    this.mImpl.unregisterCallback(paramCallback);
  }

  public static abstract class Callback
    implements IBinder.DeathRecipient
  {
    private final Object mCallbackObj;
    private MessageHandler mHandler;
    private boolean mRegistered = false;

    public Callback()
    {
      if (Build.VERSION.SDK_INT >= 21)
      {
        this.mCallbackObj = MediaControllerCompatApi21.createCallback(new MediaControllerCompat.Callback.StubApi21(this, null));
        return;
      }
      this.mCallbackObj = new MediaControllerCompat.Callback.StubCompat(this, null);
    }

    private void setHandler(Handler paramHandler)
    {
      this.mHandler = new MessageHandler(paramHandler.getLooper());
    }

    public void binderDied()
    {
      onSessionDestroyed();
    }

    public void onAudioInfoChanged(MediaControllerCompat.PlaybackInfo paramPlaybackInfo)
    {
    }

    public void onExtrasChanged(Bundle paramBundle)
    {
    }

    public void onMetadataChanged(MediaMetadataCompat paramMediaMetadataCompat)
    {
    }

    public void onPlaybackStateChanged(PlaybackStateCompat paramPlaybackStateCompat)
    {
    }

    public void onQueueChanged(List<MediaSessionCompat.QueueItem> paramList)
    {
    }

    public void onQueueTitleChanged(CharSequence paramCharSequence)
    {
    }

    public void onSessionDestroyed()
    {
    }

    public void onSessionEvent(String paramString, Bundle paramBundle)
    {
    }

    private class MessageHandler extends Handler
    {
      private static final int MSG_DESTROYED = 8;
      private static final int MSG_EVENT = 1;
      private static final int MSG_UPDATE_EXTRAS = 7;
      private static final int MSG_UPDATE_METADATA = 3;
      private static final int MSG_UPDATE_PLAYBACK_STATE = 2;
      private static final int MSG_UPDATE_QUEUE = 5;
      private static final int MSG_UPDATE_QUEUE_TITLE = 6;
      private static final int MSG_UPDATE_VOLUME = 4;

      public MessageHandler(Looper arg2)
      {
        super();
      }

      public void handleMessage(Message paramMessage)
      {
        if (!MediaControllerCompat.Callback.this.mRegistered)
          return;
        switch (paramMessage.what)
        {
        default:
          return;
        case 1:
          MediaControllerCompat.Callback.this.onSessionEvent((String)paramMessage.obj, paramMessage.getData());
          return;
        case 2:
          MediaControllerCompat.Callback.this.onPlaybackStateChanged((PlaybackStateCompat)paramMessage.obj);
          return;
        case 3:
          MediaControllerCompat.Callback.this.onMetadataChanged((MediaMetadataCompat)paramMessage.obj);
          return;
        case 5:
          MediaControllerCompat.Callback.this.onQueueChanged((List)paramMessage.obj);
          return;
        case 6:
          MediaControllerCompat.Callback.this.onQueueTitleChanged((CharSequence)paramMessage.obj);
          return;
        case 7:
          MediaControllerCompat.Callback.this.onExtrasChanged((Bundle)paramMessage.obj);
          return;
        case 4:
          MediaControllerCompat.Callback.this.onAudioInfoChanged((MediaControllerCompat.PlaybackInfo)paramMessage.obj);
          return;
        case 8:
        }
        MediaControllerCompat.Callback.this.onSessionDestroyed();
      }

      public void post(int paramInt, Object paramObject, Bundle paramBundle)
      {
        obtainMessage(paramInt, paramObject).sendToTarget();
      }
    }
  }

  static abstract interface MediaControllerImpl
  {
    public abstract void adjustVolume(int paramInt1, int paramInt2);

    public abstract boolean dispatchMediaButtonEvent(KeyEvent paramKeyEvent);

    public abstract Bundle getExtras();

    public abstract long getFlags();

    public abstract Object getMediaController();

    public abstract MediaMetadataCompat getMetadata();

    public abstract String getPackageName();

    public abstract MediaControllerCompat.PlaybackInfo getPlaybackInfo();

    public abstract PlaybackStateCompat getPlaybackState();

    public abstract List<MediaSessionCompat.QueueItem> getQueue();

    public abstract CharSequence getQueueTitle();

    public abstract int getRatingType();

    public abstract PendingIntent getSessionActivity();

    public abstract MediaControllerCompat.TransportControls getTransportControls();

    public abstract void registerCallback(MediaControllerCompat.Callback paramCallback, Handler paramHandler);

    public abstract void sendCommand(String paramString, Bundle paramBundle, ResultReceiver paramResultReceiver);

    public abstract void setVolumeTo(int paramInt1, int paramInt2);

    public abstract void unregisterCallback(MediaControllerCompat.Callback paramCallback);
  }

  public static final class PlaybackInfo
  {
    public static final int PLAYBACK_TYPE_LOCAL = 1;
    public static final int PLAYBACK_TYPE_REMOTE = 2;
    private final int mAudioStream;
    private final int mCurrentVolume;
    private final int mMaxVolume;
    private final int mPlaybackType;
    private final int mVolumeControl;

    PlaybackInfo(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5)
    {
      this.mPlaybackType = paramInt1;
      this.mAudioStream = paramInt2;
      this.mVolumeControl = paramInt3;
      this.mMaxVolume = paramInt4;
      this.mCurrentVolume = paramInt5;
    }

    public int getAudioStream()
    {
      return this.mAudioStream;
    }

    public int getCurrentVolume()
    {
      return this.mCurrentVolume;
    }

    public int getMaxVolume()
    {
      return this.mMaxVolume;
    }

    public int getPlaybackType()
    {
      return this.mPlaybackType;
    }

    public int getVolumeControl()
    {
      return this.mVolumeControl;
    }
  }

  public static abstract class TransportControls
  {
    public abstract void fastForward();

    public abstract void pause();

    public abstract void play();

    public abstract void playFromMediaId(String paramString, Bundle paramBundle);

    public abstract void playFromSearch(String paramString, Bundle paramBundle);

    public abstract void rewind();

    public abstract void seekTo(long paramLong);

    public abstract void sendCustomAction(PlaybackStateCompat.CustomAction paramCustomAction, Bundle paramBundle);

    public abstract void sendCustomAction(String paramString, Bundle paramBundle);

    public abstract void setRating(RatingCompat paramRatingCompat);

    public abstract void skipToNext();

    public abstract void skipToPrevious();

    public abstract void skipToQueueItem(long paramLong);

    public abstract void stop();
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.media.session.MediaControllerCompat
 * JD-Core Version:    0.6.2
 */
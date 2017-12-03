package android.support.v4.media.session;

import android.media.session.PlaybackState;
import android.media.session.PlaybackState.Builder;
import android.media.session.PlaybackState.CustomAction;
import android.media.session.PlaybackState.CustomAction.Builder;
import android.os.Bundle;
import java.util.Iterator;
import java.util.List;

class PlaybackStateCompatApi21
{
  public static long getActions(Object paramObject)
  {
    return ((PlaybackState)paramObject).getActions();
  }

  public static long getActiveQueueItemId(Object paramObject)
  {
    return ((PlaybackState)paramObject).getActiveQueueItemId();
  }

  public static long getBufferedPosition(Object paramObject)
  {
    return ((PlaybackState)paramObject).getBufferedPosition();
  }

  public static List<Object> getCustomActions(Object paramObject)
  {
    return ((PlaybackState)paramObject).getCustomActions();
  }

  public static CharSequence getErrorMessage(Object paramObject)
  {
    return ((PlaybackState)paramObject).getErrorMessage();
  }

  public static long getLastPositionUpdateTime(Object paramObject)
  {
    return ((PlaybackState)paramObject).getLastPositionUpdateTime();
  }

  public static float getPlaybackSpeed(Object paramObject)
  {
    return ((PlaybackState)paramObject).getPlaybackSpeed();
  }

  public static long getPosition(Object paramObject)
  {
    return ((PlaybackState)paramObject).getPosition();
  }

  public static int getState(Object paramObject)
  {
    return ((PlaybackState)paramObject).getState();
  }

  public static Object newInstance(int paramInt, long paramLong1, long paramLong2, float paramFloat, long paramLong3, CharSequence paramCharSequence, long paramLong4, List<Object> paramList, long paramLong5)
  {
    PlaybackState.Builder localBuilder = new PlaybackState.Builder();
    localBuilder.setState(paramInt, paramLong1, paramFloat, paramLong4);
    localBuilder.setBufferedPosition(paramLong2);
    localBuilder.setActions(paramLong3);
    localBuilder.setErrorMessage(paramCharSequence);
    Iterator localIterator = paramList.iterator();
    while (localIterator.hasNext())
      localBuilder.addCustomAction((PlaybackState.CustomAction)localIterator.next());
    localBuilder.setActiveQueueItemId(paramLong5);
    return localBuilder.build();
  }

  static final class CustomAction
  {
    public static String getAction(Object paramObject)
    {
      return ((PlaybackState.CustomAction)paramObject).getAction();
    }

    public static Bundle getExtras(Object paramObject)
    {
      return ((PlaybackState.CustomAction)paramObject).getExtras();
    }

    public static int getIcon(Object paramObject)
    {
      return ((PlaybackState.CustomAction)paramObject).getIcon();
    }

    public static CharSequence getName(Object paramObject)
    {
      return ((PlaybackState.CustomAction)paramObject).getName();
    }

    public static Object newInstance(String paramString, CharSequence paramCharSequence, int paramInt, Bundle paramBundle)
    {
      PlaybackState.CustomAction.Builder localBuilder = new PlaybackState.CustomAction.Builder(paramString, paramCharSequence, paramInt);
      localBuilder.setExtras(paramBundle);
      return localBuilder.build();
    }
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.media.session.PlaybackStateCompatApi21
 * JD-Core Version:    0.6.2
 */
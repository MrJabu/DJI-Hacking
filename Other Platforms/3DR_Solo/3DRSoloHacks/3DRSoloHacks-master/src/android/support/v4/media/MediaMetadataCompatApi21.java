package android.support.v4.media;

import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.MediaMetadata.Builder;
import android.media.Rating;
import java.util.Set;

class MediaMetadataCompatApi21
{
  public static Bitmap getBitmap(Object paramObject, String paramString)
  {
    return ((MediaMetadata)paramObject).getBitmap(paramString);
  }

  public static long getLong(Object paramObject, String paramString)
  {
    return ((MediaMetadata)paramObject).getLong(paramString);
  }

  public static Object getRating(Object paramObject, String paramString)
  {
    return ((MediaMetadata)paramObject).getRating(paramString);
  }

  public static CharSequence getText(Object paramObject, String paramString)
  {
    return ((MediaMetadata)paramObject).getText(paramString);
  }

  public static Set<String> keySet(Object paramObject)
  {
    return ((MediaMetadata)paramObject).keySet();
  }

  public static class Builder
  {
    public static Object build(Object paramObject)
    {
      return ((MediaMetadata.Builder)paramObject).build();
    }

    public static Object newInstance()
    {
      return new MediaMetadata.Builder();
    }

    public static void putBitmap(Object paramObject, String paramString, Bitmap paramBitmap)
    {
      ((MediaMetadata.Builder)paramObject).putBitmap(paramString, paramBitmap);
    }

    public static void putLong(Object paramObject, String paramString, long paramLong)
    {
      ((MediaMetadata.Builder)paramObject).putLong(paramString, paramLong);
    }

    public static void putRating(Object paramObject1, String paramString, Object paramObject2)
    {
      ((MediaMetadata.Builder)paramObject1).putRating(paramString, (Rating)paramObject2);
    }

    public static void putString(Object paramObject, String paramString1, String paramString2)
    {
      ((MediaMetadata.Builder)paramObject).putString(paramString1, paramString2);
    }

    public static void putText(Object paramObject, String paramString, CharSequence paramCharSequence)
    {
      ((MediaMetadata.Builder)paramObject).putText(paramString, paramCharSequence);
    }
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.media.MediaMetadataCompatApi21
 * JD-Core Version:    0.6.2
 */
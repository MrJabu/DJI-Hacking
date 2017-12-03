package android.support.v4.media;

import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public final class RatingCompat
  implements Parcelable
{
  public static final Parcelable.Creator<RatingCompat> CREATOR = new Parcelable.Creator()
  {
    public RatingCompat createFromParcel(Parcel paramAnonymousParcel)
    {
      return new RatingCompat(paramAnonymousParcel.readInt(), paramAnonymousParcel.readFloat(), null);
    }

    public RatingCompat[] newArray(int paramAnonymousInt)
    {
      return new RatingCompat[paramAnonymousInt];
    }
  };
  public static final int RATING_3_STARS = 3;
  public static final int RATING_4_STARS = 4;
  public static final int RATING_5_STARS = 5;
  public static final int RATING_HEART = 1;
  public static final int RATING_NONE = 0;
  private static final float RATING_NOT_RATED = -1.0F;
  public static final int RATING_PERCENTAGE = 6;
  public static final int RATING_THUMB_UP_DOWN = 2;
  private static final String TAG = "Rating";
  private Object mRatingObj;
  private final int mRatingStyle;
  private final float mRatingValue;

  private RatingCompat(int paramInt, float paramFloat)
  {
    this.mRatingStyle = paramInt;
    this.mRatingValue = paramFloat;
  }

  public static RatingCompat fromRating(Object paramObject)
  {
    if ((paramObject == null) || (Build.VERSION.SDK_INT < 21))
      return null;
    int i = RatingCompatApi21.getRatingStyle(paramObject);
    RatingCompat localRatingCompat;
    if (RatingCompatApi21.isRated(paramObject))
      switch (i)
      {
      default:
        return null;
      case 1:
        localRatingCompat = newHeartRating(RatingCompatApi21.hasHeart(paramObject));
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      }
    while (true)
    {
      localRatingCompat.mRatingObj = paramObject;
      return localRatingCompat;
      localRatingCompat = newThumbRating(RatingCompatApi21.isThumbUp(paramObject));
      continue;
      localRatingCompat = newStarRating(i, RatingCompatApi21.getStarRating(paramObject));
      continue;
      localRatingCompat = newPercentageRating(RatingCompatApi21.getPercentRating(paramObject));
      continue;
      localRatingCompat = newUnratedRating(i);
    }
  }

  public static RatingCompat newHeartRating(boolean paramBoolean)
  {
    if (paramBoolean);
    for (float f = 1.0F; ; f = 0.0F)
      return new RatingCompat(1, f);
  }

  public static RatingCompat newPercentageRating(float paramFloat)
  {
    if ((paramFloat < 0.0F) || (paramFloat > 100.0F))
    {
      Log.e("Rating", "Invalid percentage-based rating value");
      return null;
    }
    return new RatingCompat(6, paramFloat);
  }

  public static RatingCompat newStarRating(int paramInt, float paramFloat)
  {
    float f;
    switch (paramInt)
    {
    default:
      Log.e("Rating", "Invalid rating style (" + paramInt + ") for a star rating");
      return null;
    case 3:
      f = 3.0F;
    case 4:
    case 5:
    }
    while ((paramFloat < 0.0F) || (paramFloat > f))
    {
      Log.e("Rating", "Trying to set out of range star-based rating");
      return null;
      f = 4.0F;
      continue;
      f = 5.0F;
    }
    return new RatingCompat(paramInt, paramFloat);
  }

  public static RatingCompat newThumbRating(boolean paramBoolean)
  {
    if (paramBoolean);
    for (float f = 1.0F; ; f = 0.0F)
      return new RatingCompat(2, f);
  }

  public static RatingCompat newUnratedRating(int paramInt)
  {
    switch (paramInt)
    {
    default:
      return null;
    case 1:
    case 2:
    case 3:
    case 4:
    case 5:
    case 6:
    }
    return new RatingCompat(paramInt, -1.0F);
  }

  public int describeContents()
  {
    return this.mRatingStyle;
  }

  public float getPercentRating()
  {
    if ((this.mRatingStyle != 6) || (!isRated()))
      return -1.0F;
    return this.mRatingValue;
  }

  public Object getRating()
  {
    if ((this.mRatingObj != null) || (Build.VERSION.SDK_INT < 21))
      return this.mRatingObj;
    if (isRated())
      switch (this.mRatingStyle)
      {
      default:
        return null;
      case 1:
        this.mRatingObj = RatingCompatApi21.newHeartRating(hasHeart());
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      }
    while (true)
    {
      return this.mRatingObj;
      this.mRatingObj = RatingCompatApi21.newThumbRating(isThumbUp());
      continue;
      this.mRatingObj = RatingCompatApi21.newStarRating(this.mRatingStyle, getStarRating());
      continue;
      this.mRatingObj = RatingCompatApi21.newPercentageRating(getPercentRating());
      break;
      this.mRatingObj = RatingCompatApi21.newUnratedRating(this.mRatingStyle);
    }
  }

  public int getRatingStyle()
  {
    return this.mRatingStyle;
  }

  public float getStarRating()
  {
    switch (this.mRatingStyle)
    {
    default:
    case 3:
    case 4:
    case 5:
    }
    do
      return -1.0F;
    while (!isRated());
    return this.mRatingValue;
  }

  public boolean hasHeart()
  {
    int i = 1;
    if (this.mRatingStyle != i)
      return false;
    if (this.mRatingValue == 1.0F);
    while (true)
    {
      return i;
      int j = 0;
    }
  }

  public boolean isRated()
  {
    return this.mRatingValue >= 0.0F;
  }

  public boolean isThumbUp()
  {
    if (this.mRatingStyle != 2);
    while (this.mRatingValue != 1.0F)
      return false;
    return true;
  }

  public String toString()
  {
    StringBuilder localStringBuilder = new StringBuilder().append("Rating:style=").append(this.mRatingStyle).append(" rating=");
    if (this.mRatingValue < 0.0F);
    for (String str = "unrated"; ; str = String.valueOf(this.mRatingValue))
      return str;
  }

  public void writeToParcel(Parcel paramParcel, int paramInt)
  {
    paramParcel.writeInt(this.mRatingStyle);
    paramParcel.writeFloat(this.mRatingValue);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.media.RatingCompat
 * JD-Core Version:    0.6.2
 */
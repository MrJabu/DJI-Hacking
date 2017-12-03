package android.support.v4.media;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;

public final class MediaDescriptionCompat
  implements Parcelable
{
  public static final Parcelable.Creator<MediaDescriptionCompat> CREATOR = new Parcelable.Creator()
  {
    public MediaDescriptionCompat createFromParcel(Parcel paramAnonymousParcel)
    {
      if (Build.VERSION.SDK_INT < 21)
        return new MediaDescriptionCompat(paramAnonymousParcel, null);
      return MediaDescriptionCompat.fromMediaDescription(MediaDescriptionCompatApi21.fromParcel(paramAnonymousParcel));
    }

    public MediaDescriptionCompat[] newArray(int paramAnonymousInt)
    {
      return new MediaDescriptionCompat[paramAnonymousInt];
    }
  };
  private final CharSequence mDescription;
  private Object mDescriptionObj;
  private final Bundle mExtras;
  private final Bitmap mIcon;
  private final Uri mIconUri;
  private final String mMediaId;
  private final CharSequence mSubtitle;
  private final CharSequence mTitle;

  private MediaDescriptionCompat(Parcel paramParcel)
  {
    this.mMediaId = paramParcel.readString();
    this.mTitle = ((CharSequence)TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(paramParcel));
    this.mSubtitle = ((CharSequence)TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(paramParcel));
    this.mDescription = ((CharSequence)TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(paramParcel));
    this.mIcon = ((Bitmap)paramParcel.readParcelable(null));
    this.mIconUri = ((Uri)paramParcel.readParcelable(null));
    this.mExtras = paramParcel.readBundle();
  }

  private MediaDescriptionCompat(String paramString, CharSequence paramCharSequence1, CharSequence paramCharSequence2, CharSequence paramCharSequence3, Bitmap paramBitmap, Uri paramUri, Bundle paramBundle)
  {
    this.mMediaId = paramString;
    this.mTitle = paramCharSequence1;
    this.mSubtitle = paramCharSequence2;
    this.mDescription = paramCharSequence3;
    this.mIcon = paramBitmap;
    this.mIconUri = paramUri;
    this.mExtras = paramBundle;
  }

  public static MediaDescriptionCompat fromMediaDescription(Object paramObject)
  {
    if ((paramObject == null) || (Build.VERSION.SDK_INT < 21))
      return null;
    Builder localBuilder = new Builder();
    localBuilder.setMediaId(MediaDescriptionCompatApi21.getMediaId(paramObject));
    localBuilder.setTitle(MediaDescriptionCompatApi21.getTitle(paramObject));
    localBuilder.setSubtitle(MediaDescriptionCompatApi21.getSubtitle(paramObject));
    localBuilder.setDescription(MediaDescriptionCompatApi21.getDescription(paramObject));
    localBuilder.setIconBitmap(MediaDescriptionCompatApi21.getIconBitmap(paramObject));
    localBuilder.setIconUri(MediaDescriptionCompatApi21.getIconUri(paramObject));
    localBuilder.setExtras(MediaDescriptionCompatApi21.getExtras(paramObject));
    MediaDescriptionCompat localMediaDescriptionCompat = localBuilder.build();
    localMediaDescriptionCompat.mDescriptionObj = paramObject;
    return localMediaDescriptionCompat;
  }

  public int describeContents()
  {
    return 0;
  }

  public CharSequence getDescription()
  {
    return this.mDescription;
  }

  public Bundle getExtras()
  {
    return this.mExtras;
  }

  public Bitmap getIconBitmap()
  {
    return this.mIcon;
  }

  public Uri getIconUri()
  {
    return this.mIconUri;
  }

  public Object getMediaDescription()
  {
    if ((this.mDescriptionObj != null) || (Build.VERSION.SDK_INT < 21))
      return this.mDescriptionObj;
    Object localObject = MediaDescriptionCompatApi21.Builder.newInstance();
    MediaDescriptionCompatApi21.Builder.setMediaId(localObject, this.mMediaId);
    MediaDescriptionCompatApi21.Builder.setTitle(localObject, this.mTitle);
    MediaDescriptionCompatApi21.Builder.setSubtitle(localObject, this.mSubtitle);
    MediaDescriptionCompatApi21.Builder.setDescription(localObject, this.mDescription);
    MediaDescriptionCompatApi21.Builder.setIconBitmap(localObject, this.mIcon);
    MediaDescriptionCompatApi21.Builder.setIconUri(localObject, this.mIconUri);
    MediaDescriptionCompatApi21.Builder.setExtras(localObject, this.mExtras);
    this.mDescriptionObj = MediaDescriptionCompatApi21.Builder.build(localObject);
    return this.mDescriptionObj;
  }

  public String getMediaId()
  {
    return this.mMediaId;
  }

  public CharSequence getSubtitle()
  {
    return this.mSubtitle;
  }

  public CharSequence getTitle()
  {
    return this.mTitle;
  }

  public String toString()
  {
    return this.mTitle + ", " + this.mSubtitle + ", " + this.mDescription;
  }

  public void writeToParcel(Parcel paramParcel, int paramInt)
  {
    if (Build.VERSION.SDK_INT < 21)
    {
      paramParcel.writeString(this.mMediaId);
      TextUtils.writeToParcel(this.mTitle, paramParcel, paramInt);
      TextUtils.writeToParcel(this.mSubtitle, paramParcel, paramInt);
      TextUtils.writeToParcel(this.mDescription, paramParcel, paramInt);
      paramParcel.writeParcelable(this.mIcon, paramInt);
      paramParcel.writeParcelable(this.mIconUri, paramInt);
      paramParcel.writeBundle(this.mExtras);
      return;
    }
    MediaDescriptionCompatApi21.writeToParcel(getMediaDescription(), paramParcel, paramInt);
  }

  public static final class Builder
  {
    private CharSequence mDescription;
    private Bundle mExtras;
    private Bitmap mIcon;
    private Uri mIconUri;
    private String mMediaId;
    private CharSequence mSubtitle;
    private CharSequence mTitle;

    public MediaDescriptionCompat build()
    {
      return new MediaDescriptionCompat(this.mMediaId, this.mTitle, this.mSubtitle, this.mDescription, this.mIcon, this.mIconUri, this.mExtras, null);
    }

    public Builder setDescription(CharSequence paramCharSequence)
    {
      this.mDescription = paramCharSequence;
      return this;
    }

    public Builder setExtras(Bundle paramBundle)
    {
      this.mExtras = paramBundle;
      return this;
    }

    public Builder setIconBitmap(Bitmap paramBitmap)
    {
      this.mIcon = paramBitmap;
      return this;
    }

    public Builder setIconUri(Uri paramUri)
    {
      this.mIconUri = paramUri;
      return this;
    }

    public Builder setMediaId(String paramString)
    {
      this.mMediaId = paramString;
      return this;
    }

    public Builder setSubtitle(CharSequence paramCharSequence)
    {
      this.mSubtitle = paramCharSequence;
      return this;
    }

    public Builder setTitle(CharSequence paramCharSequence)
    {
      this.mTitle = paramCharSequence;
      return this;
    }
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.media.MediaDescriptionCompat
 * JD-Core Version:    0.6.2
 */
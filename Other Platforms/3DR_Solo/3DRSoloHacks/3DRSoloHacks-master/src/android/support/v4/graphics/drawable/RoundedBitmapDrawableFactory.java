package android.support.v4.graphics.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build.VERSION;
import android.util.Log;
import java.io.InputStream;

public class RoundedBitmapDrawableFactory
{
  private static final String TAG = "RoundedBitmapDrawableFactory";

  public static RoundedBitmapDrawable create(Resources paramResources, Bitmap paramBitmap)
  {
    if (Build.VERSION.SDK_INT >= 21)
      return new RoundedBitmapDrawable21(paramResources, paramBitmap);
    return new RoundedBitmapDrawableFactory.DefaultRoundedBitmapDrawable(paramResources, paramBitmap);
  }

  public static RoundedBitmapDrawable create(Resources paramResources, InputStream paramInputStream)
  {
    RoundedBitmapDrawable localRoundedBitmapDrawable = create(paramResources, BitmapFactory.decodeStream(paramInputStream));
    if (localRoundedBitmapDrawable.getBitmap() == null)
      Log.w("RoundedBitmapDrawableFactory", "BitmapDrawable cannot decode " + paramInputStream);
    return localRoundedBitmapDrawable;
  }

  public static RoundedBitmapDrawable create(Resources paramResources, String paramString)
  {
    RoundedBitmapDrawable localRoundedBitmapDrawable = create(paramResources, BitmapFactory.decodeFile(paramString));
    if (localRoundedBitmapDrawable.getBitmap() == null)
      Log.w("RoundedBitmapDrawableFactory", "BitmapDrawable cannot decode " + paramString);
    return localRoundedBitmapDrawable;
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
 * JD-Core Version:    0.6.2
 */
package android.support.v4.print;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build.VERSION;
import java.io.FileNotFoundException;

public final class PrintHelper
{
  public static final int COLOR_MODE_COLOR = 2;
  public static final int COLOR_MODE_MONOCHROME = 1;
  public static final int ORIENTATION_LANDSCAPE = 1;
  public static final int ORIENTATION_PORTRAIT = 2;
  public static final int SCALE_MODE_FILL = 2;
  public static final int SCALE_MODE_FIT = 1;
  PrintHelperVersionImpl mImpl;

  public PrintHelper(Context paramContext)
  {
    if (systemSupportsPrint())
    {
      this.mImpl = new PrintHelper.PrintHelperKitkatImpl(paramContext);
      return;
    }
    this.mImpl = new PrintHelper.PrintHelperStubImpl(null);
  }

  public static boolean systemSupportsPrint()
  {
    return Build.VERSION.SDK_INT >= 19;
  }

  public int getColorMode()
  {
    return this.mImpl.getColorMode();
  }

  public int getOrientation()
  {
    return this.mImpl.getOrientation();
  }

  public int getScaleMode()
  {
    return this.mImpl.getScaleMode();
  }

  public void printBitmap(String paramString, Bitmap paramBitmap)
  {
    this.mImpl.printBitmap(paramString, paramBitmap, null);
  }

  public void printBitmap(String paramString, Bitmap paramBitmap, OnPrintFinishCallback paramOnPrintFinishCallback)
  {
    this.mImpl.printBitmap(paramString, paramBitmap, paramOnPrintFinishCallback);
  }

  public void printBitmap(String paramString, Uri paramUri)
    throws FileNotFoundException
  {
    this.mImpl.printBitmap(paramString, paramUri, null);
  }

  public void printBitmap(String paramString, Uri paramUri, OnPrintFinishCallback paramOnPrintFinishCallback)
    throws FileNotFoundException
  {
    this.mImpl.printBitmap(paramString, paramUri, paramOnPrintFinishCallback);
  }

  public void setColorMode(int paramInt)
  {
    this.mImpl.setColorMode(paramInt);
  }

  public void setOrientation(int paramInt)
  {
    this.mImpl.setOrientation(paramInt);
  }

  public void setScaleMode(int paramInt)
  {
    this.mImpl.setScaleMode(paramInt);
  }

  public static abstract interface OnPrintFinishCallback
  {
    public abstract void onFinish();
  }

  static abstract interface PrintHelperVersionImpl
  {
    public abstract int getColorMode();

    public abstract int getOrientation();

    public abstract int getScaleMode();

    public abstract void printBitmap(String paramString, Bitmap paramBitmap, PrintHelper.OnPrintFinishCallback paramOnPrintFinishCallback);

    public abstract void printBitmap(String paramString, Uri paramUri, PrintHelper.OnPrintFinishCallback paramOnPrintFinishCallback)
      throws FileNotFoundException;

    public abstract void setColorMode(int paramInt);

    public abstract void setOrientation(int paramInt);

    public abstract void setScaleMode(int paramInt);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.print.PrintHelper
 * JD-Core Version:    0.6.2
 */
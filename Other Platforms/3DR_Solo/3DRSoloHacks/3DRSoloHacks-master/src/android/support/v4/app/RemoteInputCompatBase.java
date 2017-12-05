package android.support.v4.app;

import android.os.Bundle;

class RemoteInputCompatBase
{
  public static abstract class RemoteInput
  {
    protected abstract boolean getAllowFreeFormInput();

    protected abstract CharSequence[] getChoices();

    protected abstract Bundle getExtras();

    protected abstract CharSequence getLabel();

    protected abstract String getResultKey();

    public static abstract interface Factory
    {
      public abstract RemoteInputCompatBase.RemoteInput build(String paramString, CharSequence paramCharSequence, CharSequence[] paramArrayOfCharSequence, boolean paramBoolean, Bundle paramBundle);

      public abstract RemoteInputCompatBase.RemoteInput[] newArray(int paramInt);
    }
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.app.RemoteInputCompatBase
 * JD-Core Version:    0.6.2
 */
package android.support.v4.app;

import android.app.Notification;
import android.os.IInterface;
import android.os.RemoteException;

public abstract interface INotificationSideChannel extends IInterface
{
  public abstract void cancel(String paramString1, int paramInt, String paramString2)
    throws RemoteException;

  public abstract void cancelAll(String paramString)
    throws RemoteException;

  public abstract void notify(String paramString1, int paramInt, String paramString2, Notification paramNotification)
    throws RemoteException;
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.app.INotificationSideChannel
 * JD-Core Version:    0.6.2
 */
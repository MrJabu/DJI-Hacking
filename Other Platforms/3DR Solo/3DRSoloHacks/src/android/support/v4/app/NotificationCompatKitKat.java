package android.support.v4.app;

import android.app.Notification;
import android.app.Notification.Action;
import android.os.Bundle;
import android.util.SparseArray;

class NotificationCompatKitKat
{
  public static NotificationCompatBase.Action getAction(Notification paramNotification, int paramInt, NotificationCompatBase.Action.Factory paramFactory, RemoteInputCompatBase.RemoteInput.Factory paramFactory1)
  {
    Notification.Action localAction = paramNotification.actions[paramInt];
    SparseArray localSparseArray = paramNotification.extras.getSparseParcelableArray("android.support.actionExtras");
    Bundle localBundle = null;
    if (localSparseArray != null)
      localBundle = (Bundle)localSparseArray.get(paramInt);
    return NotificationCompatJellybean.readAction(paramFactory, paramFactory1, localAction.icon, localAction.title, localAction.actionIntent, localBundle);
  }

  public static int getActionCount(Notification paramNotification)
  {
    if (paramNotification.actions != null)
      return paramNotification.actions.length;
    return 0;
  }

  public static Bundle getExtras(Notification paramNotification)
  {
    return paramNotification.extras;
  }

  public static String getGroup(Notification paramNotification)
  {
    return paramNotification.extras.getString("android.support.groupKey");
  }

  public static boolean getLocalOnly(Notification paramNotification)
  {
    return paramNotification.extras.getBoolean("android.support.localOnly");
  }

  public static String getSortKey(Notification paramNotification)
  {
    return paramNotification.extras.getString("android.support.sortKey");
  }

  public static boolean isGroupSummary(Notification paramNotification)
  {
    return paramNotification.extras.getBoolean("android.support.isGroupSummary");
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.app.NotificationCompatKitKat
 * JD-Core Version:    0.6.2
 */
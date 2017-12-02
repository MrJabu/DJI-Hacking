package android.support.v4.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.util.Log;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class NotificationManagerCompat
{
  public static final String ACTION_BIND_SIDE_CHANNEL = "android.support.BIND_NOTIFICATION_SIDE_CHANNEL";
  public static final String EXTRA_USE_SIDE_CHANNEL = "android.support.useSideChannel";
  private static final Impl IMPL;
  static final int MAX_SIDE_CHANNEL_SDK_VERSION = 19;
  private static final String SETTING_ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
  private static final int SIDE_CHANNEL_BIND_FLAGS = 0;
  private static final int SIDE_CHANNEL_RETRY_BASE_INTERVAL_MS = 1000;
  private static final int SIDE_CHANNEL_RETRY_MAX_COUNT = 6;
  private static final String TAG = "NotifManCompat";
  private static Set<String> sEnabledNotificationListenerPackages;
  private static String sEnabledNotificationListeners;
  private static final Object sEnabledNotificationListenersLock = new Object();
  private static final Object sLock;
  private static SideChannelManager sSideChannelManager;
  private final Context mContext;
  private final NotificationManager mNotificationManager;

  static
  {
    sEnabledNotificationListenerPackages = new HashSet();
    sLock = new Object();
    if (Build.VERSION.SDK_INT >= 14)
      IMPL = new NotificationManagerCompat.ImplIceCreamSandwich();
    while (true)
    {
      SIDE_CHANNEL_BIND_FLAGS = IMPL.getSideChannelBindFlags();
      return;
      if (Build.VERSION.SDK_INT >= 5)
        IMPL = new NotificationManagerCompat.ImplEclair();
      else
        IMPL = new NotificationManagerCompat.ImplBase();
    }
  }

  private NotificationManagerCompat(Context paramContext)
  {
    this.mContext = paramContext;
    this.mNotificationManager = ((NotificationManager)this.mContext.getSystemService("notification"));
  }

  public static NotificationManagerCompat from(Context paramContext)
  {
    return new NotificationManagerCompat(paramContext);
  }

  public static Set<String> getEnabledListenerPackages(Context paramContext)
  {
    String str = Settings.Secure.getString(paramContext.getContentResolver(), "enabled_notification_listeners");
    HashSet localHashSet;
    if ((str != null) && (!str.equals(sEnabledNotificationListeners)))
    {
      String[] arrayOfString = str.split(":");
      localHashSet = new HashSet(arrayOfString.length);
      int i = arrayOfString.length;
      for (int j = 0; j < i; j++)
      {
        ComponentName localComponentName = ComponentName.unflattenFromString(arrayOfString[j]);
        if (localComponentName != null)
          localHashSet.add(localComponentName.getPackageName());
      }
    }
    synchronized (sEnabledNotificationListenersLock)
    {
      sEnabledNotificationListenerPackages = localHashSet;
      sEnabledNotificationListeners = str;
      return sEnabledNotificationListenerPackages;
    }
  }

  private void pushSideChannelQueue(Task paramTask)
  {
    synchronized (sLock)
    {
      if (sSideChannelManager == null)
        sSideChannelManager = new SideChannelManager(this.mContext.getApplicationContext());
      sSideChannelManager.queueTask(paramTask);
      return;
    }
  }

  private static boolean useSideChannelForNotification(Notification paramNotification)
  {
    Bundle localBundle = NotificationCompat.getExtras(paramNotification);
    return (localBundle != null) && (localBundle.getBoolean("android.support.useSideChannel"));
  }

  public void cancel(int paramInt)
  {
    cancel(null, paramInt);
  }

  public void cancel(String paramString, int paramInt)
  {
    IMPL.cancelNotification(this.mNotificationManager, paramString, paramInt);
    if (Build.VERSION.SDK_INT <= 19)
      pushSideChannelQueue(new NotificationManagerCompat.CancelTask(this.mContext.getPackageName(), paramInt, paramString));
  }

  public void cancelAll()
  {
    this.mNotificationManager.cancelAll();
    if (Build.VERSION.SDK_INT <= 19)
      pushSideChannelQueue(new NotificationManagerCompat.CancelTask(this.mContext.getPackageName()));
  }

  public void notify(int paramInt, Notification paramNotification)
  {
    notify(null, paramInt, paramNotification);
  }

  public void notify(String paramString, int paramInt, Notification paramNotification)
  {
    if (useSideChannelForNotification(paramNotification))
    {
      pushSideChannelQueue(new NotificationManagerCompat.NotifyTask(this.mContext.getPackageName(), paramInt, paramString, paramNotification));
      IMPL.cancelNotification(this.mNotificationManager, paramString, paramInt);
      return;
    }
    IMPL.postNotification(this.mNotificationManager, paramString, paramInt, paramNotification);
  }

  static abstract interface Impl
  {
    public abstract void cancelNotification(NotificationManager paramNotificationManager, String paramString, int paramInt);

    public abstract int getSideChannelBindFlags();

    public abstract void postNotification(NotificationManager paramNotificationManager, String paramString, int paramInt, Notification paramNotification);
  }

  private static class ServiceConnectedEvent
  {
    final ComponentName componentName;
    final IBinder iBinder;

    public ServiceConnectedEvent(ComponentName paramComponentName, IBinder paramIBinder)
    {
      this.componentName = paramComponentName;
      this.iBinder = paramIBinder;
    }
  }

  private static class SideChannelManager
    implements Handler.Callback, ServiceConnection
  {
    private static final String KEY_BINDER = "binder";
    private static final int MSG_QUEUE_TASK = 0;
    private static final int MSG_RETRY_LISTENER_QUEUE = 3;
    private static final int MSG_SERVICE_CONNECTED = 1;
    private static final int MSG_SERVICE_DISCONNECTED = 2;
    private Set<String> mCachedEnabledPackages = new HashSet();
    private final Context mContext;
    private final Handler mHandler;
    private final HandlerThread mHandlerThread;
    private final Map<ComponentName, ListenerRecord> mRecordMap = new HashMap();

    public SideChannelManager(Context paramContext)
    {
      this.mContext = paramContext;
      this.mHandlerThread = new HandlerThread("NotificationManagerCompat");
      this.mHandlerThread.start();
      this.mHandler = new Handler(this.mHandlerThread.getLooper(), this);
    }

    private boolean ensureServiceBound(ListenerRecord paramListenerRecord)
    {
      if (paramListenerRecord.bound)
        return true;
      Intent localIntent = new Intent("android.support.BIND_NOTIFICATION_SIDE_CHANNEL").setComponent(paramListenerRecord.componentName);
      paramListenerRecord.bound = this.mContext.bindService(localIntent, this, NotificationManagerCompat.SIDE_CHANNEL_BIND_FLAGS);
      if (paramListenerRecord.bound)
        paramListenerRecord.retryCount = 0;
      while (true)
      {
        return paramListenerRecord.bound;
        Log.w("NotifManCompat", "Unable to bind to listener " + paramListenerRecord.componentName);
        this.mContext.unbindService(this);
      }
    }

    private void ensureServiceUnbound(ListenerRecord paramListenerRecord)
    {
      if (paramListenerRecord.bound)
      {
        this.mContext.unbindService(this);
        paramListenerRecord.bound = false;
      }
      paramListenerRecord.service = null;
    }

    private void handleQueueTask(NotificationManagerCompat.Task paramTask)
    {
      updateListenerMap();
      Iterator localIterator = this.mRecordMap.values().iterator();
      while (localIterator.hasNext())
      {
        ListenerRecord localListenerRecord = (ListenerRecord)localIterator.next();
        localListenerRecord.taskQueue.add(paramTask);
        processListenerQueue(localListenerRecord);
      }
    }

    private void handleRetryListenerQueue(ComponentName paramComponentName)
    {
      ListenerRecord localListenerRecord = (ListenerRecord)this.mRecordMap.get(paramComponentName);
      if (localListenerRecord != null)
        processListenerQueue(localListenerRecord);
    }

    private void handleServiceConnected(ComponentName paramComponentName, IBinder paramIBinder)
    {
      ListenerRecord localListenerRecord = (ListenerRecord)this.mRecordMap.get(paramComponentName);
      if (localListenerRecord != null)
      {
        localListenerRecord.service = INotificationSideChannel.Stub.asInterface(paramIBinder);
        localListenerRecord.retryCount = 0;
        processListenerQueue(localListenerRecord);
      }
    }

    private void handleServiceDisconnected(ComponentName paramComponentName)
    {
      ListenerRecord localListenerRecord = (ListenerRecord)this.mRecordMap.get(paramComponentName);
      if (localListenerRecord != null)
        ensureServiceUnbound(localListenerRecord);
    }

    private void processListenerQueue(ListenerRecord paramListenerRecord)
    {
      if (Log.isLoggable("NotifManCompat", 3))
        Log.d("NotifManCompat", "Processing component " + paramListenerRecord.componentName + ", " + paramListenerRecord.taskQueue.size() + " queued tasks");
      if (paramListenerRecord.taskQueue.isEmpty());
      while (true)
      {
        return;
        if ((!ensureServiceBound(paramListenerRecord)) || (paramListenerRecord.service == null))
        {
          scheduleListenerRetry(paramListenerRecord);
          return;
        }
        try
        {
          Object localObject;
          do
          {
            if (Log.isLoggable("NotifManCompat", 3))
              Log.d("NotifManCompat", "Sending task " + localObject);
            ((NotificationManagerCompat.Task)localObject).send(paramListenerRecord.service);
            paramListenerRecord.taskQueue.remove();
            localObject = (NotificationManagerCompat.Task)paramListenerRecord.taskQueue.peek();
          }
          while (localObject != null);
          if (paramListenerRecord.taskQueue.isEmpty())
            continue;
          scheduleListenerRetry(paramListenerRecord);
          return;
        }
        catch (DeadObjectException localDeadObjectException)
        {
          while (true)
            if (Log.isLoggable("NotifManCompat", 3))
              Log.d("NotifManCompat", "Remote service has died: " + paramListenerRecord.componentName);
        }
        catch (RemoteException localRemoteException)
        {
          while (true)
            Log.w("NotifManCompat", "RemoteException communicating with " + paramListenerRecord.componentName, localRemoteException);
        }
      }
    }

    private void scheduleListenerRetry(ListenerRecord paramListenerRecord)
    {
      if (this.mHandler.hasMessages(3, paramListenerRecord.componentName))
        return;
      paramListenerRecord.retryCount = (1 + paramListenerRecord.retryCount);
      if (paramListenerRecord.retryCount > 6)
      {
        Log.w("NotifManCompat", "Giving up on delivering " + paramListenerRecord.taskQueue.size() + " tasks to " + paramListenerRecord.componentName + " after " + paramListenerRecord.retryCount + " retries");
        paramListenerRecord.taskQueue.clear();
        return;
      }
      int i = 1000 * (1 << -1 + paramListenerRecord.retryCount);
      if (Log.isLoggable("NotifManCompat", 3))
        Log.d("NotifManCompat", "Scheduling retry for " + i + " ms");
      Message localMessage = this.mHandler.obtainMessage(3, paramListenerRecord.componentName);
      this.mHandler.sendMessageDelayed(localMessage, i);
    }

    private void updateListenerMap()
    {
      Set localSet = NotificationManagerCompat.getEnabledListenerPackages(this.mContext);
      if (localSet.equals(this.mCachedEnabledPackages));
      while (true)
      {
        return;
        this.mCachedEnabledPackages = localSet;
        List localList = this.mContext.getPackageManager().queryIntentServices(new Intent().setAction("android.support.BIND_NOTIFICATION_SIDE_CHANNEL"), 4);
        HashSet localHashSet = new HashSet();
        Iterator localIterator1 = localList.iterator();
        while (localIterator1.hasNext())
        {
          ResolveInfo localResolveInfo = (ResolveInfo)localIterator1.next();
          if (localSet.contains(localResolveInfo.serviceInfo.packageName))
          {
            ComponentName localComponentName2 = new ComponentName(localResolveInfo.serviceInfo.packageName, localResolveInfo.serviceInfo.name);
            if (localResolveInfo.serviceInfo.permission != null)
              Log.w("NotifManCompat", "Permission present on component " + localComponentName2 + ", not adding listener record.");
            else
              localHashSet.add(localComponentName2);
          }
        }
        Iterator localIterator2 = localHashSet.iterator();
        while (localIterator2.hasNext())
        {
          ComponentName localComponentName1 = (ComponentName)localIterator2.next();
          if (!this.mRecordMap.containsKey(localComponentName1))
          {
            if (Log.isLoggable("NotifManCompat", 3))
              Log.d("NotifManCompat", "Adding listener record for " + localComponentName1);
            this.mRecordMap.put(localComponentName1, new ListenerRecord(localComponentName1));
          }
        }
        Iterator localIterator3 = this.mRecordMap.entrySet().iterator();
        while (localIterator3.hasNext())
        {
          Map.Entry localEntry = (Map.Entry)localIterator3.next();
          if (!localHashSet.contains(localEntry.getKey()))
          {
            if (Log.isLoggable("NotifManCompat", 3))
              Log.d("NotifManCompat", "Removing listener record for " + localEntry.getKey());
            ensureServiceUnbound((ListenerRecord)localEntry.getValue());
            localIterator3.remove();
          }
        }
      }
    }

    public boolean handleMessage(Message paramMessage)
    {
      switch (paramMessage.what)
      {
      default:
        return false;
      case 0:
        handleQueueTask((NotificationManagerCompat.Task)paramMessage.obj);
        return true;
      case 1:
        NotificationManagerCompat.ServiceConnectedEvent localServiceConnectedEvent = (NotificationManagerCompat.ServiceConnectedEvent)paramMessage.obj;
        handleServiceConnected(localServiceConnectedEvent.componentName, localServiceConnectedEvent.iBinder);
        return true;
      case 2:
        handleServiceDisconnected((ComponentName)paramMessage.obj);
        return true;
      case 3:
      }
      handleRetryListenerQueue((ComponentName)paramMessage.obj);
      return true;
    }

    public void onServiceConnected(ComponentName paramComponentName, IBinder paramIBinder)
    {
      if (Log.isLoggable("NotifManCompat", 3))
        Log.d("NotifManCompat", "Connected to service " + paramComponentName);
      this.mHandler.obtainMessage(1, new NotificationManagerCompat.ServiceConnectedEvent(paramComponentName, paramIBinder)).sendToTarget();
    }

    public void onServiceDisconnected(ComponentName paramComponentName)
    {
      if (Log.isLoggable("NotifManCompat", 3))
        Log.d("NotifManCompat", "Disconnected from service " + paramComponentName);
      this.mHandler.obtainMessage(2, paramComponentName).sendToTarget();
    }

    public void queueTask(NotificationManagerCompat.Task paramTask)
    {
      this.mHandler.obtainMessage(0, paramTask).sendToTarget();
    }

    private static class ListenerRecord
    {
      public boolean bound = false;
      public final ComponentName componentName;
      public int retryCount = 0;
      public INotificationSideChannel service;
      public LinkedList<NotificationManagerCompat.Task> taskQueue = new LinkedList();

      public ListenerRecord(ComponentName paramComponentName)
      {
        this.componentName = paramComponentName;
      }
    }
  }

  private static abstract interface Task
  {
    public abstract void send(INotificationSideChannel paramINotificationSideChannel)
      throws RemoteException;
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.app.NotificationManagerCompat
 * JD-Core Version:    0.6.2
 */
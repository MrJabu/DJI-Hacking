package android.support.v4.media.routing;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaRouter;
import android.media.MediaRouter.Callback;
import android.media.MediaRouter.RouteCategory;
import android.media.MediaRouter.RouteGroup;
import android.media.MediaRouter.RouteInfo;
import android.media.MediaRouter.UserRouteInfo;
import android.media.MediaRouter.VolumeCallback;
import android.media.RemoteControlClient;
import android.os.Build.VERSION;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

class MediaRouterJellybean
{
  public static final int ALL_ROUTE_TYPES = 8388611;
  public static final int ROUTE_TYPE_LIVE_AUDIO = 1;
  public static final int ROUTE_TYPE_LIVE_VIDEO = 2;
  public static final int ROUTE_TYPE_USER = 8388608;
  private static final String TAG = "MediaRouterJellybean";

  public static void addCallback(Object paramObject1, int paramInt, Object paramObject2)
  {
    ((MediaRouter)paramObject1).addCallback(paramInt, (MediaRouter.Callback)paramObject2);
  }

  public static void addUserRoute(Object paramObject1, Object paramObject2)
  {
    ((MediaRouter)paramObject1).addUserRoute((MediaRouter.UserRouteInfo)paramObject2);
  }

  public static Object createCallback(Callback paramCallback)
  {
    return new CallbackProxy(paramCallback);
  }

  public static Object createRouteCategory(Object paramObject, String paramString, boolean paramBoolean)
  {
    return ((MediaRouter)paramObject).createRouteCategory(paramString, paramBoolean);
  }

  public static Object createUserRoute(Object paramObject1, Object paramObject2)
  {
    return ((MediaRouter)paramObject1).createUserRoute((MediaRouter.RouteCategory)paramObject2);
  }

  public static Object createVolumeCallback(VolumeCallback paramVolumeCallback)
  {
    return new VolumeCallbackProxy(paramVolumeCallback);
  }

  public static List getCategories(Object paramObject)
  {
    MediaRouter localMediaRouter = (MediaRouter)paramObject;
    int i = localMediaRouter.getCategoryCount();
    ArrayList localArrayList = new ArrayList(i);
    for (int j = 0; j < i; j++)
      localArrayList.add(localMediaRouter.getCategoryAt(j));
    return localArrayList;
  }

  public static Object getMediaRouter(Context paramContext)
  {
    return paramContext.getSystemService("media_router");
  }

  public static List getRoutes(Object paramObject)
  {
    MediaRouter localMediaRouter = (MediaRouter)paramObject;
    int i = localMediaRouter.getRouteCount();
    ArrayList localArrayList = new ArrayList(i);
    for (int j = 0; j < i; j++)
      localArrayList.add(localMediaRouter.getRouteAt(j));
    return localArrayList;
  }

  public static Object getSelectedRoute(Object paramObject, int paramInt)
  {
    return ((MediaRouter)paramObject).getSelectedRoute(paramInt);
  }

  public static void removeCallback(Object paramObject1, Object paramObject2)
  {
    ((MediaRouter)paramObject1).removeCallback((MediaRouter.Callback)paramObject2);
  }

  public static void removeUserRoute(Object paramObject1, Object paramObject2)
  {
    ((MediaRouter)paramObject1).removeUserRoute((MediaRouter.UserRouteInfo)paramObject2);
  }

  public static void selectRoute(Object paramObject1, int paramInt, Object paramObject2)
  {
    ((MediaRouter)paramObject1).selectRoute(paramInt, (MediaRouter.RouteInfo)paramObject2);
  }

  public static abstract interface Callback
  {
    public abstract void onRouteAdded(Object paramObject);

    public abstract void onRouteChanged(Object paramObject);

    public abstract void onRouteGrouped(Object paramObject1, Object paramObject2, int paramInt);

    public abstract void onRouteRemoved(Object paramObject);

    public abstract void onRouteSelected(int paramInt, Object paramObject);

    public abstract void onRouteUngrouped(Object paramObject1, Object paramObject2);

    public abstract void onRouteUnselected(int paramInt, Object paramObject);

    public abstract void onRouteVolumeChanged(Object paramObject);
  }

  static class CallbackProxy<T extends MediaRouterJellybean.Callback> extends MediaRouter.Callback
  {
    protected final T mCallback;

    public CallbackProxy(T paramT)
    {
      this.mCallback = paramT;
    }

    public void onRouteAdded(MediaRouter paramMediaRouter, MediaRouter.RouteInfo paramRouteInfo)
    {
      this.mCallback.onRouteAdded(paramRouteInfo);
    }

    public void onRouteChanged(MediaRouter paramMediaRouter, MediaRouter.RouteInfo paramRouteInfo)
    {
      this.mCallback.onRouteChanged(paramRouteInfo);
    }

    public void onRouteGrouped(MediaRouter paramMediaRouter, MediaRouter.RouteInfo paramRouteInfo, MediaRouter.RouteGroup paramRouteGroup, int paramInt)
    {
      this.mCallback.onRouteGrouped(paramRouteInfo, paramRouteGroup, paramInt);
    }

    public void onRouteRemoved(MediaRouter paramMediaRouter, MediaRouter.RouteInfo paramRouteInfo)
    {
      this.mCallback.onRouteRemoved(paramRouteInfo);
    }

    public void onRouteSelected(MediaRouter paramMediaRouter, int paramInt, MediaRouter.RouteInfo paramRouteInfo)
    {
      this.mCallback.onRouteSelected(paramInt, paramRouteInfo);
    }

    public void onRouteUngrouped(MediaRouter paramMediaRouter, MediaRouter.RouteInfo paramRouteInfo, MediaRouter.RouteGroup paramRouteGroup)
    {
      this.mCallback.onRouteUngrouped(paramRouteInfo, paramRouteGroup);
    }

    public void onRouteUnselected(MediaRouter paramMediaRouter, int paramInt, MediaRouter.RouteInfo paramRouteInfo)
    {
      this.mCallback.onRouteUnselected(paramInt, paramRouteInfo);
    }

    public void onRouteVolumeChanged(MediaRouter paramMediaRouter, MediaRouter.RouteInfo paramRouteInfo)
    {
      this.mCallback.onRouteVolumeChanged(paramRouteInfo);
    }
  }

  public static final class GetDefaultRouteWorkaround
  {
    private Method mGetSystemAudioRouteMethod;

    public GetDefaultRouteWorkaround()
    {
      if ((Build.VERSION.SDK_INT < 16) || (Build.VERSION.SDK_INT > 17))
        throw new UnsupportedOperationException();
      try
      {
        this.mGetSystemAudioRouteMethod = MediaRouter.class.getMethod("getSystemAudioRoute", new Class[0]);
        return;
      }
      catch (NoSuchMethodException localNoSuchMethodException)
      {
      }
    }

    public Object getDefaultRoute(Object paramObject)
    {
      MediaRouter localMediaRouter = (MediaRouter)paramObject;
      if (this.mGetSystemAudioRouteMethod != null);
      try
      {
        Object localObject = this.mGetSystemAudioRouteMethod.invoke(localMediaRouter, new Object[0]);
        return localObject;
      }
      catch (InvocationTargetException localInvocationTargetException)
      {
        return localMediaRouter.getRouteAt(0);
      }
      catch (IllegalAccessException localIllegalAccessException)
      {
        label31: break label31;
      }
    }
  }

  public static final class RouteCategory
  {
    public static CharSequence getName(Object paramObject, Context paramContext)
    {
      return ((MediaRouter.RouteCategory)paramObject).getName(paramContext);
    }

    public static List getRoutes(Object paramObject)
    {
      ArrayList localArrayList = new ArrayList();
      ((MediaRouter.RouteCategory)paramObject).getRoutes(localArrayList);
      return localArrayList;
    }

    public static int getSupportedTypes(Object paramObject)
    {
      return ((MediaRouter.RouteCategory)paramObject).getSupportedTypes();
    }

    public static boolean isGroupable(Object paramObject)
    {
      return ((MediaRouter.RouteCategory)paramObject).isGroupable();
    }
  }

  public static final class RouteGroup
  {
    public static List getGroupedRoutes(Object paramObject)
    {
      MediaRouter.RouteGroup localRouteGroup = (MediaRouter.RouteGroup)paramObject;
      int i = localRouteGroup.getRouteCount();
      ArrayList localArrayList = new ArrayList(i);
      for (int j = 0; j < i; j++)
        localArrayList.add(localRouteGroup.getRouteAt(j));
      return localArrayList;
    }
  }

  public static final class RouteInfo
  {
    public static Object getCategory(Object paramObject)
    {
      return ((MediaRouter.RouteInfo)paramObject).getCategory();
    }

    public static Object getGroup(Object paramObject)
    {
      return ((MediaRouter.RouteInfo)paramObject).getGroup();
    }

    public static Drawable getIconDrawable(Object paramObject)
    {
      return ((MediaRouter.RouteInfo)paramObject).getIconDrawable();
    }

    public static CharSequence getName(Object paramObject, Context paramContext)
    {
      return ((MediaRouter.RouteInfo)paramObject).getName(paramContext);
    }

    public static int getPlaybackStream(Object paramObject)
    {
      return ((MediaRouter.RouteInfo)paramObject).getPlaybackStream();
    }

    public static int getPlaybackType(Object paramObject)
    {
      return ((MediaRouter.RouteInfo)paramObject).getPlaybackType();
    }

    public static CharSequence getStatus(Object paramObject)
    {
      return ((MediaRouter.RouteInfo)paramObject).getStatus();
    }

    public static int getSupportedTypes(Object paramObject)
    {
      return ((MediaRouter.RouteInfo)paramObject).getSupportedTypes();
    }

    public static Object getTag(Object paramObject)
    {
      return ((MediaRouter.RouteInfo)paramObject).getTag();
    }

    public static int getVolume(Object paramObject)
    {
      return ((MediaRouter.RouteInfo)paramObject).getVolume();
    }

    public static int getVolumeHandling(Object paramObject)
    {
      return ((MediaRouter.RouteInfo)paramObject).getVolumeHandling();
    }

    public static int getVolumeMax(Object paramObject)
    {
      return ((MediaRouter.RouteInfo)paramObject).getVolumeMax();
    }

    public static boolean isGroup(Object paramObject)
    {
      return paramObject instanceof MediaRouter.RouteGroup;
    }

    public static void requestSetVolume(Object paramObject, int paramInt)
    {
      ((MediaRouter.RouteInfo)paramObject).requestSetVolume(paramInt);
    }

    public static void requestUpdateVolume(Object paramObject, int paramInt)
    {
      ((MediaRouter.RouteInfo)paramObject).requestUpdateVolume(paramInt);
    }

    public static void setTag(Object paramObject1, Object paramObject2)
    {
      ((MediaRouter.RouteInfo)paramObject1).setTag(paramObject2);
    }
  }

  public static final class SelectRouteWorkaround
  {
    private Method mSelectRouteIntMethod;

    public SelectRouteWorkaround()
    {
      if ((Build.VERSION.SDK_INT < 16) || (Build.VERSION.SDK_INT > 17))
        throw new UnsupportedOperationException();
      try
      {
        Class[] arrayOfClass = new Class[2];
        arrayOfClass[0] = Integer.TYPE;
        arrayOfClass[1] = MediaRouter.RouteInfo.class;
        this.mSelectRouteIntMethod = MediaRouter.class.getMethod("selectRouteInt", arrayOfClass);
        return;
      }
      catch (NoSuchMethodException localNoSuchMethodException)
      {
      }
    }

    public void selectRoute(Object paramObject1, int paramInt, Object paramObject2)
    {
      MediaRouter localMediaRouter = (MediaRouter)paramObject1;
      MediaRouter.RouteInfo localRouteInfo = (MediaRouter.RouteInfo)paramObject2;
      if (((0x800000 & localRouteInfo.getSupportedTypes()) != 0) || (this.mSelectRouteIntMethod != null));
      while (true)
      {
        try
        {
          Method localMethod = this.mSelectRouteIntMethod;
          Object[] arrayOfObject = new Object[2];
          arrayOfObject[0] = Integer.valueOf(paramInt);
          arrayOfObject[1] = localRouteInfo;
          localMethod.invoke(localMediaRouter, arrayOfObject);
          return;
        }
        catch (IllegalAccessException localIllegalAccessException)
        {
          Log.w("MediaRouterJellybean", "Cannot programmatically select non-user route.  Media routing may not work.", localIllegalAccessException);
          localMediaRouter.selectRoute(paramInt, localRouteInfo);
          return;
        }
        catch (InvocationTargetException localInvocationTargetException)
        {
          Log.w("MediaRouterJellybean", "Cannot programmatically select non-user route.  Media routing may not work.", localInvocationTargetException);
          continue;
        }
        Log.w("MediaRouterJellybean", "Cannot programmatically select non-user route because the platform is missing the selectRouteInt() method.  Media routing may not work.");
      }
    }
  }

  public static final class UserRouteInfo
  {
    public static void setIconDrawable(Object paramObject, Drawable paramDrawable)
    {
      ((MediaRouter.UserRouteInfo)paramObject).setIconDrawable(paramDrawable);
    }

    public static void setName(Object paramObject, CharSequence paramCharSequence)
    {
      ((MediaRouter.UserRouteInfo)paramObject).setName(paramCharSequence);
    }

    public static void setPlaybackStream(Object paramObject, int paramInt)
    {
      ((MediaRouter.UserRouteInfo)paramObject).setPlaybackStream(paramInt);
    }

    public static void setPlaybackType(Object paramObject, int paramInt)
    {
      ((MediaRouter.UserRouteInfo)paramObject).setPlaybackType(paramInt);
    }

    public static void setRemoteControlClient(Object paramObject1, Object paramObject2)
    {
      ((MediaRouter.UserRouteInfo)paramObject1).setRemoteControlClient((RemoteControlClient)paramObject2);
    }

    public static void setStatus(Object paramObject, CharSequence paramCharSequence)
    {
      ((MediaRouter.UserRouteInfo)paramObject).setStatus(paramCharSequence);
    }

    public static void setVolume(Object paramObject, int paramInt)
    {
      ((MediaRouter.UserRouteInfo)paramObject).setVolume(paramInt);
    }

    public static void setVolumeCallback(Object paramObject1, Object paramObject2)
    {
      ((MediaRouter.UserRouteInfo)paramObject1).setVolumeCallback((MediaRouter.VolumeCallback)paramObject2);
    }

    public static void setVolumeHandling(Object paramObject, int paramInt)
    {
      ((MediaRouter.UserRouteInfo)paramObject).setVolumeHandling(paramInt);
    }

    public static void setVolumeMax(Object paramObject, int paramInt)
    {
      ((MediaRouter.UserRouteInfo)paramObject).setVolumeMax(paramInt);
    }
  }

  public static abstract interface VolumeCallback
  {
    public abstract void onVolumeSetRequest(Object paramObject, int paramInt);

    public abstract void onVolumeUpdateRequest(Object paramObject, int paramInt);
  }

  static class VolumeCallbackProxy<T extends MediaRouterJellybean.VolumeCallback> extends MediaRouter.VolumeCallback
  {
    protected final T mCallback;

    public VolumeCallbackProxy(T paramT)
    {
      this.mCallback = paramT;
    }

    public void onVolumeSetRequest(MediaRouter.RouteInfo paramRouteInfo, int paramInt)
    {
      this.mCallback.onVolumeSetRequest(paramRouteInfo, paramInt);
    }

    public void onVolumeUpdateRequest(MediaRouter.RouteInfo paramRouteInfo, int paramInt)
    {
      this.mCallback.onVolumeUpdateRequest(paramRouteInfo, paramInt);
    }
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.media.routing.MediaRouterJellybean
 * JD-Core Version:    0.6.2
 */
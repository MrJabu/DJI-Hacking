package android.support.v7.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.lang.reflect.Method;

class ActionBarDrawerToggleHoneycomb
{
  private static final String TAG = "ActionBarDrawerToggleHoneycomb";
  private static final int[] THEME_ATTRS = { 16843531 };

  public static Drawable getThemeUpIndicator(Activity paramActivity)
  {
    TypedArray localTypedArray = paramActivity.obtainStyledAttributes(THEME_ATTRS);
    Drawable localDrawable = localTypedArray.getDrawable(0);
    localTypedArray.recycle();
    return localDrawable;
  }

  public static SetIndicatorInfo setActionBarDescription(SetIndicatorInfo paramSetIndicatorInfo, Activity paramActivity, int paramInt)
  {
    if (paramSetIndicatorInfo == null)
      paramSetIndicatorInfo = new SetIndicatorInfo(paramActivity);
    if (paramSetIndicatorInfo.setHomeAsUpIndicator != null);
    try
    {
      ActionBar localActionBar = paramActivity.getActionBar();
      Method localMethod = paramSetIndicatorInfo.setHomeActionContentDescription;
      Object[] arrayOfObject = new Object[1];
      arrayOfObject[0] = Integer.valueOf(paramInt);
      localMethod.invoke(localActionBar, arrayOfObject);
      if (Build.VERSION.SDK_INT <= 19)
        localActionBar.setSubtitle(localActionBar.getSubtitle());
      return paramSetIndicatorInfo;
    }
    catch (Exception localException)
    {
      Log.w("ActionBarDrawerToggleHoneycomb", "Couldn't set content description via JB-MR2 API", localException);
    }
    return paramSetIndicatorInfo;
  }

  public static SetIndicatorInfo setActionBarUpIndicator(SetIndicatorInfo paramSetIndicatorInfo, Activity paramActivity, Drawable paramDrawable, int paramInt)
  {
    SetIndicatorInfo localSetIndicatorInfo = new SetIndicatorInfo(paramActivity);
    if (localSetIndicatorInfo.setHomeAsUpIndicator != null)
      try
      {
        ActionBar localActionBar = paramActivity.getActionBar();
        localSetIndicatorInfo.setHomeAsUpIndicator.invoke(localActionBar, new Object[] { paramDrawable });
        Method localMethod = localSetIndicatorInfo.setHomeActionContentDescription;
        Object[] arrayOfObject = new Object[1];
        arrayOfObject[0] = Integer.valueOf(paramInt);
        localMethod.invoke(localActionBar, arrayOfObject);
        return localSetIndicatorInfo;
      }
      catch (Exception localException)
      {
        Log.w("ActionBarDrawerToggleHoneycomb", "Couldn't set home-as-up indicator via JB-MR2 API", localException);
        return localSetIndicatorInfo;
      }
    if (localSetIndicatorInfo.upIndicatorView != null)
    {
      localSetIndicatorInfo.upIndicatorView.setImageDrawable(paramDrawable);
      return localSetIndicatorInfo;
    }
    Log.w("ActionBarDrawerToggleHoneycomb", "Couldn't set home-as-up indicator");
    return localSetIndicatorInfo;
  }

  static class SetIndicatorInfo
  {
    public Method setHomeActionContentDescription;
    public Method setHomeAsUpIndicator;
    public ImageView upIndicatorView;

    SetIndicatorInfo(Activity paramActivity)
    {
      while (true)
      {
        View localView2;
        View localView3;
        try
        {
          this.setHomeAsUpIndicator = ActionBar.class.getDeclaredMethod("setHomeAsUpIndicator", new Class[] { Drawable.class });
          Class[] arrayOfClass = new Class[1];
          arrayOfClass[0] = Integer.TYPE;
          this.setHomeActionContentDescription = ActionBar.class.getDeclaredMethod("setHomeActionContentDescription", arrayOfClass);
          return;
        }
        catch (NoSuchMethodException localNoSuchMethodException)
        {
          View localView1 = paramActivity.findViewById(16908332);
          if (localView1 == null)
            continue;
          ViewGroup localViewGroup = (ViewGroup)localView1.getParent();
          if (localViewGroup.getChildCount() != 2)
            continue;
          localView2 = localViewGroup.getChildAt(0);
          localView3 = localViewGroup.getChildAt(1);
          if (localView2.getId() != 16908332);
        }
        for (View localView4 = localView3; (localView4 instanceof ImageView); localView4 = localView2)
        {
          this.upIndicatorView = ((ImageView)localView4);
          return;
        }
      }
    }
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v7.app.ActionBarDrawerToggleHoneycomb
 * JD-Core Version:    0.6.2
 */
package android.support.v4.internal.view;

import android.support.v4.view.ActionProvider;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
import android.view.MenuItem;
import android.view.View;

public abstract interface SupportMenuItem extends MenuItem
{
  public static final int SHOW_AS_ACTION_ALWAYS = 2;
  public static final int SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW = 8;
  public static final int SHOW_AS_ACTION_IF_ROOM = 1;
  public static final int SHOW_AS_ACTION_NEVER = 0;
  public static final int SHOW_AS_ACTION_WITH_TEXT = 4;

  public abstract boolean collapseActionView();

  public abstract boolean expandActionView();

  public abstract View getActionView();

  public abstract ActionProvider getSupportActionProvider();

  public abstract boolean isActionViewExpanded();

  public abstract MenuItem setActionView(int paramInt);

  public abstract MenuItem setActionView(View paramView);

  public abstract void setShowAsAction(int paramInt);

  public abstract MenuItem setShowAsActionFlags(int paramInt);

  public abstract SupportMenuItem setSupportActionProvider(ActionProvider paramActionProvider);

  public abstract SupportMenuItem setSupportOnActionExpandListener(MenuItemCompat.OnActionExpandListener paramOnActionExpandListener);
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.internal.view.SupportMenuItem
 * JD-Core Version:    0.6.2
 */
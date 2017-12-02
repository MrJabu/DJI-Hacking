package android.support.v4.view;

import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;

class MenuItemCompatIcs
{
  public static boolean collapseActionView(MenuItem paramMenuItem)
  {
    return paramMenuItem.collapseActionView();
  }

  public static boolean expandActionView(MenuItem paramMenuItem)
  {
    return paramMenuItem.expandActionView();
  }

  public static boolean isActionViewExpanded(MenuItem paramMenuItem)
  {
    return paramMenuItem.isActionViewExpanded();
  }

  public static MenuItem setOnActionExpandListener(MenuItem paramMenuItem, SupportActionExpandProxy paramSupportActionExpandProxy)
  {
    return paramMenuItem.setOnActionExpandListener(new OnActionExpandListenerWrapper(paramSupportActionExpandProxy));
  }

  static class OnActionExpandListenerWrapper
    implements MenuItem.OnActionExpandListener
  {
    private MenuItemCompatIcs.SupportActionExpandProxy mWrapped;

    public OnActionExpandListenerWrapper(MenuItemCompatIcs.SupportActionExpandProxy paramSupportActionExpandProxy)
    {
      this.mWrapped = paramSupportActionExpandProxy;
    }

    public boolean onMenuItemActionCollapse(MenuItem paramMenuItem)
    {
      return this.mWrapped.onMenuItemActionCollapse(paramMenuItem);
    }

    public boolean onMenuItemActionExpand(MenuItem paramMenuItem)
    {
      return this.mWrapped.onMenuItemActionExpand(paramMenuItem);
    }
  }

  static abstract interface SupportActionExpandProxy
  {
    public abstract boolean onMenuItemActionCollapse(MenuItem paramMenuItem);

    public abstract boolean onMenuItemActionExpand(MenuItem paramMenuItem);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.view.MenuItemCompatIcs
 * JD-Core Version:    0.6.2
 */
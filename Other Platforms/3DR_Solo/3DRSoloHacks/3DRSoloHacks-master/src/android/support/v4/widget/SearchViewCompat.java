package android.support.v4.widget;

import android.content.ComponentName;
import android.content.Context;
import android.os.Build.VERSION;
import android.view.View;

public class SearchViewCompat
{
  private static final SearchViewCompatImpl IMPL = new SearchViewCompat.SearchViewCompatStubImpl();

  static
  {
    if (Build.VERSION.SDK_INT >= 14)
    {
      IMPL = new SearchViewCompat.SearchViewCompatIcsImpl();
      return;
    }
    if (Build.VERSION.SDK_INT >= 11)
    {
      IMPL = new SearchViewCompat.SearchViewCompatHoneycombImpl();
      return;
    }
  }

  private SearchViewCompat(Context paramContext)
  {
  }

  public static CharSequence getQuery(View paramView)
  {
    return IMPL.getQuery(paramView);
  }

  public static boolean isIconified(View paramView)
  {
    return IMPL.isIconified(paramView);
  }

  public static boolean isQueryRefinementEnabled(View paramView)
  {
    return IMPL.isQueryRefinementEnabled(paramView);
  }

  public static boolean isSubmitButtonEnabled(View paramView)
  {
    return IMPL.isSubmitButtonEnabled(paramView);
  }

  public static View newSearchView(Context paramContext)
  {
    return IMPL.newSearchView(paramContext);
  }

  public static void setIconified(View paramView, boolean paramBoolean)
  {
    IMPL.setIconified(paramView, paramBoolean);
  }

  public static void setImeOptions(View paramView, int paramInt)
  {
    IMPL.setImeOptions(paramView, paramInt);
  }

  public static void setInputType(View paramView, int paramInt)
  {
    IMPL.setInputType(paramView, paramInt);
  }

  public static void setMaxWidth(View paramView, int paramInt)
  {
    IMPL.setMaxWidth(paramView, paramInt);
  }

  public static void setOnCloseListener(View paramView, OnCloseListenerCompat paramOnCloseListenerCompat)
  {
    IMPL.setOnCloseListener(paramView, paramOnCloseListenerCompat.mListener);
  }

  public static void setOnQueryTextListener(View paramView, OnQueryTextListenerCompat paramOnQueryTextListenerCompat)
  {
    IMPL.setOnQueryTextListener(paramView, paramOnQueryTextListenerCompat.mListener);
  }

  public static void setQuery(View paramView, CharSequence paramCharSequence, boolean paramBoolean)
  {
    IMPL.setQuery(paramView, paramCharSequence, paramBoolean);
  }

  public static void setQueryHint(View paramView, CharSequence paramCharSequence)
  {
    IMPL.setQueryHint(paramView, paramCharSequence);
  }

  public static void setQueryRefinementEnabled(View paramView, boolean paramBoolean)
  {
    IMPL.setQueryRefinementEnabled(paramView, paramBoolean);
  }

  public static void setSearchableInfo(View paramView, ComponentName paramComponentName)
  {
    IMPL.setSearchableInfo(paramView, paramComponentName);
  }

  public static void setSubmitButtonEnabled(View paramView, boolean paramBoolean)
  {
    IMPL.setSubmitButtonEnabled(paramView, paramBoolean);
  }

  public static abstract class OnCloseListenerCompat
  {
    final Object mListener = SearchViewCompat.IMPL.newOnCloseListener(this);

    public boolean onClose()
    {
      return false;
    }
  }

  public static abstract class OnQueryTextListenerCompat
  {
    final Object mListener = SearchViewCompat.IMPL.newOnQueryTextListener(this);

    public boolean onQueryTextChange(String paramString)
    {
      return false;
    }

    public boolean onQueryTextSubmit(String paramString)
    {
      return false;
    }
  }

  static abstract interface SearchViewCompatImpl
  {
    public abstract CharSequence getQuery(View paramView);

    public abstract boolean isIconified(View paramView);

    public abstract boolean isQueryRefinementEnabled(View paramView);

    public abstract boolean isSubmitButtonEnabled(View paramView);

    public abstract Object newOnCloseListener(SearchViewCompat.OnCloseListenerCompat paramOnCloseListenerCompat);

    public abstract Object newOnQueryTextListener(SearchViewCompat.OnQueryTextListenerCompat paramOnQueryTextListenerCompat);

    public abstract View newSearchView(Context paramContext);

    public abstract void setIconified(View paramView, boolean paramBoolean);

    public abstract void setImeOptions(View paramView, int paramInt);

    public abstract void setInputType(View paramView, int paramInt);

    public abstract void setMaxWidth(View paramView, int paramInt);

    public abstract void setOnCloseListener(Object paramObject1, Object paramObject2);

    public abstract void setOnQueryTextListener(Object paramObject1, Object paramObject2);

    public abstract void setQuery(View paramView, CharSequence paramCharSequence, boolean paramBoolean);

    public abstract void setQueryHint(View paramView, CharSequence paramCharSequence);

    public abstract void setQueryRefinementEnabled(View paramView, boolean paramBoolean);

    public abstract void setSearchableInfo(View paramView, ComponentName paramComponentName);

    public abstract void setSubmitButtonEnabled(View paramView, boolean paramBoolean);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.widget.SearchViewCompat
 * JD-Core Version:    0.6.2
 */
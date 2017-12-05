package android.support.v4.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class ContentLoadingProgressBar extends ProgressBar
{
  private static final int MIN_DELAY = 500;
  private static final int MIN_SHOW_TIME = 500;
  private final Runnable mDelayedHide = new Runnable()
  {
    public void run()
    {
      ContentLoadingProgressBar.access$002(ContentLoadingProgressBar.this, false);
      ContentLoadingProgressBar.access$102(ContentLoadingProgressBar.this, -1L);
      ContentLoadingProgressBar.this.setVisibility(8);
    }
  };
  private final Runnable mDelayedShow = new Runnable()
  {
    public void run()
    {
      ContentLoadingProgressBar.access$202(ContentLoadingProgressBar.this, false);
      if (!ContentLoadingProgressBar.this.mDismissed)
      {
        ContentLoadingProgressBar.access$102(ContentLoadingProgressBar.this, System.currentTimeMillis());
        ContentLoadingProgressBar.this.setVisibility(0);
      }
    }
  };
  private boolean mDismissed = false;
  private boolean mPostedHide = false;
  private boolean mPostedShow = false;
  private long mStartTime = -1L;

  public ContentLoadingProgressBar(Context paramContext)
  {
    this(paramContext, null);
  }

  public ContentLoadingProgressBar(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet, 0);
  }

  private void removeCallbacks()
  {
    removeCallbacks(this.mDelayedHide);
    removeCallbacks(this.mDelayedShow);
  }

  public void hide()
  {
    this.mDismissed = true;
    removeCallbacks(this.mDelayedShow);
    long l = System.currentTimeMillis() - this.mStartTime;
    if ((l >= 500L) || (this.mStartTime == -1L))
      setVisibility(8);
    while (this.mPostedHide)
      return;
    postDelayed(this.mDelayedHide, 500L - l);
    this.mPostedHide = true;
  }

  public void onAttachedToWindow()
  {
    super.onAttachedToWindow();
    removeCallbacks();
  }

  public void onDetachedFromWindow()
  {
    super.onDetachedFromWindow();
    removeCallbacks();
  }

  public void show()
  {
    this.mStartTime = -1L;
    this.mDismissed = false;
    removeCallbacks(this.mDelayedHide);
    if (!this.mPostedShow)
    {
      postDelayed(this.mDelayedShow, 500L);
      this.mPostedShow = true;
    }
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.widget.ContentLoadingProgressBar
 * JD-Core Version:    0.6.2
 */
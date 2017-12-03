package android.support.v4.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.ViewPropertyAnimator;

class ViewPropertyAnimatorCompatJB
{
  public static void setListener(final View paramView, ViewPropertyAnimatorListener paramViewPropertyAnimatorListener)
  {
    if (paramViewPropertyAnimatorListener != null)
    {
      paramView.animate().setListener(new AnimatorListenerAdapter()
      {
        public void onAnimationCancel(Animator paramAnonymousAnimator)
        {
          this.val$listener.onAnimationCancel(paramView);
        }

        public void onAnimationEnd(Animator paramAnonymousAnimator)
        {
          this.val$listener.onAnimationEnd(paramView);
        }

        public void onAnimationStart(Animator paramAnonymousAnimator)
        {
          this.val$listener.onAnimationStart(paramView);
        }
      });
      return;
    }
    paramView.animate().setListener(null);
  }

  public static void withEndAction(View paramView, Runnable paramRunnable)
  {
    paramView.animate().withEndAction(paramRunnable);
  }

  public static void withLayer(View paramView)
  {
    paramView.animate().withLayer();
  }

  public static void withStartAction(View paramView, Runnable paramRunnable)
  {
    paramView.animate().withStartAction(paramRunnable);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.view.ViewPropertyAnimatorCompatJB
 * JD-Core Version:    0.6.2
 */
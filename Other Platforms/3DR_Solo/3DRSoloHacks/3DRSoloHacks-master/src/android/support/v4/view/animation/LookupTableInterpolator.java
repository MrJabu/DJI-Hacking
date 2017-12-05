package android.support.v4.view.animation;

import android.view.animation.Interpolator;

abstract class LookupTableInterpolator
  implements Interpolator
{
  private final float mStepSize;
  private final float[] mValues;

  public LookupTableInterpolator(float[] paramArrayOfFloat)
  {
    this.mValues = paramArrayOfFloat;
    this.mStepSize = (1.0F / (-1 + this.mValues.length));
  }

  public float getInterpolation(float paramFloat)
  {
    if (paramFloat >= 1.0F)
      return 1.0F;
    if (paramFloat <= 0.0F)
      return 0.0F;
    int i = Math.min((int)(paramFloat * (-1 + this.mValues.length)), -2 + this.mValues.length);
    float f = (paramFloat - i * this.mStepSize) / this.mStepSize;
    return this.mValues[i] + f * (this.mValues[(i + 1)] - this.mValues[i]);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.view.animation.LookupTableInterpolator
 * JD-Core Version:    0.6.2
 */
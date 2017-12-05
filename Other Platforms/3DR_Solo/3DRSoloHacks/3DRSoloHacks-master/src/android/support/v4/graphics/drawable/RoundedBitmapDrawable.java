package android.support.v4.graphics.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

public abstract class RoundedBitmapDrawable extends Drawable
{
  private static final int DEFAULT_PAINT_FLAGS = 6;
  private boolean mApplyGravity = true;
  Bitmap mBitmap;
  private int mBitmapHeight;
  private BitmapShader mBitmapShader;
  private int mBitmapWidth;
  private float mCornerRadius;
  final Rect mDstRect = new Rect();
  final RectF mDstRectF = new RectF();
  private int mGravity = 119;
  private Paint mPaint = new Paint(6);
  private int mTargetDensity = 160;

  RoundedBitmapDrawable(Resources paramResources, Bitmap paramBitmap)
  {
    if (paramResources != null)
      this.mTargetDensity = paramResources.getDisplayMetrics().densityDpi;
    this.mBitmap = paramBitmap;
    if (this.mBitmap != null)
    {
      computeBitmapSize();
      this.mBitmapShader = new BitmapShader(this.mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
      return;
    }
    this.mBitmapHeight = -1;
    this.mBitmapWidth = -1;
  }

  private void computeBitmapSize()
  {
    this.mBitmapWidth = this.mBitmap.getScaledWidth(this.mTargetDensity);
    this.mBitmapHeight = this.mBitmap.getScaledHeight(this.mTargetDensity);
  }

  private static boolean isGreaterThanZero(float paramFloat)
  {
    return Float.compare(paramFloat, 0.0F) > 0;
  }

  public void draw(Canvas paramCanvas)
  {
    Bitmap localBitmap = this.mBitmap;
    if (localBitmap == null)
      return;
    updateDstRect();
    Paint localPaint = this.mPaint;
    if (localPaint.getShader() == null)
    {
      paramCanvas.drawBitmap(localBitmap, null, this.mDstRect, localPaint);
      return;
    }
    paramCanvas.drawRoundRect(this.mDstRectF, this.mCornerRadius, this.mCornerRadius, localPaint);
  }

  public int getAlpha()
  {
    return this.mPaint.getAlpha();
  }

  public final Bitmap getBitmap()
  {
    return this.mBitmap;
  }

  public ColorFilter getColorFilter()
  {
    return this.mPaint.getColorFilter();
  }

  public float getCornerRadius()
  {
    return this.mCornerRadius;
  }

  public int getGravity()
  {
    return this.mGravity;
  }

  public int getIntrinsicHeight()
  {
    return this.mBitmapHeight;
  }

  public int getIntrinsicWidth()
  {
    return this.mBitmapWidth;
  }

  public int getOpacity()
  {
    if (this.mGravity != 119);
    Bitmap localBitmap;
    do
    {
      return -3;
      localBitmap = this.mBitmap;
    }
    while ((localBitmap == null) || (localBitmap.hasAlpha()) || (this.mPaint.getAlpha() < 255) || (isGreaterThanZero(this.mCornerRadius)));
    return -1;
  }

  public final Paint getPaint()
  {
    return this.mPaint;
  }

  void gravityCompatApply(int paramInt1, int paramInt2, int paramInt3, Rect paramRect1, Rect paramRect2)
  {
    throw new UnsupportedOperationException();
  }

  public boolean hasAntiAlias()
  {
    return this.mPaint.isAntiAlias();
  }

  public boolean hasMipMap()
  {
    throw new UnsupportedOperationException();
  }

  public void setAlpha(int paramInt)
  {
    if (paramInt != this.mPaint.getAlpha())
    {
      this.mPaint.setAlpha(paramInt);
      invalidateSelf();
    }
  }

  public void setAntiAlias(boolean paramBoolean)
  {
    this.mPaint.setAntiAlias(paramBoolean);
    invalidateSelf();
  }

  public void setColorFilter(ColorFilter paramColorFilter)
  {
    this.mPaint.setColorFilter(paramColorFilter);
    invalidateSelf();
  }

  public void setCornerRadius(float paramFloat)
  {
    if (isGreaterThanZero(paramFloat))
      this.mPaint.setShader(this.mBitmapShader);
    while (true)
    {
      this.mCornerRadius = paramFloat;
      return;
      this.mPaint.setShader(null);
    }
  }

  public void setDither(boolean paramBoolean)
  {
    this.mPaint.setDither(paramBoolean);
    invalidateSelf();
  }

  public void setFilterBitmap(boolean paramBoolean)
  {
    this.mPaint.setFilterBitmap(paramBoolean);
    invalidateSelf();
  }

  public void setGravity(int paramInt)
  {
    if (this.mGravity != paramInt)
    {
      this.mGravity = paramInt;
      this.mApplyGravity = true;
      invalidateSelf();
    }
  }

  public void setMipMap(boolean paramBoolean)
  {
    throw new UnsupportedOperationException();
  }

  public void setTargetDensity(int paramInt)
  {
    if (this.mTargetDensity != paramInt)
    {
      if (paramInt == 0)
        paramInt = 160;
      this.mTargetDensity = paramInt;
      if (this.mBitmap != null)
        computeBitmapSize();
      invalidateSelf();
    }
  }

  public void setTargetDensity(Canvas paramCanvas)
  {
    setTargetDensity(paramCanvas.getDensity());
  }

  public void setTargetDensity(DisplayMetrics paramDisplayMetrics)
  {
    setTargetDensity(paramDisplayMetrics.densityDpi);
  }

  void updateDstRect()
  {
    if (this.mApplyGravity)
    {
      gravityCompatApply(this.mGravity, this.mBitmapWidth, this.mBitmapHeight, getBounds(), this.mDstRect);
      this.mDstRectF.set(this.mDstRect);
      this.mApplyGravity = false;
    }
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.graphics.drawable.RoundedBitmapDrawable
 * JD-Core Version:    0.6.2
 */
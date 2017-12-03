package android.support.v7.app;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.appcompat.R.attr;
import android.support.v7.appcompat.R.style;
import android.support.v7.appcompat.R.styleable;

abstract class DrawerArrowDrawable extends Drawable
{
  private static final float ARROW_HEAD_ANGLE = (float)Math.toRadians(45.0D);
  private final float mBarGap;
  private final float mBarSize;
  private final float mBarThickness;
  private float mCenterOffset;
  private float mMaxCutForBarSize;
  private final float mMiddleArrowSize;
  private final Paint mPaint = new Paint();
  private final Path mPath = new Path();
  private float mProgress;
  private final int mSize;
  private final boolean mSpin;
  private final float mTopBottomArrowSize;
  private boolean mVerticalMirror = false;

  DrawerArrowDrawable(Context paramContext)
  {
    TypedArray localTypedArray = paramContext.getTheme().obtainStyledAttributes(null, R.styleable.DrawerArrowToggle, R.attr.drawerArrowStyle, R.style.Base_Widget_AppCompat_DrawerArrowToggle);
    this.mPaint.setAntiAlias(true);
    this.mPaint.setColor(localTypedArray.getColor(R.styleable.DrawerArrowToggle_color, 0));
    this.mSize = localTypedArray.getDimensionPixelSize(R.styleable.DrawerArrowToggle_drawableSize, 0);
    this.mBarSize = Math.round(localTypedArray.getDimension(R.styleable.DrawerArrowToggle_barSize, 0.0F));
    this.mTopBottomArrowSize = Math.round(localTypedArray.getDimension(R.styleable.DrawerArrowToggle_topBottomBarArrowSize, 0.0F));
    this.mBarThickness = localTypedArray.getDimension(R.styleable.DrawerArrowToggle_thickness, 0.0F);
    this.mBarGap = Math.round(localTypedArray.getDimension(R.styleable.DrawerArrowToggle_gapBetweenBars, 0.0F));
    this.mSpin = localTypedArray.getBoolean(R.styleable.DrawerArrowToggle_spinBars, true);
    this.mMiddleArrowSize = localTypedArray.getDimension(R.styleable.DrawerArrowToggle_middleBarArrowSize, 0.0F);
    this.mCenterOffset = (2 * ((int)(this.mSize - 3.0F * this.mBarThickness - 2.0F * this.mBarGap) / 4));
    this.mCenterOffset = ((float)(this.mCenterOffset + (1.5D * this.mBarThickness + this.mBarGap)));
    localTypedArray.recycle();
    this.mPaint.setStyle(Paint.Style.STROKE);
    this.mPaint.setStrokeJoin(Paint.Join.MITER);
    this.mPaint.setStrokeCap(Paint.Cap.BUTT);
    this.mPaint.setStrokeWidth(this.mBarThickness);
    this.mMaxCutForBarSize = ((float)(this.mBarThickness / 2.0F * Math.cos(ARROW_HEAD_ANGLE)));
  }

  private static float lerp(float paramFloat1, float paramFloat2, float paramFloat3)
  {
    return paramFloat1 + paramFloat3 * (paramFloat2 - paramFloat1);
  }

  public void draw(Canvas paramCanvas)
  {
    Rect localRect = getBounds();
    boolean bool = isLayoutRtl();
    float f1 = lerp(this.mBarSize, this.mTopBottomArrowSize, this.mProgress);
    float f2 = lerp(this.mBarSize, this.mMiddleArrowSize, this.mProgress);
    float f3 = Math.round(lerp(0.0F, this.mMaxCutForBarSize, this.mProgress));
    float f4 = lerp(0.0F, ARROW_HEAD_ANGLE, this.mProgress);
    float f5;
    float f6;
    label90: int i;
    if (bool)
    {
      f5 = 0.0F;
      if (!bool)
        break label324;
      f6 = 180.0F;
      float f7 = lerp(f5, f6, this.mProgress);
      float f8 = (float)Math.round(f1 * Math.cos(f4));
      float f9 = (float)Math.round(f1 * Math.sin(f4));
      this.mPath.rewind();
      float f10 = lerp(this.mBarGap + this.mBarThickness, -this.mMaxCutForBarSize, this.mProgress);
      float f11 = -f2 / 2.0F;
      this.mPath.moveTo(f11 + f3, 0.0F);
      this.mPath.rLineTo(f2 - 2.0F * f3, 0.0F);
      this.mPath.moveTo(f11, f10);
      this.mPath.rLineTo(f8, f9);
      this.mPath.moveTo(f11, -f10);
      this.mPath.rLineTo(f8, -f9);
      this.mPath.close();
      paramCanvas.save();
      paramCanvas.translate(localRect.centerX(), this.mCenterOffset);
      if (!this.mSpin)
        break label336;
      if (!(bool ^ this.mVerticalMirror))
        break label330;
      i = -1;
      label290: paramCanvas.rotate(f7 * i);
    }
    while (true)
    {
      paramCanvas.drawPath(this.mPath, this.mPaint);
      paramCanvas.restore();
      return;
      f5 = -180.0F;
      break;
      label324: f6 = 0.0F;
      break label90;
      label330: i = 1;
      break label290;
      label336: if (bool)
        paramCanvas.rotate(180.0F);
    }
  }

  public int getIntrinsicHeight()
  {
    return this.mSize;
  }

  public int getIntrinsicWidth()
  {
    return this.mSize;
  }

  public int getOpacity()
  {
    return -3;
  }

  public float getProgress()
  {
    return this.mProgress;
  }

  public boolean isAutoMirrored()
  {
    return true;
  }

  abstract boolean isLayoutRtl();

  public void setAlpha(int paramInt)
  {
    this.mPaint.setAlpha(paramInt);
  }

  public void setColorFilter(ColorFilter paramColorFilter)
  {
    this.mPaint.setColorFilter(paramColorFilter);
  }

  public void setProgress(float paramFloat)
  {
    this.mProgress = paramFloat;
    invalidateSelf();
  }

  protected void setVerticalMirror(boolean paramBoolean)
  {
    this.mVerticalMirror = paramBoolean;
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v7.app.DrawerArrowDrawable
 * JD-Core Version:    0.6.2
 */
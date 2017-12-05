package android.support.v4.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.support.annotation.NonNull;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

class MaterialProgressDrawable extends Drawable
  implements Animatable
{
  private static final int ANIMATION_DURATION = 1332;
  private static final int ARROW_HEIGHT = 5;
  private static final int ARROW_HEIGHT_LARGE = 6;
  private static final float ARROW_OFFSET_ANGLE = 5.0F;
  private static final int ARROW_WIDTH = 10;
  private static final int ARROW_WIDTH_LARGE = 12;
  private static final float CENTER_RADIUS = 8.75F;
  private static final float CENTER_RADIUS_LARGE = 12.5F;
  private static final int CIRCLE_DIAMETER = 40;
  private static final int CIRCLE_DIAMETER_LARGE = 56;
  private static final float COLOR_START_DELAY_OFFSET = 0.75F;
  static final int DEFAULT = 1;
  private static final float END_TRIM_START_DELAY_OFFSET = 0.5F;
  private static final float FULL_ROTATION = 1080.0F;
  static final int LARGE = 0;
  private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
  private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();
  private static final float MAX_PROGRESS_ARC = 0.8F;
  private static final float NUM_POINTS = 5.0F;
  private static final float START_TRIM_DURATION_OFFSET = 0.5F;
  private static final float STROKE_WIDTH = 2.5F;
  private static final float STROKE_WIDTH_LARGE = 3.0F;
  private final int[] COLORS = { -16777216 };
  private Animation mAnimation;
  private final ArrayList<Animation> mAnimators = new ArrayList();
  private final Drawable.Callback mCallback = new Drawable.Callback()
  {
    public void invalidateDrawable(Drawable paramAnonymousDrawable)
    {
      MaterialProgressDrawable.this.invalidateSelf();
    }

    public void scheduleDrawable(Drawable paramAnonymousDrawable, Runnable paramAnonymousRunnable, long paramAnonymousLong)
    {
      MaterialProgressDrawable.this.scheduleSelf(paramAnonymousRunnable, paramAnonymousLong);
    }

    public void unscheduleDrawable(Drawable paramAnonymousDrawable, Runnable paramAnonymousRunnable)
    {
      MaterialProgressDrawable.this.unscheduleSelf(paramAnonymousRunnable);
    }
  };
  boolean mFinishing;
  private double mHeight;
  private View mParent;
  private Resources mResources;
  private final Ring mRing;
  private float mRotation;
  private float mRotationCount;
  private double mWidth;

  public MaterialProgressDrawable(Context paramContext, View paramView)
  {
    this.mParent = paramView;
    this.mResources = paramContext.getResources();
    this.mRing = new Ring(this.mCallback);
    this.mRing.setColors(this.COLORS);
    updateSizes(1);
    setupAnimators();
  }

  private void applyFinishTranslation(float paramFloat, Ring paramRing)
  {
    updateRingColor(paramFloat, paramRing);
    float f1 = (float)(1.0D + Math.floor(paramRing.getStartingRotation() / 0.8F));
    float f2 = getMinProgressArc(paramRing);
    paramRing.setStartTrim(paramRing.getStartingStartTrim() + paramFloat * (paramRing.getStartingEndTrim() - f2 - paramRing.getStartingStartTrim()));
    paramRing.setEndTrim(paramRing.getStartingEndTrim());
    paramRing.setRotation(paramRing.getStartingRotation() + paramFloat * (f1 - paramRing.getStartingRotation()));
  }

  private int evaluateColorChange(float paramFloat, int paramInt1, int paramInt2)
  {
    int i = Integer.valueOf(paramInt1).intValue();
    int j = 0xFF & i >> 24;
    int k = 0xFF & i >> 16;
    int m = 0xFF & i >> 8;
    int n = i & 0xFF;
    int i1 = Integer.valueOf(paramInt2).intValue();
    int i2 = 0xFF & i1 >> 24;
    int i3 = 0xFF & i1 >> 16;
    int i4 = 0xFF & i1 >> 8;
    int i5 = i1 & 0xFF;
    return j + (int)(paramFloat * (i2 - j)) << 24 | k + (int)(paramFloat * (i3 - k)) << 16 | m + (int)(paramFloat * (i4 - m)) << 8 | n + (int)(paramFloat * (i5 - n));
  }

  private float getMinProgressArc(Ring paramRing)
  {
    return (float)Math.toRadians(paramRing.getStrokeWidth() / (6.283185307179586D * paramRing.getCenterRadius()));
  }

  private float getRotation()
  {
    return this.mRotation;
  }

  private void setSizeParameters(double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4, float paramFloat1, float paramFloat2)
  {
    Ring localRing = this.mRing;
    float f = this.mResources.getDisplayMetrics().density;
    this.mWidth = (paramDouble1 * f);
    this.mHeight = (paramDouble2 * f);
    localRing.setStrokeWidth(f * (float)paramDouble4);
    localRing.setCenterRadius(paramDouble3 * f);
    localRing.setColorIndex(0);
    localRing.setArrowDimensions(paramFloat1 * f, paramFloat2 * f);
    localRing.setInsets((int)this.mWidth, (int)this.mHeight);
  }

  private void setupAnimators()
  {
    final Ring localRing = this.mRing;
    Animation local1 = new Animation()
    {
      public void applyTransformation(float paramAnonymousFloat, Transformation paramAnonymousTransformation)
      {
        if (MaterialProgressDrawable.this.mFinishing)
        {
          MaterialProgressDrawable.this.applyFinishTranslation(paramAnonymousFloat, localRing);
          return;
        }
        float f1 = MaterialProgressDrawable.this.getMinProgressArc(localRing);
        float f2 = localRing.getStartingEndTrim();
        float f3 = localRing.getStartingStartTrim();
        float f4 = localRing.getStartingRotation();
        MaterialProgressDrawable.this.updateRingColor(paramAnonymousFloat, localRing);
        if (paramAnonymousFloat <= 0.5F)
        {
          float f10 = paramAnonymousFloat / 0.5F;
          float f11 = f3 + (0.8F - f1) * MaterialProgressDrawable.MATERIAL_INTERPOLATOR.getInterpolation(f10);
          localRing.setStartTrim(f11);
        }
        if (paramAnonymousFloat > 0.5F)
        {
          float f7 = 0.8F - f1;
          float f8 = (paramAnonymousFloat - 0.5F) / 0.5F;
          float f9 = f2 + f7 * MaterialProgressDrawable.MATERIAL_INTERPOLATOR.getInterpolation(f8);
          localRing.setEndTrim(f9);
        }
        float f5 = f4 + 0.25F * paramAnonymousFloat;
        localRing.setRotation(f5);
        float f6 = 216.0F * paramAnonymousFloat + 1080.0F * (MaterialProgressDrawable.this.mRotationCount / 5.0F);
        MaterialProgressDrawable.this.setRotation(f6);
      }
    };
    local1.setRepeatCount(-1);
    local1.setRepeatMode(1);
    local1.setInterpolator(LINEAR_INTERPOLATOR);
    local1.setAnimationListener(new Animation.AnimationListener()
    {
      public void onAnimationEnd(Animation paramAnonymousAnimation)
      {
      }

      public void onAnimationRepeat(Animation paramAnonymousAnimation)
      {
        localRing.storeOriginals();
        localRing.goToNextColor();
        localRing.setStartTrim(localRing.getEndTrim());
        if (MaterialProgressDrawable.this.mFinishing)
        {
          MaterialProgressDrawable.this.mFinishing = false;
          paramAnonymousAnimation.setDuration(1332L);
          localRing.setShowArrow(false);
          return;
        }
        MaterialProgressDrawable.access$402(MaterialProgressDrawable.this, (1.0F + MaterialProgressDrawable.this.mRotationCount) % 5.0F);
      }

      public void onAnimationStart(Animation paramAnonymousAnimation)
      {
        MaterialProgressDrawable.access$402(MaterialProgressDrawable.this, 0.0F);
      }
    });
    this.mAnimation = local1;
  }

  private void updateRingColor(float paramFloat, Ring paramRing)
  {
    if (paramFloat > 0.75F)
      paramRing.setColor(evaluateColorChange((paramFloat - 0.75F) / 0.25F, paramRing.getStartingColor(), paramRing.getNextColor()));
  }

  public void draw(Canvas paramCanvas)
  {
    Rect localRect = getBounds();
    int i = paramCanvas.save();
    paramCanvas.rotate(this.mRotation, localRect.exactCenterX(), localRect.exactCenterY());
    this.mRing.draw(paramCanvas, localRect);
    paramCanvas.restoreToCount(i);
  }

  public int getAlpha()
  {
    return this.mRing.getAlpha();
  }

  public int getIntrinsicHeight()
  {
    return (int)this.mHeight;
  }

  public int getIntrinsicWidth()
  {
    return (int)this.mWidth;
  }

  public int getOpacity()
  {
    return -3;
  }

  public boolean isRunning()
  {
    ArrayList localArrayList = this.mAnimators;
    int i = localArrayList.size();
    for (int j = 0; j < i; j++)
    {
      Animation localAnimation = (Animation)localArrayList.get(j);
      if ((localAnimation.hasStarted()) && (!localAnimation.hasEnded()))
        return true;
    }
    return false;
  }

  public void setAlpha(int paramInt)
  {
    this.mRing.setAlpha(paramInt);
  }

  public void setArrowScale(float paramFloat)
  {
    this.mRing.setArrowScale(paramFloat);
  }

  public void setBackgroundColor(int paramInt)
  {
    this.mRing.setBackgroundColor(paramInt);
  }

  public void setColorFilter(ColorFilter paramColorFilter)
  {
    this.mRing.setColorFilter(paramColorFilter);
  }

  public void setColorSchemeColors(int[] paramArrayOfInt)
  {
    this.mRing.setColors(paramArrayOfInt);
    this.mRing.setColorIndex(0);
  }

  public void setProgressRotation(float paramFloat)
  {
    this.mRing.setRotation(paramFloat);
  }

  void setRotation(float paramFloat)
  {
    this.mRotation = paramFloat;
    invalidateSelf();
  }

  public void setStartEndTrim(float paramFloat1, float paramFloat2)
  {
    this.mRing.setStartTrim(paramFloat1);
    this.mRing.setEndTrim(paramFloat2);
  }

  public void showArrow(boolean paramBoolean)
  {
    this.mRing.setShowArrow(paramBoolean);
  }

  public void start()
  {
    this.mAnimation.reset();
    this.mRing.storeOriginals();
    if (this.mRing.getEndTrim() != this.mRing.getStartTrim())
    {
      this.mFinishing = true;
      this.mAnimation.setDuration(666L);
      this.mParent.startAnimation(this.mAnimation);
      return;
    }
    this.mRing.setColorIndex(0);
    this.mRing.resetOriginals();
    this.mAnimation.setDuration(1332L);
    this.mParent.startAnimation(this.mAnimation);
  }

  public void stop()
  {
    this.mParent.clearAnimation();
    setRotation(0.0F);
    this.mRing.setShowArrow(false);
    this.mRing.setColorIndex(0);
    this.mRing.resetOriginals();
  }

  public void updateSizes(@ProgressDrawableSize int paramInt)
  {
    if (paramInt == 0)
    {
      setSizeParameters(56.0D, 56.0D, 12.5D, 3.0D, 12.0F, 6.0F);
      return;
    }
    setSizeParameters(40.0D, 40.0D, 8.75D, 2.5D, 10.0F, 5.0F);
  }

  @Retention(RetentionPolicy.CLASS)
  public static @interface ProgressDrawableSize
  {
  }

  private static class Ring
  {
    private int mAlpha;
    private Path mArrow;
    private int mArrowHeight;
    private final Paint mArrowPaint = new Paint();
    private float mArrowScale;
    private int mArrowWidth;
    private int mBackgroundColor;
    private final Drawable.Callback mCallback;
    private final Paint mCirclePaint = new Paint(1);
    private int mColorIndex;
    private int[] mColors;
    private int mCurrentColor;
    private float mEndTrim = 0.0F;
    private final Paint mPaint = new Paint();
    private double mRingCenterRadius;
    private float mRotation = 0.0F;
    private boolean mShowArrow;
    private float mStartTrim = 0.0F;
    private float mStartingEndTrim;
    private float mStartingRotation;
    private float mStartingStartTrim;
    private float mStrokeInset = 2.5F;
    private float mStrokeWidth = 5.0F;
    private final RectF mTempBounds = new RectF();

    public Ring(Drawable.Callback paramCallback)
    {
      this.mCallback = paramCallback;
      this.mPaint.setStrokeCap(Paint.Cap.SQUARE);
      this.mPaint.setAntiAlias(true);
      this.mPaint.setStyle(Paint.Style.STROKE);
      this.mArrowPaint.setStyle(Paint.Style.FILL);
      this.mArrowPaint.setAntiAlias(true);
    }

    private void drawTriangle(Canvas paramCanvas, float paramFloat1, float paramFloat2, Rect paramRect)
    {
      if (this.mShowArrow)
      {
        if (this.mArrow != null)
          break label209;
        this.mArrow = new Path();
        this.mArrow.setFillType(Path.FillType.EVEN_ODD);
      }
      while (true)
      {
        float f1 = (int)this.mStrokeInset / 2 * this.mArrowScale;
        float f2 = (float)(this.mRingCenterRadius * Math.cos(0.0D) + paramRect.exactCenterX());
        float f3 = (float)(this.mRingCenterRadius * Math.sin(0.0D) + paramRect.exactCenterY());
        this.mArrow.moveTo(0.0F, 0.0F);
        this.mArrow.lineTo(this.mArrowWidth * this.mArrowScale, 0.0F);
        this.mArrow.lineTo(this.mArrowWidth * this.mArrowScale / 2.0F, this.mArrowHeight * this.mArrowScale);
        this.mArrow.offset(f2 - f1, f3);
        this.mArrow.close();
        this.mArrowPaint.setColor(this.mCurrentColor);
        paramCanvas.rotate(paramFloat1 + paramFloat2 - 5.0F, paramRect.exactCenterX(), paramRect.exactCenterY());
        paramCanvas.drawPath(this.mArrow, this.mArrowPaint);
        return;
        label209: this.mArrow.reset();
      }
    }

    private int getNextColorIndex()
    {
      return (1 + this.mColorIndex) % this.mColors.length;
    }

    private void invalidateSelf()
    {
      this.mCallback.invalidateDrawable(null);
    }

    public void draw(Canvas paramCanvas, Rect paramRect)
    {
      RectF localRectF = this.mTempBounds;
      localRectF.set(paramRect);
      localRectF.inset(this.mStrokeInset, this.mStrokeInset);
      float f1 = 360.0F * (this.mStartTrim + this.mRotation);
      float f2 = 360.0F * (this.mEndTrim + this.mRotation) - f1;
      this.mPaint.setColor(this.mCurrentColor);
      paramCanvas.drawArc(localRectF, f1, f2, false, this.mPaint);
      drawTriangle(paramCanvas, f1, f2, paramRect);
      if (this.mAlpha < 255)
      {
        this.mCirclePaint.setColor(this.mBackgroundColor);
        this.mCirclePaint.setAlpha(255 - this.mAlpha);
        paramCanvas.drawCircle(paramRect.exactCenterX(), paramRect.exactCenterY(), paramRect.width() / 2, this.mCirclePaint);
      }
    }

    public int getAlpha()
    {
      return this.mAlpha;
    }

    public double getCenterRadius()
    {
      return this.mRingCenterRadius;
    }

    public float getEndTrim()
    {
      return this.mEndTrim;
    }

    public float getInsets()
    {
      return this.mStrokeInset;
    }

    public int getNextColor()
    {
      return this.mColors[getNextColorIndex()];
    }

    public float getRotation()
    {
      return this.mRotation;
    }

    public float getStartTrim()
    {
      return this.mStartTrim;
    }

    public int getStartingColor()
    {
      return this.mColors[this.mColorIndex];
    }

    public float getStartingEndTrim()
    {
      return this.mStartingEndTrim;
    }

    public float getStartingRotation()
    {
      return this.mStartingRotation;
    }

    public float getStartingStartTrim()
    {
      return this.mStartingStartTrim;
    }

    public float getStrokeWidth()
    {
      return this.mStrokeWidth;
    }

    public void goToNextColor()
    {
      setColorIndex(getNextColorIndex());
    }

    public void resetOriginals()
    {
      this.mStartingStartTrim = 0.0F;
      this.mStartingEndTrim = 0.0F;
      this.mStartingRotation = 0.0F;
      setStartTrim(0.0F);
      setEndTrim(0.0F);
      setRotation(0.0F);
    }

    public void setAlpha(int paramInt)
    {
      this.mAlpha = paramInt;
    }

    public void setArrowDimensions(float paramFloat1, float paramFloat2)
    {
      this.mArrowWidth = ((int)paramFloat1);
      this.mArrowHeight = ((int)paramFloat2);
    }

    public void setArrowScale(float paramFloat)
    {
      if (paramFloat != this.mArrowScale)
      {
        this.mArrowScale = paramFloat;
        invalidateSelf();
      }
    }

    public void setBackgroundColor(int paramInt)
    {
      this.mBackgroundColor = paramInt;
    }

    public void setCenterRadius(double paramDouble)
    {
      this.mRingCenterRadius = paramDouble;
    }

    public void setColor(int paramInt)
    {
      this.mCurrentColor = paramInt;
    }

    public void setColorFilter(ColorFilter paramColorFilter)
    {
      this.mPaint.setColorFilter(paramColorFilter);
      invalidateSelf();
    }

    public void setColorIndex(int paramInt)
    {
      this.mColorIndex = paramInt;
      this.mCurrentColor = this.mColors[this.mColorIndex];
    }

    public void setColors(@NonNull int[] paramArrayOfInt)
    {
      this.mColors = paramArrayOfInt;
      setColorIndex(0);
    }

    public void setEndTrim(float paramFloat)
    {
      this.mEndTrim = paramFloat;
      invalidateSelf();
    }

    public void setInsets(int paramInt1, int paramInt2)
    {
      float f1 = Math.min(paramInt1, paramInt2);
      if ((this.mRingCenterRadius <= 0.0D) || (f1 < 0.0F));
      for (float f2 = (float)Math.ceil(this.mStrokeWidth / 2.0F); ; f2 = (float)(f1 / 2.0F - this.mRingCenterRadius))
      {
        this.mStrokeInset = f2;
        return;
      }
    }

    public void setRotation(float paramFloat)
    {
      this.mRotation = paramFloat;
      invalidateSelf();
    }

    public void setShowArrow(boolean paramBoolean)
    {
      if (this.mShowArrow != paramBoolean)
      {
        this.mShowArrow = paramBoolean;
        invalidateSelf();
      }
    }

    public void setStartTrim(float paramFloat)
    {
      this.mStartTrim = paramFloat;
      invalidateSelf();
    }

    public void setStrokeWidth(float paramFloat)
    {
      this.mStrokeWidth = paramFloat;
      this.mPaint.setStrokeWidth(paramFloat);
      invalidateSelf();
    }

    public void storeOriginals()
    {
      this.mStartingStartTrim = this.mStartTrim;
      this.mStartingEndTrim = this.mEndTrim;
      this.mStartingRotation = this.mRotation;
    }
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.widget.MaterialProgressDrawable
 * JD-Core Version:    0.6.2
 */
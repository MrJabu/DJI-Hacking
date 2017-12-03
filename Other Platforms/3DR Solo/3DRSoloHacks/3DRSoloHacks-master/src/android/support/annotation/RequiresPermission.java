package android.support.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({java.lang.annotation.ElementType.ANNOTATION_TYPE, java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.CONSTRUCTOR, java.lang.annotation.ElementType.FIELD})
public @interface RequiresPermission
{
  public abstract String[] allOf();

  public abstract String[] anyOf();

  public abstract boolean conditional();

  public abstract String value();

  @Target({java.lang.annotation.ElementType.FIELD})
  public static @interface Read
  {
    public abstract RequiresPermission value();
  }

  @Target({java.lang.annotation.ElementType.FIELD})
  public static @interface Write
  {
    public abstract RequiresPermission value();
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.annotation.RequiresPermission
 * JD-Core Version:    0.6.2
 */
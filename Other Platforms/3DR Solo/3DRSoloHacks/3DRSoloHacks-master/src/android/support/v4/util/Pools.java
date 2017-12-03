package android.support.v4.util;

public final class Pools
{
  public static abstract interface Pool<T>
  {
    public abstract T acquire();

    public abstract boolean release(T paramT);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.util.Pools
 * JD-Core Version:    0.6.2
 */
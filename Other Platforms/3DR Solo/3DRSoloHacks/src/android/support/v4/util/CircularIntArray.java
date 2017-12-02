package android.support.v4.util;

public final class CircularIntArray
{
  private int mCapacityBitmask;
  private int[] mElements;
  private int mHead;
  private int mTail;

  public CircularIntArray()
  {
    this(8);
  }

  public CircularIntArray(int paramInt)
  {
    if (paramInt <= 0)
      throw new IllegalArgumentException("capacity must be positive");
    int i = paramInt;
    if (Integer.bitCount(paramInt) != 1)
      i = 1 << 1 + Integer.highestOneBit(paramInt);
    this.mCapacityBitmask = (i - 1);
    this.mElements = new int[i];
  }

  private void doubleCapacity()
  {
    int i = this.mElements.length;
    int j = i - this.mHead;
    int k = i << 1;
    if (k < 0)
      throw new RuntimeException("Max array capacity exceeded");
    int[] arrayOfInt = new int[k];
    System.arraycopy(this.mElements, this.mHead, arrayOfInt, 0, j);
    System.arraycopy(this.mElements, 0, arrayOfInt, j, this.mHead);
    this.mElements = arrayOfInt;
    this.mHead = 0;
    this.mTail = i;
    this.mCapacityBitmask = (k - 1);
  }

  public void addFirst(int paramInt)
  {
    this.mHead = (-1 + this.mHead & this.mCapacityBitmask);
    this.mElements[this.mHead] = paramInt;
    if (this.mHead == this.mTail)
      doubleCapacity();
  }

  public void addLast(int paramInt)
  {
    this.mElements[this.mTail] = paramInt;
    this.mTail = (1 + this.mTail & this.mCapacityBitmask);
    if (this.mTail == this.mHead)
      doubleCapacity();
  }

  public void clear()
  {
    this.mTail = this.mHead;
  }

  public int get(int paramInt)
  {
    if ((paramInt < 0) || (paramInt >= size()))
      throw new ArrayIndexOutOfBoundsException();
    return this.mElements[(paramInt + this.mHead & this.mCapacityBitmask)];
  }

  public int getFirst()
  {
    if (this.mHead == this.mTail)
      throw new ArrayIndexOutOfBoundsException();
    return this.mElements[this.mHead];
  }

  public int getLast()
  {
    if (this.mHead == this.mTail)
      throw new ArrayIndexOutOfBoundsException();
    return this.mElements[(-1 + this.mTail & this.mCapacityBitmask)];
  }

  public boolean isEmpty()
  {
    return this.mHead == this.mTail;
  }

  public int popFirst()
  {
    if (this.mHead == this.mTail)
      throw new ArrayIndexOutOfBoundsException();
    int i = this.mElements[this.mHead];
    this.mHead = (1 + this.mHead & this.mCapacityBitmask);
    return i;
  }

  public int popLast()
  {
    if (this.mHead == this.mTail)
      throw new ArrayIndexOutOfBoundsException();
    int i = -1 + this.mTail & this.mCapacityBitmask;
    int j = this.mElements[i];
    this.mTail = i;
    return j;
  }

  public void removeFromEnd(int paramInt)
  {
    if (paramInt <= 0)
      return;
    if (paramInt > size())
      throw new ArrayIndexOutOfBoundsException();
    this.mTail = (this.mTail - paramInt & this.mCapacityBitmask);
  }

  public void removeFromStart(int paramInt)
  {
    if (paramInt <= 0)
      return;
    if (paramInt > size())
      throw new ArrayIndexOutOfBoundsException();
    this.mHead = (paramInt + this.mHead & this.mCapacityBitmask);
  }

  public int size()
  {
    return this.mTail - this.mHead & this.mCapacityBitmask;
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.util.CircularIntArray
 * JD-Core Version:    0.6.2
 */
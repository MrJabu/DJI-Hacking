package android.support.v4.media;

public abstract class TransportController
{
  public abstract int getBufferPercentage();

  public abstract long getCurrentPosition();

  public abstract long getDuration();

  public abstract int getTransportControlFlags();

  public abstract boolean isPlaying();

  public abstract void pausePlaying();

  public abstract void registerStateListener(TransportStateListener paramTransportStateListener);

  public abstract void seekTo(long paramLong);

  public abstract void startPlaying();

  public abstract void stopPlaying();

  public abstract void unregisterStateListener(TransportStateListener paramTransportStateListener);
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.media.TransportController
 * JD-Core Version:    0.6.2
 */
package android.support.v4.net;

import android.os.Build.VERSION;
import java.net.Socket;
import java.net.SocketException;

public class TrafficStatsCompat
{
  private static final TrafficStatsCompatImpl IMPL = new TrafficStatsCompat.BaseTrafficStatsCompatImpl();

  static
  {
    if (Build.VERSION.SDK_INT >= 14)
    {
      IMPL = new TrafficStatsCompat.IcsTrafficStatsCompatImpl();
      return;
    }
  }

  public static void clearThreadStatsTag()
  {
    IMPL.clearThreadStatsTag();
  }

  public static int getThreadStatsTag()
  {
    return IMPL.getThreadStatsTag();
  }

  public static void incrementOperationCount(int paramInt)
  {
    IMPL.incrementOperationCount(paramInt);
  }

  public static void incrementOperationCount(int paramInt1, int paramInt2)
  {
    IMPL.incrementOperationCount(paramInt1, paramInt2);
  }

  public static void setThreadStatsTag(int paramInt)
  {
    IMPL.setThreadStatsTag(paramInt);
  }

  public static void tagSocket(Socket paramSocket)
    throws SocketException
  {
    IMPL.tagSocket(paramSocket);
  }

  public static void untagSocket(Socket paramSocket)
    throws SocketException
  {
    IMPL.untagSocket(paramSocket);
  }

  static abstract interface TrafficStatsCompatImpl
  {
    public abstract void clearThreadStatsTag();

    public abstract int getThreadStatsTag();

    public abstract void incrementOperationCount(int paramInt);

    public abstract void incrementOperationCount(int paramInt1, int paramInt2);

    public abstract void setThreadStatsTag(int paramInt);

    public abstract void tagSocket(Socket paramSocket)
      throws SocketException;

    public abstract void untagSocket(Socket paramSocket)
      throws SocketException;
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.net.TrafficStatsCompat
 * JD-Core Version:    0.6.2
 */
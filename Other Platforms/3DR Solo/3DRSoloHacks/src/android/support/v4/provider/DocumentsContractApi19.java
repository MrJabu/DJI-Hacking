package android.support.v4.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.Log;

class DocumentsContractApi19
{
  private static final String TAG = "DocumentFile";

  public static boolean canRead(Context paramContext, Uri paramUri)
  {
    if (paramContext.checkCallingOrSelfUriPermission(paramUri, 1) != 0);
    while (TextUtils.isEmpty(getRawType(paramContext, paramUri)))
      return false;
    return true;
  }

  public static boolean canWrite(Context paramContext, Uri paramUri)
  {
    if (paramContext.checkCallingOrSelfUriPermission(paramUri, 2) != 0);
    String str;
    int i;
    do
    {
      do
      {
        return false;
        str = getRawType(paramContext, paramUri);
        i = queryForInt(paramContext, paramUri, "flags", 0);
      }
      while (TextUtils.isEmpty(str));
      if ((i & 0x4) != 0)
        return true;
      if (("vnd.android.document/directory".equals(str)) && ((i & 0x8) != 0))
        return true;
    }
    while ((TextUtils.isEmpty(str)) || ((i & 0x2) == 0));
    return true;
  }

  private static void closeQuietly(AutoCloseable paramAutoCloseable)
  {
    if (paramAutoCloseable != null);
    try
    {
      paramAutoCloseable.close();
      return;
    }
    catch (RuntimeException localRuntimeException)
    {
      throw localRuntimeException;
    }
    catch (Exception localException)
    {
    }
  }

  public static boolean delete(Context paramContext, Uri paramUri)
  {
    return DocumentsContract.deleteDocument(paramContext.getContentResolver(), paramUri);
  }

  public static boolean exists(Context paramContext, Uri paramUri)
  {
    ContentResolver localContentResolver = paramContext.getContentResolver();
    Cursor localCursor = null;
    try
    {
      localCursor = localContentResolver.query(paramUri, new String[] { "document_id" }, null, null, null);
      int i = localCursor.getCount();
      if (i > 0);
      for (boolean bool = true; ; bool = false)
        return bool;
    }
    catch (Exception localException)
    {
      Log.w("DocumentFile", "Failed query: " + localException);
      return false;
    }
    finally
    {
      closeQuietly(localCursor);
    }
  }

  public static String getName(Context paramContext, Uri paramUri)
  {
    return queryForString(paramContext, paramUri, "_display_name", null);
  }

  private static String getRawType(Context paramContext, Uri paramUri)
  {
    return queryForString(paramContext, paramUri, "mime_type", null);
  }

  public static String getType(Context paramContext, Uri paramUri)
  {
    String str = getRawType(paramContext, paramUri);
    if ("vnd.android.document/directory".equals(str))
      str = null;
    return str;
  }

  public static boolean isDirectory(Context paramContext, Uri paramUri)
  {
    return "vnd.android.document/directory".equals(getRawType(paramContext, paramUri));
  }

  public static boolean isDocumentUri(Context paramContext, Uri paramUri)
  {
    return DocumentsContract.isDocumentUri(paramContext, paramUri);
  }

  public static boolean isFile(Context paramContext, Uri paramUri)
  {
    String str = getRawType(paramContext, paramUri);
    return (!"vnd.android.document/directory".equals(str)) && (!TextUtils.isEmpty(str));
  }

  public static long lastModified(Context paramContext, Uri paramUri)
  {
    return queryForLong(paramContext, paramUri, "last_modified", 0L);
  }

  public static long length(Context paramContext, Uri paramUri)
  {
    return queryForLong(paramContext, paramUri, "_size", 0L);
  }

  private static int queryForInt(Context paramContext, Uri paramUri, String paramString, int paramInt)
  {
    return (int)queryForLong(paramContext, paramUri, paramString, paramInt);
  }

  private static long queryForLong(Context paramContext, Uri paramUri, String paramString, long paramLong)
  {
    ContentResolver localContentResolver = paramContext.getContentResolver();
    Cursor localCursor = null;
    try
    {
      localCursor = localContentResolver.query(paramUri, new String[] { paramString }, null, null, null);
      if ((localCursor.moveToFirst()) && (!localCursor.isNull(0)))
      {
        long l = localCursor.getLong(0);
        return l;
      }
      return paramLong;
    }
    catch (Exception localException)
    {
      Log.w("DocumentFile", "Failed query: " + localException);
      return paramLong;
    }
    finally
    {
      closeQuietly(localCursor);
    }
  }

  private static String queryForString(Context paramContext, Uri paramUri, String paramString1, String paramString2)
  {
    ContentResolver localContentResolver = paramContext.getContentResolver();
    Cursor localCursor = null;
    try
    {
      localCursor = localContentResolver.query(paramUri, new String[] { paramString1 }, null, null, null);
      if ((localCursor.moveToFirst()) && (!localCursor.isNull(0)))
      {
        String str = localCursor.getString(0);
        return str;
      }
      return paramString2;
    }
    catch (Exception localException)
    {
      Log.w("DocumentFile", "Failed query: " + localException);
      return paramString2;
    }
    finally
    {
      closeQuietly(localCursor);
    }
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.provider.DocumentsContractApi19
 * JD-Core Version:    0.6.2
 */
package android.support.v4.content;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import java.io.File;

public class ContextCompat
{
  private static final String DIR_ANDROID = "Android";
  private static final String DIR_CACHE = "cache";
  private static final String DIR_DATA = "data";
  private static final String DIR_FILES = "files";
  private static final String DIR_OBB = "obb";
  private static final String TAG = "ContextCompat";

  private static File buildPath(File paramFile, String[] paramArrayOfString)
  {
    int i = paramArrayOfString.length;
    int j = 0;
    Object localObject1 = paramFile;
    String str;
    Object localObject2;
    if (j < i)
    {
      str = paramArrayOfString[j];
      if (localObject1 == null)
        localObject2 = new File(str);
    }
    while (true)
    {
      j++;
      localObject1 = localObject2;
      break;
      if (str != null)
      {
        localObject2 = new File((File)localObject1, str);
        continue;
        return localObject1;
      }
      else
      {
        localObject2 = localObject1;
      }
    }
  }

  private static File createFilesDir(File paramFile)
  {
    try
    {
      if ((!paramFile.exists()) && (!paramFile.mkdirs()))
      {
        boolean bool = paramFile.exists();
        if (!bool)
          break label31;
      }
      while (true)
      {
        return paramFile;
        label31: Log.w("ContextCompat", "Unable to create files subdir " + paramFile.getPath());
        paramFile = null;
      }
    }
    finally
    {
    }
  }

  public static final Drawable getDrawable(Context paramContext, int paramInt)
  {
    if (Build.VERSION.SDK_INT >= 21)
      return ContextCompatApi21.getDrawable(paramContext, paramInt);
    return paramContext.getResources().getDrawable(paramInt);
  }

  public static File[] getExternalCacheDirs(Context paramContext)
  {
    int i = Build.VERSION.SDK_INT;
    if (i >= 19)
      return ContextCompatKitKat.getExternalCacheDirs(paramContext);
    if (i >= 8);
    File localFile1;
    String[] arrayOfString;
    for (File localFile2 = ContextCompatFroyo.getExternalCacheDir(paramContext); ; localFile2 = buildPath(localFile1, arrayOfString))
    {
      return new File[] { localFile2 };
      localFile1 = Environment.getExternalStorageDirectory();
      arrayOfString = new String[4];
      arrayOfString[0] = "Android";
      arrayOfString[1] = "data";
      arrayOfString[2] = paramContext.getPackageName();
      arrayOfString[3] = "cache";
    }
  }

  public static File[] getExternalFilesDirs(Context paramContext, String paramString)
  {
    int i = Build.VERSION.SDK_INT;
    if (i >= 19)
      return ContextCompatKitKat.getExternalFilesDirs(paramContext, paramString);
    if (i >= 8);
    File localFile1;
    String[] arrayOfString;
    for (File localFile2 = ContextCompatFroyo.getExternalFilesDir(paramContext, paramString); ; localFile2 = buildPath(localFile1, arrayOfString))
    {
      return new File[] { localFile2 };
      localFile1 = Environment.getExternalStorageDirectory();
      arrayOfString = new String[5];
      arrayOfString[0] = "Android";
      arrayOfString[1] = "data";
      arrayOfString[2] = paramContext.getPackageName();
      arrayOfString[3] = "files";
      arrayOfString[4] = paramString;
    }
  }

  public static File[] getObbDirs(Context paramContext)
  {
    int i = Build.VERSION.SDK_INT;
    if (i >= 19)
      return ContextCompatKitKat.getObbDirs(paramContext);
    if (i >= 11);
    File localFile1;
    String[] arrayOfString;
    for (File localFile2 = ContextCompatHoneycomb.getObbDir(paramContext); ; localFile2 = buildPath(localFile1, arrayOfString))
    {
      return new File[] { localFile2 };
      localFile1 = Environment.getExternalStorageDirectory();
      arrayOfString = new String[3];
      arrayOfString[0] = "Android";
      arrayOfString[1] = "obb";
      arrayOfString[2] = paramContext.getPackageName();
    }
  }

  public static boolean startActivities(Context paramContext, Intent[] paramArrayOfIntent)
  {
    return startActivities(paramContext, paramArrayOfIntent, null);
  }

  public static boolean startActivities(Context paramContext, Intent[] paramArrayOfIntent, Bundle paramBundle)
  {
    int i = Build.VERSION.SDK_INT;
    if (i >= 16)
    {
      ContextCompatJellybean.startActivities(paramContext, paramArrayOfIntent, paramBundle);
      return true;
    }
    if (i >= 11)
    {
      ContextCompatHoneycomb.startActivities(paramContext, paramArrayOfIntent);
      return true;
    }
    return false;
  }

  public final File getCodeCacheDir(Context paramContext)
  {
    if (Build.VERSION.SDK_INT >= 21)
      return ContextCompatApi21.getCodeCacheDir(paramContext);
    return createFilesDir(new File(paramContext.getApplicationInfo().dataDir, "code_cache"));
  }

  public final File getNoBackupFilesDir(Context paramContext)
  {
    if (Build.VERSION.SDK_INT >= 21)
      return ContextCompatApi21.getNoBackupFilesDir(paramContext);
    return createFilesDir(new File(paramContext.getApplicationInfo().dataDir, "no_backup"));
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.content.ContextCompat
 * JD-Core Version:    0.6.2
 */
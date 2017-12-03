package android.support.v4.provider;

import android.content.Context;
import android.net.Uri;
import android.os.Build.VERSION;
import java.io.File;

public abstract class DocumentFile
{
  static final String TAG = "DocumentFile";
  private final DocumentFile mParent;

  DocumentFile(DocumentFile paramDocumentFile)
  {
    this.mParent = paramDocumentFile;
  }

  public static DocumentFile fromFile(File paramFile)
  {
    return new RawDocumentFile(null, paramFile);
  }

  public static DocumentFile fromSingleUri(Context paramContext, Uri paramUri)
  {
    if (Build.VERSION.SDK_INT >= 19)
      return new SingleDocumentFile(null, paramContext, paramUri);
    return null;
  }

  public static DocumentFile fromTreeUri(Context paramContext, Uri paramUri)
  {
    if (Build.VERSION.SDK_INT >= 21)
      return new TreeDocumentFile(null, paramContext, DocumentsContractApi21.prepareTreeUri(paramUri));
    return null;
  }

  public static boolean isDocumentUri(Context paramContext, Uri paramUri)
  {
    if (Build.VERSION.SDK_INT >= 19)
      return DocumentsContractApi19.isDocumentUri(paramContext, paramUri);
    return false;
  }

  public abstract boolean canRead();

  public abstract boolean canWrite();

  public abstract DocumentFile createDirectory(String paramString);

  public abstract DocumentFile createFile(String paramString1, String paramString2);

  public abstract boolean delete();

  public abstract boolean exists();

  public DocumentFile findFile(String paramString)
  {
    for (DocumentFile localDocumentFile : listFiles())
      if (paramString.equals(localDocumentFile.getName()))
        return localDocumentFile;
    return null;
  }

  public abstract String getName();

  public DocumentFile getParentFile()
  {
    return this.mParent;
  }

  public abstract String getType();

  public abstract Uri getUri();

  public abstract boolean isDirectory();

  public abstract boolean isFile();

  public abstract long lastModified();

  public abstract long length();

  public abstract DocumentFile[] listFiles();

  public abstract boolean renameTo(String paramString);
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.provider.DocumentFile
 * JD-Core Version:    0.6.2
 */
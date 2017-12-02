package android.support.v4.text;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.Locale;

public class TextUtilsCompat
{
  private static String ARAB_SCRIPT_SUBTAG = "Arab";
  private static String HEBR_SCRIPT_SUBTAG = "Hebr";
  public static final Locale ROOT = new Locale("", "");

  private static int getLayoutDirectionFromFirstChar(Locale paramLocale)
  {
    switch (Character.getDirectionality(paramLocale.getDisplayName(paramLocale).charAt(0)))
    {
    default:
      return 0;
    case 1:
    case 2:
    }
    return 1;
  }

  public static int getLayoutDirectionFromLocale(@Nullable Locale paramLocale)
  {
    if ((paramLocale != null) && (!paramLocale.equals(ROOT)))
    {
      String str = ICUCompat.getScript(ICUCompat.addLikelySubtags(paramLocale.toString()));
      if (str == null)
        return getLayoutDirectionFromFirstChar(paramLocale);
      if ((str.equalsIgnoreCase(ARAB_SCRIPT_SUBTAG)) || (str.equalsIgnoreCase(HEBR_SCRIPT_SUBTAG)))
        return 1;
    }
    return 0;
  }

  @NonNull
  public static String htmlEncode(@NonNull String paramString)
  {
    StringBuilder localStringBuilder = new StringBuilder();
    int i = 0;
    if (i < paramString.length())
    {
      char c = paramString.charAt(i);
      switch (c)
      {
      default:
        localStringBuilder.append(c);
      case '<':
      case '>':
      case '&':
      case '\'':
      case '"':
      }
      while (true)
      {
        i++;
        break;
        localStringBuilder.append("&lt;");
        continue;
        localStringBuilder.append("&gt;");
        continue;
        localStringBuilder.append("&amp;");
        continue;
        localStringBuilder.append("&#39;");
        continue;
        localStringBuilder.append("&quot;");
      }
    }
    return localStringBuilder.toString();
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.text.TextUtilsCompat
 * JD-Core Version:    0.6.2
 */
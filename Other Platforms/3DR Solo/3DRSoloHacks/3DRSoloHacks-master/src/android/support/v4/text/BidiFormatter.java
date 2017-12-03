package android.support.v4.text;

import java.util.Locale;

public final class BidiFormatter
{
  private static final int DEFAULT_FLAGS = 2;
  private static final BidiFormatter DEFAULT_LTR_INSTANCE = new BidiFormatter(false, 2, DEFAULT_TEXT_DIRECTION_HEURISTIC);
  private static final BidiFormatter DEFAULT_RTL_INSTANCE = new BidiFormatter(true, 2, DEFAULT_TEXT_DIRECTION_HEURISTIC);
  private static TextDirectionHeuristicCompat DEFAULT_TEXT_DIRECTION_HEURISTIC = TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR;
  private static final int DIR_LTR = -1;
  private static final int DIR_RTL = 1;
  private static final int DIR_UNKNOWN = 0;
  private static final String EMPTY_STRING = "";
  private static final int FLAG_STEREO_RESET = 2;
  private static final char LRE = '‪';
  private static final char LRM = '‎';
  private static final String LRM_STRING = Character.toString('‎');
  private static final char PDF = '‬';
  private static final char RLE = '‫';
  private static final char RLM = '‏';
  private static final String RLM_STRING = Character.toString('‏');
  private final TextDirectionHeuristicCompat mDefaultTextDirectionHeuristicCompat;
  private final int mFlags;
  private final boolean mIsRtlContext;

  private BidiFormatter(boolean paramBoolean, int paramInt, TextDirectionHeuristicCompat paramTextDirectionHeuristicCompat)
  {
    this.mIsRtlContext = paramBoolean;
    this.mFlags = paramInt;
    this.mDefaultTextDirectionHeuristicCompat = paramTextDirectionHeuristicCompat;
  }

  private static int getEntryDir(String paramString)
  {
    return new DirectionalityEstimator(paramString, false).getEntryDir();
  }

  private static int getExitDir(String paramString)
  {
    return new DirectionalityEstimator(paramString, false).getExitDir();
  }

  public static BidiFormatter getInstance()
  {
    return new Builder().build();
  }

  public static BidiFormatter getInstance(Locale paramLocale)
  {
    return new Builder(paramLocale).build();
  }

  public static BidiFormatter getInstance(boolean paramBoolean)
  {
    return new Builder(paramBoolean).build();
  }

  private static boolean isRtlLocale(Locale paramLocale)
  {
    return TextUtilsCompat.getLayoutDirectionFromLocale(paramLocale) == 1;
  }

  private String markAfter(String paramString, TextDirectionHeuristicCompat paramTextDirectionHeuristicCompat)
  {
    boolean bool = paramTextDirectionHeuristicCompat.isRtl(paramString, 0, paramString.length());
    if ((!this.mIsRtlContext) && ((bool) || (getExitDir(paramString) == 1)))
      return LRM_STRING;
    if ((this.mIsRtlContext) && ((!bool) || (getExitDir(paramString) == -1)))
      return RLM_STRING;
    return "";
  }

  private String markBefore(String paramString, TextDirectionHeuristicCompat paramTextDirectionHeuristicCompat)
  {
    boolean bool = paramTextDirectionHeuristicCompat.isRtl(paramString, 0, paramString.length());
    if ((!this.mIsRtlContext) && ((bool) || (getEntryDir(paramString) == 1)))
      return LRM_STRING;
    if ((this.mIsRtlContext) && ((!bool) || (getEntryDir(paramString) == -1)))
      return RLM_STRING;
    return "";
  }

  public boolean getStereoReset()
  {
    return (0x2 & this.mFlags) != 0;
  }

  public boolean isRtl(String paramString)
  {
    return this.mDefaultTextDirectionHeuristicCompat.isRtl(paramString, 0, paramString.length());
  }

  public boolean isRtlContext()
  {
    return this.mIsRtlContext;
  }

  public String unicodeWrap(String paramString)
  {
    return unicodeWrap(paramString, this.mDefaultTextDirectionHeuristicCompat, true);
  }

  public String unicodeWrap(String paramString, TextDirectionHeuristicCompat paramTextDirectionHeuristicCompat)
  {
    return unicodeWrap(paramString, paramTextDirectionHeuristicCompat, true);
  }

  public String unicodeWrap(String paramString, TextDirectionHeuristicCompat paramTextDirectionHeuristicCompat, boolean paramBoolean)
  {
    boolean bool = paramTextDirectionHeuristicCompat.isRtl(paramString, 0, paramString.length());
    StringBuilder localStringBuilder = new StringBuilder();
    TextDirectionHeuristicCompat localTextDirectionHeuristicCompat2;
    if ((getStereoReset()) && (paramBoolean))
    {
      if (bool)
      {
        localTextDirectionHeuristicCompat2 = TextDirectionHeuristicsCompat.RTL;
        localStringBuilder.append(markBefore(paramString, localTextDirectionHeuristicCompat2));
      }
    }
    else
    {
      if (bool == this.mIsRtlContext)
        break label149;
      if (!bool)
        break label141;
      int i = 8235;
      label76: localStringBuilder.append(i);
      localStringBuilder.append(paramString);
      localStringBuilder.append('‬');
      label100: if (paramBoolean)
        if (!bool)
          break label159;
    }
    label141: label149: label159: for (TextDirectionHeuristicCompat localTextDirectionHeuristicCompat1 = TextDirectionHeuristicsCompat.RTL; ; localTextDirectionHeuristicCompat1 = TextDirectionHeuristicsCompat.LTR)
    {
      localStringBuilder.append(markAfter(paramString, localTextDirectionHeuristicCompat1));
      return localStringBuilder.toString();
      localTextDirectionHeuristicCompat2 = TextDirectionHeuristicsCompat.LTR;
      break;
      int j = 8234;
      break label76;
      localStringBuilder.append(paramString);
      break label100;
    }
  }

  public String unicodeWrap(String paramString, boolean paramBoolean)
  {
    return unicodeWrap(paramString, this.mDefaultTextDirectionHeuristicCompat, paramBoolean);
  }

  public static final class Builder
  {
    private int mFlags;
    private boolean mIsRtlContext;
    private TextDirectionHeuristicCompat mTextDirectionHeuristicCompat;

    public Builder()
    {
      initialize(BidiFormatter.isRtlLocale(Locale.getDefault()));
    }

    public Builder(Locale paramLocale)
    {
      initialize(BidiFormatter.isRtlLocale(paramLocale));
    }

    public Builder(boolean paramBoolean)
    {
      initialize(paramBoolean);
    }

    private static BidiFormatter getDefaultInstanceFromContext(boolean paramBoolean)
    {
      if (paramBoolean)
        return BidiFormatter.DEFAULT_RTL_INSTANCE;
      return BidiFormatter.DEFAULT_LTR_INSTANCE;
    }

    private void initialize(boolean paramBoolean)
    {
      this.mIsRtlContext = paramBoolean;
      this.mTextDirectionHeuristicCompat = BidiFormatter.DEFAULT_TEXT_DIRECTION_HEURISTIC;
      this.mFlags = 2;
    }

    public BidiFormatter build()
    {
      if ((this.mFlags == 2) && (this.mTextDirectionHeuristicCompat == BidiFormatter.DEFAULT_TEXT_DIRECTION_HEURISTIC))
        return getDefaultInstanceFromContext(this.mIsRtlContext);
      return new BidiFormatter(this.mIsRtlContext, this.mFlags, this.mTextDirectionHeuristicCompat, null);
    }

    public Builder setTextDirectionHeuristic(TextDirectionHeuristicCompat paramTextDirectionHeuristicCompat)
    {
      this.mTextDirectionHeuristicCompat = paramTextDirectionHeuristicCompat;
      return this;
    }

    public Builder stereoReset(boolean paramBoolean)
    {
      if (paramBoolean)
      {
        this.mFlags = (0x2 | this.mFlags);
        return this;
      }
      this.mFlags = (0xFFFFFFFD & this.mFlags);
      return this;
    }
  }

  private static class DirectionalityEstimator
  {
    private static final byte[] DIR_TYPE_CACHE = new byte[1792];
    private static final int DIR_TYPE_CACHE_SIZE = 1792;
    private int charIndex;
    private final boolean isHtml;
    private char lastChar;
    private final int length;
    private final String text;

    static
    {
      for (int i = 0; i < 1792; i++)
        DIR_TYPE_CACHE[i] = Character.getDirectionality(i);
    }

    DirectionalityEstimator(String paramString, boolean paramBoolean)
    {
      this.text = paramString;
      this.isHtml = paramBoolean;
      this.length = paramString.length();
    }

    private static byte getCachedDirectionality(char paramChar)
    {
      if (paramChar < '܀')
        return DIR_TYPE_CACHE[paramChar];
      return Character.getDirectionality(paramChar);
    }

    private byte skipEntityBackward()
    {
      int i = this.charIndex;
      do
      {
        if (this.charIndex <= 0)
          break;
        String str = this.text;
        int j = -1 + this.charIndex;
        this.charIndex = j;
        this.lastChar = str.charAt(j);
        if (this.lastChar == '&')
          return 12;
      }
      while (this.lastChar != ';');
      this.charIndex = i;
      this.lastChar = ';';
      return 13;
    }

    private byte skipEntityForward()
    {
      char c;
      do
      {
        if (this.charIndex >= this.length)
          break;
        String str = this.text;
        int i = this.charIndex;
        this.charIndex = (i + 1);
        c = str.charAt(i);
        this.lastChar = c;
      }
      while (c != ';');
      return 12;
    }

    private byte skipTagBackward()
    {
      int i = this.charIndex;
      label147: 
      while (true)
      {
        if (this.charIndex > 0)
        {
          String str1 = this.text;
          int j = -1 + this.charIndex;
          this.charIndex = j;
          this.lastChar = str1.charAt(j);
          if (this.lastChar == '<')
            return 12;
          if (this.lastChar != '>');
        }
        else
        {
          this.charIndex = i;
          this.lastChar = '>';
          return 13;
        }
        if ((this.lastChar == '"') || (this.lastChar == '\''))
        {
          int k = this.lastChar;
          while (true)
          {
            if (this.charIndex <= 0)
              break label147;
            String str2 = this.text;
            int m = -1 + this.charIndex;
            this.charIndex = m;
            char c = str2.charAt(m);
            this.lastChar = c;
            if (c == k)
              break;
          }
        }
      }
    }

    private byte skipTagForward()
    {
      int i = this.charIndex;
      label132: 
      while (this.charIndex < this.length)
      {
        String str1 = this.text;
        int j = this.charIndex;
        this.charIndex = (j + 1);
        this.lastChar = str1.charAt(j);
        if (this.lastChar == '>')
          return 12;
        if ((this.lastChar == '"') || (this.lastChar == '\''))
        {
          int k = this.lastChar;
          while (true)
          {
            if (this.charIndex >= this.length)
              break label132;
            String str2 = this.text;
            int m = this.charIndex;
            this.charIndex = (m + 1);
            char c = str2.charAt(m);
            this.lastChar = c;
            if (c == k)
              break;
          }
        }
      }
      this.charIndex = i;
      this.lastChar = '<';
      return 13;
    }

    byte dirTypeBackward()
    {
      this.lastChar = this.text.charAt(-1 + this.charIndex);
      byte b;
      if (Character.isLowSurrogate(this.lastChar))
      {
        int i = Character.codePointBefore(this.text, this.charIndex);
        this.charIndex -= Character.charCount(i);
        b = Character.getDirectionality(i);
      }
      do
      {
        do
        {
          return b;
          this.charIndex = (-1 + this.charIndex);
          b = getCachedDirectionality(this.lastChar);
        }
        while (!this.isHtml);
        if (this.lastChar == '>')
          return skipTagBackward();
      }
      while (this.lastChar != ';');
      return skipEntityBackward();
    }

    byte dirTypeForward()
    {
      this.lastChar = this.text.charAt(this.charIndex);
      byte b;
      if (Character.isHighSurrogate(this.lastChar))
      {
        int i = Character.codePointAt(this.text, this.charIndex);
        this.charIndex += Character.charCount(i);
        b = Character.getDirectionality(i);
      }
      do
      {
        do
        {
          return b;
          this.charIndex = (1 + this.charIndex);
          b = getCachedDirectionality(this.lastChar);
        }
        while (!this.isHtml);
        if (this.lastChar == '<')
          return skipTagForward();
      }
      while (this.lastChar != '&');
      return skipEntityForward();
    }

    int getEntryDir()
    {
      this.charIndex = 0;
      int i = 0;
      int j = 0;
      int k = 0;
      while (true)
        if ((this.charIndex < this.length) && (k == 0))
          switch (dirTypeForward())
          {
          case 9:
          case 3:
          case 4:
          case 5:
          case 6:
          case 7:
          case 8:
          case 10:
          case 11:
          case 12:
          case 13:
          default:
            k = i;
            break;
          case 14:
          case 15:
            i++;
            j = -1;
            break;
          case 16:
          case 17:
            i++;
            j = 1;
            break;
          case 18:
            i--;
            j = 0;
            break;
          case 0:
            if (i == 0)
              j = -1;
            break;
          case 1:
          case 2:
          }
      do
      {
        return j;
        k = i;
        break;
        if (i == 0)
          return 1;
        k = i;
        break;
        if (k == 0)
          return 0;
      }
      while (j != 0);
      while (this.charIndex > 0)
        switch (dirTypeBackward())
        {
        default:
          break;
        case 14:
        case 15:
          if (k == i)
            return -1;
          i--;
          break;
        case 16:
        case 17:
          if (k == i)
            return 1;
          i--;
          break;
        case 18:
          i++;
        }
      return 0;
    }

    int getExitDir()
    {
      this.charIndex = this.length;
      int i = 0;
      int j = 0;
      while (this.charIndex > 0)
        switch (dirTypeBackward())
        {
        case 9:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
        case 8:
        case 10:
        case 11:
        case 12:
        case 13:
        default:
          if (j == 0)
            j = i;
          break;
        case 0:
          if (i != 0);
        case 14:
        case 15:
          do
          {
            return -1;
            if (j != 0)
              break;
            j = i;
            break;
          }
          while (j == i);
          i--;
          break;
        case 1:
        case 2:
          if (i == 0)
            return 1;
          if (j == 0)
            j = i;
          break;
        case 16:
        case 17:
          if (j == i)
            return 1;
          i--;
          break;
        case 18:
          i++;
        }
      return 0;
    }
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.text.BidiFormatter
 * JD-Core Version:    0.6.2
 */
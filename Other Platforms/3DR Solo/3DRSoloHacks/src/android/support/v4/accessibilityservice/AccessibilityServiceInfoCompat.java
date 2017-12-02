package android.support.v4.accessibilityservice;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.pm.ResolveInfo;
import android.os.Build.VERSION;

public class AccessibilityServiceInfoCompat
{
  public static final int CAPABILITY_CAN_FILTER_KEY_EVENTS = 8;
  public static final int CAPABILITY_CAN_REQUEST_ENHANCED_WEB_ACCESSIBILITY = 4;
  public static final int CAPABILITY_CAN_REQUEST_TOUCH_EXPLORATION = 2;
  public static final int CAPABILITY_CAN_RETRIEVE_WINDOW_CONTENT = 1;
  public static final int DEFAULT = 1;
  public static final int FEEDBACK_ALL_MASK = -1;
  public static final int FEEDBACK_BRAILLE = 32;
  public static final int FLAG_INCLUDE_NOT_IMPORTANT_VIEWS = 2;
  public static final int FLAG_REPORT_VIEW_IDS = 16;
  public static final int FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY = 8;
  public static final int FLAG_REQUEST_FILTER_KEY_EVENTS = 32;
  public static final int FLAG_REQUEST_TOUCH_EXPLORATION_MODE = 4;
  private static final AccessibilityServiceInfoVersionImpl IMPL = new AccessibilityServiceInfoCompat.AccessibilityServiceInfoStubImpl();

  static
  {
    if (Build.VERSION.SDK_INT >= 18)
    {
      IMPL = new AccessibilityServiceInfoCompat.AccessibilityServiceInfoJellyBeanMr2();
      return;
    }
    if (Build.VERSION.SDK_INT >= 14)
    {
      IMPL = new AccessibilityServiceInfoCompat.AccessibilityServiceInfoIcsImpl();
      return;
    }
  }

  public static String capabilityToString(int paramInt)
  {
    switch (paramInt)
    {
    case 3:
    case 5:
    case 6:
    case 7:
    default:
      return "UNKNOWN";
    case 1:
      return "CAPABILITY_CAN_RETRIEVE_WINDOW_CONTENT";
    case 2:
      return "CAPABILITY_CAN_REQUEST_TOUCH_EXPLORATION";
    case 4:
      return "CAPABILITY_CAN_REQUEST_ENHANCED_WEB_ACCESSIBILITY";
    case 8:
    }
    return "CAPABILITY_CAN_FILTER_KEY_EVENTS";
  }

  public static String feedbackTypeToString(int paramInt)
  {
    StringBuilder localStringBuilder = new StringBuilder();
    localStringBuilder.append("[");
    while (paramInt > 0)
    {
      int i = 1 << Integer.numberOfTrailingZeros(paramInt);
      paramInt &= (i ^ 0xFFFFFFFF);
      if (localStringBuilder.length() > 1)
        localStringBuilder.append(", ");
      switch (i)
      {
      default:
        break;
      case 1:
        localStringBuilder.append("FEEDBACK_SPOKEN");
        break;
      case 4:
        localStringBuilder.append("FEEDBACK_AUDIBLE");
        break;
      case 2:
        localStringBuilder.append("FEEDBACK_HAPTIC");
        break;
      case 16:
        localStringBuilder.append("FEEDBACK_GENERIC");
        break;
      case 8:
        localStringBuilder.append("FEEDBACK_VISUAL");
      }
    }
    localStringBuilder.append("]");
    return localStringBuilder.toString();
  }

  public static String flagToString(int paramInt)
  {
    switch (paramInt)
    {
    default:
      return null;
    case 1:
      return "DEFAULT";
    case 2:
      return "FLAG_INCLUDE_NOT_IMPORTANT_VIEWS";
    case 4:
      return "FLAG_REQUEST_TOUCH_EXPLORATION_MODE";
    case 8:
      return "FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY";
    case 16:
      return "FLAG_REPORT_VIEW_IDS";
    case 32:
    }
    return "FLAG_REQUEST_FILTER_KEY_EVENTS";
  }

  public static boolean getCanRetrieveWindowContent(AccessibilityServiceInfo paramAccessibilityServiceInfo)
  {
    return IMPL.getCanRetrieveWindowContent(paramAccessibilityServiceInfo);
  }

  public static int getCapabilities(AccessibilityServiceInfo paramAccessibilityServiceInfo)
  {
    return IMPL.getCapabilities(paramAccessibilityServiceInfo);
  }

  public static String getDescription(AccessibilityServiceInfo paramAccessibilityServiceInfo)
  {
    return IMPL.getDescription(paramAccessibilityServiceInfo);
  }

  public static String getId(AccessibilityServiceInfo paramAccessibilityServiceInfo)
  {
    return IMPL.getId(paramAccessibilityServiceInfo);
  }

  public static ResolveInfo getResolveInfo(AccessibilityServiceInfo paramAccessibilityServiceInfo)
  {
    return IMPL.getResolveInfo(paramAccessibilityServiceInfo);
  }

  public static String getSettingsActivityName(AccessibilityServiceInfo paramAccessibilityServiceInfo)
  {
    return IMPL.getSettingsActivityName(paramAccessibilityServiceInfo);
  }

  static abstract interface AccessibilityServiceInfoVersionImpl
  {
    public abstract boolean getCanRetrieveWindowContent(AccessibilityServiceInfo paramAccessibilityServiceInfo);

    public abstract int getCapabilities(AccessibilityServiceInfo paramAccessibilityServiceInfo);

    public abstract String getDescription(AccessibilityServiceInfo paramAccessibilityServiceInfo);

    public abstract String getId(AccessibilityServiceInfo paramAccessibilityServiceInfo);

    public abstract ResolveInfo getResolveInfo(AccessibilityServiceInfo paramAccessibilityServiceInfo);

    public abstract String getSettingsActivityName(AccessibilityServiceInfo paramAccessibilityServiceInfo);
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.accessibilityservice.AccessibilityServiceInfoCompat
 * JD-Core Version:    0.6.2
 */
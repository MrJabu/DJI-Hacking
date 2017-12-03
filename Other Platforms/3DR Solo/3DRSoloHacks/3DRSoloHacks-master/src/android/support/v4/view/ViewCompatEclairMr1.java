package android.support.v4.view;

import android.view.View;
import java.lang.reflect.Method;

class ViewCompatEclairMr1
{
  public static final String TAG = "ViewCompat";
  private static Method sChildrenDrawingOrderMethod;

  public static boolean isOpaque(View paramView)
  {
    return paramView.isOpaque();
  }

  // ERROR //
  public static void setChildrenDrawingOrderEnabled(android.view.ViewGroup paramViewGroup, boolean paramBoolean)
  {
    // Byte code:
    //   0: getstatic 33	android/support/v4/view/ViewCompatEclairMr1:sChildrenDrawingOrderMethod	Ljava/lang/reflect/Method;
    //   3: ifnonnull +35 -> 38
    //   6: iconst_1
    //   7: anewarray 35	java/lang/Class
    //   10: astore 13
    //   12: aload 13
    //   14: iconst_0
    //   15: getstatic 41	java/lang/Boolean:TYPE	Ljava/lang/Class;
    //   18: aastore
    //   19: ldc 43
    //   21: ldc 44
    //   23: aload 13
    //   25: invokevirtual 48	java/lang/Class:getDeclaredMethod	(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
    //   28: putstatic 33	android/support/v4/view/ViewCompatEclairMr1:sChildrenDrawingOrderMethod	Ljava/lang/reflect/Method;
    //   31: getstatic 33	android/support/v4/view/ViewCompatEclairMr1:sChildrenDrawingOrderMethod	Ljava/lang/reflect/Method;
    //   34: iconst_1
    //   35: invokevirtual 54	java/lang/reflect/Method:setAccessible	(Z)V
    //   38: getstatic 33	android/support/v4/view/ViewCompatEclairMr1:sChildrenDrawingOrderMethod	Ljava/lang/reflect/Method;
    //   41: astore 8
    //   43: iconst_1
    //   44: anewarray 4	java/lang/Object
    //   47: astore 9
    //   49: aload 9
    //   51: iconst_0
    //   52: iload_1
    //   53: invokestatic 58	java/lang/Boolean:valueOf	(Z)Ljava/lang/Boolean;
    //   56: aastore
    //   57: aload 8
    //   59: aload_0
    //   60: aload 9
    //   62: invokevirtual 62	java/lang/reflect/Method:invoke	(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
    //   65: pop
    //   66: return
    //   67: astore 11
    //   69: ldc 8
    //   71: ldc 64
    //   73: aload 11
    //   75: invokestatic 70	android/util/Log:e	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    //   78: pop
    //   79: goto -48 -> 31
    //   82: astore 6
    //   84: ldc 8
    //   86: ldc 72
    //   88: aload 6
    //   90: invokestatic 70	android/util/Log:e	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    //   93: pop
    //   94: return
    //   95: astore 4
    //   97: ldc 8
    //   99: ldc 72
    //   101: aload 4
    //   103: invokestatic 70	android/util/Log:e	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    //   106: pop
    //   107: return
    //   108: astore_2
    //   109: ldc 8
    //   111: ldc 72
    //   113: aload_2
    //   114: invokestatic 70	android/util/Log:e	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    //   117: pop
    //   118: return
    //
    // Exception table:
    //   from	to	target	type
    //   6	31	67	java/lang/NoSuchMethodException
    //   38	66	82	java/lang/IllegalAccessException
    //   38	66	95	java/lang/IllegalArgumentException
    //   38	66	108	java/lang/reflect/InvocationTargetException
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v4.view.ViewCompatEclairMr1
 * JD-Core Version:    0.6.2
 */
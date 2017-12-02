package android.support.v7.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.appcompat.R.attr;
import android.support.v7.appcompat.R.id;
import android.support.v7.appcompat.R.styleable;
import android.support.v7.internal.widget.TintTypedArray;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import java.lang.ref.WeakReference;

class AlertController
{
  private ListAdapter mAdapter;
  private int mAlertDialogLayout;
  private final View.OnClickListener mButtonHandler = new View.OnClickListener()
  {
    public void onClick(View paramAnonymousView)
    {
      Message localMessage;
      if ((paramAnonymousView == AlertController.this.mButtonPositive) && (AlertController.this.mButtonPositiveMessage != null))
        localMessage = Message.obtain(AlertController.this.mButtonPositiveMessage);
      while (true)
      {
        if (localMessage != null)
          localMessage.sendToTarget();
        AlertController.this.mHandler.obtainMessage(1, AlertController.this.mDialog).sendToTarget();
        return;
        if ((paramAnonymousView == AlertController.this.mButtonNegative) && (AlertController.this.mButtonNegativeMessage != null))
          localMessage = Message.obtain(AlertController.this.mButtonNegativeMessage);
        else if ((paramAnonymousView == AlertController.this.mButtonNeutral) && (AlertController.this.mButtonNeutralMessage != null))
          localMessage = Message.obtain(AlertController.this.mButtonNeutralMessage);
        else
          localMessage = null;
      }
    }
  };
  private Button mButtonNegative;
  private Message mButtonNegativeMessage;
  private CharSequence mButtonNegativeText;
  private Button mButtonNeutral;
  private Message mButtonNeutralMessage;
  private CharSequence mButtonNeutralText;
  private int mButtonPanelLayoutHint = 0;
  private int mButtonPanelSideLayout;
  private Button mButtonPositive;
  private Message mButtonPositiveMessage;
  private CharSequence mButtonPositiveText;
  private int mCheckedItem = -1;
  private final Context mContext;
  private View mCustomTitleView;
  private final AppCompatDialog mDialog;
  private Handler mHandler;
  private Drawable mIcon;
  private int mIconId = 0;
  private ImageView mIconView;
  private int mListItemLayout;
  private int mListLayout;
  private ListView mListView;
  private CharSequence mMessage;
  private TextView mMessageView;
  private int mMultiChoiceItemLayout;
  private ScrollView mScrollView;
  private int mSingleChoiceItemLayout;
  private CharSequence mTitle;
  private TextView mTitleView;
  private View mView;
  private int mViewLayoutResId;
  private int mViewSpacingBottom;
  private int mViewSpacingLeft;
  private int mViewSpacingRight;
  private boolean mViewSpacingSpecified = false;
  private int mViewSpacingTop;
  private final Window mWindow;

  public AlertController(Context paramContext, AppCompatDialog paramAppCompatDialog, Window paramWindow)
  {
    this.mContext = paramContext;
    this.mDialog = paramAppCompatDialog;
    this.mWindow = paramWindow;
    this.mHandler = new ButtonHandler(paramAppCompatDialog);
    TypedArray localTypedArray = paramContext.obtainStyledAttributes(null, R.styleable.AlertDialog, R.attr.alertDialogStyle, 0);
    this.mAlertDialogLayout = localTypedArray.getResourceId(R.styleable.AlertDialog_android_layout, 0);
    this.mButtonPanelSideLayout = localTypedArray.getResourceId(R.styleable.AlertDialog_buttonPanelSideLayout, 0);
    this.mListLayout = localTypedArray.getResourceId(R.styleable.AlertDialog_listLayout, 0);
    this.mMultiChoiceItemLayout = localTypedArray.getResourceId(R.styleable.AlertDialog_multiChoiceItemLayout, 0);
    this.mSingleChoiceItemLayout = localTypedArray.getResourceId(R.styleable.AlertDialog_singleChoiceItemLayout, 0);
    this.mListItemLayout = localTypedArray.getResourceId(R.styleable.AlertDialog_listItemLayout, 0);
    localTypedArray.recycle();
  }

  static boolean canTextInput(View paramView)
  {
    if (paramView.onCheckIsTextEditor())
      return true;
    if (!(paramView instanceof ViewGroup))
      return false;
    ViewGroup localViewGroup = (ViewGroup)paramView;
    int i = localViewGroup.getChildCount();
    while (i > 0)
    {
      i--;
      if (canTextInput(localViewGroup.getChildAt(i)))
        return true;
    }
    return false;
  }

  private void centerButton(Button paramButton)
  {
    LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams)paramButton.getLayoutParams();
    localLayoutParams.gravity = 1;
    localLayoutParams.weight = 0.5F;
    paramButton.setLayoutParams(localLayoutParams);
  }

  private int selectContentView()
  {
    if (this.mButtonPanelSideLayout == 0)
      return this.mAlertDialogLayout;
    if (this.mButtonPanelLayoutHint == 1)
      return this.mButtonPanelSideLayout;
    return this.mAlertDialogLayout;
  }

  private boolean setupButtons()
  {
    int i = 0;
    this.mButtonPositive = ((Button)this.mWindow.findViewById(16908313));
    this.mButtonPositive.setOnClickListener(this.mButtonHandler);
    if (TextUtils.isEmpty(this.mButtonPositiveText))
    {
      this.mButtonPositive.setVisibility(8);
      this.mButtonNegative = ((Button)this.mWindow.findViewById(16908314));
      this.mButtonNegative.setOnClickListener(this.mButtonHandler);
      if (!TextUtils.isEmpty(this.mButtonNegativeText))
        break label197;
      this.mButtonNegative.setVisibility(8);
      label95: this.mButtonNeutral = ((Button)this.mWindow.findViewById(16908315));
      this.mButtonNeutral.setOnClickListener(this.mButtonHandler);
      if (!TextUtils.isEmpty(this.mButtonNeutralText))
        break label223;
      this.mButtonNeutral.setVisibility(8);
      label142: if (shouldCenterSingleButton(this.mContext))
      {
        if (i != 1)
          break label249;
        centerButton(this.mButtonPositive);
      }
    }
    while (true)
    {
      if (i == 0)
        break label281;
      return true;
      this.mButtonPositive.setText(this.mButtonPositiveText);
      this.mButtonPositive.setVisibility(0);
      i = 0x0 | 0x1;
      break;
      label197: this.mButtonNegative.setText(this.mButtonNegativeText);
      this.mButtonNegative.setVisibility(0);
      i |= 2;
      break label95;
      label223: this.mButtonNeutral.setText(this.mButtonNeutralText);
      this.mButtonNeutral.setVisibility(0);
      i |= 4;
      break label142;
      label249: if (i == 2)
        centerButton(this.mButtonNegative);
      else if (i == 4)
        centerButton(this.mButtonNeutral);
    }
    label281: return false;
  }

  private void setupContent(ViewGroup paramViewGroup)
  {
    this.mScrollView = ((ScrollView)this.mWindow.findViewById(R.id.scrollView));
    this.mScrollView.setFocusable(false);
    this.mMessageView = ((TextView)this.mWindow.findViewById(16908299));
    if (this.mMessageView == null)
      return;
    if (this.mMessage != null)
    {
      this.mMessageView.setText(this.mMessage);
      return;
    }
    this.mMessageView.setVisibility(8);
    this.mScrollView.removeView(this.mMessageView);
    if (this.mListView != null)
    {
      ViewGroup localViewGroup = (ViewGroup)this.mScrollView.getParent();
      int i = localViewGroup.indexOfChild(this.mScrollView);
      localViewGroup.removeViewAt(i);
      localViewGroup.addView(this.mListView, i, new ViewGroup.LayoutParams(-1, -1));
      return;
    }
    paramViewGroup.setVisibility(8);
  }

  private boolean setupTitle(ViewGroup paramViewGroup)
  {
    if (this.mCustomTitleView != null)
    {
      ViewGroup.LayoutParams localLayoutParams = new ViewGroup.LayoutParams(-1, -2);
      paramViewGroup.addView(this.mCustomTitleView, 0, localLayoutParams);
      this.mWindow.findViewById(R.id.title_template).setVisibility(8);
      return true;
    }
    this.mIconView = ((ImageView)this.mWindow.findViewById(16908294));
    boolean bool = TextUtils.isEmpty(this.mTitle);
    int i = 0;
    if (!bool)
      i = 1;
    if (i != 0)
    {
      this.mTitleView = ((TextView)this.mWindow.findViewById(R.id.alertTitle));
      this.mTitleView.setText(this.mTitle);
      if (this.mIconId != 0)
      {
        this.mIconView.setImageResource(this.mIconId);
        return true;
      }
      if (this.mIcon != null)
      {
        this.mIconView.setImageDrawable(this.mIcon);
        return true;
      }
      this.mTitleView.setPadding(this.mIconView.getPaddingLeft(), this.mIconView.getPaddingTop(), this.mIconView.getPaddingRight(), this.mIconView.getPaddingBottom());
      this.mIconView.setVisibility(8);
      return true;
    }
    this.mWindow.findViewById(R.id.title_template).setVisibility(8);
    this.mIconView.setVisibility(8);
    paramViewGroup.setVisibility(8);
    return false;
  }

  private void setupView()
  {
    setupContent((ViewGroup)this.mWindow.findViewById(R.id.contentPanel));
    boolean bool = setupButtons();
    ViewGroup localViewGroup = (ViewGroup)this.mWindow.findViewById(R.id.topPanel);
    TintTypedArray localTintTypedArray = TintTypedArray.obtainStyledAttributes(this.mContext, null, R.styleable.AlertDialog, R.attr.alertDialogStyle, 0);
    setupTitle(localViewGroup);
    View localView1 = this.mWindow.findViewById(R.id.buttonPanel);
    if (!bool)
    {
      localView1.setVisibility(8);
      View localView3 = this.mWindow.findViewById(R.id.textSpacerNoButtons);
      if (localView3 != null)
        localView3.setVisibility(0);
    }
    FrameLayout localFrameLayout1 = (FrameLayout)this.mWindow.findViewById(R.id.customPanel);
    View localView2;
    int i;
    if (this.mView != null)
    {
      localView2 = this.mView;
      if (localView2 == null)
        break label343;
      i = 1;
      label140: if ((i == 0) || (!canTextInput(localView2)))
        this.mWindow.setFlags(131072, 131072);
      if (i == 0)
        break label349;
      FrameLayout localFrameLayout2 = (FrameLayout)this.mWindow.findViewById(R.id.custom);
      localFrameLayout2.addView(localView2, new ViewGroup.LayoutParams(-1, -1));
      if (this.mViewSpacingSpecified)
        localFrameLayout2.setPadding(this.mViewSpacingLeft, this.mViewSpacingTop, this.mViewSpacingRight, this.mViewSpacingBottom);
      if (this.mListView != null)
        ((LinearLayout.LayoutParams)localFrameLayout1.getLayoutParams()).weight = 0.0F;
    }
    while (true)
    {
      ListView localListView = this.mListView;
      if ((localListView != null) && (this.mAdapter != null))
      {
        localListView.setAdapter(this.mAdapter);
        int j = this.mCheckedItem;
        if (j > -1)
        {
          localListView.setItemChecked(j, true);
          localListView.setSelection(j);
        }
      }
      localTintTypedArray.recycle();
      return;
      if (this.mViewLayoutResId != 0)
      {
        localView2 = LayoutInflater.from(this.mContext).inflate(this.mViewLayoutResId, localFrameLayout1, false);
        break;
      }
      localView2 = null;
      break;
      label343: i = 0;
      break label140;
      label349: localFrameLayout1.setVisibility(8);
    }
  }

  private static boolean shouldCenterSingleButton(Context paramContext)
  {
    TypedValue localTypedValue = new TypedValue();
    paramContext.getTheme().resolveAttribute(R.attr.alertDialogCenterButtons, localTypedValue, true);
    return localTypedValue.data != 0;
  }

  public Button getButton(int paramInt)
  {
    switch (paramInt)
    {
    default:
      return null;
    case -1:
      return this.mButtonPositive;
    case -2:
      return this.mButtonNegative;
    case -3:
    }
    return this.mButtonNeutral;
  }

  public int getIconAttributeResId(int paramInt)
  {
    TypedValue localTypedValue = new TypedValue();
    this.mContext.getTheme().resolveAttribute(paramInt, localTypedValue, true);
    return localTypedValue.resourceId;
  }

  public ListView getListView()
  {
    return this.mListView;
  }

  public void installContent()
  {
    this.mDialog.supportRequestWindowFeature(1);
    int i = selectContentView();
    this.mDialog.setContentView(i);
    setupView();
  }

  public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent)
  {
    return (this.mScrollView != null) && (this.mScrollView.executeKeyEvent(paramKeyEvent));
  }

  public boolean onKeyUp(int paramInt, KeyEvent paramKeyEvent)
  {
    return (this.mScrollView != null) && (this.mScrollView.executeKeyEvent(paramKeyEvent));
  }

  public void setButton(int paramInt, CharSequence paramCharSequence, DialogInterface.OnClickListener paramOnClickListener, Message paramMessage)
  {
    if ((paramMessage == null) && (paramOnClickListener != null))
      paramMessage = this.mHandler.obtainMessage(paramInt, paramOnClickListener);
    switch (paramInt)
    {
    default:
      throw new IllegalArgumentException("Button does not exist");
    case -1:
      this.mButtonPositiveText = paramCharSequence;
      this.mButtonPositiveMessage = paramMessage;
      return;
    case -2:
      this.mButtonNegativeText = paramCharSequence;
      this.mButtonNegativeMessage = paramMessage;
      return;
    case -3:
    }
    this.mButtonNeutralText = paramCharSequence;
    this.mButtonNeutralMessage = paramMessage;
  }

  public void setButtonPanelLayoutHint(int paramInt)
  {
    this.mButtonPanelLayoutHint = paramInt;
  }

  public void setCustomTitle(View paramView)
  {
    this.mCustomTitleView = paramView;
  }

  public void setIcon(int paramInt)
  {
    this.mIcon = null;
    this.mIconId = paramInt;
    if (this.mIconView != null)
    {
      if (paramInt != 0)
        this.mIconView.setImageResource(this.mIconId);
    }
    else
      return;
    this.mIconView.setVisibility(8);
  }

  public void setIcon(Drawable paramDrawable)
  {
    this.mIcon = paramDrawable;
    this.mIconId = 0;
    if (this.mIconView != null)
    {
      if (paramDrawable != null)
        this.mIconView.setImageDrawable(paramDrawable);
    }
    else
      return;
    this.mIconView.setVisibility(8);
  }

  public void setMessage(CharSequence paramCharSequence)
  {
    this.mMessage = paramCharSequence;
    if (this.mMessageView != null)
      this.mMessageView.setText(paramCharSequence);
  }

  public void setTitle(CharSequence paramCharSequence)
  {
    this.mTitle = paramCharSequence;
    if (this.mTitleView != null)
      this.mTitleView.setText(paramCharSequence);
  }

  public void setView(int paramInt)
  {
    this.mView = null;
    this.mViewLayoutResId = paramInt;
    this.mViewSpacingSpecified = false;
  }

  public void setView(View paramView)
  {
    this.mView = paramView;
    this.mViewLayoutResId = 0;
    this.mViewSpacingSpecified = false;
  }

  public void setView(View paramView, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.mView = paramView;
    this.mViewLayoutResId = 0;
    this.mViewSpacingSpecified = true;
    this.mViewSpacingLeft = paramInt1;
    this.mViewSpacingTop = paramInt2;
    this.mViewSpacingRight = paramInt3;
    this.mViewSpacingBottom = paramInt4;
  }

  public static class AlertParams
  {
    public ListAdapter mAdapter;
    public boolean mCancelable;
    public int mCheckedItem = -1;
    public boolean[] mCheckedItems;
    public final Context mContext;
    public Cursor mCursor;
    public View mCustomTitleView;
    public boolean mForceInverseBackground;
    public Drawable mIcon;
    public int mIconAttrId = 0;
    public int mIconId = 0;
    public final LayoutInflater mInflater;
    public String mIsCheckedColumn;
    public boolean mIsMultiChoice;
    public boolean mIsSingleChoice;
    public CharSequence[] mItems;
    public String mLabelColumn;
    public CharSequence mMessage;
    public DialogInterface.OnClickListener mNegativeButtonListener;
    public CharSequence mNegativeButtonText;
    public DialogInterface.OnClickListener mNeutralButtonListener;
    public CharSequence mNeutralButtonText;
    public DialogInterface.OnCancelListener mOnCancelListener;
    public DialogInterface.OnMultiChoiceClickListener mOnCheckboxClickListener;
    public DialogInterface.OnClickListener mOnClickListener;
    public DialogInterface.OnDismissListener mOnDismissListener;
    public AdapterView.OnItemSelectedListener mOnItemSelectedListener;
    public DialogInterface.OnKeyListener mOnKeyListener;
    public OnPrepareListViewListener mOnPrepareListViewListener;
    public DialogInterface.OnClickListener mPositiveButtonListener;
    public CharSequence mPositiveButtonText;
    public boolean mRecycleOnMeasure = true;
    public CharSequence mTitle;
    public View mView;
    public int mViewLayoutResId;
    public int mViewSpacingBottom;
    public int mViewSpacingLeft;
    public int mViewSpacingRight;
    public boolean mViewSpacingSpecified = false;
    public int mViewSpacingTop;

    public AlertParams(Context paramContext)
    {
      this.mContext = paramContext;
      this.mCancelable = true;
      this.mInflater = ((LayoutInflater)paramContext.getSystemService("layout_inflater"));
    }

    private void createListView(final AlertController paramAlertController)
    {
      final ListView localListView = (ListView)this.mInflater.inflate(paramAlertController.mListLayout, null);
      Object localObject;
      if (this.mIsMultiChoice)
        if (this.mCursor == null)
        {
          localObject = new ArrayAdapter(this.mContext, paramAlertController.mMultiChoiceItemLayout, 16908308, this.mItems)
          {
            public View getView(int paramAnonymousInt, View paramAnonymousView, ViewGroup paramAnonymousViewGroup)
            {
              View localView = super.getView(paramAnonymousInt, paramAnonymousView, paramAnonymousViewGroup);
              if ((AlertController.AlertParams.this.mCheckedItems != null) && (AlertController.AlertParams.this.mCheckedItems[paramAnonymousInt] != 0))
                localListView.setItemChecked(paramAnonymousInt, true);
              return localView;
            }
          };
          if (this.mOnPrepareListViewListener != null)
            this.mOnPrepareListViewListener.onPrepareListView(localListView);
          AlertController.access$1202(paramAlertController, (ListAdapter)localObject);
          AlertController.access$1302(paramAlertController, this.mCheckedItem);
          if (this.mOnClickListener == null)
            break label293;
          localListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
          {
            public void onItemClick(AdapterView<?> paramAnonymousAdapterView, View paramAnonymousView, int paramAnonymousInt, long paramAnonymousLong)
            {
              AlertController.AlertParams.this.mOnClickListener.onClick(paramAlertController.mDialog, paramAnonymousInt);
              if (!AlertController.AlertParams.this.mIsSingleChoice)
                paramAlertController.mDialog.dismiss();
            }
          });
          label108: if (this.mOnItemSelectedListener != null)
            localListView.setOnItemSelectedListener(this.mOnItemSelectedListener);
          if (!this.mIsSingleChoice)
            break label317;
          localListView.setChoiceMode(1);
        }
      while (true)
      {
        AlertController.access$1402(paramAlertController, localListView);
        return;
        Context localContext2 = this.mContext;
        Cursor localCursor2 = this.mCursor;
        localObject = new CursorAdapter(localContext2, localCursor2, false)
        {
          private final int mIsCheckedIndex;
          private final int mLabelIndex;

          public void bindView(View paramAnonymousView, Context paramAnonymousContext, Cursor paramAnonymousCursor)
          {
            int i = 1;
            ((CheckedTextView)paramAnonymousView.findViewById(16908308)).setText(paramAnonymousCursor.getString(this.mLabelIndex));
            ListView localListView = localListView;
            int k = paramAnonymousCursor.getPosition();
            if (paramAnonymousCursor.getInt(this.mIsCheckedIndex) == i);
            while (true)
            {
              localListView.setItemChecked(k, i);
              return;
              int j = 0;
            }
          }

          public View newView(Context paramAnonymousContext, Cursor paramAnonymousCursor, ViewGroup paramAnonymousViewGroup)
          {
            return AlertController.AlertParams.this.mInflater.inflate(paramAlertController.mMultiChoiceItemLayout, paramAnonymousViewGroup, false);
          }
        };
        break;
        int i;
        if (this.mIsSingleChoice)
        {
          i = paramAlertController.mSingleChoiceItemLayout;
          label186: if (this.mCursor != null)
            break label240;
          if (this.mAdapter == null)
            break label217;
        }
        label217: for (localObject = this.mAdapter; ; localObject = new AlertController.CheckedItemAdapter(this.mContext, i, 16908308, this.mItems))
        {
          break;
          i = paramAlertController.mListItemLayout;
          break label186;
        }
        label240: Context localContext1 = this.mContext;
        Cursor localCursor1 = this.mCursor;
        String[] arrayOfString = new String[1];
        arrayOfString[0] = this.mLabelColumn;
        localObject = new SimpleCursorAdapter(localContext1, i, localCursor1, arrayOfString, new int[] { 16908308 });
        break;
        label293: if (this.mOnCheckboxClickListener == null)
          break label108;
        localListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
          public void onItemClick(AdapterView<?> paramAnonymousAdapterView, View paramAnonymousView, int paramAnonymousInt, long paramAnonymousLong)
          {
            if (AlertController.AlertParams.this.mCheckedItems != null)
              AlertController.AlertParams.this.mCheckedItems[paramAnonymousInt] = localListView.isItemChecked(paramAnonymousInt);
            AlertController.AlertParams.this.mOnCheckboxClickListener.onClick(paramAlertController.mDialog, paramAnonymousInt, localListView.isItemChecked(paramAnonymousInt));
          }
        });
        break label108;
        label317: if (this.mIsMultiChoice)
          localListView.setChoiceMode(2);
      }
    }

    public void apply(AlertController paramAlertController)
    {
      if (this.mCustomTitleView != null)
      {
        paramAlertController.setCustomTitle(this.mCustomTitleView);
        if (this.mMessage != null)
          paramAlertController.setMessage(this.mMessage);
        if (this.mPositiveButtonText != null)
          paramAlertController.setButton(-1, this.mPositiveButtonText, this.mPositiveButtonListener, null);
        if (this.mNegativeButtonText != null)
          paramAlertController.setButton(-2, this.mNegativeButtonText, this.mNegativeButtonListener, null);
        if (this.mNeutralButtonText != null)
          paramAlertController.setButton(-3, this.mNeutralButtonText, this.mNeutralButtonListener, null);
        if ((this.mItems != null) || (this.mCursor != null) || (this.mAdapter != null))
          createListView(paramAlertController);
        if (this.mView == null)
          break label236;
        if (!this.mViewSpacingSpecified)
          break label227;
        paramAlertController.setView(this.mView, this.mViewSpacingLeft, this.mViewSpacingTop, this.mViewSpacingRight, this.mViewSpacingBottom);
      }
      label227: label236: 
      while (this.mViewLayoutResId == 0)
      {
        return;
        if (this.mTitle != null)
          paramAlertController.setTitle(this.mTitle);
        if (this.mIcon != null)
          paramAlertController.setIcon(this.mIcon);
        if (this.mIconId != 0)
          paramAlertController.setIcon(this.mIconId);
        if (this.mIconAttrId == 0)
          break;
        paramAlertController.setIcon(paramAlertController.getIconAttributeResId(this.mIconAttrId));
        break;
        paramAlertController.setView(this.mView);
        return;
      }
      paramAlertController.setView(this.mViewLayoutResId);
    }

    public static abstract interface OnPrepareListViewListener
    {
      public abstract void onPrepareListView(ListView paramListView);
    }
  }

  private static final class ButtonHandler extends Handler
  {
    private static final int MSG_DISMISS_DIALOG = 1;
    private WeakReference<DialogInterface> mDialog;

    public ButtonHandler(DialogInterface paramDialogInterface)
    {
      this.mDialog = new WeakReference(paramDialogInterface);
    }

    public void handleMessage(Message paramMessage)
    {
      switch (paramMessage.what)
      {
      case 0:
      default:
        return;
      case -3:
      case -2:
      case -1:
        ((DialogInterface.OnClickListener)paramMessage.obj).onClick((DialogInterface)this.mDialog.get(), paramMessage.what);
        return;
      case 1:
      }
      ((DialogInterface)paramMessage.obj).dismiss();
    }
  }

  private static class CheckedItemAdapter extends ArrayAdapter<CharSequence>
  {
    public CheckedItemAdapter(Context paramContext, int paramInt1, int paramInt2, CharSequence[] paramArrayOfCharSequence)
    {
      super(paramInt1, paramInt2, paramArrayOfCharSequence);
    }

    public long getItemId(int paramInt)
    {
      return paramInt;
    }

    public boolean hasStableIds()
    {
      return true;
    }
  }
}

/* Location:           /Users/kfinisterre/Desktop/Solo/3DRSoloHacks/unpacked_apk/classes_dex2jar.jar
 * Qualified Name:     android.support.v7.app.AlertController
 * JD-Core Version:    0.6.2
 */
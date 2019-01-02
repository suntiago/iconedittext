package suntiago.com.iconedit;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by JS on 2016/7/27.
 */
public class IconEditText extends LinearLayout implements View.OnClickListener {
    private EditText mEditText;
    private ImageView mLeftImage;
    private ImageView mRightImage;
    private OnEditTextChangeListener mOnEditTextChangeListener;
    private OnEditTextEditorActioListener editorActioListener;
    private OnEditTextFocusChangeListener focusChangeListener;
    private boolean showRight;
    private Drawable mRightImageD;
    private Drawable mLeftImageD;
    private Drawable mShowImageD;

    //输入表情前的光标位置
    private int mCursorPos;
    //输入表情前EditText中的文本
    private String mInputAfterText;
    //是否重置了EditText的内容
    private boolean mResetText;
    private boolean mEmojiAllow;

    public IconEditText(Context context) {
        this(context, null);
    }

    public IconEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(getContext(), R.layout.icon_edittext, this);
        mLeftImage = (ImageView) findViewById(R.id.left_image);
        mRightImage = (ImageView) findViewById(R.id.right_image);
        mEditText = (EditText) findViewById(R.id.content);
        setAttributions(context, attrs);
        this.setOnClickListener(this);

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!mEmojiAllow) {
                    mCursorPos = mEditText.getSelectionEnd();
                    // 这里用s.toString()而不直接用s是因为如果用s，
                    // 那么，inputAfterText和s在内存中指向的是同一个地址，s改变了，
                    // inputAfterText也就改变了，那么表情过滤就失败了
                    mInputAfterText = s.toString();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mEmojiAllow) {
                    if (count >= 2) {//表情符号的字符长度最小为2
                        if (mCursorPos + count > s.length()) {
                            return;
                        }
                        CharSequence input = s.subSequence(mCursorPos, mCursorPos + count);
                        if (CharacterUtil.containsEmoji(input.toString())) {
                            mResetText = true;
                            //Toast.makeText(mContext, "不支持输入Emoji表情符号", Toast.LENGTH_SHORT).show();
                            //是表情符号就将文本还原为输入表情符号之前的内容
                            mEditText.setText(mInputAfterText);
                            CharSequence text = mEditText.getText();
                            if (text instanceof Spannable) {
                                Spannable spanText = (Spannable) text;
                                Selection.setSelection(spanText, text.length());
                            }
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (null != mRightImageD || null != mShowImageD) {
                    String content = mEditText.getText().toString();
                    if (mEditText.hasFocus() && !TextUtils.isEmpty(content)) {
                        mRightImage.setVisibility(VISIBLE);
                    } else {
                        mRightImage.setVisibility(GONE);
                    }
                }
                if (mOnEditTextChangeListener != null) {
                    mOnEditTextChangeListener.onEditTextChanged();
                }
            }
        });
    }

    /**
     * 设置属性
     *
     * @param context
     * @param attrs
     */
    private void setAttributions(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IconEditText);

        //设置左边图标显示
        boolean showLeft = typedArray.getBoolean(R.styleable.IconEditText_showLeft, true);
        if (showLeft) {
            mLeftImageD = typedArray.getDrawable(R.styleable.IconEditText_leftImageSrc);
            if (mLeftImageD != null) {
                mLeftImage.setImageDrawable(mLeftImageD);
            } else {
                mLeftImage.setVisibility(GONE);
            }
        } else {
            mLeftImage.setVisibility(GONE);
        }

        //设置右边图标显示
        showRight = typedArray.getBoolean(R.styleable.IconEditText_showRight, false);
        mRightImageD = typedArray.getDrawable(R.styleable.IconEditText_rightImageSrc);
        if (showRight) {
            mRightImage.setVisibility(VISIBLE);
        }
        if (mRightImageD != null) {
            mRightImage.setVisibility(VISIBLE);
            changeWithText();
            mRightImage.setImageDrawable(mRightImageD);
        } else {
            mRightImage.setVisibility(GONE);
        }

        mShowImageD = typedArray.getDrawable(R.styleable.IconEditText_showImageSrc);
        if (mShowImageD != null) {
            mRightImage.setVisibility(VISIBLE);
            showWithText();
            mRightImage.setImageDrawable(mShowImageD);
        } else {
            mRightImage.setVisibility(GONE);
        }

        mEmojiAllow = typedArray.getBoolean(R.styleable.IconEditText_emojiallow, false);
        String hint = typedArray.getString(R.styleable.IconEditText_hint);
        if (hint != null) {
            mEditText.setHint(hint);
        }

        int maxLength = typedArray.getInteger(R.styleable.IconEditText_maxLength, 40);
        if (maxLength != 0) {
            mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        }

        int hintColor = typedArray.getColor(R.styleable.IconEditText_hintColor, 0XFF999999);
        int textSize = typedArray.getDimensionPixelSize(R.styleable.IconEditText_textSize, 13);
        int textColor = typedArray.getColor(R.styleable.IconEditText_textColor, 0XFF303030);

        mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        mEditText.setTextColor(textColor);
        mEditText.setHintTextColor(hintColor);
        int leftImageMarginLeft = typedArray.getDimensionPixelSize(R.styleable.IconEditText_leftImageMarginLeft, 0);
        LayoutParams mLeftImageLayoutParams = (LayoutParams) mLeftImage.getLayoutParams();
        mLeftImageLayoutParams.setMargins(leftImageMarginLeft, 0, 0, 0);
        mLeftImage.setLayoutParams(mLeftImageLayoutParams);

        int editTextMarginLeft = typedArray.getDimensionPixelSize(R.styleable.IconEditText_editTextMarginLeft, 0);
        LayoutParams mEditTextLayoutParams = (LayoutParams) mEditText.getLayoutParams();
        mEditTextLayoutParams.setMargins(editTextMarginLeft, 0, 0, 0);
        mEditText.setLayoutParams(mEditTextLayoutParams);

        int inputType = typedArray.getInteger(R.styleable.IconEditText_inputType, 1);
        if (inputType == 128) {
            mEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            mEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            mEditText.setInputType(inputType);
        }

        typedArray.recycle();
    }

    /**
     * 设置右侧图片的监听
     */
    private void showWithText() {
        if (mEditText.hasFocus() && !TextUtils.isEmpty(mEditText.getText().toString())) {
            mRightImage.setVisibility(VISIBLE);
        } else {
            mRightImage.setVisibility(GONE);
        }
        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (mRightImage == null) {
                    return;
                }
                if (hasFocus && !TextUtils.isEmpty(mEditText.getText().toString())) {
                    mRightImage.setVisibility(VISIBLE);
                } else {
                    mRightImage.setVisibility(GONE);
                }
                if (null != focusChangeListener) {
                    focusChangeListener.onFocusChange(hasFocus);
                }
            }
        });
        mEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    if (null != editorActioListener) {
                        editorActioListener.done();
                        mEditText.clearFocus();
                    }
                    return true;
                }
                return false;
            }
        });

        mRightImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEditText != null) {
                    if (mRightImage.isSelected()) {
                        //隐藏密码
                        mRightImage.setSelected(false);
                        mEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    } else {
                        //显示密码
                        mRightImage.setSelected(true);
                        mEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    }
                }
            }
        });
    }

    /**
     * 设置右侧图片的监听
     */
    private void changeWithText() {
        if (mEditText.hasFocus() && !TextUtils.isEmpty(mEditText.getText().toString())) {
            mRightImage.setVisibility(VISIBLE);
        } else {
            mRightImage.setVisibility(GONE);
        }

        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (mRightImage == null) {
                    return;
                }
                if (hasFocus && !TextUtils.isEmpty(mEditText.getText().toString())) {
                    mRightImage.setVisibility(VISIBLE);
                } else {
                    mRightImage.setVisibility(GONE);
                }
                if (null != focusChangeListener) {
                    focusChangeListener.onFocusChange(hasFocus);
                }
            }
        });
        mEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    if (null != editorActioListener) {
                        editorActioListener.done();
                        mEditText.clearFocus();
                    }
                    return true;
                }
                return false;
            }
        });

        mRightImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEditText != null) {
                    mEditText.setText("");
                }
            }
        });
    }

    /**
     * 返回编辑框的内容
     *
     * @return content
     */
    public String getText() {
        String content = mEditText.getText().toString();
        if (content == null || content.trim().equals("")) {
            content = null;
        }
        return content;
    }

    /**
     * 设置编辑框的内容
     *
     * @param text
     */
    public void setText(String text) {
        if (text != null) {
            mEditText.setText(text);
        }
    }

    /**
     * 设置字体的颜色
     *
     * @param colorResourceId
     */
    public void setTextColor(int colorResourceId) {
        if (colorResourceId != 0) {
            mEditText.setTextColor(colorResourceId);
        }
    }

    /**
     * 设置字体的大小
     *
     * @param textSize
     */
    public void setTextSize(float textSize) {
        if (textSize != 0) {
            mEditText.setTextSize(textSize);
        }
    }

    /**
     * 设置输入内容的长度
     *
     * @param maxLength
     */
    public void setMaxLength(int maxLength) {
        if (maxLength != 0) {
            mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        }
    }

    /**
     * 设置左边图片的资源
     *
     * @param resourceId
     */
    public void setLeftImageResource(int resourceId) {
        if (resourceId != 0) {
            mLeftImage.setImageResource(resourceId);
        }
    }

    /**
     * 设置是否显示左边的图片
     *
     * @param showLeft
     */
    public void setShowLeft(boolean showLeft) {
        if (showLeft) {
            mLeftImage.setVisibility(VISIBLE);
        } else {
            mLeftImage.setVisibility(GONE);
        }
    }

    /**
     * 设置右边图片的资源
     *
     * @param resourceId
     */
    public void setRightImageResource(int resourceId) {
        if (resourceId != 0) {
            mRightImage.setImageResource(resourceId);
        }
    }

    /**
     * 设置是否显示左边的图片
     *
     * @param showRight
     */
    public void setShowRight(boolean showRight) {
        if (showRight) {
            mRightImage.setVisibility(VISIBLE);
        } else {
            mRightImage.setVisibility(GONE);
        }
    }

    /**
     * 设置提示语
     *
     * @param hint
     */
    public void sethint(String hint) {
        if (hint != null) {
            mEditText.setHint(hint);
        }
    }

    /**
     * 设置提示语的颜色
     *
     * @param colorResourceId
     */
    public void setHintColor(int colorResourceId) {
        if (colorResourceId != 0) {
            mEditText.setHintTextColor(colorResourceId);
        }
    }

    /**
     * 设置输入框的焦点
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        mEditText.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(mEditText, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 清除输入框的焦点
     */
    public void clearFocus() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

        mEditText.clearFocus();
    }

    public void setImeOptions(int o) {
        mEditText.setImeOptions(o);
    }

    public interface OnEditTextChangeListener {
        void onEditTextChanged();
    }

    public interface OnEditTextEditorActioListener {
        void done();
    }

    public interface OnEditTextFocusChangeListener {
        void onFocusChange(boolean f);
    }

    public void setEditorActioListener(OnEditTextEditorActioListener editorActioListener) {
        this.editorActioListener = editorActioListener;
    }

    public void setFocusChangeListener(OnEditTextFocusChangeListener focusChangeListener) {
        this.focusChangeListener = focusChangeListener;
    }

    /**
     * 设置文字改变后的监听
     *
     * @param onEditTextChangeListener
     */
    public void setOnEditTextChangeListener(OnEditTextChangeListener onEditTextChangeListener) {
        mOnEditTextChangeListener = onEditTextChangeListener;
    }

    public void closeKeybord() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }
}
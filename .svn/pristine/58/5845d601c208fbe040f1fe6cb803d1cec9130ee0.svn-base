package me.wcy.lrcview;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LrcView extends View {
    private List<LrcEntry> mLrcEntryList = new ArrayList<>();
    private TextPaint mPaint = new TextPaint();
    private float mTextSize;
    private float mDividerHeight;
    private long mAnimationDuration;
    private int mNormalColor;
    private int mCurrentColor;
    private String mLabel;
    private float mLrcPadding;
    private ValueAnimator mAnimator;
    private float mAnimateOffset;
    private long mNextTime = 0L;
    private int mCurrentLine = 0;

    public LrcView(Context context) {
        this(context, null);
    }

    public LrcView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LrcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.LrcView);
        mTextSize = ta.getDimension(R.styleable.LrcView_lrcTextSize, sp2px(12));
        mDividerHeight = ta.getDimension(R.styleable.LrcView_lrcDividerHeight, dp2px(16));
        mAnimationDuration = ta.getInt(R.styleable.LrcView_lrcAnimationDuration, 1000);
        mAnimationDuration = mAnimationDuration < 0 ? 1000 : mAnimationDuration;
        mNormalColor = ta.getColor(R.styleable.LrcView_lrcNormalTextColor, 0xFFFFFFFF);
        mCurrentColor = ta.getColor(R.styleable.LrcView_lrcCurrentTextColor, 0xFFFF4081);
        mLabel = ta.getString(R.styleable.LrcView_lrcLabel);
        mLabel = TextUtils.isEmpty(mLabel) ? "No lyrics" : mLabel;
        mLrcPadding = ta.getDimension(R.styleable.LrcView_lrcPadding, 0);
        ta.recycle();

        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mTextSize);
        mPaint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initEntryList();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(0, mAnimateOffset);

        //y cordinate
        float centerY = getHeight() / 2;

        mPaint.setColor(mCurrentColor);

        // no lyrics file
        if (!hasLrc()) {
            @SuppressLint("DrawAllocation")
            StaticLayout staticLayout = new StaticLayout(mLabel, mPaint, (int) getLrcWidth(),
                    Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
            drawText(canvas, staticLayout, centerY - staticLayout.getLineCount() * mTextSize / 2);
            return;
        }

        // draw the line that is playing now
        float currY = centerY - mLrcEntryList.get(mCurrentLine).getTextHeight() / 2;
        drawText(canvas, mLrcEntryList.get(mCurrentLine).getStaticLayout(), currY);

        // draw the previous line
        mPaint.setColor(mNormalColor);
        float upY = currY;
        for (int i = mCurrentLine - 1; i >= 0; i--) {
            upY -= mDividerHeight + mLrcEntryList.get(i).getTextHeight();

            if (mAnimator == null || !mAnimator.isRunning()) {
                // if the song stops and out of screen, stop drawing the lyrics
                if (upY < 0) {
                    break;
                }
            }

            drawText(canvas, mLrcEntryList.get(i).getStaticLayout(), upY);

            // if the song is not over and out of screen, draw the next line of lyrics
            if (upY < 0) {
                break;
            }
        }

        // draw the next line
        float downY = currY + mLrcEntryList.get(mCurrentLine).getTextHeight() + mDividerHeight;
        for (int i = mCurrentLine + 1; i < mLrcEntryList.size(); i++) {
            if (mAnimator == null || !mAnimator.isRunning()) {
                // if the song stops and out of screen, stop drawing the lyrics
                if (downY + mLrcEntryList.get(i).getTextHeight() > getHeight()) {
                    break;
                }
            }

            drawText(canvas, mLrcEntryList.get(i).getStaticLayout(), downY);

            // if the song is not over and out of screen, draw the next line of lyrics
            if (downY + mLrcEntryList.get(i).getTextHeight() > getHeight()) {
                break;
            }

            downY += mLrcEntryList.get(i).getTextHeight() + mDividerHeight;
        }
    }

    private void drawText(Canvas canvas, StaticLayout staticLayout, float y) {
        canvas.save();
        canvas.translate(mLrcPadding, y);
        staticLayout.draw(canvas);
        canvas.restore();
    }

    private float getLrcWidth() {
        return getWidth() - mLrcPadding * 2;
    }

    /**
     * set the message for no lyrics situation
     */
    public void setLabel(String label) {
        mLabel = label;
        postInvalidate();
    }

    /**
     * load lyrics file
     *
     * @param lrcFile
     */
    public void loadLrc(File lrcFile) {
        reset();

        if (lrcFile != null && lrcFile.exists()) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(lrcFile), "utf-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    parseLine(line);
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            initEntryList();
            initNextTime();
        }

        postInvalidate();
    }

    /**
     * load lyrics file
     *
     * @param lrcText
     */
    public void loadLrc(String lrcText) {
        reset();

        if (!TextUtils.isEmpty(lrcText)) {
            String[] array = lrcText.split("\\n");
            for (String line : array) {
                parseLine(line);
            }

            initEntryList();
            initNextTime();
        }

        postInvalidate();
    }

    /**
     * refresh the lyrics
     *
     * @param time for now
     */
    public void updateTime(long time) {
        // prevent from drawing repeatly
        if (time < mNextTime) {
            return;
        }
        for (int i = mCurrentLine; i < mLrcEntryList.size(); i++) {
            if (mLrcEntryList.get(i).getTime() > time) {
                mNextTime = mLrcEntryList.get(i).getTime();
                mCurrentLine = i < 1 ? 0 : i - 1;
                newlineAnimation(i);
                break;
            } else if (i == mLrcEntryList.size() - 1) {
                // last line
                mCurrentLine = mLrcEntryList.size() - 1;
                mNextTime = Long.MAX_VALUE;
                newlineAnimation(i);
                break;
            }
        }
    }

    /**
     * move the lyrics to the certain time
     *
     * @param time
     */
    public void onDrag(long time) {
        for (int i = 0; i < mLrcEntryList.size(); i++) {
            if (mLrcEntryList.get(i).getTime() > time) {
                if (i == 0) {
                    mCurrentLine = i;
                    initNextTime();
                } else {
                    mCurrentLine = i - 1;
                    mNextTime = mLrcEntryList.get(i).getTime();
                }
                newlineAnimation(i);
                break;
            }
        }
    }

    /**
     * if the lyrics file is usable
     *
     * @return true，if it works, otherwise, return false
     */
    public boolean hasLrc() {
        return !mLrcEntryList.isEmpty();
    }

    private void reset() {
        mLrcEntryList.clear();
        mCurrentLine = 0;
        mNextTime = 0L;

        stopAnimation();
    }

    private void initEntryList() {
        if (getWidth() == 0) {
            return;
        }

        for (LrcEntry lrcEntry : mLrcEntryList) {
            lrcEntry.init(mPaint, (int) getLrcWidth());
        }
    }

    private void initNextTime() {
        if (mLrcEntryList.size() > 1) {
            mNextTime = mLrcEntryList.get(1).getTime();
        } else {
            mNextTime = Long.MAX_VALUE;
        }
    }

    /**
     * read a line of lyrics
     *
     * @param line [00:10.61]xxxxx
     */
    private void parseLine(String line) {
        line = line.trim();
        Matcher matcher = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d\\d)\\](.+)").matcher(line);
        if (!matcher.matches()) {
            return;
        }

        long min = Long.parseLong(matcher.group(1));
        long sec = Long.parseLong(matcher.group(2));
        long mil = Long.parseLong(matcher.group(3));
        String text = matcher.group(4);

        long time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil * 10;

        LrcEntry lrcEntry = new LrcEntry(time, text);
        mLrcEntryList.add(lrcEntry);
    }

    /**
     * switch line animation<br>
     * 属性动画只能在主线程使用
     */
    private void newlineAnimation(int index) {
        stopAnimation();

        mAnimator = ValueAnimator.ofFloat(mLrcEntryList.get(index).getTextHeight() + mDividerHeight, 0.0f);
        mAnimator.setDuration(mAnimationDuration * mLrcEntryList.get(index).getStaticLayout().getLineCount());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimateOffset = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mAnimator.start();
    }

    private void stopAnimation() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.end();
        }
    }

    private int dp2px(float dpValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private int sp2px(float spValue) {
        float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}

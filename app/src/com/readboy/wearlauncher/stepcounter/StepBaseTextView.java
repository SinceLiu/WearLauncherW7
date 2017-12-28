package com.readboy.wearlauncher.stepcounter;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by 1 on 2017/5/4.
 */

public class StepBaseTextView extends TextView implements StepController.StepChangeCallback {

    private Context mContext;
    private StepController mStepController;
    protected int mStepCount;

    public StepBaseTextView(Context context) {
        this(context,null);
    }

    public StepBaseTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        mContext = context;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        mStepController = new StepController(mContext);
        mStepController.registerStepAddReceiver();
        mStepController.addStepChangeCallback(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mStepController != null){
            mStepController.unregisterStepAddReceiver();
            mStepController.removeStepChangeCallback(this);
        }
    }

    @Override
    public void onStepChange(int step) {
        mStepCount = step;
    }
}

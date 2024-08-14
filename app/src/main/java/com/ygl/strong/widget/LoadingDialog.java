package com.ygl.strong.widget;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.ygl.strong.R;

public class LoadingDialog extends Dialog{

    private TextView mTv;

    public LoadingDialog(Context context) {
        super(context, R.style.Dialog);
        View view = LayoutInflater.from(context).inflate(R.layout.loading_dialog_layout, null);
        mTv = view.findViewById(R.id.tv);
        setContentView(view);
        setCanceledOnTouchOutside(false);
    }

    @Override
    public void show() {
        super.show();
    }

    public void show(boolean isCancelable) {
        setCancelable(isCancelable);
        show();
    }

    public void show(boolean isCancelable,String text) {
        if (!TextUtils.isEmpty(text)){
            mTv.setVisibility(View.VISIBLE);
            mTv.setText(text);
        }
        show(isCancelable);
    }

    @Override
    public void dismiss() {
        mTv.setVisibility(View.GONE);
        super.dismiss();
    }
}

package com.ygl.strong.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import com.ygl.strong.R;

public class LoadingDialog extends Dialog{

    public LoadingDialog(Context context) {
        super(context, R.style.Dialog);
        View view = LayoutInflater.from(context).inflate(R.layout.loading_dialog_layout, null);
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

    @Override
    public void dismiss() {
        super.dismiss();

    }
}

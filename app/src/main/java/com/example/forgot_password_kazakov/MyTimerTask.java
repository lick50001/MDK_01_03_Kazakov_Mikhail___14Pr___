package com.example.forgot_password_kazakov;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import java.util.TimerTask;

public class MyTimerTask extends TimerTask {

    public int time = 30;
    public Activity activity;
    public TextView tvText, tvSendMail;

    public MyTimerTask(Activity activity, TextView tvText, TextView tvSendMail) {
        this.activity = activity;
        this.tvText = tvText;
        this.tvSendMail = tvSendMail;
    }

    @Override
    public void run() {
        time--;

        if (time <= 0) {
            this.cancel();
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String second = time > 9 ? String.valueOf(time) : "0" + time;
                tvText.setText("00:" + second);

                if (time == 0) {
                    tvText.setVisibility(View.GONE);
                    tvSendMail.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
package com.example.forgot_password_kazakov;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Verification extends AppCompatActivity {

    private static final String TAG = "Verification";

    private List<EditText> otpFields = new ArrayList<>();

    private TextView tvTimer, tvSendMail;

    private int currentFieldIndex = 0;

    private String serverCode;

    private SendCommon sendCommon;

    private CountDownTimer countDownTimer;

    private Context context;

    private EditText tbUserEmail;

    private Drawable backgroundRed, backgroundNormal;

    private final CallbackResponse callbackResponseError = new CallbackResponse() {
        @Override
        public void returner(String response) {
            runOnUiThread(() -> {
                Toast.makeText(context, "Ошибка сервера", Toast.LENGTH_SHORT).show();
                // Пересоздаём объект SendCommon
                if (sendCommon != null) {
                    sendCommon = new SendCommon(tbUserEmail, callbackResponseCode, callbackResponseError);
                }
            });
        }
    };

    private final CallbackResponse callbackResponseCode = new CallbackResponse() {
        @Override
        public void returner(String response) {
            runOnUiThread(() -> {
                Toast.makeText(context, "Новый код отправлен!", Toast.LENGTH_SHORT).show();
                serverCode = response;
                for (EditText field : otpFields) {
                    field.setText("");
                }
                if (!otpFields.isEmpty()) {
                    otpFields.get(0).requestFocus();
                }
                highlightFields(false);
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        context = this;

        backgroundRed = ContextCompat.getDrawable(this, R.drawable.edittext_backround_red);
        backgroundNormal = ContextCompat.getDrawable(this, R.drawable.edittext_backround);

        tvTimer = findViewById(R.id.timer);
        tvSendMail = findViewById(R.id.send_mail);
        tbUserEmail = findViewById(R.id.user_email);

        otpFields.add(findViewById(R.id.number1));
        otpFields.add(findViewById(R.id.number2));
        otpFields.add(findViewById(R.id.number3));
        otpFields.add(findViewById(R.id.number4));
        otpFields.add(findViewById(R.id.number5));
        otpFields.add(findViewById(R.id.number6));

        setupOtpFieldListeners();

        Bundle args = getIntent().getExtras();
        if (args != null) {
            serverCode = args.getString("Code", "");
            String email = args.getString("Email", "");
            tbUserEmail.setText(email);
        }

        startTimer();

        sendCommon = new SendCommon(tbUserEmail, callbackResponseCode, callbackResponseError);
    }

    private void setupOtpFieldListeners() {
        for (int i = 0; i < otpFields.size(); i++) {
            final int index = i;
            EditText field = otpFields.get(i);

            field.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable.length() > 0) {
                        if (index < otpFields.size() - 1) {
                            otpFields.get(index + 1).requestFocus();
                        }
                    } else {
                        if (index > 0) {
                            otpFields.get(index - 1).requestFocus();
                        }
                    }

                    checkCode();
                }
            });

            field.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL &&
                        field.getText().length() == 0 && index > 0) {
                    otpFields.get(index - 1).requestFocus();
                    return true;
                }
                return false;
            });
        }
    }

    private void checkCode() {
        StringBuilder enteredCode = new StringBuilder();
        for (EditText field : otpFields) {
            enteredCode.append(field.getText().toString());
        }

        String userCode = enteredCode.toString();

        if (userCode.length() == 6) {
            if (serverCode != null && userCode.equals(serverCode)) {
                showSuccessDialog();
            } else if (serverCode != null) {
                highlightFields(true);
                Toast.makeText(context, "Неверный код", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void highlightFields(boolean isError) {
        Drawable bg = isError ? backgroundRed : backgroundNormal;
        for (EditText field : otpFields) {
            field.setBackground(bg);
        }
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Авторизация")
                .setMessage("Успешное подтверждение OTP кода!")
                .setPositiveButton("OK", (dialog, which) -> {
                    finishAffinity();
                })
                .setCancelable(false)
                .show();
    }

    private void startTimer() {
        tvTimer.setVisibility(View.VISIBLE);
        tvSendMail.setVisibility(View.GONE);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(30000, 1000) { // 30 сек, шаг 1 сек
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimer.setText(String.format("00:%02d", seconds));
            }

            @Override
            public void onFinish() {
                tvTimer.setVisibility(View.GONE);
                tvSendMail.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    public void OnSendMail(View view) {
        startTimer();

        if (sendCommon != null && sendCommon.getStatus() != android.os.AsyncTask.Status.RUNNING) {
            Log.d(TAG, "Повторная отправка кода на email: " + tbUserEmail.getText().toString());
            sendCommon.execute();
        } else {
            Log.w(TAG, "Запрос уже выполняется или sendCommon null.");
        }
    }

    public void OnBack(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (sendCommon != null && sendCommon.getStatus() == android.os.AsyncTask.Status.RUNNING) {
            sendCommon.cancel(true);
        }
        Log.d(TAG, "Активность уничтожена, ресурсы очищены.");
    }
}
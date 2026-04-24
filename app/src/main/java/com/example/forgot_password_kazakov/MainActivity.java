package com.example.forgot_password_kazakov;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private EditText tbUserEmail;
    private Drawable backgroundRed, backgroundNormal;
    private Context context;
    private String code;

    private final CallbackResponse callbackResponseError = new CallbackResponse() {
        @Override
        public void returner(String response) {
            runOnUiThread(() -> {
                Toast.makeText(context, "Ошибка сервера", Toast.LENGTH_SHORT).show();

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
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                ConstraintLayout customView = (ConstraintLayout) getLayoutInflater().inflate(R.layout.check_email, null);
                builder.setView(customView);
                builder.setOnCancelListener(alertDialogCancelListener);

                AlertDialog dialog = builder.create();
                dialog.show();

                code = response;
                Log.d(TAG, "Получен код подтверждения: " + code);
            });
        }
    };

    private SendCommon sendCommon;

    private DialogInterface.OnCancelListener alertDialogCancelListener = dialogInterface -> {
        Intent verificationIntent = new Intent(context, Verification.class);
        verificationIntent.putExtra("Code", code);
        verificationIntent.putExtra("Email", tbUserEmail.getText().toString());
        startActivity(verificationIntent);
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        backgroundRed = ContextCompat.getDrawable(this, R.drawable.edittext_backround_red);
        backgroundNormal = ContextCompat.getDrawable(this, R.drawable.edittext_backround);

        tbUserEmail = findViewById(R.id.user_email);

        sendCommon = new SendCommon(tbUserEmail, callbackResponseCode, callbackResponseError);
    }

    public boolean isValid(String value) {
        if (value == null || value.isEmpty()) return false;
        Pattern pattern = Pattern.compile("^\\w{2,20}@\\w{2,10}\\.\\w{2,4}$");
        return pattern.matcher(value).matches();
    }


    public void SendMessage(View view) {
        String userEmail = tbUserEmail.getText().toString().trim();

        if (!isValid(userEmail)) {
            if (backgroundRed != null) tbUserEmail.setBackground(backgroundRed);
            Toast.makeText(context, "Не верно введён Email.", Toast.LENGTH_SHORT).show();
        } else {

            if (backgroundNormal != null) tbUserEmail.setBackground(backgroundNormal);

            if (sendCommon != null && sendCommon.getStatus() != android.os.AsyncTask.Status.RUNNING) {
                Log.d(TAG, "Запуск отправки кода на email: " + userEmail);
                sendCommon.execute();
            } else {
                Log.w(TAG, "Запрос уже выполняется или sendCommon null.");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sendCommon != null && sendCommon.getStatus() == android.os.AsyncTask.Status.RUNNING) {
            sendCommon.cancel(true);
            Log.d(TAG, "AsyncTask отменён при уничтожении активности.");
        }
    }

    public void OnBack(View view) {
        finish();
    }
}
package com.example.forgot_password_kazakov;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Timer;

public class Verification extends AppCompatActivity {

    public ArrayList<EditText> BthNumbers = new ArrayList<>();
    public TextView tvText, tvSendMail;
    public Integer SelectNumber = 0;
    public String Code;
    public SendCommon SendCommon;
    public MyTimerTask TimerTask;
    public Context context;
    public Timer timer = new Timer();
    public EditText tbUserEmail;
    public Drawable BackgroundRed, Background;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        context = this;

        tvText = findViewById(R.id.timer);
        tvSendMail = findViewById(R.id.send_mail);
        tbUserEmail = findViewById(R.id.user_email);

        BthNumbers.add(findViewById(R.id.number1));
        BthNumbers.add(findViewById(R.id.number2));
        BthNumbers.add(findViewById(R.id.number3));
        BthNumbers.add(findViewById(R.id.number4));
        BthNumbers.add(findViewById(R.id.number5));
        BthNumbers.add(findViewById(R.id.number6));

        for (EditText BthNumber : BthNumbers)
            BthNumber.addTextChangedListener(TextChangedListener);

        TimerTask = new MyTimerTask(this, tvText, tvSendMail);
        timer.schedule(TimerTask, 0, 1000);

        SendCommon = new SendCommon(tbUserEmail, CallbackResponseCode, CallbackResponseError);

        Bundle arguments = getIntent().getExtras();
        Code = arguments.get("Code").toString();
        tbUserEmail.setText(arguments.get("Email").toString());
        BackgroundRed = ContextCompat.getDrawable(this, R.drawable.edittext_backround_red);
        Background = ContextCompat.getDrawable(this, R.drawable.edittext_backround);
    }

    TextWatcher TextChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable.length() > 0) {
                if (SelectNumber == BthNumbers.size() - 1) {
                    Log.d("Test", "MAX");
                } else {
                    SelectNumber++;
                    BthNumbers.get(SelectNumber).requestFocus();
                }
            }
            CheckCode();
        }
    };

    public void CheckCode() {
        String UserCode = "";
        for (EditText BthNumber : BthNumbers)
            UserCode += String.valueOf(BthNumber.getText());
        if (UserCode.equals(Code)) {
            for (EditText BthNumber : BthNumbers)
                BthNumber.setBackground(Background);
            AlertDialog.Builder AlertDialogBuilder = new AlertDialog.Builder(this);
            AlertDialogBuilder.setTitle("Авторизация");
            AlertDialogBuilder.setMessage("Успешное подтверждение OTP кода");
            AlertDialog AlertDialog = AlertDialogBuilder.create();
            AlertDialog.show();
        } else if (UserCode.length() == 6) {
            for (EditText BthNumber : BthNumbers)
                BthNumber.setBackground(BackgroundRed);
        }
    }

    public void SendCode(View view) {
        TimerTask = new MyTimerTask(this, tvText, tvSendMail);
        timer.schedule(TimerTask, 0, 1000);

        tvText.setVisibility(View.VISIBLE);
        tvSendMail.setVisibility(View.GONE);

        if (SendCommon.getStatus() != AsyncTask.Status.RUNNING)
            SendCommon.execute();
    }

    CallbackResponse CallbackResponseError = new CallbackResponse() {
        @Override
        public void returner(String Response) {
            Toast.makeText(context, "Ошибка сервера", Toast.LENGTH_SHORT).show();

            SendCommon = new SendCommon(tbUserEmail, CallbackResponseCode, CallbackResponseError);
        }
    };

    CallbackResponse CallbackResponseCode = new CallbackResponse() {
        @Override
        public void returner(String Response) {
            Toast.makeText(context, "Код успешно отправлен", Toast.LENGTH_SHORT).show();
            Code = Response;
        }
    };

    public void OnBack(View view) {
        finish();
    }
}
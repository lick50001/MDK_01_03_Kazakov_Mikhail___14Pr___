package com.example.forgot_password_kazakov;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    public EditText tbUserEmail;
    public Drawable BackgroundRed, Background;
    public Context Context;
    public SendCommon SendCommon;
    public String Code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tbUserEmail = findViewById(R.id.user_email);
        this.Context = this;

        // Инициализация SendCommon с callback
        SendCommon = new SendCommon(tbUserEmail,
                code -> {
                    // Успешный ответ от сервера
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Код отправлен: " + code, Toast.LENGTH_SHORT).show();
                    });
                },
                error -> {
                    // Ошибка
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Ошибка: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
        );
    }

    public Boolean IsValid(String Value) {
        Pattern sPattern = Pattern.compile( "^\\w{2,20}@\\w{2,10}\\.\\w{2,4}$");
        return sPattern.matcher(Value).matches();
    }

    public void SendMessage(View view) {
        String UserEmail = tbUserEmail.getText().toString();
        if (!IsValid(UserEmail)) {
            tbUserEmail.setBackgroundColor(Color.RED);
            Toast.makeText(this, "Не верно введён Email.", Toast.LENGTH_SHORT).show();
        } else {
            tbUserEmail.setBackgroundColor(Color.GREEN);
            if (SendCommon != null && SendCommon.getStatus() != AsyncTask.Status.RUNNING) {
                SendCommon.execute();
            }
        }
    }

    DialogInterface.OnCancelListener AlertDialogCancelListener = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialogInterface) {
            Intent Verification = new Intent(Context, Verification.class);
            Verification.putExtra( "Code", Code);
            Verification.putExtra( "Email", tbUserEmail.getText());
            startActivity(Verification);
        }
    };

    CallbackResponse CallbackResponseError = new CallbackResponse() {
        @Override
        public void returner(String Response) {
            Toast.makeText(Context,  "Ошибка сервера", Toast.LENGTH_SHORT).show();
            SendCommon = new SendCommon(tbUserEmail, CallbackResponseCode, CallbackResponseError);
        }
    };

    CallbackResponse CallbackResponseCode = new CallbackResponse() {
        @Override
        public void returner(String Response) {
            Code = Response;

            AlertDialog.Builder AlertDialogBuilder = new AlertDialog.Builder(Context);
            ConstraintLayout View = (ConstraintLayout) getLayoutInflater().inflate(R.layout.check_email, null);
            AlertDialogBuilder.setView(View);
            AlertDialogBuilder.setOnCancelListener(AlertDialogCancelListener);
            AlertDialog Dialog = AlertDialogBuilder.create();
            Dialog.show();
        }
    };;

    public void OnBack(View view) {
        onBackPressed(); // или finish();
    }
}
package com.example.forgot_password_kazakov;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        BackgroundRed = ContextCompat.getDrawable(this, R.drawable.edittext_backround_red);
        Background = ContextCompat.getDrawable(this, R.drawable.edittext_backround);
        tbUserEmail = findViewById(R.id.user_email);
        SendCommon = new SendCommon(tbUserEmail, CallbackResponseCode, CallbackResponseError);
        Context = this;

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public Boolean IsValid(String Value) {
        Pattern sPattern = Pattern.compile("\\w{1,20}.*@\\w{1,10}\\.\\w{1,4}$");
        return sPattern.matcher(Value).matches();
    }

    public void SendMessage(View view) {
        String UserEmail = String.valueOf(tbUserEmail.getText());
        if (!IsValid(UserEmail)) {
            tbUserEmail.setBackground(BackgroundRed);
            Toast.makeText(this, "Не верно введён Email", Toast.LENGTH_SHORT).show();
        } else {
            tbUserEmail.setBackground(Background);
            if (SendCommon.getStatus() != AsyncTask.Status.RUNNING)
                SendCommon.execute();
        }
    }

    DialogInterface.OnCancelListener AlterDialogCancelListener = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            Intent Verification = new Intent(Context, Verification.class);
            Verification.putExtra("Code", Code);
            Verification.putExtra("Email", tbUserEmail.getText());
            startActivity(Verification);
        }
    };

    CallbackResponse CallbackResponseError = new CallbackResponse() {
        @Override
        public void returner(String Response) {
            Toast.makeText(Context, "Ошибка запроса", Toast.LENGTH_SHORT).show();
            SendCommon = new SendCommon(tbUserEmail, CallbackResponseCode, CallbackResponseError);
        }
    };

    CallbackResponse CallbackResponseCode = new CallbackResponse() {
        @Override
        public void returner(String Response) {
            Code = Response;

            AlertDialog.Builder AlertDialogBuilder = new AlertDialog.Builder(Context);
            ConstraintLayout View = (ConstraintLayout)getLayoutInflater().inflate(R.layout.check_email, null);
            AlertDialogBuilder.setView(View);
            AlertDialogBuilder.setOnCancelListener(AlterDialogCancelListener);
            AlertDialog Dialog = AlertDialogBuilder.create();
            Dialog.show();
        }
    };
}
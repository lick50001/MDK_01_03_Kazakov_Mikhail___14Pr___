package com.example.forgot_password_kazakov;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import org.jsoup.Jsoup;
import org.w3c.dom.Document;

import java.io.IOException;

public class SendCommon extends AsyncTask<Void, Void, Void> {
    public String Url = "http://192.168.0.109:5000/api/CommonController/Send", Code;
    public EditText tbEmail;
    CallbackResponse CallbackResponse, CallbackError;
    public SendCommon(EditText tbEmail, CallbackResponse callbackResponse, CallbackResponse callbackError){
        this.tbEmail = tbEmail;
        this.CallbackResponse = callbackResponse;
        this.CallbackError = callbackError;
    }

    @Override
    protected Void doInBackground(Void... Voids){
        try {
            String email = tbEmail.getText().toString();
            if (email.isEmpty()) {
                Log.e("Errors", "Email is empty");
                return null;
            }

            String responseBody = Jsoup.connect(Url + "?Email=" + email)
                    .ignoreContentType(true)
                    .timeout(10000) // Добавьте таймаут
                    .execute()
                    .body();
            Code = responseBody;
            Log.d("SendCommon", "Response: " + responseBody);
        } catch (IOException e){
            Log.e("Errors", e.getMessage());
            Code = null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid){
        super.onPostExecute(aVoid);
        if (Code == null)CallbackError.returner("Error");
        else CallbackResponse.returner(Code);
    }
}

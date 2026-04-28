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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.forgot_password_kazakov.MyTimerTask;
import com.example.forgot_password_kazakov.R;

import java.util.ArrayList;
import java.util.Timer;

public class Verification extends AppCompatActivity {

    public ArrayList<EditText> BthNumbers = new ArrayList<>(); // список кнопок на слое
    public TextView tvText, tvSendMail; // Текстовое поле таймера, и кнопки отправить код
    public Integer SelectNumber = 0; // Переменная для переключения выбранного поля
    public String Code; // Код полученный от сервера
    public SendCommon SendCommon; // объект выполняющие запрос к серверу
    public MyTimerTask TimerTask; // объект таймера
    public Context Context; // ссылка на контекст активности
    public Timer Timer = new Timer(); // таймер, выполняющие отсчёт
    public EditText tbUserEmail; // Текстовое поле с почтой пользователя
    public Drawable BackgroundRed, Background; // Ресурсы фона текстового поля

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        Context = this; // Запоминаем контекст активности

        tvText = findViewById(R.id.timer); // Получаем поле таймера на слое
        tvSendMail = findViewById(R.id.send_mail); // Получаем поле кнопки отправить на слое
        tbUserEmail = findViewById(R.id.user_email); // Получаем поле почты на слое

        BthNumbers.add(findViewById(R.id.number1)); // получаем кнопку №1
        BthNumbers.add(findViewById(R.id.number2)); // получаем кнопку №2
        BthNumbers.add(findViewById(R.id.number3)); // получаем кнопку №3
        BthNumbers.add(findViewById(R.id.number4)); // получаем кнопку №4
        BthNumbers.add(findViewById(R.id.number5)); // получаем кнопку №5
        BthNumbers.add(findViewById(R.id.number6)); // получаем кнопку №6

        for (EditText BthNumber : BthNumbers) // перебираем кнопки
            BthNumber.addTextChangedListener(TextChangedListener); // Назначаем событие на ввод текста

        TimerTask = new MyTimerTask( this, tvText, tvSendMail); // Создаём объект таймера, и передаём активность и два поля
        Timer.schedule(TimerTask,  0,  1000); // Запускаем таймер, с периодом в 1 секунду

        SendCommon = new SendCommon(tbUserEmail, CallbackResponseCode, CallbackResponseError); // Инициализируем объект запроса

        Bundle arguments = getIntent().getExtras(); // получаем данные, переданные на активность
        Code = arguments.get("Code").toString(); // получаем код
        tbUserEmail.setText(arguments.get("Email").toString()); // в поле почты, указываем почту
        // Получаем ресурсы фона для текстового поля
        BackgroundRed = ContextCompat.getDrawable( this, R.drawable.edittext_backround_red);
        Background = ContextCompat.getDrawable( this, R.drawable.edittext_backround);
    }

    // Событие изменения текста
    TextWatcher TextChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            if(editable.length() > 0) { // если количество введённых символов больше 0
                if(SelectNumber == BthNumbers.size() - 1) { // Если не последний символ
                    Log.d("Test", "MAX"); // Выводим уведомление о том что достигнуто максимальное количество
                } else {
                    SelectNumber++; // Увеличиваем счётчик
                    BthNumbers.get(SelectNumber).requestFocus(); // Переключаем фокус на следующее поле
                }
            }
            CheckCode(); // проверяем введённый код
        }
    };

    // Проверка введённого кода
    public void CheckCode() {
        String UserCode = ""; // введённый код
        for (EditText BthNumber : BthNumbers) // Перебираем кнопки
            UserCode += String.valueOf(BthNumber.getText()); // добавляем введённый пользователем символ в введённый код

        if(UserCode.equals(Code)) { // Проверяем, если код соответствует
            for (EditText BthNumber : BthNumbers) // перебираем кнопки
                BthNumber.setBackground(Background); // меняем цвет, без выделения красным
            AlertDialog.Builder AlertDialogBuilder = new AlertDialog.Builder( this); // Создаём уведомление
            AlertDialogBuilder.setTitle("Авторизация"); // Указываем заголовок
            AlertDialogBuilder.setMessage("Успешное подтверждение OTP кода"); // Указываем сообщение
            AlertDialog AlertDialog = AlertDialogBuilder.create(); // Создаём диалог
            AlertDialog.show(); // отображаем пользователю
        } else if(UserCode.length() == 6) { // если код не совпадает, и длина кода 6 символов
            for (EditText BthNumber : BthNumbers) // Перебираем кнопки
                BthNumber.setBackground(BackgroundRed); // меняем цвет, с красным выделением
        }
    }

    // Отправка сообщения
    public void SendCode(View view) {
        TimerTask = new MyTimerTask( this, tvText, tvSendMail); // Создаём объект таймера, и передаём активность и два поля
        Timer.schedule(TimerTask,  0,  1000); // Запускаем таймер, с периодом в 1 секунду

        tvText.setVisibility(View.VISIBLE); // показываем текст с екундами
        tvSendMail.setVisibility(View.GONE); // скрываем текст с кнопкой отправить

        if(SendCommon.getStatus() != AsyncTask.Status.RUNNING) // Если процесс запроса на сервер не запущен
            SendCommon.execute(); // Запускаем процесс запроса
    }

    // Обработчик события, если запрос не удался
    CallbackResponse CallbackResponseError = new CallbackResponse() {
        @Override
        public void returner(String Response) {
            Toast.makeText(Context,  "Ошибка сервера", Toast.LENGTH_SHORT).show(); // Выводим сообщение об ошибке
            // Инициализируем объект запроса заново
            SendCommon = new SendCommon(tbUserEmail, CallbackResponseCode, CallbackResponseError);
        }
    };

    // Обработчик события, если запрос удался
    CallbackResponse CallbackResponseCode = new CallbackResponse() {
        @Override
        public void returner(String Response) {
            Toast.makeText(Context,  "Код успешно отправлен", Toast.LENGTH_SHORT).show(); // отображаем сообщение
            Code = Response; // Запоминаем код
        }
    };

    // Обработчик события нажатия на кнопку
    public void OnBack(View view) {
        finish(); // закрытие активности
    }
}
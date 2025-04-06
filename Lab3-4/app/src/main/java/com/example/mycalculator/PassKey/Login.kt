package com.example.mycalculator.PassKey

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mycalculator.CalculatorActivity
import com.example.mycalculator.MainActivity
import com.example.mycalculator.R
import com.example.mycalculator.PassKey.SetupPassKeyActivity

// Класс для экрана входа
class LoginActivity : AppCompatActivity() {

    // Объявляем переменные для UI элементов
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnForgotPassword: Button
    private lateinit var btnReRegister: Button

    // Метод, вызываемый при создании активности
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)  // Устанавливаем разметку активности

        // Инициализируем элементы интерфейса
        etPassword = findViewById(R.id.etPassKey)
        btnLogin = findViewById(R.id.btnLogin)
        btnForgotPassword = findViewById(R.id.btnResetPassKey)
        btnReRegister = findViewById(R.id.btnReRegister)

        checkFirstLaunch()  // Проверяем, является ли это первым запуском приложения

        // Устанавливаем обработчик клика для кнопки входа
        btnLogin.setOnClickListener {
            validateAndLogin()  // Валидация и вход
        }

        // Устанавливаем обработчик клика для кнопки восстановления пароля
        btnForgotPassword.setOnClickListener {
            showRecoveryDialog()  // Показываем диалог для восстановления пароля
        }

        // Устанавливаем обработчик клика для кнопки повторной регистрации
        btnReRegister.setOnClickListener {
            clearUserData()  // Очищаем данные пользователя
            startRegistration()  // Начинаем процесс регистрации
        }
    }

    // Метод для проверки первого запуска приложения
    private fun checkFirstLaunch() {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)  // Получаем настройки приложения
        if (!prefs.contains("password")) {  // Проверяем, установлен ли пароль
            startRegistration()  // Если нет, начинаем регистрацию
            finish()  // Закрываем текущую активность
        }
    }

    // Метод для валидации введенного пароля и входа в систему
    private fun validateAndLogin() {
        val enteredPassword = etPassword.text.toString()  // Получаем введенный пароль
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val storedPassword = prefs.getString("password", null)  // Получаем сохраненный пароль

        // Сравниваем введенный и сохраненный пароли
        if (enteredPassword == storedPassword) {
            prefs.edit().putString("isAuth", "true").apply()  // Устанавливаем статус авторизации
            startActivity(Intent(this, CalculatorActivity::class.java))  // Переходим к калькулятору
            finish()  // Закрываем текущую активность
        } else {
            etPassword.text.clear()  // Очищаем поле пароля
            Toast.makeText(this, "Неверный пароль", Toast.LENGTH_SHORT).show()  // Показываем сообщение об ошибке
        }
    }

    // Метод для показа диалога восстановления пароля
    private fun showRecoveryDialog() {
        val input = EditText(this)  // Создаем новое поле ввода
        AlertDialog.Builder(this)
            .setTitle("Восстановление пароля")  // Заголовок диалога
            .setMessage("Введите ваше ключевое слово")  // Сообщение диалога
            .setView(input)  // Устанавливаем поле ввода
            .setPositiveButton("Подтвердить") { _, _ ->
                verifySecurityAnswer(input.text.toString())  // Проверяем введенный ответ
            }
            .setNegativeButton("Отмена", null)  // Кнопка отмены
            .show()  // Показываем диалог
    }

    // Метод для проверки ответов на секретный вопрос
    private fun verifySecurityAnswer(answer: String) {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val storedAnswer = prefs.getString("security_answer", null)  // Получаем сохраненный ответ

        // Сравниваем введенный ответ с сохраненным
        if (answer == storedAnswer) {
            startRegistration()  // Если совпадает, начинаем регистрацию
        } else {
            Toast.makeText(this, "Неверное ключевое слово", Toast.LENGTH_SHORT).show()  // Сообщаем об ошибке
        }
    }

    // Метод для очистки данных пользователя
    private fun clearUserData() {
        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit()
            .remove("password")  // Удаляем пароль
            .remove("security_answer")  // Удаляем ответ на секретный вопрос
            .apply()  // Применяем изменения
    }

    // Метод для начала процесса регистрации
    private fun startRegistration() {
        startActivity(Intent(this, SetupPassKeyActivity::class.java))  // Переходим к регистрации
        finish()  // Закрываем текущую активность
    }
}
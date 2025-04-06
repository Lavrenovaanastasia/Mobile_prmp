package com.example.mycalculator.PassKey

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mycalculator.MainActivity
import com.example.mycalculator.R

// Класс для настройки пароля и ключевого слова
class SetupPassKeyActivity : AppCompatActivity() {

    // Объявляем переменные для UI элементов
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var etSecurityAnswer: EditText
    private lateinit var btnSubmit: Button

    // Метод, вызываемый при создании активности
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pass_key)  // Устанавливаем разметку активности

        // Инициализируем элементы интерфейса
        etPassword = findViewById(R.id.etPassKey)
        etConfirmPassword = findViewById(R.id.etConfirmPassKey)
        etSecurityAnswer = findViewById(R.id.etSecurityAnswer)
        btnSubmit = findViewById(R.id.btnSetPassKey)

        // Устанавливаем обработчик клика для кнопки отправки
        btnSubmit.setOnClickListener {
            if (validateInputs()) {  // Проверяем введенные данные
                saveCredentials()  // Сохраняем учетные данные
                startActivity(Intent(this, MainActivity::class.java))  // Переходим к главной активности
                finish()  // Закрываем текущую активность
            }
        }
    }

    // Метод для валидации введенных данных
    private fun validateInputs(): Boolean {
        return when {
            etPassword.text.toString().length < 4 -> {  // Проверка длины пароля
                showError("Пароль должен быть не менее 4 символов")
                false
            }
            etPassword.text.toString() != etConfirmPassword.text.toString() -> {  // Проверка совпадения паролей
                showError("Пароли не совпадают")
                false
            }
            etSecurityAnswer.text.toString().isEmpty() -> {  // Проверка на наличие ключевого слова
                showError("Введите ключевое слово")
                false
            }
            else -> true  // Все проверки пройдены
        }
    }

    // Метод для сохранения учетных данных в SharedPreferences
    private fun saveCredentials() {
        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().apply {
            putString("password", etPassword.text.toString())  // Сохраняем пароль
            putString("security_answer", etSecurityAnswer.text.toString())  // Сохраняем ответ на секретный вопрос
            apply()  // Применяем изменения
        }
        Toast.makeText(this, "Регистрация завершена", Toast.LENGTH_SHORT).show()  // Уведомляем пользователя
    }

    // Метод для отображения сообщения об ошибке
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()  // Показываем сообщение
    }
}
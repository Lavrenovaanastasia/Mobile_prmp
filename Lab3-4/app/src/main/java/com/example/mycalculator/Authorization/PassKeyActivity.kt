package com.example.mycalculator.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mycalculator.CalculatorActivity
import com.example.mycalculator.R
import kotlinx.coroutines.launch

// Активность для аутентификации с использованием ключей
class PassKeyAuthActivity : AppCompatActivity() {
    private lateinit var accountManager: AccountManager  // Менеджер аккаунтов для управления аутентификацией

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)  // Установка макета активности

        accountManager = AccountManager(this)  // Инициализация менеджера аккаунтов
        val biometricButton = findViewById<Button>(R.id.biometricButton) // Добавьте кнопку в ваш макет

        biometricButton.setOnClickListener {
            lifecycleScope.launch {
                accountManager.authenticateWithBiometrics { result ->
                    handleSignInResult(result) // Обработка результата аутентификации
                }
            }
        }
        val signInButton = findViewById<Button>(R.id.signInButton)  // Кнопка для входа
        val registerButton = findViewById<Button>(R.id.registerButton)  // Кнопка для регистрации

        // Обработчик нажатия кнопки входа
        signInButton.setOnClickListener {
            lifecycleScope.launch {  // Запуск корутины
                val result = accountManager.signInWithPasskey()  // Попытка входа с помощью ключа
                handleSignInResult(result)  // Обработка результата входа
            }
        }

        // Обработчик нажатия кнопки регистрации
        registerButton.setOnClickListener {
            lifecycleScope.launch {  // Запуск корутины
                val result = accountManager.registerWithPasskey("username")  // Попытка регистрации с указанием имени пользователя
                handleSignUpResult(result)  // Обработка результата регистрации
            }
        }
    }

    // Обработка результата входа
    private fun handleSignInResult(result: SignInResult) {
        when (result) {
            is SignInResult.Success -> {  // Успешный вход
                navigateToCalculatorActivity()  // Переход к калькулятору
            }
            SignInResult.Cancelled -> {  // Если вход отменен
                Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show()
            }
            SignInResult.Failure -> {  // Если вход не удался
                Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show()
            }
            SignInResult.NoCredentials -> {  // Если нет учетных данных
                Toast.makeText(this, "No credentials found. Please register first.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Обработка результата регистрации
    private fun handleSignUpResult(result: SignUpResult) {
        when (result) {
            is SignUpResult.Success -> {  // Успешная регистрация
                navigateToCalculatorActivity()  // Переход к калькулятору
            }
            SignUpResult.Cancelled -> {  // Если регистрация отменена
                Toast.makeText(this, "Sign up cancelled", Toast.LENGTH_SHORT).show()
            }
            SignUpResult.Failure -> {  // Если регистрация не удалась
                Toast.makeText(this, "Sign up failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Метод для перехода к калькулятору
    private fun navigateToCalculatorActivity() {
        val intent = Intent(this, CalculatorActivity::class.java)  // Создание интента для перехода
        startActivity(intent)  // Запуск активности калькулятора
        finish()  // Закрытие текущей активности
    }
}
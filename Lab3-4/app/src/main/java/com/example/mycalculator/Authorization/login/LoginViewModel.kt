package com.example.mycalculator.Authorization.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.mycalculator.auth.SignInResult
import com.example.mycalculator.auth.SignUpResult

// ViewModel для управления состоянием экрана входа
class LoginViewModel: ViewModel() {

    // Состояние экрана входа, доступно для чтения
    var state by mutableStateOf(LoginState())
        private set

    // Функция для обработки действий, связанных с входом и регистрацией
    fun onAction(action: LoginAction) {
        when (action) {
            // Обработка результата регистрации
            is LoginAction.OnSignUp -> {
                when (action.result) {
                    SignUpResult.Cancelled -> {
                        state = state.copy(errorMessage = "Sign up was cancelled.")  // Сообщение об отмене
                    }
                    SignUpResult.Failure -> {
                        state = state.copy(errorMessage = "Sign up failed.")  // Сообщение о неудаче
                    }
                    is SignUpResult.Success -> {
                        state = state.copy(loggedInUser = action.result.username)  // Установка имени пользователя при успешной регистрации
                    }
                }
            }
            // Обработка результата входа
            is LoginAction.OnSignIn -> {
                when (action.result) {
                    SignInResult.Cancelled -> {
                        state = state.copy(errorMessage = "Sign in was cancelled.")  // Сообщение об отмене
                    }
                    SignInResult.Failure -> {
                        state = state.copy(errorMessage = "Sign in failed.")  // Сообщение о неудаче
                    }
                    is SignInResult.Success -> {
                        state = state.copy(loggedInUser = action.result.username)  // Установка имени пользователя при успешном входе
                    }
                    is SignInResult.NoCredentials -> {
                        state = state.copy(errorMessage = "No Passkeys found. Register first.")  // Сообщение о отсутствии учетных данных
                    }
                }
            }
            // Переключение между режимами входа и регистрации
            LoginAction.OnToggleIsRegister -> {
                state = state.copy(isRegister = !state.isRegister)  // Изменение состояния регистрации
            }
            // Обработка изменения имени пользователя
            is LoginAction.OnUsernameChange -> {
                state = state.copy(username = action.username)  // Установка нового имени пользователя
            }
        }
    }
}
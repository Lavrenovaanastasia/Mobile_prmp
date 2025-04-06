package com.example.mycalculator.Authorization.login

import com.example.mycalculator.auth.SignInResult
import com.example.mycalculator.auth.SignUpResult

sealed interface LoginAction {
    // Действие для обработки результата входа
    data class OnSignIn(val result: SignInResult): LoginAction

    // Действие для обработки результата регистрации
    data class OnSignUp(val result: SignUpResult): LoginAction

    // Действие для изменения имени пользователя
    data class OnUsernameChange(val username: String): LoginAction

    // Действие для переключения между режимами входа и регистрации
    data object OnToggleIsRegister: LoginAction
}
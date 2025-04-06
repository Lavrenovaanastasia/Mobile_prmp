package com.example.mycalculator.auth

import android.app.Activity
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.*
import org.json.JSONObject
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat

// Класс для управления аккаунтами и аутентификацией
class AccountManager(private val activity: Activity) {
    private val credentialManager = CredentialManager.create(activity)  // Инициализация менеджера учетных данных

    // Функция для регистрации с использованием ключа
    suspend fun registerWithPasskey(username: String): SignUpResult {
        return try {
            // Формирование JSON-запроса для создания учетных данных
            val requestJson = """
            {
                "rp": { "name": "MyCalc App" },
                "user": { "id": "${Base64.encodeToString(username.toByteArray(), Base64.NO_WRAP)}", "name": "$username", "displayName": "$username" },
                "pubKeyCredParams": [ { "alg": -7, "type": "public-key" } ],
                "authenticatorSelection": { "residentKey": "preferred", "userVerification": "required" },
                "attestation": "none",
                "timeout": 60000
            }
        """.trimIndent()

            // Создание запроса на создание учетных данных
            val createRequest = CreatePublicKeyCredentialRequest(requestJson)
            credentialManager.createCredential(activity, createRequest)  // Выполнение запроса на создание учетных данных

            SignUpResult.Success(username)  // Возврат успешного результата регистрации
        } catch (e: CreateCredentialCancellationException) {
            Log.e("PassKeyAuth", "Registration cancelled", e)  // Логирование отмены регистрации
            SignUpResult.Cancelled  // Возврат результата отмены
        } catch (e: CreateCredentialException) {
            Log.e("PassKeyAuth", "Registration failed", e)  // Логирование неудачной регистрации
            SignUpResult.Failure  // Возврат результата неудачи
        }
    }

    // Метод для биометрической аутентификации
    suspend fun authenticateWithBiometrics(callback: (SignInResult) -> Unit) {
        val biometricManager = BiometricManager.from(activity)
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric login for MyCalc App")
                    .setSubtitle("Log in using your biometric credential")
                    .setNegativeButtonText("Use account password")
                    .build()

                val executor = ContextCompat.getMainExecutor(activity)
                val biometricPrompt = BiometricPrompt(activity as AppCompatActivity, executor, object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        Log.d("BiometricAuth", "Authentication succeeded")
                        callback(SignInResult.Success("username"))  // Замените "username" на реальное имя пользователя
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        Log.e("BiometricAuth", "Authentication error: $errorCode - $errString")
                        callback(SignInResult.Failure)  // Обработка ошибки
                    }

                    override fun onAuthenticationFailed() {
                        Log.w("BiometricAuth", "Authentication failed")
                        callback(SignInResult.Failure)  // Обработка неудачи
                    }
                })

                biometricPrompt.authenticate(promptInfo)  // Запускаем аутентификацию
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                callback(SignInResult.NoCredentials)  // Нет зарегистрированных биометрических данных
            }
            else -> {
                callback(SignInResult.Failure)  // Ошибка аутентификации
            }
        }
    }
    // Функция для входа с использованием ключа
    suspend fun signInWithPasskey(): SignInResult {
        return try {
            // Формирование JSON-запроса для получения учетных данных
            val requestJson = """{ "userVerification": "required" }"""
            val getRequest = GetCredentialRequest(listOf(GetPublicKeyCredentialOption(requestJson)))  // Создание запроса на получение учетных данных
            val credentialResponse = credentialManager.getCredential(activity, getRequest)  // Выполнение запроса на получение учетных данных

            // Преобразование ответа в учетные данные
            val credential = credentialResponse.credential as? PublicKeyCredential
                ?: return SignInResult.Failure  // Возврат неудачи, если учетные данные не получены

            val responseJson = credential.authenticationResponseJson  // Получение JSON-ответа аутентификации
            val jsonResponse = JSONObject(responseJson)  // Преобразование в объект JSON

            val username = jsonResponse.getString("userHandle")  // Извлечение имени пользователя из ответа

            SignInResult.Success(username)  // Возврат успешного результата входа
        } catch (e: GetCredentialCancellationException) {
            SignInResult.Cancelled  // Возврат результата отмены
        } catch (e: NoCredentialException) {
            SignInResult.NoCredentials  // Возврат результата отсутствия учетных данных
        } catch (e: GetCredentialException) {
            SignInResult.Failure  // Возврат результата неудачи
        }
    }
}
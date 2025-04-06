package com.example.mycalculator.Authorization.login

import com.example.mycalculator.auth.SignInResult
import com.example.mycalculator.auth.SignUpResult
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mycalculator.auth.AccountManager
import kotlinx.coroutines.launch

// Функция для отображения экрана входа
@Composable
fun LoginScreen(
    state: LoginState,  // Состояние экрана входа
    onAction: (LoginAction) -> Unit,  // Лямбда-функция для обработки действий
    onLoggedIn: (String) -> Unit  // Лямбда-функция для обработки успешного входа
) {
    val scope = rememberCoroutineScope()  // Создаем корутинный скоуп для запуска корутин
    val context = LocalContext.current  // Получаем текущий контекст
    val accountManager = remember {
        AccountManager(context as ComponentActivity)  // Инициализируем менеджер аккаунта
    }

    // Эффект, который запускается при первом рендеринге
    LaunchedEffect(key1 = true) {
        val result = accountManager.signInWithPasskey()  // Попытка входа с помощью ключа
        onAction(LoginAction.OnSignIn(result))  // Обработка результата входа
    }

    // Эффект, который запускается при изменении пользователя
    LaunchedEffect(key1 = state.loggedInUser) {
        if (state.loggedInUser != null) {  // Если пользователь успешно вошел
            onLoggedIn(state.loggedInUser)  // Обработка успешного входа
        }
    }

    // Основной контейнер для размещения элементов интерфейса
    Column(
        modifier = Modifier
            .fillMaxSize()  // Заполняем доступное пространство
            .padding(16.dp),  // Устанавливаем отступы
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)  // Расположение элементов
    ) {
        // Поле ввода для имени пользователя
        TextField(
            value = state.username,
            onValueChange = {
                onAction(LoginAction.OnUsernameChange(it))  // Обработка изменения имени пользователя
            },
            label = { Text(text = "Username") },  // Подпись для поля ввода
            modifier = Modifier.fillMaxWidth()  // Заполняем ширину
        )

        // Ряд для переключателя регистрации
        Row {
            Text(text = "Register")  // Подпись для переключателя
            Spacer(modifier = Modifier.width(8.dp))  // Пробел между текстом и переключателем
            Switch(
                checked = state.isRegister,  // Состояние переключателя
                onCheckedChange = {
                    onAction(LoginAction.OnToggleIsRegister)  // Обработка изменения состояния переключателя
                }
            )
        }

        // Отображение сообщения об ошибке, если оно есть
        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,  // Текст сообщения об ошибке
                color = MaterialTheme.colorScheme.error  // Цвет текста для ошибки
            )
        }

        // Кнопка для входа или регистрации
        Button(onClick = {
            scope.launch {  // Запускаем корутину
                if (state.isRegister) {  // Если режим регистрации
                    val result = accountManager.registerWithPasskey(state.username)  // Регистрация с помощью ключа
                    onAction(LoginAction.OnSignUp(result))  // Обработка результата регистрации
                } else {  // Если режим входа
                    val result = accountManager.signInWithPasskey()  // Вход с помощью ключа
                    onAction(LoginAction.OnSignIn(result))  // Обработка результата входа
                }
            }
        }) {
            // Текст кнопки в зависимости от режима (вход/регистрация)
            Text(text = if (state.isRegister) "Register with Passkey" else "Login with Passkey")
        }
    }
}
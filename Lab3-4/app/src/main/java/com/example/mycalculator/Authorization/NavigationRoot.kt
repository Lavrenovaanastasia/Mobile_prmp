package com.example.mycalculator.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.mycalculator.Authorization.login.LoginScreen
import com.example.mycalculator.Authorization.login.LoginViewModel

import kotlinx.serialization.Serializable

// Определение маршрута для экрана входа
@Serializable
data object LoginRoute

// Определение маршрута для экрана после входа с указанием имени пользователя
@Serializable
data class LoggedInRoute(val username: String)

// Основная функция для настройки навигации в приложении
@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val navController = rememberNavController()  // Создаем контроллер навигации

    // Настройка навигационного графа
    NavHost(
        navController = navController,
        startDestination = LoginRoute  // Устанавливаем начальный маршрут
    ) {
        // Определение экрана входа
        composable<LoginRoute> {
            val viewModel = viewModel<LoginViewModel>()  // Получаем ViewModel для управления состоянием
            LoginScreen(
                state = viewModel.state,  // Передаем текущее состояние
                onAction = viewModel::onAction,  // Передаем обработчик действий
                onLoggedIn = { username ->  // Обработка успешного входа
                    navController.navigate(LoggedInRoute(username)) {  // Переход на экран после входа
                        popUpTo(LoginRoute) {  // Удаляем экран входа из стека навигации
                            inclusive = true
                        }
                    }
                }
            )
        }
        // Определение экрана после входа
        composable<LoggedInRoute> { backStackEntry ->
            val username = backStackEntry.toRoute<LoggedInRoute>().username  // Получаем имя пользователя из маршрута
            Box(
                modifier = Modifier.fillMaxSize(),  // Заполняем доступное пространство
                contentAlignment = Alignment.Center  // Центрируем содержимое
            ) {
                Text(text = "Hello $username!")  // Приветственное сообщение с именем пользователя
            }
        }
    }
}
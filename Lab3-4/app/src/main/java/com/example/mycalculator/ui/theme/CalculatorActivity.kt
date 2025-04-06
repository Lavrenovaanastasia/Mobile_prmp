package com.example.mycalculator

import android.app.NotificationChannel
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import kotlin.math.*
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mycalculator.adapters.HistoryAdapter
import com.example.mycalculator.utils.FirebaseManager
import com.example.mycalculator.utils.CalculationUtils
import com.example.mycalculator.ui.ThemeBottomSheetDialog
import com.example.mycalculator.utils.ThemeManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.mycalculator.PassKey.LoginActivity
import com.example.mycalculator.auth.PassKeyAuthActivity
import com.google.firebase.messaging.FirebaseMessaging

class CalculatorActivity : AppCompatActivity() {

    private lateinit var tvResult: TextView // TextView для отображения результата
    private var activeOperationButton: Button? = null // Кнопка, выполняющая активную операцию
    private var activeFunctionButton: Button? = null // Кнопка, выполняющая активную функцию
    private var currentNumber: String = "" // Текущее число, вводимое пользователем
    private var previousNumber: String = "" // Предыдущее число, для операций
    private var operation: String? = null // Операция, выбранная пользователем
    private var resultDisplayed: Boolean = false // Флаг, показывающий, отображается ли результат
    private lateinit var gestureDetector: GestureDetector // Объект для обработки жестов
    private lateinit var shakeDetector: ShakeDetector // Объект для обнаружения тряски

    private lateinit var firebaseManager: FirebaseManager // Менеджер для работы с Firebase
    private lateinit var historyAdapter: HistoryAdapter // Адаптер для истории вычислений
    private val historyList = mutableListOf<Pair<String, String>>() // Список истории вычислений

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверка аутентификации пользователя
        if (!isUserAuthenticated()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_calculator) // Установка содержимого макета
        createNotificationChannel() // Создание канала для уведомлений

        // Получение токена Firebase Cloud Messaging
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM Token", token)
        }

        // Применение сохраненной темы
        val rootLayout = findViewById<View>(R.id.rootLayout)
        ThemeManager.getSavedThemeColor(this) { color ->
            ThemeManager.applyTheme(rootLayout, color)
        }

        // Настройка кнопки для выбора темы
        val btnThemeSettings: Button = findViewById(R.id.btnThemeSettings)
        btnThemeSettings.setOnClickListener {
            ThemeBottomSheetDialog(rootLayout).show(supportFragmentManager, "ThemeBottomSheetDialog")
        }

        firebaseManager = FirebaseManager() // Инициализация FirebaseManager
        tvResult = findViewById(R.id.tvResult) // Получение ссылки на TextView результата
        gestureDetector = GestureDetector(this, GestureListener { operation ->
            setOperation(operation) // Установка операции при жесте
        })

        // Инициализация кнопок калькулятора
        val btnClear: Button = findViewById(R.id.btnClear)
        val btnPlusMinus: Button = findViewById(R.id.btnPlusMinus)
        val btnPercent: Button = findViewById(R.id.btnPercent)
        val btnAdd: Button = findViewById(R.id.btnAdd)
        val btnMinus: Button = findViewById(R.id.btnMinus)
        val btnMultiply: Button = findViewById(R.id.btnMultiply)
        val btnDivide: Button = findViewById(R.id.btnDivide)
        val btnEqual: Button = findViewById(R.id.btnEqual)
        val btnFloat: Button = findViewById(R.id.btnFloat)
        val btnClearEntry: Button = findViewById(R.id.btnClearEntry)
        val btnSin: Button = findViewById(R.id.btnSin)
        val btnCos: Button = findViewById(R.id.btnCos)
        val btnTan: Button = findViewById(R.id.btnTan)
        val btnCtg: Button = findViewById(R.id.btnCtg)
        val btnSqrt: Button = findViewById(R.id.btnSqrt)

        // Список кнопок чисел
        val numberButtons = listOf(
            findViewById<Button>(R.id.btn0),
            findViewById<Button>(R.id.btn1),
            findViewById<Button>(R.id.btn2),
            findViewById<Button>(R.id.btn3),
            findViewById<Button>(R.id.btn4),
            findViewById<Button>(R.id.btn5),
            findViewById<Button>(R.id.btn6),
            findViewById<Button>(R.id.btn7),
            findViewById<Button>(R.id.btn8),
            findViewById<Button>(R.id.btn9)
        )

        // Установка обработчиков нажатий для кнопок чисел
        for (button in numberButtons) {
            GestureUtils.setButtonClickListener(this, button) {
                appendNumber(button.text.toString()) // Добавление числа к текущему вводу
            }
        }

        // Назначение обработчиков нажатий для других кнопок
        GestureUtils.setButtonClickListener(this, btnClear, vibrationDuration = 100) {
            clear() // Очистка ввода
        }

        GestureUtils.setButtonClickListener(this, btnAdd) {
            setOperation("+") // Установка операции сложения
        }

        GestureUtils.setButtonClickListener(this, btnEqual) {
            calculateResult() // Вычисление результата
        }

        // Обработка касаний на корневом элементе
        rootLayout.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        // Установка обработчиков нажатий для числовых кнопок
        for (button in numberButtons) {
            button.setOnClickListener {
                appendNumber(button.text.toString())
                ApiUtils.vibrate(this) // Вибрация при нажатии
                ApiUtils.playClickSound() // Звук при нажатии
            }
        }

        btnClear.setOnClickListener {
            clear() // Очистка ввода
            ApiUtils.vibrate(this, 100) // Вибрация
            ApiUtils.playClickSound() // Звук
        }

        btnAdd.setOnClickListener { setOperation("+") }
        btnMinus.setOnClickListener { setOperation("-") }
        btnMultiply.setOnClickListener { setOperation("*") }
        btnDivide.setOnClickListener { setOperation("/") }
        btnEqual.setOnClickListener { calculateResult() }
        btnFloat.setOnClickListener { appendNumber(".") }
        btnPlusMinus.setOnClickListener { toggleSign() }
        btnPercent.setOnClickListener { applyPercent() }
        btnClearEntry.setOnClickListener { clearEntry() }
        btnSin.setOnClickListener { applyFunction("sin") }
        btnCos.setOnClickListener { applyFunction("cos") }
        btnTan.setOnClickListener { applyFunction("tan") }
        btnCtg.setOnClickListener { applyFunction("ctg") }
        btnSqrt.setOnClickListener { applyFunction("sqrt") }

        // Инициализация RecyclerView для истории вычислений
        val rvHistory: RecyclerView = findViewById(R.id.rvHistory)
        historyAdapter = HistoryAdapter(historyList)
        rvHistory.layoutManager = LinearLayoutManager(this) // Установка менеджера компоновки
        rvHistory.adapter = historyAdapter // Установка адаптера

        loadCalculationHistory() // Загрузка истории вычислений

        // Инициализация ShakeDetector для очистки экрана при тряске
        shakeDetector = ShakeDetector {
            clear() // Очистка экрана
        }
    }

    override fun onResume() {
        super.onResume()
        shakeDetector.start(this) // Запуск детектора тряски
    }

    override fun onPause() {
        super.onPause()
        shakeDetector.stop() // Остановка детектора тряски
    }

    // Проверка аутентификации пользователя
    private fun isUserAuthenticated(): Boolean {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("isAuth", "false") == "true"
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    // Создание канала для уведомлений
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "default_channel_id"
            val channelName = "Default Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Применение функции к текущему числу
    private fun applyFunction(func: String) {
        if (currentNumber.isEmpty()) return

        val value = currentNumber.toDoubleOrNull() ?: return
        val result = CalculationUtils.applyFunction(value, func)

        if (result.isNotEmpty()) {
            saveCalculationToFirebase("$func($currentNumber)", result) // Сохранение вычисления в Firebase
            updateHistory("$func($currentNumber)", result) // Обновление истории
            tvResult.text = result // Обновление отображаемого результата
            currentNumber = result // Обновление текущего числа
            resultDisplayed = true // Установка флага отображения результата
        }
    }

    // Сохранение вычисления в Firebase
    private fun saveCalculationToFirebase(expression: String, result: String) {
        firebaseManager.saveCalculation(expression, result,
            { println("История сохранена") },
            { e -> println("Ошибка сохранения: $e") }
        )
    }

    // Обновление истории вычислений
    private fun updateHistory(expression: String, result: String) {
        historyList.add(0, expression to result) // Добавление нового элемента в историю
        historyAdapter.notifyItemInserted(0) // Уведомление адаптера об обновлении
    }

    override fun onDestroy() {
        super.onDestroy()
        ApiUtils.unlockScreenOrientation(this) // Разблокировка ориентации экрана
    }

    // Очистка текущего ввода
    private fun clearEntry() {
        currentNumber = "0" // Сброс текущего числа
        tvResult.text = currentNumber // Обновление отображаемого результата
    }

    // Добавление числа к текущему вводу
    private fun appendNumber(number: String) {
        if (resultDisplayed) {
            currentNumber = "" // Сброс текущего числа, если результат отображается
            resultDisplayed = false
        }
        if (currentNumber.length >= 9 && number != ".") return // Ограничение длины ввода
        if (currentNumber == "0" && number != ".") {
            currentNumber = "" // Сброс текущего числа, если оно равно 0
        }

        if (number == "." && currentNumber.contains(".")) return // Запрет на множественные точки
        currentNumber += number // Добавление числа
        tvResult.text = currentNumber // Обновление отображаемого результата
    }

    // Установка операции
    private fun setOperation(op: String) {
        if (currentNumber.isEmpty() && previousNumber.isEmpty()) return // Проверка на пустые числа

        if (previousNumber.isNotEmpty() && currentNumber.isNotEmpty()) {
            val result = when (operation) {
                "+" -> previousNumber.toDouble() + currentNumber.toDouble()
                "-" -> previousNumber.toDouble() - currentNumber.toDouble()
                "*" -> previousNumber.toDouble() * currentNumber.toDouble()
                "/" -> if (currentNumber == "0") Double.NaN else previousNumber.toDouble() / currentNumber.toDouble()
                else -> return
            }

            previousNumber = if (result.isNaN()) {
                "Ошибка" // Обработка ошибки деления на 0
            } else {
                result.toString() // Установка результата в предыдущее число
            }
            tvResult.text = previousNumber // Обновление отображаемого результата
            currentNumber = "" // Сброс текущего числа
        } else if (currentNumber.isNotEmpty()) {
            previousNumber = currentNumber // Установка текущего числа как предыдущее
            currentNumber = "" // Сброс текущего числа
        }

        operation = op // Установка текущей операции
        val operationButton = when (op) {
            "+" -> findViewById<Button>(R.id.btnAdd)
            "-" -> findViewById<Button>(R.id.btnMinus)
            "*" -> findViewById<Button>(R.id.btnMultiply)
            "/" -> findViewById<Button>(R.id.btnDivide)
            else -> null
        }

        activeOperationButton?.setBackgroundResource(R.drawable.rounded_button) // Сброс фона предыдущей кнопки
        operationButton?.setBackgroundResource(R.drawable.button_active) // Установка фона для активной кнопки
        activeOperationButton = operationButton // Запоминание активной кнопки
    }

    // Вычисление результата
    private fun calculateResult() {
        val result = CalculationUtils.calculate(previousNumber, currentNumber, operation)
        if (result.isNotEmpty()) {
            saveCalculationToFirebase("$previousNumber $operation $currentNumber", result) // Сохранение вычисления в Firebase
            updateHistory("$previousNumber $operation $currentNumber", result) // Обновление истории
            previousNumber = "" // Сброс предыдущее число
            currentNumber = result // Установка результата как текущего числа
            operation = null // Сброс операции
            resultDisplayed = true // Установка флага отображения результата
        }
    }

    // Загрузка истории вычислений из Firebase
    private fun loadCalculationHistory() {
        firebaseManager.loadCalculationHistory(
            { history ->
                historyList.clear() // Очистка текущей истории
                historyList.addAll(history) // Добавление новой истории
                historyAdapter.notifyDataSetChanged() // Уведомление адаптера об обновлении
            },
            { e -> println("Ошибка загрузки истории: $e") } // Обработка ошибок
        )
    }

    // Очистка экрана и сброс всех значений
    private fun clear() {
        currentNumber = "" // Сброс текущего числа
        previousNumber = "" // Сброс предыдущее число
        operation = null // Сброс операции
        tvResult.text = "0" // Установка отображаемого результата в 0
        activeOperationButton?.setBackgroundResource(R.drawable.rounded_button) // Сброс фона активной кнопки
        activeOperationButton = null // Сброс активной кнопки
    }

    // Изменение знака текущего числа
    private fun toggleSign() {
        if (currentNumber.isEmpty()) return
        currentNumber = if (currentNumber.startsWith("-")) {
            currentNumber.substring(1) // Удаление знака минус
        } else {
            "-$currentNumber" // Добавление знака минус
        }
        tvResult.text = currentNumber // Обновление отображаемого результата
    }

    // Применение процента к текущему числу
    private fun applyPercent() {
        if (currentNumber.isEmpty()) return
        currentNumber = (currentNumber.toDouble() / 100).toString() // Применение процента
        tvResult.text = currentNumber // Обновление отображаемого результата
    }
}
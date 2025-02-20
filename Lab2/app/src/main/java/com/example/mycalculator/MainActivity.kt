package com.example.mycalculator

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {

    private var activeOperationButton: Button? = null
    private lateinit var tvResult: TextView
    private var currentNumber: String = ""
    private var previousNumber: String = ""
    private var operation: String? = null
    private var resultDisplayed: Boolean = false
    private lateinit var gestureDetector: GestureDetector
    private lateinit var shakeDetector: ShakeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvResult = findViewById(R.id.tvResult)

        // Инициализация GestureDetector
        gestureDetector = GestureDetector(this, GestureListener(this::setOperation))

        // Инициализация ShakeDetector
        shakeDetector = ShakeDetector { clear() }

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
        val btnSqrt: Button = findViewById(R.id.btnSqrt)
        val btnSquare: Button = findViewById(R.id.btnSquare)
        val btnSin: Button = findViewById(R.id.btnSin)
        val btnCos: Button = findViewById(R.id.btnCos)
        val btnTan: Button = findViewById(R.id.btnTan)
        val btnCot: Button = findViewById(R.id.btnCot)
        val btnLog: Button = findViewById(R.id.btnLog)
        val btnInverse: Button = findViewById(R.id.btnInverse)

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

        // Обработчики для чисел
        for (button in numberButtons) {
            button.setOnClickListener { appendNumber(button.text.toString()) }
        }

        // Обработчики операций
        btnSqrt.setOnClickListener { applyUnaryOperation("sqrt") }
        btnSquare.setOnClickListener { applyUnaryOperation("square") }
        btnSin.setOnClickListener { applyUnaryOperation("sin") }
        btnCos.setOnClickListener { applyUnaryOperation("cos") }
        btnTan.setOnClickListener { applyUnaryOperation("tan") }
        btnCot.setOnClickListener { applyUnaryOperation("cot") }
        btnLog.setOnClickListener { applyUnaryOperation("log") }
        btnInverse.setOnClickListener { applyUnaryOperation("inverse") }
        btnAdd.setOnClickListener { setOperation("+") }
        btnMinus.setOnClickListener { setOperation("-") }
        btnMultiply.setOnClickListener { setOperation("*") }
        btnDivide.setOnClickListener { setOperation("/") }
        btnEqual.setOnClickListener { calculateResult() }
        btnClear.setOnClickListener { clear() }
        btnFloat.setOnClickListener { appendNumber(".") }
        btnPlusMinus.setOnClickListener { toggleSign() }
        btnPercent.setOnClickListener { applyPercent() }
        btnClearEntry.setOnClickListener { deleteLastCharacter() }
    }

    override fun onResume() {
        super.onResume()
        shakeDetector.start(this)
    }

    override fun onPause() {
        super.onPause()
        shakeDetector.stop()
    }

    //Свайп
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)

        // Обработка двух пальцев
        if (event.pointerCount == 2 && event.actionMasked == MotionEvent.ACTION_MOVE) {
            if (event.getY(0) < event.getY(1)) {
                clear() // Сброс текста
                return true
            }
        }

        return super.onTouchEvent(event)
    }
    private fun deleteLastCharacter() {
        if (currentNumber.isNotEmpty()) {
            currentNumber = currentNumber.substring(0, currentNumber.length - 1)
            tvResult.text = if (currentNumber.isEmpty()) "0" else currentNumber
        }
    }

    private fun applyUnaryOperation(operation: String) {
        if (currentNumber.isEmpty()) return

        val number = currentNumber.toDoubleOrNull() ?: return
        val result = when (operation) {
            "sqrt" -> if (number >= 0) Math.sqrt(number) else Double.NaN
            "square" -> number * number
            "sin" -> Math.sin(Math.toRadians(number))
            "cos" -> Math.cos(Math.toRadians(number))
            "tan" -> Math.tan(Math.toRadians(number))
            "cot" -> if (number % 180.0 == 0.0) Double.NaN else 1 / Math.tan(Math.toRadians(number))
            "log" -> if (number > 0) Math.log10(number) else Double.NaN
            "inverse" -> if (number != 0.0) 1 / number else Double.NaN
            else -> return
        }

        tvResult.text = if (result.isNaN()) "Ошибка" else result.toString()
        currentNumber = result.toString()
    }

    private fun appendNumber(number: String) {
        if (resultDisplayed) {
            currentNumber = ""
            resultDisplayed = false
        }
        if (currentNumber.length >= 9 && number != ".") return
        if (currentNumber == "0" && number != ".") {
            currentNumber = ""
        }
        if (number == "." && currentNumber.contains(".")) return
        currentNumber += number
        tvResult.text = currentNumber
        activeOperationButton?.setBackgroundResource(R.drawable.rounded_button)
        activeOperationButton = null
    }

    private fun setOperation(op: String) {
        if (currentNumber.isEmpty() && previousNumber.isEmpty()) return

        if (previousNumber.isNotEmpty() && currentNumber.isNotEmpty()) {
            val result = when (operation) {
                "+" -> previousNumber.toDouble() + currentNumber.toDouble()
                "-" -> previousNumber.toDouble() - currentNumber.toDouble()
                "*" -> previousNumber.toDouble() * currentNumber.toDouble()
                "/" -> if (currentNumber == "0") Double.NaN else previousNumber.toDouble() / currentNumber.toDouble()
                else -> return
            }

            previousNumber = if (result.isNaN()) {
                "Ошибка"
            } else {
                result.toString()
            }
            tvResult.text = previousNumber
            currentNumber = ""
        } else if (currentNumber.isNotEmpty()) {
            previousNumber = currentNumber
            currentNumber = ""
        }

        operation = op
        val operationButton = when (op) {
            "+" -> findViewById<Button>(R.id.btnAdd)
            "-" -> findViewById<Button>(R.id.btnMinus)
            "*" -> findViewById<Button>(R.id.btnMultiply)
            "/" -> findViewById<Button>(R.id.btnDivide)
            else -> null
        }

        activeOperationButton?.setBackgroundResource(R.drawable.rounded_button)
        operationButton?.setBackgroundResource(R.drawable.button_active)
        activeOperationButton = operationButton
    }

    private fun calculateResult() {
        if (currentNumber.isEmpty() || previousNumber.isEmpty() || operation == null) return

        val result = when (operation) {
            "+" -> previousNumber.toDouble() + currentNumber.toDouble()
            "-" -> previousNumber.toDouble() - currentNumber.toDouble()
            "*" -> previousNumber.toDouble() * currentNumber.toDouble()
            "/" -> if (currentNumber == "0") Double.NaN else previousNumber.toDouble() / currentNumber.toDouble()
            else -> return
        }

        tvResult.text = if (result.isNaN()) "Ошибка" else result.toString()
        currentNumber = result.toString()
        previousNumber = ""
        operation = null
        resultDisplayed = true
    }

    private fun clear() {
        currentNumber = ""
        previousNumber = ""
        operation = null
        tvResult.text = "0"
    }

    private fun toggleSign() {
        if (currentNumber.isEmpty()) return
        currentNumber = if (currentNumber.startsWith("-")) {
            currentNumber.substring(1)
        } else {
            "-$currentNumber"
        }
        tvResult.text = currentNumber
    }

    private fun applyPercent() {
        if (currentNumber.isEmpty()) return
        currentNumber = (currentNumber.toDouble() / 100).toString()
        tvResult.text = currentNumber
    }
}
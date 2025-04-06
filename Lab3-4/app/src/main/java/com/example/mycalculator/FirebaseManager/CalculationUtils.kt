package com.example.mycalculator.utils

import kotlin.math.*

object CalculationUtils {

    fun calculate(previousNumber: String, currentNumber: String, operation: String?): String {
        // Проверка на пустые значения или отсутствие операции
        if (previousNumber.isEmpty() || currentNumber.isEmpty() || operation == null) return ""

        // Выполнение соответствующей арифметической операции
        val result = when (operation) {
            "+" -> previousNumber.toDouble() + currentNumber.toDouble()
            "-" -> previousNumber.toDouble() - currentNumber.toDouble()
            "*" -> previousNumber.toDouble() * currentNumber.toDouble()
            "/" -> if (currentNumber == "0") Double.NaN  // Деление на ноль
            else previousNumber.toDouble() / currentNumber.toDouble()
            else -> return ""
        }

        // Проверка результата: если он недопустим (NaN), вернуть сообщение об ошибке
        return if (result.isNaN()) "Ошибка" else result.toString()
    }


    fun applyFunction(value: Double, func: String): String {
        val result = when (func) {

            "sin" -> sin(Math.toRadians(value))

            "cos" -> cos(Math.toRadians(value))
            "tan" -> tan(Math.toRadians(value))

            "ctg" -> if (tan(Math.toRadians(value)) != 0.0)
                1 / tan(Math.toRadians(value))
            else Double.NaN
            "sqrt" -> if (value >= 0)
                sqrt(value)
            else Double.NaN
            else -> return "Ошибка"
        }

        // Если результат невалиден (NaN) — вернуть "Ошибка"
        return if (result.isNaN()) "Ошибка" else result.toString()
    }
}

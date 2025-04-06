package com.example.mycalculator.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * - Сохраняет вычисления
 * - Загружает историю
 * - Очищает историю
 */
class FirebaseManager {

    // Получение экземпляра базы данных Firestore
    private val db = FirebaseFirestore.getInstance()

    /**
     * Сохраняет новое вычисление в коллекцию "calculations".
     * @param onSuccess функция обратного вызова при успешном сохранении
     * @param onFailure функция обратного вызова при ошибке сохранения
     */
    fun saveCalculation(
        expression: String,
        result: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Создаем запись для сохранения
        val calcEntry = hashMapOf(
            "expression" to expression,
            "result" to result,
            "timestamp" to System.currentTimeMillis() // сохраняем текущее время
        )

        // Добавляем в коллекцию "calculations"
        db.collection("calculations")
            .add(calcEntry)
            .addOnSuccessListener { onSuccess() }    // вызываем успех
            .addOnFailureListener { onFailure(it) }  // обрабатываем ошибку
    }

    /**
     * Загружает последние 10 вычислений из Firestore, отсортированных по времени.
     *
     * @param onSuccess функция, вызываемая при успешной загрузке.
     * @param onFailure функция, вызываемая при ошибке загрузки.
     */
    fun loadCalculationHistory(
        onSuccess: (List<Pair<String, String>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("calculations")
            .orderBy("timestamp", Query.Direction.DESCENDING) // сортировка от новых к старым
            .limit(10) // ограничение до 10 последних
            .get()
            .addOnSuccessListener { documents ->
                val history = mutableListOf<Pair<String, String>>()
                for (document in documents) {
                    val expression = document.getString("expression") ?: ""
                    val result = document.getString("result") ?: ""
                    history.add(expression to result)
                }
                onSuccess(history)
            }
            .addOnFailureListener { onFailure(it) }
    }

    /**
     * Удаляет все документы из коллекции "calculations".
     *
     * @param onSuccess вызывается после успешного удаления
     * @param onFailure вызывается при возникновении ошибки
     */
    fun clearHistory(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("calculations")
            .get()
            .addOnSuccessListener { documents ->
                // Удаляем каждый документ в коллекции
                for (document in documents) {
                    db.collection("calculations").document(document.id).delete()
                }
                onSuccess()
            }
            .addOnFailureListener { onFailure(it) }
    }
}

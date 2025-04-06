package com.example.mycalculator.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mycalculator.R

/**
 * Адаптер для отображения истории вычислений в RecyclerView.
 */
class HistoryAdapter(private val historyList: MutableList<Pair<String, String>>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    /**
     * ViewHolder — внутренний класс, хранящий ссылки на элементы интерфейса
     * одного элемента списка (строки истории).
     */
    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Текстовое поле для выражения (например, "2+2")
        val tvExpression: TextView = view.findViewById(R.id.tvExpression)
        // Текстовое поле для результата (например, "4")
        val tvResult: TextView = view.findViewById(R.id.tvResultHistory)
    }

    /**
     * Создаёт новый ViewHolder при необходимости.
     *
     * @param parent Родительский ViewGroup (обычно RecyclerView)
     * @param viewType Тип представления (не используется здесь)
     * @return Экземпляр HistoryViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        // Загружаем layout для одной строки из item_history.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    /**
     * Привязывает данные (выражение и результат) к элементам ViewHolder.
     *
     * @param holder Элемент, в который загружаются данные
     * @param position Позиция в списке
     */
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val (expression, result) = historyList[position] // Получаем пару выражение/результат
        holder.tvExpression.text = expression
        holder.tvResult.text = result
    }

    /**
     * Возвращает общее количество элементов в списке.
     */
    override fun getItemCount(): Int = historyList.size
}

package com.example.mycalculator.ui

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.example.mycalculator.R
import com.example.mycalculator.databinding.DialogThemeSelectionBinding
import com.example.mycalculator.utils.ThemeManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// Класс для отображения диалогового окна выбора темы
class ThemeBottomSheetDialog(private val rootView: View) : BottomSheetDialogFragment() {

    private var _binding: DialogThemeSelectionBinding? = null
    private val binding get() = _binding!!

    // Создание диалогового окна
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Создаем диалог с заданной темой
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        // Инфлейтим представление из файла разметки
        _binding = DialogThemeSelectionBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)  // Устанавливаем корневое представление диалога

        // Устанавливаем обработчики кликов для выбора цвета темы
        binding.colorRed.setOnClickListener { selectTheme(Color.WHITE) }
        binding.colorPINK.setOnClickListener { selectTheme(Color.BLUE) }
        binding.colorGreen.setOnClickListener { selectTheme(Color.GREEN) }
        binding.colorDefault.setOnClickListener { selectTheme(Color.parseColor("#2A2D3E")) }

        return dialog  // Возвращаем созданный диалог
    }

    // Метод для выбора темы
    private fun selectTheme(color: Int) {
        ThemeManager.saveThemeColor(requireContext(), color)  // Сохраняем цвет в Firebase
        ThemeManager.applyTheme(rootView, color)  // Применяем выбранный цвет к корневому представлению
        dismiss()  // Закрываем диалог
    }

    // Освобождение ресурсов при уничтожении представления
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Обнуляем ссылку на привязку
    }
}
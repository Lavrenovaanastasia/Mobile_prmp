package com.example.mycalculator.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mycalculator.CalculatorActivity
import com.example.mycalculator.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Сервис для обработки входящих push-уведомлений от Firebase Cloud Messaging (FCM).
 * Наследуется от FirebaseMessagingService и автоматически активируется при получении сообщений.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Вызывается, когда получено сообщение от FCM.
     * Если в сообщении присутствует поле notification (заголовок/текст), вызывается метод showNotification.
     *
     * @param remoteMessage полученное удалённое сообщение
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Проверяем, содержит ли сообщение поля уведомления
        remoteMessage.notification?.let {
            showNotification(it.title, it.body)
        }
    }

    /**
     * Отображает системное уведомление на устройстве.
     *
     * @param title Заголовок уведомления
     * @param message Текст уведомления
     */
    private fun showNotification(title: String?, message: String?) {
        val channelId = "default_channel_id" // ID канала уведомлений (должен совпадать с созданным в App)

        // Создаём интент, чтобы при нажатии на уведомление открылось главное окно калькулятора
        val intent = Intent(this, CalculatorActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Оборачиваем интент в PendingIntent (требуется для взаимодействия с уведомлениями)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Строим само уведомление
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Иконка уведомления
            .setContentTitle(title) // Заголовок
            .setContentText(message) // Текст
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Приоритет (можно изменить)
            .setContentIntent(pendingIntent) // Действие по нажатию
            .setAutoCancel(true) // Уведомление исчезнет после нажатия

        // Получаем экземпляр менеджера уведомлений
        with(NotificationManagerCompat.from(this@MyFirebaseMessagingService)) {
            // Проверка разрешения на отправку уведомлений 
            if (ActivityCompat.checkSelfPermission(
                    this@MyFirebaseMessagingService,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Если разрешение не предоставлено — ничего не делаем
                return
            }

            // Показываем уведомление, используя уникальный ID (на основе текущего времени)
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}

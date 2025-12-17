package com.example.sotaysv_nhom2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    // Hàm này được gọi khi hệ thống nhận được tín hiệu báo thức
    @Override
    public void onReceive(Context context, Intent intent) {
        // 1. Lấy dữ liệu (Tiêu đề, Nội dung, ID) được gửi kèm từ Intent khi đặt báo thức
        String title = intent.getStringExtra("TITLE");
        String content = intent.getStringExtra("CONTENT");
        int noteId = intent.getIntExtra("ID", 0);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "channel_sotaysv_v3"; // ID định danh cho kênh thông báo

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Nhắc nhở", NotificationManager.IMPORTANCE_HIGH);

            // Cài đặt hiệu ứng: Rung
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500}); // Mẫu rung: nghỉ-rung-nghỉ-rung
            // Tạo kênh trong hệ thống
            nm.createNotificationChannel(channel);
        }
        // 4. Tạo sự kiện khi người dùng nhấn vào thông báo -> Mở MainActivity
        Intent intentApp = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, noteId, intentApp, PendingIntent.FLAG_IMMUTABLE);

        // 5. Xây dựng giao diện thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Icon nhỏ trên thanh trạng thái
                .setContentTitle(title)      // Hiển thị tiêu đề ghi chú
                .setContentText(content)     // Hiển thị nội dung ghi chú
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Đặt độ ưu tiên cao (có thể hiện popup)
                .setVibrate(new long[]{0, 500, 200, 500})      // Cài đặt rung
                .setAutoCancel(true)         // Tự động đóng thông báo khi người dùng nhấn vào
                .setContentIntent(pi);       // Gắn sự kiện click (mở App)

        // 6. Hiển thị thông báo lên màn hình
        nm.notify(noteId, builder.build());
    }
}
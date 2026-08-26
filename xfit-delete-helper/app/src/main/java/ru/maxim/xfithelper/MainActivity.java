package ru.maxim.xfithelper;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.net.Uri;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;

public class MainActivity extends Activity {
    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24), dp(54), dp(24), dp(24));
        root.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("Помощник XFIT");
        title.setTextSize(30);
        title.setTextColor(Color.rgb(41, 54, 92));
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView info = new TextView(this);
        info.setText("Помощник работает только в XFIT STAFF. На карточке тренировки появится оранжевая кнопка «Удалить запись». После вашего подтверждения помощник нажмёт корзину, а окончательное удаление нужно подтвердить в XFIT.");
        info.setTextSize(18);
        info.setTextColor(Color.DKGRAY);
        info.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(-1, -2);
        infoParams.setMargins(0, dp(30), 0, dp(28));
        root.addView(info, infoParams);

        Button enable = new Button(this);
        enable.setText("1. Включить помощника");
        enable.setTextSize(17);
        enable.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        root.addView(enable, new LinearLayout.LayoutParams(-1, dp(58)));

        Button open = new Button(this);
        open.setText("2. Открыть XFIT STAFF");
        open.setTextSize(17);
        LinearLayout.LayoutParams openParams = new LinearLayout.LayoutParams(-1, dp(58));
        openParams.setMargins(0, dp(16), 0, 0);
        open.setOnClickListener(v -> {
            Intent launch = getPackageManager().getLaunchIntentForPackage("ru.xfit.staff");
            if (launch == null) Toast.makeText(this, "XFIT STAFF не найден", Toast.LENGTH_LONG).show();
            else startActivity(launch);
        });
        root.addView(open, openParams);

        Button schedule = new Button(this);
        schedule.setText("3. Назначить время проверки");
        schedule.setTextSize(17);
        LinearLayout.LayoutParams scheduleParams = new LinearLayout.LayoutParams(-1, dp(58));
        scheduleParams.setMargins(0, dp(16), 0, 0);
        schedule.setOnClickListener(v -> chooseTestTime());
        root.addView(schedule, scheduleParams);
        setContentView(root);
    }

    private void chooseTestTime() {
        Calendar now = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> scheduleTest(hour, minute),
            now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
    }

    private void scheduleTest(int hour, int minute) {
        AlarmManager alarms = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= 31 && !alarms.canScheduleExactAlarms()) {
            Intent permission = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:" + getPackageName()));
            startActivity(permission);
            Toast.makeText(this, "Разрешите точные напоминания, затем выберите время ещё раз", Toast.LENGTH_LONG).show();
            return;
        }

        Calendar when = Calendar.getInstance();
        when.set(Calendar.HOUR_OF_DAY, hour);
        when.set(Calendar.MINUTE, minute);
        when.set(Calendar.SECOND, 0);
        when.set(Calendar.MILLISECOND, 0);
        if (when.getTimeInMillis() <= System.currentTimeMillis()) when.add(Calendar.DAY_OF_YEAR, 1);

        Intent intent = new Intent(this, TestAlarmReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(this, 1200, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(), pending);
        Toast.makeText(this, String.format("Проверка назначена на %02d:%02d", hour, minute), Toast.LENGTH_LONG).show();
    }
}

package ru.maxim.xfithelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class TestAlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        context.getSharedPreferences("transfer", Context.MODE_PRIVATE)
            .edit().putBoolean("open_client_pending", true).apply();
        Intent launch = context.getPackageManager().getLaunchIntentForPackage("ru.xfit.staff");
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(launch);
            android.content.SharedPreferences prefs = context.getSharedPreferences("transfer", Context.MODE_PRIVATE);
            String client = prefs.getString("client_name", "клиента");
            String date = prefs.getString("target_date", "выбранную дату");
            String time = prefs.getString("target_time", "выбранное время");
            Toast.makeText(context, "Открываю: " + client + ". Перенос: " + date + " " + time, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "XFIT STAFF не найден", Toast.LENGTH_LONG).show();
        }
    }
}

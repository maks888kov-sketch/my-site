package ru.maxim.xfithelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class TestAlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        Intent launch = context.getPackageManager().getLaunchIntentForPackage("ru.xfit.staff");
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(launch);
            Toast.makeText(context, "Время проверить перенос тренировки", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "XFIT STAFF не найден", Toast.LENGTH_LONG).show();
        }
    }
}

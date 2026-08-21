package ru.maxim.xfithelper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
        setContentView(root);
    }
}

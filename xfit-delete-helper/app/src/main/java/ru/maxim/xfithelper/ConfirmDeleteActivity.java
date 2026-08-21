package ru.maxim.xfithelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

public class ConfirmDeleteActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        new AlertDialog.Builder(this)
            .setTitle("Удалить старую запись?")
            .setMessage("Помощник нажмёт корзину в XFIT. Новую запись он создавать не будет. Окончательное удаление подтвердите в самом XFIT.")
            .setNegativeButton("Отмена", (d, w) -> finish())
            .setPositiveButton("Продолжить", (d, w) -> {
                Intent i = new Intent(this, XfitAccessibilityService.class);
                i.setAction(XfitAccessibilityService.ACTION_DELETE);
                startService(i);
                finish();
            })
            .setOnCancelListener(d -> finish())
            .show();
    }
}

package ru.maxim.xfithelper;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.Toast;
import java.util.List;

public class XfitAccessibilityService extends AccessibilityService {
    public static final String ACTION_DELETE = "ru.maxim.xfithelper.DELETE";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private WindowManager windowManager;
    private Button overlayButton;
    private boolean deleteRequested;

    @Override public void onServiceConnected() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_DELETE.equals(intent.getAction())) {
            deleteRequested = true;
            Intent launch = getPackageManager().getLaunchIntentForPackage("ru.xfit.staff");
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launch);
            }
            handler.postDelayed(this::clickTrash, 450);
        }
        return START_NOT_STICKY;
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) { hideOverlay(); return; }
        CharSequence pkg = root.getPackageName();
        boolean xfit = pkg != null && "ru.xfit.staff".contentEquals(pkg);
        boolean detail = xfit && (hasText(root, "Записать на тренировку") || hasText(root, "Сохранить"));
        if (detail) showOverlay(); else hideOverlay();
        if (deleteRequested && xfit) handler.postDelayed(this::clickTrash, 250);
    }

    private boolean hasText(AccessibilityNodeInfo root, String text) {
        List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(text);
        return nodes != null && !nodes.isEmpty();
    }

    private void showOverlay() {
        if (overlayButton != null || windowManager == null) return;
        overlayButton = new Button(this);
        overlayButton.setText("Удалить запись");
        overlayButton.setTextColor(Color.WHITE);
        overlayButton.setBackgroundColor(Color.rgb(255, 90, 0));
        overlayButton.setOnClickListener(v -> {
            Intent i = new Intent(this, ConfirmDeleteActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        });
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.END;
        p.x = 20;
        p.y = Math.round(250 * getResources().getDisplayMetrics().density);
        windowManager.addView(overlayButton, p);
    }

    private void hideOverlay() {
        if (overlayButton != null && windowManager != null) {
            windowManager.removeView(overlayButton);
            overlayButton = null;
        }
    }

    private void clickTrash() {
        if (!deleteRequested) return;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null || root.getPackageName() == null || !"ru.xfit.staff".contentEquals(root.getPackageName())) return;

        AccessibilityNodeInfo target = findDeleteNode(root);
        if (target != null) {
            AccessibilityNodeInfo clickable = target;
            while (clickable != null && !clickable.isClickable()) clickable = clickable.getParent();
            if (clickable != null && clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                deleteRequested = false;
                Toast.makeText(this, "Корзина нажата. Проверьте и подтвердите удаление в XFIT.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        // Запасной вариант для показанного экрана: центр корзины в правом верхнем углу.
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        Path path = new Path();
        path.moveTo(width * 0.94f, height * 0.095f);
        GestureDescription gesture = new GestureDescription.Builder()
            .addStroke(new GestureDescription.StrokeDescription(path, 0, 80))
            .build();
        dispatchGesture(gesture, new GestureResultCallback() {
            @Override public void onCompleted(GestureDescription gestureDescription) {
                deleteRequested = false;
                Toast.makeText(XfitAccessibilityService.this, "Корзина нажата. Проверьте и подтвердите удаление в XFIT.", Toast.LENGTH_LONG).show();
            }
        }, null);
    }

    private AccessibilityNodeInfo findDeleteNode(AccessibilityNodeInfo node) {
        if (node == null) return null;
        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        if (containsDelete(text) || containsDelete(desc)) return node;
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo found = findDeleteNode(node.getChild(i));
            if (found != null) return found;
        }
        return null;
    }

    private boolean containsDelete(CharSequence value) {
        if (value == null) return false;
        String s = value.toString().toLowerCase();
        return s.contains("удал") || s.contains("корзин");
    }

    @Override public void onInterrupt() { hideOverlay(); }
    @Override public void onDestroy() { hideOverlay(); super.onDestroy(); }
}

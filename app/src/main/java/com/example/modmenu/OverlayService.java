package com.example.modmenu;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OverlayService extends Service {
    private WindowManager windowManager;
    private View panel;
    private WindowManager.LayoutParams params;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        buildPanel();
    }

    private void buildPanel() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(14, 12, 14, 12);
        box.setBackgroundColor(0xEE172033);

        TextView header = new TextView(this);
        header.setText("MOD MENU  •  перетащи");
        header.setTextColor(0xFFFFFFFF);
        header.setTextSize(16);
        header.setPadding(4, 4, 4, 10);
        box.addView(header);

        addDemoToggle(box, "Демо-переключатель 1");
        addDemoToggle(box, "Демо-переключатель 2");

        Button hide = new Button(this);
        hide.setText("Закрыть панель");
        box.addView(hide);
        hide.setOnClickListener(v -> stopSelf());

        header.setOnTouchListener(new View.OnTouchListener() {
            private int startX, startY;
            private float touchX, touchY;
            @Override public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startX = params.x; startY = params.y;
                    touchX = event.getRawX(); touchY = event.getRawY();
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    params.x = startX + (int) (event.getRawX() - touchX);
                    params.y = startY + (int) (event.getRawY() - touchY);
                    windowManager.updateViewLayout(panel, params);
                    return true;
                }
                return true;
            }
        });

        panel = box;
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 24;
        params.y = 120;
        windowManager.addView(panel, params);
    }

    private void addDemoToggle(LinearLayout parent, String label) {
        Button toggle = new Button(this);
        toggle.setText(label + ": OFF");
        toggle.setOnClickListener(v -> {
            boolean enabled = "OFF".equals(toggle.getTag());
            toggle.setTag(enabled ? "ON" : "OFF");
            toggle.setText(label + ": " + (enabled ? "ON" : "OFF"));
        });
        toggle.setTag("OFF");
        parent.addView(toggle);
    }

    @Override
    public void onDestroy() {
        if (windowManager != null && panel != null) {
            windowManager.removeView(panel);
            panel = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}

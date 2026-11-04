package com.example.modmenu;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

public class OverlayService extends Service {
    private WindowManager wm;
    private View bubble;
    private View panel;
    private WindowManager.LayoutParams bubbleParams;
    private WindowManager.LayoutParams panelParams;

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        showBubble();
    }

    private WindowManager.LayoutParams makeParams(int width, int height) {
        return new WindowManager.LayoutParams(width, height,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
    }

    private GradientDrawable shape(int color, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(8));
        d.setStroke(dp(1), stroke);
        return d;
    }

    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        return t;
    }

    private void showBubble() {
        if (bubble != null) return;
        TextView icon = text("M", 20, Color.WHITE);
        icon.setGravity(Gravity.CENTER);
        icon.setTypeface(Typeface.DEFAULT_BOLD);
        icon.setBackground(shape(Color.rgb(28, 38, 45), Color.rgb(50, 203, 0)));
        bubble = icon;
        bubbleParams = makeParams(dp(48), dp(48));
        bubbleParams.gravity = Gravity.TOP | Gravity.START;
        bubbleParams.x = dp(12);
        bubbleParams.y = dp(130);
        icon.setOnTouchListener(dragListener(bubbleParams, true));
        icon.setOnClickListener(v -> {
            removeBubble();
            showPanel();
        });
        wm.addView(bubble, bubbleParams);
    }

    private void removeBubble() {
        if (bubble != null) {
            wm.removeView(bubble);
            bubble = null;
        }
    }

    private void showPanel() {
        if (panel != null) return;
        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setPadding(dp(10), dp(8), dp(10), dp(10));
        outer.setBackground(shape(0xEE1C2A35, 0xFF32CB00));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(4), dp(2), dp(2), dp(8));
        TextView title = text("MOD MENU", 17, 0xFF82CAFD);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        TextView collapse = text("—", 24, Color.WHITE);
        collapse.setGravity(Gravity.CENTER);
        header.addView(collapse, new LinearLayout.LayoutParams(dp(36), dp(32)));
        outer.addView(header);

        TextView subtitle = text("Floating interface • UI demo", 10, 0xFF82CAFD);
        outer.addView(subtitle);

        ScrollView scroll = new ScrollView(this);
        LinearLayout features = new LinearLayout(this);
        features.setOrientation(LinearLayout.VERTICAL);
        features.setPadding(0, dp(8), 0, 0);

        addCategory(features, "DEMO OPTIONS");
        addDemoSwitch(features, "Demo toggle 1");
        addDemoSwitch(features, "Demo toggle 2");
        addDemoSwitch(features, "Demo toggle 3");
        addCategory(features, "DISPLAY");
        addDemoSwitch(features, "Compact mode (visual only)");
        scroll.addView(features);
        outer.addView(scroll, new LinearLayout.LayoutParams(dp(270), dp(230)));

        TextView footer = text("Controls do not modify the game", 10, 0xFFB8C7D9);
        footer.setPadding(dp(2), dp(8), 0, 0);
        outer.addView(footer);

        panel = outer;
        panelParams = makeParams(-2, -2);
        panelParams.gravity = Gravity.TOP | Gravity.START;
        panelParams.x = dp(12);
        panelParams.y = dp(130);
        header.setOnTouchListener(dragListener(panelParams, false));
        collapse.setOnClickListener(v -> {
            removePanel();
            showBubble();
        });
        wm.addView(panel, panelParams);
    }

    private void addCategory(LinearLayout parent, String label) {
        TextView category = text(label, 11, Color.WHITE);
        category.setTypeface(Typeface.DEFAULT_BOLD);
        category.setPadding(dp(8), dp(7), dp(8), dp(7));
        category.setBackgroundColor(0xFF2F3D4C);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(5);
        parent.addView(category, p);
    }

    private void addDemoSwitch(LinearLayout parent, String label) {
        Switch control = new Switch(this);
        control.setText(label);
        control.setTextColor(Color.WHITE);
        control.setTextSize(13);
        control.setPadding(dp(8), dp(7), dp(8), dp(7));
        control.setButtonTintList(android.content.res.ColorStateList.valueOf(0xFF80CBC4));
        control.setOnCheckedChangeListener((button, checked) -> {
            // UI demonstration only: no game or process interaction.
        });
        parent.addView(control, new LinearLayout.LayoutParams(-1, -2));
    }

    private View.OnTouchListener dragListener(WindowManager.LayoutParams lp, boolean isBubble) {
        return new View.OnTouchListener() {
            private int startX, startY;
            private float downX, downY;
            private boolean moved;
            @Override public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startX = lp.x; startY = lp.y;
                    downX = event.getRawX(); downY = event.getRawY();
                    moved = false;
                    return isBubble;
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    int dx = (int) (event.getRawX() - downX);
                    int dy = (int) (event.getRawY() - downY);
                    if (Math.abs(dx) > dp(3) || Math.abs(dy) > dp(3)) moved = true;
                    if (moved) {
                        lp.x = startX + dx;
                        lp.y = startY + dy;
                        View target = isBubble ? bubble : panel;
                        if (target != null) wm.updateViewLayout(target, lp);
                    }
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) return moved || isBubble;
                return isBubble;
            }
        };
    }

    private void removePanel() {
        if (panel != null) {
            wm.removeView(panel);
            panel = null;
        }
    }

    @Override public void onDestroy() {
        removeBubble();
        removePanel();
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}

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

    private GradientDrawable shape(int color, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        if (stroke != 0) d.setStroke(dp(1), stroke);
        return d;
    }

    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setIncludeFontPadding(true);
        return t;
    }

    private void showBubble() {
        if (bubble != null) return;
        TextView icon = text("M", 20, Color.WHITE);
        icon.setGravity(Gravity.CENTER);
        icon.setTypeface(Typeface.DEFAULT_BOLD);
        icon.setBackground(shape(Color.rgb(28, 38, 45), Color.rgb(50, 203, 0), 24));
        bubble = icon;
        bubbleParams = makeParams(dp(48), dp(48));
        bubbleParams.gravity = Gravity.TOP | Gravity.START;
        bubbleParams.x = dp(12);
        bubbleParams.y = dp(130);
        icon.setOnTouchListener(dragListener(bubbleParams, true));
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
        outer.setBackground(shape(0xF20A1722, 0xFF263B49, 2));
        outer.setClipToOutline(true);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(14), dp(8), dp(10), dp(6));
        header.setBackgroundColor(0xFF152A38);

        TextView title = text("MOD MENU", 25, 0xFF39BCEB);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        header.addView(title, new LinearLayout.LayoutParams(0, dp(56), 1));

        TextView settings = text("⚙", 34, 0xFFE3EAF0);
        settings.setGravity(Gravity.CENTER);
        header.addView(settings, new LinearLayout.LayoutParams(dp(54), dp(56)));
        outer.addView(header);

        TextView subtitle = text(" ", 10, Color.WHITE);
        subtitle.setPadding(dp(12), 0, dp(12), 0);
        subtitle.setBackgroundColor(0xFF152A38);
        outer.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(12)));

        TextView slider = text("▽ Slider ▽", 21, Color.WHITE);
        slider.setTypeface(Typeface.DEFAULT_BOLD);
        slider.setGravity(Gravity.CENTER);
        slider.setBackgroundColor(0xFF183342);
        outer.addView(slider, new LinearLayout.LayoutParams(-1, dp(62)));

        ScrollView scroll = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(8), dp(8), dp(8), dp(8));
        content.setBackgroundColor(0xF2081723);

        for (int i = 1; i <= 5; i++) addToggle(content, "Toggles " + i);
        addCheckbox(content, "Checkbox 1");
        scroll.addView(content);
        outer.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout footer = new LinearLayout(this);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        footer.setPadding(dp(12), dp(8), dp(12), dp(8));
        footer.setBackgroundColor(0xFF142C3A);

        TextView hide = text("HIDE/KILL (HOLD)", 17, 0xFF32BCEB);
        hide.setTypeface(Typeface.DEFAULT_BOLD);
        hide.setGravity(Gravity.CENTER_VERTICAL);
        footer.addView(hide, new LinearLayout.LayoutParams(0, dp(58), 1));

        TextView minimize = text("MINIMIZE", 18, 0xFF32BCEB);
        minimize.setTypeface(Typeface.DEFAULT_BOLD);
        minimize.setGravity(Gravity.CENTER);
        footer.addView(minimize, new LinearLayout.LayoutParams(-2, dp(58)));
        minimize.setOnClickListener(v -> {
            removePanel();
            showBubble();
        });
        outer.addView(footer);

        panel = outer;
        panelParams = makeParams(dp(340), dp(490));
        panelParams.gravity = Gravity.CENTER;
        header.setOnTouchListener(dragListener(panelParams, false));
        wm.addView(panel, panelParams);
    }

    private void addToggle(LinearLayout parent, String label) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(4), dp(3), dp(8), dp(3));

        TextView name = text(label, 20, 0xFFF1F1F1);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        row.addView(name, new LinearLayout.LayoutParams(0, dp(58), 1));

        TextView toggle = text("", 12, Color.WHITE);
        toggle.setGravity(Gravity.CENTER);
        toggle.setBackground(shape(0xFF963342, 0, 28));
        LinearLayout.LayoutParams toggleParams = new LinearLayout.LayoutParams(dp(52), dp(28));
        toggleParams.leftMargin = dp(8);
        row.addView(toggle, toggleParams);

        final boolean[] enabled = {false};
        toggle.setOnClickListener(v -> {
            enabled[0] = !enabled[0];
            toggle.setBackground(shape(enabled[0] ? 0xFF28B7D7 : 0xFF963342, 0, 28));
            // Visual demo switch only; it does not interact with the game.
        });
        parent.addView(row, new LinearLayout.LayoutParams(-1, -2));
    }

    private void addCheckbox(LinearLayout parent, String label) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(4), dp(6), dp(8), dp(6));

        TextView box = text("", 16, Color.WHITE);
        box.setBackground(shape(Color.TRANSPARENT, 0xFF21C6D0, 2));
        row.addView(box, new LinearLayout.LayoutParams(dp(30), dp(30)));

        TextView name = text(label, 20, 0xFFF1F1F1);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(-1, dp(48));
        nameParams.leftMargin = dp(10);
        row.addView(name, nameParams);

        final boolean[] checked = {false};
        View.OnClickListener listener = v -> {
            checked[0] = !checked[0];
            box.setBackground(shape(checked[0] ? 0xFF21C6D0 : Color.TRANSPARENT, 0xFF21C6D0, 2));
        };
        box.setOnClickListener(listener);
        name.setOnClickListener(listener);
        parent.addView(row, new LinearLayout.LayoutParams(-1, -2));
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
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (isBubble && !moved) {
                        removeBubble();
                        showPanel();
                    }
                    return true;
                }
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
package com.example.modmenu;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int OVERLAY_REQUEST = 501;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        showScreen();
    }

    private void showScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(28, 32, 28, 32);
        root.setBackgroundColor(0xFF101522);

        TextView title = new TextView(this);
        title.setText("MOD MENU");
        title.setTextSize(28);
        title.setTextColor(0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView info = new TextView(this);
        info.setText("Отдельное плавающее демо-окно. Оно не изменяет игру и не подключается к её памяти.");
        info.setTextSize(15);
        info.setTextColor(0xFFCBD5E1);
        info.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(-1, -2);
        ip.setMargins(0, 18, 0, 24);
        root.addView(info, ip);

        Button permission = new Button(this);
        permission.setText("Разрешить показ поверх окон");
        root.addView(permission, new LinearLayout.LayoutParams(-1, -2));
        permission.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(i, OVERLAY_REQUEST);
            } else {
                Toast.makeText(this, "Разрешение уже выдано", Toast.LENGTH_SHORT).show();
            }
        });

        Button play = new Button(this);
        play.setText("PLAY — открыть Chicken Gun");
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, -2);
        bp.setMargins(0, 14, 0, 0);
        root.addView(play, bp);
        play.setOnClickListener(v -> launchGame());

        Button panel = new Button(this);
        panel.setText("Показать плавающую панель");
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1, -2);
        pp.setMargins(0, 10, 0, 0);
        root.addView(panel, pp);
        panel.setOnClickListener(v -> startOverlay());

        setContentView(root);
    }

    private void startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Сначала разреши показ поверх окон", Toast.LENGTH_LONG).show();
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(i, OVERLAY_REQUEST);
            return;
        }
        startService(new Intent(this, OverlayService.class));
        Toast.makeText(this, "Плавающая панель запущена", Toast.LENGTH_SHORT).show();
    }

    private void launchGame() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Сначала выдай разрешение на плавающее окно", Toast.LENGTH_LONG).show();
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(i, OVERLAY_REQUEST);
            return;
        }
        startOverlay();
        Intent launch = getPackageManager().getLaunchIntentForPackage("com.chaloapps.gun");
        if (launch == null) {
            Toast.makeText(this, "Chicken Gun не найдена. Проверь, установлена ли игра.", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            startActivity(launch);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Не удалось открыть Chicken Gun", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_REQUEST && Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Разрешение выдано", Toast.LENGTH_SHORT).show();
        }
    }
}

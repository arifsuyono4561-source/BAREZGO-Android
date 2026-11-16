
package com.barezgo.downloader;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import org.json.JSONObject;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private static final String API =
        "https://barezgo-api.arif-suyono4561.workers.dev";

    private EditText linkInput;
    private TextView resultText;
    private Button detectButton;
    private Handler handler = new Handler(Looper.getMainLooper());

    private int bg = Color.rgb(9, 16, 32);
    private int panel = Color.rgb(20, 31, 52);
    private int cyan = Color.rgb(0, 220, 220);
    private int white = Color.WHITE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bg);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 35, 24, 30);

        scroll.addView(root);

        TextView logo = new TextView(this);
        logo.setText("B  BAREZGO");
        logo.setTextSize(30);
        logo.setTextColor(cyan);
        logo.setTypeface(null, Typeface.BOLD);
        root.addView(logo);

        TextView subtitle = new TextView(this);
        subtitle.setText("VIDEO DOWNLOADER  •  VERSION 2.0");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(11);
        subtitle.setPadding(0, 5, 0, 30);
        root.addView(subtitle);

        TextView heading = new TextView(this);
        heading.setText("Download video favoritmu");
        heading.setTextColor(white);
        heading.setTextSize(23);
        heading.setTypeface(null, Typeface.BOLD);
        root.addView(heading);

        TextView description = new TextView(this);
        description.setText("Tempel tautan TikTok, Instagram, atau YouTube");
        description.setTextColor(Color.LTGRAY);
        description.setTextSize(13);
        description.setPadding(0, 8, 0, 20);
        root.addView(description);

        linkInput = new EditText(this);
        linkInput.setSingleLine(true);
        linkInput.setHint("Tempel tautan video di sini...");
        linkInput.setHintTextColor(Color.GRAY);
        linkInput.setTextColor(white);
        linkInput.setTextSize(14);
        linkInput.setPadding(16, 15, 16, 15);
        linkInput.setBackground(background(panel, 14));
        root.addView(linkInput, match());

        Button paste = new Button(this);
        paste.setText("TEMPEL TAUTAN");
        paste.setTextColor(cyan);
        paste.setBackground(background(panel, 12));
        LinearLayout.LayoutParams pasteParams = match();
        pasteParams.topMargin = 12;
        root.addView(paste, pasteParams);

        paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.content.ClipboardManager clipboard =
                    (android.content.ClipboardManager)
                    getSystemService(CLIPBOARD_SERVICE);

                if (clipboard != null && clipboard.hasPrimaryClip()) {
                    CharSequence value = clipboard.getPrimaryClip()
                        .getItemAt(0).coerceToText(MainActivity.this);
                    linkInput.setText(value);
                }
            }
        });

        detectButton = new Button(this);
        detectButton.setText("PERIKSA TAUTAN");
        detectButton.setTextColor(bg);
        detectButton.setTextSize(15);
        detectButton.setTypeface(null, Typeface.BOLD);
        detectButton.setBackground(background(cyan, 14));

        LinearLayout.LayoutParams buttonParams = match();
        buttonParams.topMargin = 15;
        root.addView(detectButton, buttonParams);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(18, 20, 18, 20);
        card.setBackground(background(panel, 16));

        LinearLayout.LayoutParams cardParams = match();
        cardParams.topMargin = 25;
        root.addView(card, cardParams);

        TextView platformTitle = new TextView(this);
        platformTitle.setText("PLATFORM DIDUKUNG");
        platformTitle.setTextColor(cyan);
        platformTitle.setTextSize(12);
        platformTitle.setTypeface(null, Typeface.BOLD);
        card.addView(platformTitle);

        TextView platforms = new TextView(this);
        platforms.setText("TikTok     •     Instagram     •     YouTube");
        platforms.setTextColor(white);
        platforms.setTextSize(14);
        platforms.setPadding(0, 14, 0, 10);
        card.addView(platforms);

        resultText = new TextView(this);
        resultText.setText("Status: Siap menerima tautan");
        resultText.setTextColor(Color.LTGRAY);
        resultText.setTextSize(14);
        resultText.setPadding(0, 20, 0, 5);
        card.addView(resultText);

        TextView footer = new TextView(this);
        footer.setText("BAREZGO  •  Powered by Cloudflare");
        footer.setTextColor(Color.GRAY);
        footer.setGravity(Gravity.CENTER);
        footer.setTextSize(11);
        footer.setPadding(0, 35, 0, 0);
        root.addView(footer);

        detectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectLink();
            }
        });

        setContentView(scroll);
    }

    private void detectLink() {
        final String link = linkInput.getText().toString().trim();

        if (link.isEmpty()) {
            resultText.setText("Masukkan tautan terlebih dahulu.");
            return;
        }

        if (!link.startsWith("https://") &&
            !link.startsWith("http://")) {
            resultText.setText("Tautan tidak valid.");
            return;
        }

        detectButton.setEnabled(false);
        resultText.setText("Menghubungkan ke BAREZGO API...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;

                try {
                    URL url = new URL(API + "/api/detect");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);
                    connection.setDoOutput(true);
                    connection.setRequestProperty(
                        "Content-Type", "application/json");

                    JSONObject body = new JSONObject();
                    body.put("url", link);

                    OutputStream output = connection.getOutputStream();
                    output.write(body.toString().getBytes("UTF-8"));
                    output.close();

                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                            connection.getInputStream(), "UTF-8"));

                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    final JSONObject data =
                        new JSONObject(response.toString());

                    final String platform =
                        data.optString("platform", "unknown");

                    final boolean detected =
                        data.optBoolean("detected", false);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (detected) {
                                resultText.setText(
                                    "Platform terdeteksi: " +
                                    platform.toUpperCase() +
                                    "\n\nTautan berhasil diperiksa.\n" +
                                    "Pengambilan media belum tersedia.");
                            } else {
                                resultText.setText(
                                    "Platform tidak dikenali.");
                            }
                            detectButton.setEnabled(true);
                        }
                    });

                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            resultText.setText(
                                "Gagal menghubungkan API.\n" +
                                "Periksa koneksi internet.");
                            detectButton.setEnabled(true);
                        }
                    });
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    private GradientDrawable background(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private LinearLayout.LayoutParams match() {
        return new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
    }
}

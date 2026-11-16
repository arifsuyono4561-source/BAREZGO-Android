
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
import android.view.Window;
import android.content.Intent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.net.Uri;
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
    private Button openButton;

    private String lastUrl = "";

    private final Handler handler =
        new Handler(Looper.getMainLooper());

    private final int bg = Color.rgb(9, 16, 32);
    private final int panel = Color.rgb(20, 31, 52);
    private final int cyan = Color.rgb(0, 220, 220);
    private final int white = Color.WHITE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(bg);
        window.setNavigationBarColor(bg);

        if (android.os.Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(true);
        }

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bg);
        scroll.setFillViewport(true);
        scroll.setFitsSystemWindows(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(18), dp(16), dp(30));
        root.setFitsSystemWindows(true);

        scroll.addView(root);

        TextView logo = new TextView(this);
        logo.setText("B  BAREZGO");
        logo.setTextSize(29);
        logo.setTextColor(cyan);
        logo.setTypeface(null, Typeface.BOLD);
        root.addView(logo);

        TextView subtitle = new TextView(this);
        subtitle.setText("VIDEO DOWNLOADER  •  VERSION 2.1");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(12);
        subtitle.setPadding(0, dp(5), 0, dp(24));
        root.addView(subtitle);

        TextView heading = new TextView(this);
        heading.setText("Download video favoritmu");
        heading.setTextColor(white);
        heading.setTextSize(23);
        heading.setTypeface(null, Typeface.BOLD);
        root.addView(heading);

        TextView description = new TextView(this);
        description.setText(
            "Tempel tautan TikTok, Instagram, atau YouTube"
        );
        description.setTextColor(Color.LTGRAY);
        description.setTextSize(14);
        description.setPadding(0, dp(8), 0, dp(18));
        root.addView(description);

        linkInput = new EditText(this);
        linkInput.setSingleLine(true);
        linkInput.setHint("Tempel tautan video di sini...");
        linkInput.setHintTextColor(Color.GRAY);
        linkInput.setTextColor(white);
        linkInput.setTextSize(15);
        linkInput.setPadding(
            dp(14), dp(13), dp(14), dp(13)
        );
        linkInput.setBackground(background(panel, 12));
        root.addView(linkInput, match());

        Button pasteButton = new Button(this);
        pasteButton.setText("TEMPEL TAUTAN");
        pasteButton.setTextColor(cyan);
        pasteButton.setBackground(background(panel, 12));

        LinearLayout.LayoutParams pasteParams = match();
        pasteParams.topMargin = dp(10);
        root.addView(pasteButton, pasteParams);

        pasteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard =
                    (ClipboardManager) getSystemService(
                        CLIPBOARD_SERVICE
                    );

                if (clipboard != null &&
                    clipboard.hasPrimaryClip()) {

                    ClipData clip = clipboard.getPrimaryClip();

                    if (clip != null && clip.getItemCount() > 0) {
                        CharSequence value =
                            clip.getItemAt(0).coerceToText(
                                MainActivity.this
                            );

                        linkInput.setText(value);
                    }
                }
            }
        });

        detectButton = new Button(this);
        detectButton.setText("PERIKSA TAUTAN");
        detectButton.setTextColor(bg);
        detectButton.setTextSize(15);
        detectButton.setTypeface(null, Typeface.BOLD);
        detectButton.setBackground(background(cyan, 12));

        LinearLayout.LayoutParams detectParams = match();
        detectParams.topMargin = dp(12);
        root.addView(detectButton, detectParams);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(
            dp(14), dp(16), dp(14), dp(16)
        );
        card.setBackground(background(panel, 14));

        LinearLayout.LayoutParams cardParams = match();
        cardParams.topMargin = dp(18);
        root.addView(card, cardParams);

        TextView platformTitle = new TextView(this);
        platformTitle.setText("PLATFORM DIDUKUNG");
        platformTitle.setTextColor(cyan);
        platformTitle.setTextSize(13);
        platformTitle.setTypeface(null, Typeface.BOLD);
        card.addView(platformTitle);

        TextView platforms = new TextView(this);
        platforms.setText(
            "TikTok     •     Instagram     •     YouTube"
        );
        platforms.setTextColor(white);
        platforms.setTextSize(15);
        platforms.setPadding(0, dp(12), 0, dp(10));
        card.addView(platforms);

        resultText = new TextView(this);
        resultText.setText("Status: Siap menerima tautan");
        resultText.setTextColor(Color.LTGRAY);
        resultText.setTextSize(14);
        resultText.setPadding(0, dp(12), 0, dp(8));
        card.addView(resultText);

        openButton = new Button(this);
        openButton.setText("BUKA DI APLIKASI RESMI");
        openButton.setTextColor(bg);
        openButton.setTextSize(14);
        openButton.setTypeface(null, Typeface.BOLD);
        openButton.setBackground(background(cyan, 12));
        openButton.setVisibility(View.GONE);

        LinearLayout.LayoutParams openParams = match();
        openParams.topMargin = dp(10);
        card.addView(openButton, openParams);

        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOfficialLink();
            }
        });

        TextView footer = new TextView(this);
        footer.setText("BAREZGO  •  Powered by Cloudflare");
        footer.setTextColor(Color.GRAY);
        footer.setGravity(Gravity.CENTER);
        footer.setTextSize(12);
        footer.setPadding(0, dp(28), 0, 0);
        root.addView(footer);

        detectButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    detectLink();
                }
            }
        );

        setContentView(scroll);
    }

    private void detectLink() {
        final String link =
            linkInput.getText().toString().trim();

        if (link.isEmpty()) {
            resultText.setText(
                "Masukkan tautan terlebih dahulu."
            );
            openButton.setVisibility(View.GONE);
            return;
        }

        if (!link.startsWith("https://") &&
            !link.startsWith("http://")) {
            resultText.setText("Format tautan tidak valid.");
            openButton.setVisibility(View.GONE);
            return;
        }

        lastUrl = link;
        detectButton.setEnabled(false);
        openButton.setVisibility(View.GONE);

        resultText.setText("Menghubungkan ke BAREZGO API...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;

                try {
                    URL url = new URL(API + "/api/detect");

                    connection =
                        (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);
                    connection.setDoOutput(true);

                    connection.setRequestProperty(
                        "Content-Type", "application/json"
                    );

                    JSONObject body = new JSONObject();
                    body.put("url", link);

                    OutputStream output =
                        connection.getOutputStream();

                    output.write(
                        body.toString().getBytes("UTF-8")
                    );
                    output.close();

                    int status = connection.getResponseCode();

                    java.io.InputStream stream =
                        status >= 200 && status < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                    if (stream == null) {
                        throw new Exception("Respons API kosong");
                    }

                    BufferedReader reader =
                        new BufferedReader(
                            new InputStreamReader(stream, "UTF-8")
                        );

                    StringBuilder response =
                        new StringBuilder();

                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();

                    if (status < 200 || status >= 300) {
                        throw new Exception(
                            "API memberikan status " + status
                        );
                    }

                    final JSONObject data =
                        new JSONObject(response.toString());

                    final String platform =
                        data.optString("platform", "unknown");

                    final boolean detected =
                        data.optBoolean("detected", false);

                    final String title =
                        data.optString(
                            "title", "Tautan diperiksa."
                        );

                    final String message =
                        data.optString(
                            "message", "Tidak ada informasi."
                        );

                    final String action =
                        data.optString(
                            "action", "Tidak tersedia"
                        );

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (detected) {
                                resultText.setText(
                                    "Platform terdeteksi: " +
                                    platform.toUpperCase() +
                                    "\n\n" + title +
                                    "\n\n" + message +
                                    "\n\nTindakan: " + action
                                );

                                openButton.setVisibility(
                                    View.VISIBLE
                                );
                            } else {
                                resultText.setText(
                                    "Platform tidak dikenali.\n\n" +
                                    message
                                );

                                openButton.setVisibility(
                                    View.GONE
                                );
                            }

                            detectButton.setEnabled(true);
                        }
                    });

                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            resultText.setText(
                                "Gagal memeriksa tautan.\n\n" +
                                e.getMessage()
                            );

                            openButton.setVisibility(View.GONE);
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

    private void openOfficialLink() {
        if (lastUrl.isEmpty()) {
            return;
        }

        try {
            Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse(lastUrl)
            );

            startActivity(intent);

        } catch (Exception e) {
            resultText.setText(
                "Tidak dapat membuka tautan."
            );
        }
    }

    private GradientDrawable background(
        int color, int radius
    ) {
        GradientDrawable drawable =
            new GradientDrawable();

        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));

        return drawable;
    }

    private LinearLayout.LayoutParams match() {
        return new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private int dp(int value) {
        return (int) (
            value * getResources()
                .getDisplayMetrics().density + 0.5f
        );
    }
}

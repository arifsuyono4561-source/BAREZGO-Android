
package com.barezgo.downloader;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.widget.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import java.net.URL;

public class MainActivity extends Activity {
    private EditText linkInput;
    private Spinner qualitySpinner;
    private WebView preview;
    private TextView status;

    private int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density);
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(null, Typeface.BOLD);
        return t;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackground(shape(Color.rgb(28,116,145),14));
        return b;
    }

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);

        getWindow().setStatusBarColor(Color.rgb(15,22,36));
        getWindow().setNavigationBarColor(Color.rgb(15,22,36));

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20),dp(18),dp(20),dp(18));
        page.setBackgroundColor(Color.rgb(15,22,36));

        ScrollView scroll = new ScrollView(this);
        scroll.addView(page);

        TextView brand = text("⬇ BAREZGO",27,Color.WHITE,true);
        page.addView(brand);

        TextView subtitle = text(
            "Tempel tautan. Pratinjau. Simpan video.",
            14,Color.rgb(184,198,218),false
        );
        page.addView(subtitle);

        linkInput = new EditText(this);
        linkInput.setSingleLine(true);
        linkInput.setTextColor(Color.WHITE);
        linkInput.setHintTextColor(Color.GRAY);
        linkInput.setHint("https://contoh.com/video.mp4");
        linkInput.setPadding(dp(14),dp(12),dp(14),dp(12));
        linkInput.setBackground(shape(Color.rgb(35,46,65),14));
        page.addView(linkInput);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        Button paste = button("Tempel Link");
        Button previewBtn = button("Pratinjau");

        LinearLayout.LayoutParams half =
            new LinearLayout.LayoutParams(0,-2,1);
        half.setMargins(0,dp(10),dp(5),dp(8));
        actions.addView(paste,half);

        LinearLayout.LayoutParams half2 =
            new LinearLayout.LayoutParams(0,-2,1);
        half2.setMargins(dp(5),dp(10),0,dp(8));
        actions.addView(previewBtn,half2);

        page.addView(actions);

        page.addView(text(
            "Kualitas (jika tersedia dari tautan sumber)",
            13,Color.rgb(184,198,218),true
        ));

        qualitySpinner = new Spinner(this);
        String[] qualities = {
            "Otomatis","360p","480p","720p","1080p"
        };

        ArrayAdapter<String> adapter =
            new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                qualities
            );

        qualitySpinner.setAdapter(adapter);
        page.addView(qualitySpinner);

        Button download = button("⬇ Download Video");
        LinearLayout.LayoutParams dl =
            new LinearLayout.LayoutParams(-1,-2);
        dl.topMargin = dp(12);
        page.addView(download,dl);

        status = text(
            "Siap. Tempel tautan video yang boleh diunduh.",
            12,Color.rgb(184,198,218),false
        );
        page.addView(status);

        TextView previewTitle =
            text("PREVIEW",12,Color.rgb(92,225,230),true);
        page.addView(previewTitle);

        preview = new WebView(this);
        preview.setBackgroundColor(Color.BLACK);
        preview.getSettings().setJavaScriptEnabled(true);
        preview.getSettings().setDomStorageEnabled(true);
        preview.setWebViewClient(new WebViewClient());
        preview.setWebChromeClient(new WebChromeClient());

        page.addView(preview,new LinearLayout.LayoutParams(-1,dp(260)));

        TextView note = text(
            "BAREZGO versi awal mendukung tautan file video langsung. " +
            "Situs dengan DRM atau proteksi unduhan tidak dapat diakali. " +
            "Pilihan resolusi bergantung pada tautan yang tersedia.",
            11,Color.rgb(160,174,196),false
        );
        page.addView(note);

        setContentView(scroll);

        paste.setOnClickListener(v -> {
            ClipboardManager cm =
                (ClipboardManager)getSystemService(
                    Context.CLIPBOARD_SERVICE
                );

            if(cm != null && cm.hasPrimaryClip()) {
                ClipData.Item item =
                    cm.getPrimaryClip().getItemAt(0);

                if(item != null && item.getText() != null) {
                    linkInput.setText(item.getText().toString());
                }
            } else {
                Toast.makeText(
                    this,"Clipboard kosong",Toast.LENGTH_SHORT
                ).show();
            }
        });

        previewBtn.setOnClickListener(v -> loadPreview());
        download.setOnClickListener(v -> startDownload());
    }

    private String validUrl() {
        String s = linkInput.getText().toString().trim();

        try {
            URL u = new URL(s);

            if(!"http".equalsIgnoreCase(u.getProtocol())
                && !"https".equalsIgnoreCase(u.getProtocol())) {
                return null;
            }

            return s;
        } catch(Exception e) {
            return null;
        }
    }

    private void loadPreview() {
        String url = validUrl();

        if(url == null) {
            status.setText("Masukkan URL http/https yang valid.");
            return;
        }

        preview.loadUrl(url);
        status.setText("Membuka pratinjau...");
    }

    private void startDownload() {
        final String url = validUrl();

        if(url == null) {
            status.setText("Masukkan URL yang valid.");
            return;
        }

        String path = "";

        try {
            path = new URL(url).getPath();
        } catch(Exception ignored) {}

        String name = path.substring(path.lastIndexOf('/')+1);

        if(name.isEmpty() || !name.contains(".")) {
            name = "BAREZGO_video.mp4";
        }

        try {
            DownloadManager.Request req =
                new DownloadManager.Request(Uri.parse(url));

            req.setTitle(name);
            req.setDescription("Diunduh melalui BAREZGO");
            req.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            );

            req.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "BAREZGO/"+name
            );

            req.setAllowedOverMetered(true);
            req.setAllowedOverRoaming(false);

            DownloadManager dm =
                (DownloadManager)getSystemService(DOWNLOAD_SERVICE);

            dm.enqueue(req);

            status.setText(
                "Download dimulai. Periksa folder Download/BAREZGO."
            );

        } catch(Exception e) {
            status.setText("Gagal memulai download. Periksa tautan video.");
        }
    }
}

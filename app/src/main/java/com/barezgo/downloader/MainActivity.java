package com.barezgo.downloader;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private final int bg = Color.rgb(8, 14, 28);
    private final int panel = Color.rgb(19, 29, 49);
    private final int cyan = Color.rgb(35, 220, 215);
    private final int white = Color.rgb(244, 248, 255);
    private EditText linkInput;
    private TextView status, detail;
    private Button checkButton, downloadButton, openButton;
    private String checkedUrl = "";
    private String detectedMime = "";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        getWindow().getDecorView().setSystemUiVisibility(0);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(24), dp(22), dp(28));
        root.setBackgroundColor(bg);
        scroll.addView(root);

        TextView brand = new TextView(this);
        brand.setText("B  BAREZGO");
        brand.setTextColor(cyan);
        brand.setTextSize(28);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(brand);

        TextView sub = new TextView(this);
        sub.setText("VIDEO DOWNLOADER");
        sub.setTextColor(Color.LTGRAY);
        sub.setTextSize(11);
        sub.setLetterSpacing(0.16f);
        LinearLayout.LayoutParams lp = wrap();
        lp.topMargin = dp(3);
        root.addView(sub, lp);

        TextView headline = new TextView(this);
        headline.setText("Simpan video favoritmu.");
        headline.setTextColor(white);
        headline.setTextSize(24);
        headline.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        lp = wrap(); lp.topMargin = dp(30);
        root.addView(headline, lp);

        TextView intro = new TextView(this);
        intro.setText("Tempel tautan file media langsung untuk mengunduhnya ke perangkat.");
        intro.setTextColor(Color.rgb(165, 178, 198));
        intro.setTextSize(14);
        lp = wrap(); lp.topMargin = dp(7);
        root.addView(intro, lp);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(round(panel, 18));
        lp = match(); lp.topMargin = dp(22);
        root.addView(card, lp);

        TextView label = new TextView(this);
        label.setText("TAUTAN VIDEO");
        label.setTextColor(cyan);
        label.setTextSize(11);
        label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        card.addView(label);

        linkInput = new EditText(this);
        linkInput.setSingleLine(true);
        linkInput.setTextColor(white);
        linkInput.setHintTextColor(Color.rgb(119, 132, 153));
        linkInput.setHint("https://contoh.com/video.mp4");
        linkInput.setTextSize(14);
        linkInput.setPadding(dp(12), dp(12), dp(12), dp(12));
        linkInput.setSelectAllOnFocus(false);
        linkInput.setBackground(round(Color.rgb(10, 19, 35), 10));
        lp = match(); lp.topMargin = dp(12);
        card.addView(linkInput, lp);

        Button paste = button("TEMPEL DARI CLIPBOARD", panel, cyan);
        lp = match(); lp.topMargin = dp(10);
        card.addView(paste, lp);
        paste.setOnClickListener(v -> pasteLink());

        checkButton = button("PERIKSA TAUTAN", cyan, bg);
        lp = match(); lp.topMargin = dp(10);
        card.addView(checkButton, lp);
        checkButton.setOnClickListener(v -> checkLink());

        downloadButton = button("UNDUH SEKARANG", cyan, bg);
        lp = match(); lp.topMargin = dp(10);
        card.addView(downloadButton, lp);
        downloadButton.setVisibility(View.GONE);
        downloadButton.setOnClickListener(v -> startDownload());

        openButton = button("BUKA TAUTAN DI BROWSER / APLIKASI", panel, white);
        lp = match(); lp.topMargin = dp(10);
        card.addView(openButton, lp);
        openButton.setVisibility(View.GONE);
        openButton.setOnClickListener(v -> openLink());

        LinearLayout statusCard = new LinearLayout(this);
        statusCard.setOrientation(LinearLayout.VERTICAL);
        statusCard.setPadding(dp(16), dp(15), dp(16), dp(15));
        statusCard.setBackground(round(Color.rgb(14, 24, 42), 16));
        lp = match(); lp.topMargin = dp(18);
        root.addView(statusCard, lp);

        TextView statusTitle = new TextView(this);
        statusTitle.setText("STATUS");
        statusTitle.setTextColor(cyan);
        statusTitle.setTextSize(11);
        statusTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        statusCard.addView(statusTitle);

        status = new TextView(this);
        status.setText("Siap menerima tautan");
        status.setTextColor(white);
        status.setTextSize(15);
        status.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        lp = wrap(); lp.topMargin = dp(7);
        statusCard.addView(status, lp);

        detail = new TextView(this);
        detail.setText("Hasil pemeriksaan akan muncul di sini.");
        detail.setTextColor(Color.rgb(165, 178, 198));
        detail.setTextSize(13);
        lp = wrap(); lp.topMargin = dp(5);
        statusCard.addView(detail, lp);

        TextView note = new TextView(this);
        note.setText("CATATAN\nBAREZGO mendukung URL file media langsung yang diizinkan. Tautan halaman TikTok, Instagram, dan YouTube akan dibuka melalui layanan resminya, bukan diekstrak atau melewati pembatasan platform.");
        note.setTextColor(Color.rgb(135, 150, 173));
        note.setTextSize(12);
        note.setLineSpacing(dp(3), 1f);
        lp = wrap(); lp.topMargin = dp(22);
        root.addView(note, lp);

        TextView footer = new TextView(this);
        footer.setText("BAREZGO  •  VERSION 1.1");
        footer.setTextColor(Color.rgb(95, 111, 135));
        footer.setGravity(Gravity.CENTER);
        footer.setTextSize(10);
        lp = match(); lp.topMargin = dp(30);
        root.addView(footer, lp);

        setContentView(scroll);
    }

    private void pasteLink() {
        try {
            ClipboardManager cb = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            if (cb != null && cb.hasPrimaryClip()) {
                ClipData clip = cb.getPrimaryClip();
                if (clip != null && clip.getItemCount() > 0) {
                    linkInput.setText(clip.getItemAt(0).coerceToText(this));
                    status.setText("Tautan ditempel");
                    detail.setText("Tekan PERIKSA TAUTAN untuk melanjutkan.");
                }
            } else {
                status.setText("Clipboard kosong");
            }
        } catch (Exception e) {
            status.setText("Tidak bisa membaca clipboard");
        }
    }

    private void checkLink() {
        String raw = linkInput.getText().toString().trim();
        if (raw.isEmpty()) { status.setText("Tautan belum diisi"); detail.setText("Tempel URL video terlebih dahulu."); return; }
        Uri uri;
        try { uri = Uri.parse(raw); }
        catch (Exception e) { status.setText("Tautan tidak valid"); return; }
        String scheme = uri.getScheme();
        if (uri.getHost() == null || scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            status.setText("Format tautan tidak valid");
            detail.setText("Gunakan tautan yang diawali http:// atau https://");
            return;
        }
        checkedUrl = raw;
        hideKeyboard();
        downloadButton.setVisibility(View.GONE);
        openButton.setVisibility(View.GONE);
        checkButton.setEnabled(false);
        checkButton.setText("MEMERIKSA...");
        status.setText("Sedang memeriksa file...");
        detail.setText("Memastikan apakah tautan mengarah langsung ke file media.");

        executor.execute(() -> {
            String mime = "";
            int code = -1;
            try {
                HttpURLConnection c = (HttpURLConnection)new URL(raw).openConnection();
                c.setRequestMethod("HEAD");
                c.setConnectTimeout(10000);
                c.setReadTimeout(10000);
                c.setInstanceFollowRedirects(true);
                c.setRequestProperty("User-Agent", "Mozilla/5.0 BAREZGO/1.1");
                code = c.getResponseCode();
                mime = c.getContentType();
                c.disconnect();
            } catch (Exception ignored) {}
            final String resultMime = mime == null ? "" : mime.toLowerCase(Locale.ROOT);
            final int resultCode = code;
            runOnUiThread(() -> {
                checkButton.setEnabled(true);
                checkButton.setText("PERIKSA TAUTAN");
                if (!checkedUrl.equals(raw)) return;
                String host = uri.getHost().toLowerCase(Locale.ROOT);
                if (isSocialHost(host)) {
                    status.setText("Tautan platform terdeteksi");
                    detail.setText("Gunakan tombol buka. Fitur simpan mengikuti opsi resmi dan izin pemilik konten.");
                    openButton.setVisibility(View.VISIBLE);
                } else if (isMediaType(resultMime) || hasMediaExtension(uri.getPath())) {
                    detectedMime = resultMime;
                    status.setText("File media terdeteksi");
                    detail.setText("Server merespons " + (resultCode > 0 ? resultCode : "tanpa status") + " • " + (resultMime.isEmpty() ? "jenis file dari URL" : resultMime) + ". Tekan UNDUH SEKARANG.");
                    downloadButton.setVisibility(View.VISIBLE);
                } else {
                    status.setText("Belum terdeteksi sebagai file video langsung");
                    detail.setText("Tautan halaman web tidak selalu menyediakan file unduhan. Buka tautan untuk melihat opsi resmi.");
                    openButton.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    private boolean isSocialHost(String h) {
        return h.equals("tiktok.com") || h.endsWith(".tiktok.com") ||
               h.equals("instagram.com") || h.endsWith(".instagram.com") ||
               h.equals("youtube.com") || h.endsWith(".youtube.com") ||
               h.equals("youtu.be") || h.equals("facebook.com") || h.endsWith(".facebook.com");
    }

    private boolean isMediaType(String m) {
        return m.startsWith("video/") || m.startsWith("audio/") || m.equals("application/octet-stream");
    }

    private boolean hasMediaExtension(String path) {
        if (path == null) return false;
        String p = path.toLowerCase(Locale.ROOT);
        return p.matches(".*\\.(mp4|m4v|webm|mov|mkv|mp3|m4a|aac|ogg|wav)$");
    }

    private void startDownload() {
        if (checkedUrl.isEmpty()) return;
        try {
            String name = URLUtil.guessFileName(checkedUrl, null, detectedMime.isEmpty() ? "video/mp4" : detectedMime);
            if (name == null || name.trim().isEmpty()) name = "BAREZGO-video.mp4";
            DownloadManager.Request req = new DownloadManager.Request(Uri.parse(checkedUrl));
            req.setTitle("BAREZGO • " + name);
            req.setDescription("Mengunduh ke folder Download/BAREZGO");
            req.setMimeType(detectedMime.isEmpty() ? "video/mp4" : detectedMime);
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setAllowedOverMetered(true);
            req.setAllowedOverRoaming(false);
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "BAREZGO/" + name);
            DownloadManager dm = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
            if (dm == null) throw new IllegalStateException("Download Manager tidak tersedia");
            dm.enqueue(req);
            status.setText("Unduhan dimulai");
            detail.setText("File: " + name + "\nPantau progres melalui notifikasi Android atau folder Download/BAREZGO.");
            downloadButton.setEnabled(false);
            downloadButton.setText("UNDUHAN DIANTREKAN");
        } catch (Exception e) {
            status.setText("Unduhan gagal dimulai");
            detail.setText(e.getMessage() == null ? "Periksa kembali tautan." : e.getMessage());
        }
    }

    private void openLink() {
        if (checkedUrl.isEmpty()) return;
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(checkedUrl))); }
        catch (Exception e) { status.setText("Tidak ada aplikasi untuk membuka tautan"); }
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(linkInput.getWindowToken(), 0);
        } catch (Exception ignored) {}
    }

    private Button button(String label, int color, int textColor) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(textColor);
        b.setTextSize(12);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setAllCaps(false);
        b.setBackground(round(color, 12));
        return b;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private LinearLayout.LayoutParams match() { return new LinearLayout.LayoutParams(-1, -2); }
    private LinearLayout.LayoutParams wrap() { return new LinearLayout.LayoutParams(-2, -2); }
    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    @Override protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
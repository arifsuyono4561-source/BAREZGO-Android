
package com.barezgo.downloader;

import android.app.Activity;
import android.app.DownloadManager;
import android.os.Bundle;
import android.os.Environment;
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
import android.webkit.URLUtil;
import android.widget.*;

public class MainActivity extends Activity {

    private EditText linkInput;
    private TextView resultText;
    private Button detectButton;
    private Button downloadButton;
    private Button openButton;
    private String lastUrl = "";

    private final int bg = Color.rgb(9,16,32);
    private final int panel = Color.rgb(20,31,52);
    private final int cyan = Color.rgb(0,220,220);

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18),dp(25),dp(18),dp(25));
        root.setBackgroundColor(bg);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);

        TextView logo = new TextView(this);
        logo.setText("B  BAREZGO");
        logo.setTextColor(cyan);
        logo.setTextSize(29);
        logo.setTypeface(null,Typeface.BOLD);
        root.addView(logo);

        TextView heading = new TextView(this);
        heading.setText("Video Downloader");
        heading.setTextColor(Color.WHITE);
        heading.setTextSize(23);
        heading.setTypeface(null,Typeface.BOLD);
        heading.setPadding(0,dp(20),0,dp(8));
        root.addView(heading);

        TextView info = new TextView(this);
        info.setText("Unduh file video langsung atau periksa tautan sosial media.");
        info.setTextColor(Color.LTGRAY);
        info.setTextSize(14);
        root.addView(info);

        linkInput = new EditText(this);
        linkInput.setSingleLine(true);
        linkInput.setHint("Tempel tautan video...");
        linkInput.setHintTextColor(Color.GRAY);
        linkInput.setTextColor(Color.WHITE);
        linkInput.setPadding(dp(12),dp(12),dp(12),dp(12));
        linkInput.setBackground(background(panel,12));

        LinearLayout.LayoutParams p = match();
        p.topMargin = dp(20);
        root.addView(linkInput,p);

        Button paste = makeButton("TEMPEL TAUTAN",panel,cyan);
        p = match();
        p.topMargin = dp(10);
        root.addView(paste,p);

        paste.setOnClickListener(v -> {
            ClipboardManager cb = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            if(cb != null && cb.hasPrimaryClip()) {
                ClipData clip = cb.getPrimaryClip();
                if(clip != null && clip.getItemCount() > 0) {
                    linkInput.setText(clip.getItemAt(0).coerceToText(this));
                }
            }
        });

        detectButton = makeButton("PERIKSA TAUTAN",cyan,bg);
        p = match();
        p.topMargin = dp(10);
        root.addView(detectButton,p);

        downloadButton = makeButton("UNDUH FILE VIDEO",cyan,bg);
        p = match();
        p.topMargin = dp(10);
        root.addView(downloadButton,p);
        downloadButton.setVisibility(View.GONE);

        openButton = makeButton("BUKA DI APLIKASI RESMI",panel,cyan);
        p = match();
        p.topMargin = dp(10);
        root.addView(openButton,p);
        openButton.setVisibility(View.GONE);

        resultText = new TextView(this);
        resultText.setText("Status: Siap menerima tautan");
        resultText.setTextColor(Color.WHITE);
        resultText.setTextSize(14);
        resultText.setPadding(0,dp(22),0,dp(15));
        root.addView(resultText);

        TextView footer = new TextView(this);
        footer.setText("BAREZGO • VIDEO DOWNLOADER");
        footer.setTextColor(Color.GRAY);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0,dp(30),0,0);
        root.addView(footer);

        detectButton.setOnClickListener(v -> checkLink());
        downloadButton.setOnClickListener(v -> downloadFile());
        openButton.setOnClickListener(v -> openLink());

        setContentView(scroll);
    }

    private void checkLink() {
        String link = linkInput.getText().toString().trim();

        if(link.isEmpty()) {
            resultText.setText("Masukkan tautan terlebih dahulu.");
            return;
        }

        if(!link.startsWith("https://") && !link.startsWith("http://")) {
            resultText.setText("Format tautan tidak valid.");
            return;
        }

        lastUrl = link;

        if(isDirectMedia(link)) {
            resultText.setText("File media langsung terdeteksi. Tekan tombol unduh.");
            downloadButton.setVisibility(View.VISIBLE);
            openButton.setVisibility(View.GONE);
            return;
        }

        downloadButton.setVisibility(View.GONE);
        openButton.setVisibility(View.VISIBLE);

        String host = Uri.parse(link).getHost();
        String platform = "Tautan web";

        if(host != null) {
            host = host.toLowerCase();
            if(host.contains("tiktok.com")) platform = "TikTok";
            else if(host.contains("instagram.com")) platform = "Instagram";
            else if(host.contains("youtube.com") || host.contains("youtu.be")) platform = "YouTube";
        }

        resultText.setText(
            platform + " terdeteksi.\n\n" +
            "Gunakan fitur unduhan resmi platform jika tersedia."
        );
    }

    private boolean isDirectMedia(String link) {
        try {
            String path = Uri.parse(link).getPath();
            if(path == null) return false;
            path = path.toLowerCase();

            String[] ext = {
                ".mp4",".m4v",".webm",".mov",".mkv",
                ".mp3",".m4a",".aac",".ogg",".wav"
            };

            for(String e : ext) {
                if(path.endsWith(e)) return true;
            }
        } catch(Exception ignored) {}
        return false;
    }

    private void downloadFile() {
        if(lastUrl.isEmpty() || !isDirectMedia(lastUrl)) {
            resultText.setText("Tautan media langsung tidak valid.");
            return;
        }

        try {
            String filename = URLUtil.guessFileName(lastUrl,null,"video.mp4");

            DownloadManager.Request request =
                new DownloadManager.Request(Uri.parse(lastUrl));

            request.setTitle("BAREZGO: " + filename);
            request.setDescription("Mengunduh file media");
            request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            );
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(false);
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,filename
            );

            DownloadManager manager =
                (DownloadManager)getSystemService(DOWNLOAD_SERVICE);

            if(manager == null) throw new Exception("Download Manager tidak tersedia");

            manager.enqueue(request);

            resultText.setText(
                "Unduhan dimulai!\n\nFile: " + filename +
                "\nPeriksa notifikasi atau folder Download."
            );

        } catch(Exception e) {
            resultText.setText("Gagal mengunduh: " + e.getMessage());
        }
    }

    private void openLink() {
        if(lastUrl.isEmpty()) return;
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(lastUrl)));
        } catch(Exception e) {
            resultText.setText("Tidak dapat membuka tautan.");
        }
    }

    private Button makeButton(String text,int color,int textColor) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(textColor);
        b.setTypeface(null,Typeface.BOLD);
        b.setBackground(background(color,12));
        return b;
    }

    private GradientDrawable background(int color,int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private LinearLayout.LayoutParams match() {
        return new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private int dp(int value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }
}

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
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final String API = "https://barezgo-api.arif-suyono4561.workers.dev";
    private final int bg = Color.rgb(8, 14, 28), panel = Color.rgb(19, 29, 49);
    private final int cyan = Color.rgb(35, 220, 215), white = Color.rgb(244, 248, 255);
    private EditText linkInput;
    private TextView status, detail;
    private Button actionButton;
    private String activeUrl = "";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(24), dp(22), dp(28));
        root.setBackgroundColor(bg);
        scroll.addView(root);

        TextView brand = new TextView(this);
        brand.setText("B  BAREZGO");
        brand.setTextColor(cyan); brand.setTextSize(28);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(brand);

        TextView sub = new TextView(this);
        sub.setText("VIDEO DOWNLOADER");
        sub.setTextColor(Color.LTGRAY); sub.setTextSize(11); sub.setLetterSpacing(.16f);
        LinearLayout.LayoutParams lp = wrap(); lp.topMargin = dp(3); root.addView(sub, lp);

        TextView headline = new TextView(this);
        headline.setText("Unduh video langsung.");
        headline.setTextColor(white); headline.setTextSize(24);
        headline.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        lp = wrap(); lp.topMargin = dp(30); root.addView(headline, lp);

        TextView intro = new TextView(this);
        intro.setText("Tempel tautan TikTok, Instagram, YouTube, atau URL video langsung.");
        intro.setTextColor(Color.rgb(165,178,198)); intro.setTextSize(14);
        lp = wrap(); lp.topMargin = dp(7); root.addView(intro, lp);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(16),dp(16),dp(16),dp(16));
        card.setBackground(round(panel,18)); lp = match(); lp.topMargin = dp(22); root.addView(card,lp);

        TextView label = new TextView(this);
        label.setText("TAUTAN VIDEO"); label.setTextColor(cyan); label.setTextSize(11);
        label.setTypeface(Typeface.DEFAULT,Typeface.BOLD); card.addView(label);

        linkInput = new EditText(this);
        linkInput.setSingleLine(true); linkInput.setTextColor(white);
        linkInput.setHintTextColor(Color.rgb(119,132,153));
        linkInput.setHint("Tempel tautan video di sini"); linkInput.setTextSize(14);
        linkInput.setPadding(dp(12),dp(12),dp(12),dp(12));
        linkInput.setBackground(round(Color.rgb(10,19,35),10));
        lp=match(); lp.topMargin=dp(12); card.addView(linkInput,lp);

        Button paste=button("TEMPEL TAUTAN",panel,cyan);
        lp=match(); lp.topMargin=dp(10); card.addView(paste,lp);
        paste.setOnClickListener(v -> pasteLink());

        actionButton=button("UNDUH MP4",cyan,bg);
        lp=match(); lp.topMargin=dp(10); card.addView(actionButton,lp);
        actionButton.setOnClickListener(v -> resolveAndDownload());

        LinearLayout statusCard=new LinearLayout(this);
        statusCard.setOrientation(LinearLayout.VERTICAL);
        statusCard.setPadding(dp(16),dp(15),dp(16),dp(15));
        statusCard.setBackground(round(Color.rgb(14,24,42),16));
        lp=match(); lp.topMargin=dp(18); root.addView(statusCard,lp);

        TextView st=new TextView(this);
        st.setText("STATUS"); st.setTextColor(cyan); st.setTextSize(11);
        st.setTypeface(Typeface.DEFAULT,Typeface.BOLD); statusCard.addView(st);
        status=new TextView(this); status.setText("Siap menerima tautan");
        status.setTextColor(white); status.setTextSize(15);
        status.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        lp=wrap(); lp.topMargin=dp(7); statusCard.addView(status,lp);
        detail=new TextView(this); detail.setText("Tempel tautan lalu tekan UNDUH MP4.");
        detail.setTextColor(Color.rgb(165,178,198)); detail.setTextSize(13);
        lp=wrap(); lp.topMargin=dp(5); statusCard.addView(detail,lp);

        TextView note=new TextView(this);
        note.setText("BAREZGO memproses tautan melalui server downloader. Gunakan hanya untuk konten publik yang kamu miliki atau yang pemiliknya mengizinkan untuk diunduh. Ketersediaan dapat berubah mengikuti dukungan dan pembatasan platform.");
        note.setTextColor(Color.rgb(135,150,173)); note.setTextSize(12); note.setLineSpacing(dp(3),1f);
        lp=wrap(); lp.topMargin=dp(22); root.addView(note,lp);

        TextView footer=new TextView(this);
        footer.setText("BAREZGO  •  DIRECT MP4"); footer.setTextColor(Color.rgb(95,111,135));
        footer.setGravity(Gravity.CENTER); footer.setTextSize(10);
        lp=match(); lp.topMargin=dp(30); root.addView(footer,lp);
        setContentView(scroll);
    }

    private void pasteLink() {
        try {
            ClipboardManager cb=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            if(cb!=null && cb.hasPrimaryClip()) {
                ClipData clip=cb.getPrimaryClip();
                if(clip!=null && clip.getItemCount()>0) {
                    linkInput.setText(clip.getItemAt(0).coerceToText(this));
                    status.setText("Tautan ditempel"); detail.setText("Tekan UNDUH MP4 untuk memulai.");
                }
            } else status.setText("Clipboard kosong");
        } catch(Exception e) { status.setText("Clipboard tidak bisa dibaca"); }
    }

    private void resolveAndDownload() {
        String raw=linkInput.getText().toString().trim();
        if(raw.isEmpty()){status.setText("Tautan belum diisi");return;}
        Uri uri;
        try { uri=Uri.parse(raw); }
        catch(Exception e){status.setText("Tautan tidak valid");return;}
        if(uri.getHost()==null || uri.getScheme()==null ||
           !(uri.getScheme().equalsIgnoreCase("http")||uri.getScheme().equalsIgnoreCase("https"))) {
            status.setText("Format tautan tidak valid");
            detail.setText("Gunakan tautan http:// atau https://"); return;
        }
        activeUrl=raw; hideKeyboard();
        actionButton.setEnabled(false); actionButton.setText("MEMPROSES...");
        status.setText("Menghubungi server downloader");
        detail.setText("Sedang mengambil informasi video. Tunggu sebentar...");
        if(isDirectMedia(uri.getHost(),uri.getPath())) {
            enqueueDownload(raw,"BAREZGO-video.mp4","video/mp4");
            return;
        }
        executor.execute(() -> {
            HttpURLConnection c=null;
            try {
                URL endpoint=new URL(API+"/api/download");
                c=(HttpURLConnection)endpoint.openConnection();
                c.setRequestMethod("POST"); c.setConnectTimeout(20000); c.setReadTimeout(45000);
                c.setDoOutput(true); c.setRequestProperty("Content-Type","application/json");
                c.setRequestProperty("Accept","application/json");
                JSONObject body=new JSONObject();
                body.put("url",raw); body.put("downloadMode","auto");
                body.put("videoQuality","1080"); body.put("youtubeVideoContainer","mp4");
                body.put("alwaysProxy",true); body.put("localProcessing","disabled");
                byte[] bytes=body.toString().getBytes(StandardCharsets.UTF_8);
                try(OutputStream os=c.getOutputStream()){os.write(bytes);}
                int code=c.getResponseCode();
                InputStream is=code>=200&&code<300?c.getInputStream():c.getErrorStream();
                StringBuilder sb=new StringBuilder();
                if(is!=null) try(BufferedReader br=new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8))){
                    String line; while((line=br.readLine())!=null) sb.append(line);
                }
                JSONObject result=new JSONObject(sb.toString());
                runOnUiThread(() -> handleResult(result,code));
            } catch(Exception e) {
                runOnUiThread(() -> {
                    actionButton.setEnabled(true); actionButton.setText("COBA LAGI");
                    status.setText("Server belum siap");
                    detail.setText("Periksa koneksi atau konfigurasi server BAREZGO. "+(e.getMessage()==null?"":e.getMessage()));
                });
            } finally { if(c!=null)c.disconnect(); }
        });
    }

    private void handleResult(JSONObject r,int httpCode) {
        actionButton.setEnabled(true); actionButton.setText("UNDUH MP4");
        String state=r.optString("status","");
        if(httpCode<200||httpCode>=300||"error".equals(state)) {
            JSONObject err=r.optJSONObject("error");
            String code=err==null?"":err.optString("code","");
            status.setText("Video belum bisa diproses");
            detail.setText(code.isEmpty()?"Server mengembalikan HTTP "+httpCode: "Kode: "+code);
            return;
        }
        if("tunnel".equals(state)||"redirect".equals(state)) {
            String url=r.optString("url","");
            String name=r.optString("filename","BAREZGO-video.mp4");
            if(url.startsWith("https://")||url.startsWith("http://")) {
                enqueueDownload(url,name,"video/mp4");
            } else {
                status.setText("Alamat unduhan tidak valid");
                detail.setText("Server tidak mengirim URL file yang dapat diunduh.");
            }
        } else if("picker".equals(state)) {
            status.setText("Tautan berisi beberapa media");
            detail.setText("Server mengembalikan beberapa pilihan media. Pemilih media akan ditambahkan pada pembaruan berikutnya.");
        } else {
            status.setText("Format hasil belum didukung");
            detail.setText("Status server: "+state);
        }
    }

    private boolean isDirectMedia(String host,String path) {
        String h=host.toLowerCase(Locale.ROOT);
        if(h.equals("tiktok.com")||h.endsWith(".tiktok.com")||h.equals("instagram.com")||
           h.endsWith(".instagram.com")||h.equals("youtube.com")||h.endsWith(".youtube.com")||
           h.equals("youtu.be")) return false;
        if(path==null)return false;
        String p=path.toLowerCase(Locale.ROOT);
        return p.matches(".*\\.(mp4|m4v|webm|mov|mkv)$");
    }

    private void enqueueDownload(String url,String filename,String mime) {
        try {
            String safe=filename==null||filename.trim().isEmpty()?"BAREZGO-video.mp4":filename.replaceAll("[\\\\/:*?\"<>|]","_");
            DownloadManager.Request req=new DownloadManager.Request(Uri.parse(url));
            req.setTitle("BAREZGO • "+safe); req.setDescription("Mengunduh video ke folder Download/BAREZGO");
            req.setMimeType(mime); req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setAllowedOverMetered(true); req.setAllowedOverRoaming(false);
            req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"BAREZGO/"+safe);
            DownloadManager dm=(DownloadManager)getSystemService(DOWNLOAD_SERVICE);
            if(dm==null)throw new IllegalStateException("Download Manager Android tidak tersedia");
            dm.enqueue(req);
            status.setText("Unduhan dimulai");
            detail.setText("File: "+safe+"\nLokasi: Download/BAREZGO. Pantau progres melalui notifikasi Android.");
            actionButton.setEnabled(true); actionButton.setText("UNDUH VIDEO LAIN");
        } catch(Exception e) {
            status.setText("Gagal memulai unduhan");
            detail.setText(e.getMessage()==null?"Periksa kembali tautan.":e.getMessage());
            actionButton.setEnabled(true); actionButton.setText("COBA LAGI");
        }
    }

    private void hideKeyboard() {
        try { InputMethodManager imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(linkInput.getWindowToken(),0); } catch(Exception ignored){}
    }
    private Button button(String label,int color,int textColor) {
        Button b=new Button(this); b.setText(label); b.setTextColor(textColor); b.setTextSize(12);
        b.setTypeface(Typeface.DEFAULT,Typeface.BOLD); b.setAllCaps(false); b.setBackground(round(color,12)); return b;
    }
    private GradientDrawable round(int color,int radius) {
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private LinearLayout.LayoutParams match(){return new LinearLayout.LayoutParams(-1,-2);}
    private LinearLayout.LayoutParams wrap(){return new LinearLayout.LayoutParams(-2,-2);}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    @Override protected void onDestroy(){super.onDestroy();executor.shutdownNow();}
}

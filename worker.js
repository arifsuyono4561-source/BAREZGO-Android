
export default {
  async fetch(request) {
    const cors = {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type"
    };

    const json = (data, status = 200) =>
      new Response(JSON.stringify(data), {
        status,
        headers: {
          ...cors,
          "Content-Type": "application/json"
        }
      });

    if (request.method === "OPTIONS") {
      return new Response(null, { headers: cors });
    }

    const url = new URL(request.url);

    if (url.pathname === "/") {
      return json({
        app: "BAREZGO API",
        version: "2.1",
        status: "online"
      });
    }

    if (url.pathname === "/health") {
      return json({ status: "ok" });
    }

    if (url.pathname === "/api/detect" &&
        request.method === "POST") {
      let body;

      try {
        body = await request.json();
      } catch {
        return json({ error: "JSON tidak valid" }, 400);
      }

      let input;

      try {
        input = new URL(String(body.url || ""));
      } catch {
        return json({ error: "Tautan tidak valid" }, 400);
      }

      if (input.protocol !== "https:" &&
          input.protocol !== "http:") {
        return json({ error: "Protokol tidak didukung" }, 400);
      }

      const host = input.hostname.toLowerCase();
      let platform = "unsupported";
      let title = "Platform tidak didukung";
      let detail = "Gunakan tautan dari platform yang didukung.";
      let action = "Tidak tersedia";

      if (host === "tiktok.com" ||
          host.endsWith(".tiktok.com")) {
        platform = "tiktok";
        title = "Video TikTok terdeteksi";
        detail =
          "Buka video di TikTok. Gunakan fitur Simpan video jika diizinkan kreator.";
        action = "Buka TikTok";
      } else if (
        host === "instagram.com" ||
        host.endsWith(".instagram.com")
      ) {
        platform = "instagram";
        title = "Konten Instagram terdeteksi";
        detail =
          "Buka konten di Instagram. Gunakan opsi unduh resmi jika tersedia.";
        action = "Buka Instagram";
      } else if (
        host === "youtube.com" ||
        host.endsWith(".youtube.com") ||
        host === "youtu.be"
      ) {
        platform = "youtube";
        title = "Video YouTube terdeteksi";
        detail =
          "Tonton melalui YouTube. Unduhan offline mengikuti fitur resmi YouTube.";
        action = "Buka YouTube";
      }

      return json({
        app: "BAREZGO",
        version: "2.1",
        platform,
        detected: platform !== "unsupported",
        title,
        message: detail,
        action,
        originalUrl: input.toString(),
        downloadAvailable: false
      });
    }

    return json({ error: "Endpoint tidak ditemukan" }, 404);
  }
};

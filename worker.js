
export default {
  async fetch(request) {
    const corsHeaders = {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type"
    };

    const json = (data, status = 200) =>
      new Response(JSON.stringify(data), {
        status,
        headers: {
          ...corsHeaders,
          "Content-Type": "application/json"
        }
      });

    if (request.method === "OPTIONS") {
      return new Response(null, { headers: corsHeaders });
    }

    const url = new URL(request.url);

    if (url.pathname === "/") {
      return json({
        app: "BAREZGO API",
        version: "2.0",
        status: "online"
      });
    }

    if (url.pathname === "/health") {
      return json({ status: "ok" });
    }

    if (url.pathname === "/api/detect" && request.method === "POST") {
      let body;

      try {
        body = await request.json();
      } catch {
        return json({ error: "Invalid JSON" }, 400);
      }

      const link = String(body.url || "");

      let platform = "unsupported";

      if (/tiktok\.com/i.test(link)) platform = "tiktok";
      else if (/instagram\.com/i.test(link)) platform = "instagram";
      else if (/youtube\.com|youtu\.be/i.test(link)) platform = "youtube";

      return json({
        app: "BAREZGO",
        platform,
        detected: platform !== "unsupported",
        message: "Link berhasil diperiksa. Media belum diproses."
      });
    }

    return json({ error: "Endpoint not found" }, 404);
  }
};

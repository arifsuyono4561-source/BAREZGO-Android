export default {
  async fetch(request, env) {
    const cors = {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type, Authorization"
    };
    const json = (data, status = 200) => new Response(JSON.stringify(data), {
      status, headers: { ...cors, "Content-Type": "application/json" }
    });
    if (request.method === "OPTIONS") return new Response(null, { headers: cors });
    const url = new URL(request.url);

    if (url.pathname === "/" || url.pathname === "/health") {
      return json({
        app: "BAREZGO API",
        version: "3.0",
        status: "online",
        downloaderConfigured: Boolean(env.COBALT_API_URL)
      });
    }

    if (url.pathname === "/api/download" && request.method === "POST") {
      if (!env.COBALT_API_URL) {
        return json({ status: "error", error: { code: "BAREZGO_BACKEND_NOT_CONFIGURED" } }, 503);
      }
      let body;
      try { body = await request.json(); }
      catch { return json({ status: "error", error: { code: "INVALID_JSON" } }, 400); }

      let input;
      try { input = new URL(String(body.url || "")); }
      catch { return json({ status: "error", error: { code: "INVALID_URL" } }, 400); }
      if (input.protocol !== "https:") {
        return json({ status: "error", error: { code: "HTTPS_REQUIRED" } }, 400);
      }

      const host = input.hostname.toLowerCase();
      const allowed = [
        "tiktok.com", "instagram.com", "youtube.com", "youtu.be",
        "facebook.com", "fb.watch", "twitter.com", "x.com",
        "reddit.com", "vimeo.com", "pinterest.com"
      ];
      if (!allowed.some(domain => host === domain || host.endsWith("." + domain))) {
        return json({ status: "error", error: { code: "PLATFORM_NOT_SUPPORTED" } }, 400);
      }

      let cobalt;
      try { cobalt = new URL(env.COBALT_API_URL); }
      catch { return json({ status: "error", error: { code: "INVALID_BACKEND_URL" } }, 500); }
      if (cobalt.protocol !== "https:") {
        return json({ status: "error", error: { code: "BACKEND_MUST_USE_HTTPS" } }, 500);
      }

      const headers = { "Accept": "application/json", "Content-Type": "application/json" };
      if (env.COBALT_API_KEY) headers.Authorization = "Api-Key " + env.COBALT_API_KEY;
      const payload = {
        url: input.toString(),
        downloadMode: "auto",
        videoQuality: "1080",
        youtubeVideoContainer: "mp4",
        alwaysProxy: true,
        localProcessing: "disabled"
      };

      try {
        const response = await fetch(new URL("/", cobalt).toString(), {
          method: "POST", headers, body: JSON.stringify(payload),
          signal: AbortSignal.timeout(45000)
        });
        const text = await response.text();
        let result;
        try { result = JSON.parse(text); }
        catch { return json({ status: "error", error: { code: "BACKEND_INVALID_RESPONSE" } }, 502); }
        return json(result, response.status);
      } catch (error) {
        return json({ status: "error", error: { code: "BACKEND_UNAVAILABLE" } }, 502);
      }
    }
    return json({ error: "Endpoint tidak ditemukan" }, 404);
  }
};

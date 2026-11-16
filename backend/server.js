
const express = require("express");

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());

// CORS untuk aplikasi Android
app.use((req, res, next) => {
  res.header("Access-Control-Allow-Origin", "*");
  res.header(
    "Access-Control-Allow-Headers",
    "Content-Type"
  );
  res.header(
    "Access-Control-Allow-Methods",
    "GET, POST, OPTIONS"
  );

  if (req.method === "OPTIONS") {
    return res.sendStatus(204);
  }

  next();
});

// Pemeriksaan server
app.get("/", (req, res) => {
  res.json({
    app: "BAREZGO API",
    status: "online",
    version: "1.0.0"
  });
});

// Pemeriksaan kesehatan server
app.get("/health", (req, res) => {
  res.json({ status: "ok" });
});

// Endpoint permintaan video
app.post("/api/download", (req, res) => {
  const { url } = req.body;

  if (!url) {
    return res.status(400).json({
      error: "Link video wajib diisi"
    });
  }

  let parsed;

  try {
    parsed = new URL(url);
  } catch {
    return res.status(400).json({
      error: "Link tidak valid"
    });
  }

  if (parsed.protocol !== "https:") {
    return res.status(400).json({
      error: "Hanya link HTTPS yang diterima"
    });
  }

  return res.status(501).json({
    status: "provider_not_configured",
    message:
      "Layanan pengunduhan belum dikonfigurasi."
  });
});

app.listen(PORT, "0.0.0.0", () => {
  console.log(
    `BAREZGO API berjalan di port ${PORT}`
  );
});

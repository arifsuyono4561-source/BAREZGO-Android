# BAREZGO Android Downloader

BAREZGO is an Android client that accepts a supported media URL and saves the returned file in `Download/BAREZGO`.

## Architecture

- Android app: paste link, request processing, download the returned file using Android DownloadManager.
- Cloudflare Worker: `worker.js`, endpoint `POST /api/download`, proxies requests to the private/self-hosted Cobalt API.
- Cobalt API: self-hosted processing service. Its current supported-services list includes TikTok, Instagram and YouTube. Availability can change and platform restrictions still apply.

## Required backend setup

The Android client calls:

`https://barezgo-api.arif-suyono4561.workers.dev/api/download`

The Cloudflare Worker must be deployed from `worker.js`. In Worker Settings > Variables and Secrets, set:

- `COBALT_API_URL`: public HTTPS URL of your own Cobalt API, with trailing slash, e.g. `https://download-api.example.com/`.
- `COBALT_API_KEY`: optional API key if authentication is enabled on the Cobalt instance. Store it as a secret.

The Cobalt API must run on a Linux server/VPS with Docker. A Cloudflare Worker alone cannot run the Cobalt processing engine.

### Deploy Cobalt

1. Provision a Linux VPS with Docker and Docker Compose.
2. Point a domain's DNS to the VPS and configure HTTPS using a reverse proxy such as Caddy or Nginx.
3. Copy `deploy/cobalt/docker-compose.yml` and `.env.example` to the server.
4. Set `COBALT_PUBLIC_URL` in the server's `.env` to the public HTTPS API address.
5. Run `docker compose up -d`.
6. Verify `https://YOUR-DOMAIN.example.com/` returns Cobalt instance information.
7. Set the Cloudflare Worker variable `COBALT_API_URL` to the same URL, then redeploy the Worker.

Do not expose an unauthenticated processing server to the public internet. Configure API-key or Turnstile protection and a reverse proxy before production use. Keep API keys in server/Worker secrets, never in the Android app.

## Android build

GitHub Actions builds a debug APK on pushes to `main`. Download the `BAREZGO-debug-apk` artifact from the latest successful workflow run.

## Current implementation notes

- Direct MP4 links are downloaded directly.
- Social-platform links are sent to Cobalt through the Worker.
- Standard Cobalt `tunnel` and `redirect` results are handed to Android DownloadManager.
- Cobalt `picker` results (multi-item posts) are reported but the in-app media picker is not implemented yet.
- This integration build still requires a working Cobalt server and Worker deployment. It has not yet been device-tested.

Use only content you own or have permission to download. The app does not bypass DRM, private-content restrictions, or platform access controls.

# Installation

## 1. Prerequisites

- Clojure with tools.deps (the official CLI). Install via the [getting started guide](https://clojure.org/guides/getting_started).

## 2. Get the Code

Clone the repository:

```bash
git clone <repository-url>
cd nova
```

## 3. Install Dependencies

Run the provided installation script to download Chart.js and set up fonts:

```bash
./install.sh
```

This will place Chart.js under the configured `CHART_JS_DIR` and generate font CSS. The script uses the environment variables `CHART_JS_DIR`, `CHART_JS_FILE`, `CHART_VERSION`, `FONTS_DIR`, `FONTS_URL`, `FONTS_CSS`, and `FONT_CSS_CONTENT` (see [README.md](README.md#environment-variables) for details). You can override them if needed, but the defaults are suitable for local development.

## 4. Run the Development Server

Start the dashboard with:

```bash
clojure -M -m nova.core
```

The server will start at `http://localhost:3000` by default.

## 5. Build for Production

To create a standalone uberjar, use the build alias:

```bash
clojure -T:build
```

## Troubleshooting

- **Missing fonts or Chart.js:** Ensure you ran `./install.sh`. Check that the environment variables point to valid paths.
- **Port already in use:** The default port is 3000. If that port is occupied, set the `PORT` environment variable before starting.
- **Clojure CLI not found:** Verify your Clojure tools installation and that the `clojure` command is on your PATH.
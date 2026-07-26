#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

echo "=== Nova — Install ==="
echo ""

# ── 1. Verify toolchain ───────────────────────────────────
command -v clojure >/dev/null 2>&1 || { echo "ERROR: clojure CLI not found on PATH"; exit 1; }
echo "✓ clojure CLI found"

# ── 2. Download Clojure dependencies (prefetch) ────────────
echo "▶ Resolving Clojure dependencies..."
clojure -P -M:run 2>&1 | tail -1
echo "✓ Dependencies resolved"

# ── 3. Download Chart.js (self-hosted) ─────────────────────
CHART_JS_DIR="resources/public/js"
CHART_JS_FILE="$CHART_JS_DIR/chart.umd.min.js"
CHART_VERSION="4.4.7"

if [ ! -f "$CHART_JS_FILE" ]; then
  echo "▶ Downloading Chart.js v$CHART_VERSION..."
  mkdir -p "$CHART_JS_DIR"
  curl -fsSL -o "$CHART_JS_FILE" \
    "https://cdn.jsdelivr.net/npm/chart.js@${CHART_VERSION}/dist/chart.umd.min.js" \
    || { echo "ERROR: Failed to download Chart.js"; exit 1; }
  echo "✓ Chart.js downloaded ($(wc -c < "$CHART_JS_FILE") bytes)"
else
  echo "✓ Chart.js already present ($(wc -c < "$CHART_JS_FILE") bytes)"
fi

# ── 4. Download font files (self-hosted) ───────────────────
FONTS_DIR="resources/public/fonts"
FONTS_CSS="resources/public/css/fonts.css"

if [ ! -f "$FONTS_CSS" ]; then
  echo "▶ Downloading web fonts (Plus Jakarta Sans + Public Sans)..."
  mkdir -p "$FONTS_DIR"

  # Fetch the Google Fonts CSS
  FONTS_URL="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;700&family=Public+Sans:wght@400;500;700&display=swap"
  FONT_CSS_CONTENT=$(curl -fsSL -H "User-Agent: Mozilla/5.0" "$FONTS_URL" 2>/dev/null || echo "")

  if [ -n "$FONT_CSS_CONTENT" ]; then
    # Extract woff2 URLs and download each
    echo "$FONT_CSS_CONTENT" | grep -oP 'url\(\Khttps://[^)]+\.woff2' | sort -u | while read -r url; do
      fname=$(basename "$url" | cut -d'?' -f1)
      echo "  Downloading $fname..."
      curl -fsSL -o "$FONTS_DIR/$fname" "$url" || echo "  WARN: Failed to download $fname"
    done

    # Rewrite CSS to use local paths
    echo "$FONT_CSS_CONTENT" | sed 's|https://fonts.gstatic.com/[^)]*|/fonts/|g' | \
      sed 's|url(/fonts/[^)]*|&|g' | \
      perl -pe 's|url\(https://fonts.gstatic.com/[^)]+\)|url(/fonts/\1)|g' 2>/dev/null || \
      echo "$FONT_CSS_CONTENT" | sed -E 's|url\(https://fonts\.gstatic\.com/[^)]+/([^/?)]+\.[^/?)"]+)([^)]*)\)|url(/fonts/\1\2)|g' \
      > /tmp/nova-fonts.css 2>/dev/null || true

    # Simpler approach: just use sed to replace the domain with /fonts/
    echo "$FONT_CSS_CONTENT" | sed 's|https://fonts.gstatic.com/s/|/fonts/|g' > "$FONTS_CSS"

    echo "✓ Fonts CSS written to $FONTS_CSS"
  else
    echo "⚠ Could not fetch Google Fonts CSS — creating fallback"
    # Fallback: create a minimal fonts.css that uses system fonts
    cat > "$FONTS_CSS" << 'FALLBACK'
/* Fallback font declarations — system fonts will be used */
@font-face {
  font-family: 'Plus Jakarta Sans';
  font-style: normal;
  font-weight: 400;
  src: local('Plus Jakarta Sans'), local('PlusJakartaSans');
}
@font-face {
  font-family: 'Public Sans';
  font-style: normal;
  font-weight: 400;
  src: local('Public Sans'), local('PublicSans');
}
FALLBACK
  fi
else
  echo "✓ Fonts CSS already present"
fi

# ── 5. Run seed (idempotent) ───────────────────────────────
echo "▶ Running seed..."
clojure -M:seed
echo "✓ Seed complete"

# ── Done ──────────────────────────────────────────────────
echo ""
echo "=== Install complete ==="
echo "Run the server:  clojure -M:run"
echo "Dashboard:       http://localhost:3000"

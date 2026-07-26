# Usage

Once the server is running (via `clojure -M -m nova.core`), open a browser to:

```
http://localhost:3000
```

## Screens

### Dashboard (`/`)

The main overview with history charts (powered by Chart.js) showing device trends. Toggle between light and dark themes using the control in the header.

### Devices (`/devices`)

List all configured smart home devices. Each device shows its name, type, and current status.

### Device Detail (`/devices/:id`)

Click a device on the Devices page to view detailed information, including real-time data and individual controls (e.g., on/off, brightness).

### Settings (`/settings`)

Adjust user preferences, theme selection, and other dashboard configuration options.

## API Endpoints

The backend exposes RESTful endpoints used by the frontend:

- `GET /api/devices` – list all devices (JSON)
- `GET /api/devices/:id` – get a single device's details
- `POST /api/devices/:id/action` – send a command to a device (payload: `{"action": "..."}`)

Example using curl:

```bash
curl http://localhost:3000/api/devices
```

```bash
curl -X POST http://localhost:3000/api/devices/1/action \
  -H "Content-Type: application/json" \
  -d '{"action": "toggle"}'
```

All device data is seeded from `src/nova/seed.clj` when the server starts.
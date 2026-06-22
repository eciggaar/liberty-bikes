# Grafana Dashboard Setup - 2026-06-22

## Summary
Successfully configured Grafana monitoring dashboard for Liberty Bikes application and resolved game service SSL certificate issues.

## Actions Taken

### 1. Grafana Port Configuration
- **Issue**: Port 3000 occupied by Lima (Docker infrastructure)
- **Solution**: Changed Grafana host port from 3000 to 3002
- **Command**: Modified `startMonitoring.sh` to use `-p 3002:3000`
- **Result**: Grafana now accessible at http://localhost:3002

### 2. Game Service SSL Certificate Issue
- **Issue**: Browser rejecting self-signed SSL certificate (expired 2021)
- **Error**: `net::ERR_CERT_AUTHORITY_INVALID` on https://localhost:8443
- **Solution**: Switched to HTTP instead of HTTPS for local development

### 3. Port Reconfiguration
**Frontend** (`frontend/prebuild/src/environments/environment.ts`):
- Changed game service URLs from HTTPS port 8443 to HTTP port 9080
- `API_URL_PARTY`: https://localhost:8443 → http://localhost:9080
- `API_URL_GAME_ROUND`: https://localhost:8443 → http://localhost:9080  
- `API_URL_GAME_WS`: wss://localhost:8443 → ws://localhost:9080

**Game Service** (`game-service/build.gradle`):
- Changed `httpPort` from 8080 to 9080
- Changed `httpsPort` from 8443 to 9443
- Cleaned and restarted service

### 4. Current Status
✅ Grafana running on port 3002 (credentials: admin/admin)
✅ Game service on HTTP port 9080, HTTPS port 9443
✅ Frontend rebuilt and restarted
✅ Game playable without SSL errors
⚠️ Stats not showing in Grafana dashboard

## Next Steps
- Investigate why metrics aren't appearing in Grafana
- Check Prometheus data source configuration
- Verify MicroProfile Metrics endpoints are accessible
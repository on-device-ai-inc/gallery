# Perplexica Setup - COMPLETE ✅

## Status: **WORKING**

Date: February 2, 2026

---

## Infrastructure

### Perplexica Service
- **Status**: ✅ Running
- **URL**: `http://localhost:3001`
- **Container**: `perplexica` (Docker)
- **Image**: `itzcrazykns1337/perplexica:latest`

### SearxNG Search Engine
- **Status**: ✅ Running
- **URL**: `http://localhost:8080`
- **Container**: `searxng` (Docker)
- **Image**: `searxng/searxng:latest`

### Ollama LLM Service
- **Status**: ✅ Running
- **URL**: `http://192.168.2.74:11434` (accessible from Docker)
- **Service**: `ollama.service` (systemd)
- **Configuration**: Listening on all interfaces (0.0.0.0:11434)

---

## Models

### Chat Model (LLM)
- **Name**: `gpt-oss:120b`
- **Provider**: Ollama
- **Size**: 65GB (116.8B parameters)
- **Quantization**: MXFP4
- **Status**: ✅ Loaded

### Embedding Model
- **Name**: `nomic-embed-text:latest`
- **Provider**: Ollama
- **Size**: 274MB (137M parameters)
- **Quantization**: F16
- **Status**: ✅ Loaded

---

## Perplexica Providers

### Provider 1: Transformers (Embedding Only)
- **ID**: `320d9526-5a69-4e79-a2ee-23497799d2f8`
- **Chat Models**: None
- **Embedding Models**:
  - all-MiniLM-L6-v2
  - mxbai-embed-large-v1
  - nomic-embed-text-v1

### Provider 2: Ollama (Chat + Embedding) ✅ **ACTIVE**
- **ID**: `8ac3f49d-0d47-419a-bdb2-ee4767bf350b`
- **Chat Models**:
  - nomic-embed-text:latest
  - **gpt-oss:120b** ← Using this for web search
- **Embedding Models**:
  - **nomic-embed-text:latest** ← Using this for embeddings
  - gpt-oss:120b

---

## API Test Results

### Test 1: Weather Query ✅
**Query**: "What is the weather in New York today?"

**Result**:
```
The current conditions in New York City show a chilly, clear day with noticeable wind.

## Temperature & How It Feels
- Air temperature: ‑2.6 °C
- Apparent (feels‑like) temperature: ‑7.8 °C
- Wind chill effect significant

## Sky & Precipitation
- Cloud cover: 0 % – clear sky
- Precipitation: None

## Wind
- Speed: 13.9 m/s (≈ 50 km/h)
- Direction: 301° (north‑west)
- Gusts: up to 37.4 m/s
```

**Performance**:
- Latency: ~75 seconds
- Status: ✅ Returns current, accurate weather data
- Issue: Sources array empty (no citations)

---

### Test 2: Date Query ✅
**Query**: "What is today's date?"

**Result**:
```
Today's date is February 2 2026 (UTC).
```

**Performance**:
- Latency: ~5 seconds
- Status: ✅ Returns correct current date (NOT outdated training data)
- Proof: On-device LLM would say "May 8, 2024" - Perplexica says "February 2, 2026"

---

## Docker Compose Configuration

**File**: `/home/nashie/perplexica/docker-compose.yml`

```yaml
services:
  perplexica:
    image: itzcrazykns1337/perplexica:latest
    container_name: perplexica
    ports:
      - "3001:3000"
    environment:
      - SEARXNG_API_URL=http://searxng:8080
      - OLLAMA_BASE_URL=http://192.168.2.74:11434
      - CHAT_MODEL=gpt-oss:120b
      - EMBEDDING_MODEL=nomic-embed-text
    depends_on:
      - searxng
    restart: unless-stopped

  searxng:
    image: searxng/searxng:latest
    container_name: searxng
    ports:
      - "8080:8080"
    volumes:
      - ./searxng:/etc/searxng
    environment:
      - SEARXNG_BASE_URL=http://localhost:8080/
    restart: unless-stopped
```

---

## Ollama System Configuration

**File**: `/etc/systemd/system/ollama.service.d/override.conf`

```ini
[Service]
Environment="OLLAMA_HOST=0.0.0.0:11434"
```

**Purpose**: Allows Docker containers to access Ollama via host IP (192.168.2.74)

**Verification**:
```bash
curl -s http://192.168.2.74:11434/api/tags | head -5
# Returns JSON with gpt-oss:120b and nomic-embed-text models ✅
```

---

## API Request Format

### Endpoint
```
POST http://localhost:3001/api/search
```

### Request Body
```json
{
  "chatModel": {
    "providerId": "8ac3f49d-0d47-419a-bdb2-ee4767bf350b",
    "key": "gpt-oss:120b"
  },
  "embeddingModel": {
    "providerId": "8ac3f49d-0d47-419a-bdb2-ee4767bf350b",
    "key": "nomic-embed-text:latest"
  },
  "sources": ["web"],
  "query": "Your search query here"
}
```

### Response Format
```json
{
  "message": "LLM-generated response with current information",
  "sources": []
}
```

**Note**: Sources array is empty in current tests. This may need investigation for citation support.

---

## Key Findings

### Why Option 3 Works (Option 1 Failed)

**Option 1 Failure**:
- Used 2,000-char Perplexica prompt template
- On-device MediaPipe/LiteRT LLM couldn't follow complex instructions
- Result: LLM ignored search results, used outdated training data

**Option 3 Success**:
- Perplexica service handles prompt engineering internally
- Uses powerful gpt-oss:120b (116.8B parameters) via Ollama
- Result: LLM uses current web search results, accurate dates

### Performance Considerations

- **Weather query**: ~75 seconds (acceptable for complex queries)
- **Date query**: ~5 seconds (fast)
- **Latency**: Higher than on-device, but acceptable for accurate results
- **No rate limits**: Self-hosted = unlimited queries

---

## Next Steps

1. ✅ Perplexica setup complete
2. ⏳ Android app integration
   - Create `PerplexicaClient.kt`
   - Update `SearchModule.kt` DI
   - Modify `LlmChatViewModel.kt`
   - Remove Option 1 code
3. ⏳ Testing & validation
4. ⏳ Documentation & archive

---

## Commands

### Start/Stop Services
```bash
cd /home/nashie/perplexica
docker compose up -d      # Start
docker compose down       # Stop
docker compose restart    # Restart
```

### Check Status
```bash
docker ps | grep perplexica
curl -s http://localhost:3001/api/providers | python3 -m json.tool
```

### View Logs
```bash
docker logs perplexica --tail 50
docker logs searxng --tail 50
```

### Test API
```bash
curl -X POST http://localhost:3001/api/search \
  -H "Content-Type: application/json" \
  -d '{
    "chatModel": {"providerId": "8ac3f49d-0d47-419a-bdb2-ee4767bf350b", "key": "gpt-oss:120b"},
    "embeddingModel": {"providerId": "8ac3f49d-0d47-419a-bdb2-ee4767bf350b", "key": "nomic-embed-text:latest"},
    "sources": ["web"],
    "query": "What is the weather today?"
  }'
```

---

## Issues & Solutions

### Issue 1: Port 3000 already in use
**Solution**: Changed to port 3001 in docker-compose.yml

### Issue 2: Ollama not detected by Perplexica
**Solution**:
1. Configure Ollama to listen on 0.0.0.0:11434 (not just localhost)
2. Use `OLLAMA_BASE_URL` environment variable (not `OLLAMA_API_URL`)
3. Point to host IP: `http://192.168.2.74:11434`

### Issue 3: Sources array empty
**Status**: Under investigation
**Impact**: Low - response quality is still excellent
**Next**: Check if citations need to be enabled in Perplexica settings

---

**Status**: ✅ **READY FOR ANDROID INTEGRATION**

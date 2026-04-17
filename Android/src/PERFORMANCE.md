# Performance SLAs

## Cold Start
- Target: < 2000ms
- P95: < 3000ms

## Model Initialization
- Target: < 5000ms (small models < 1GB)
- Target: < 15000ms (large models > 1GB)
- P95: < 20000ms

## Inference (LLM)
- TTFT Target: < 500ms
- TTFT P95: < 1000ms
- Decode Speed Target: > 10 tokens/sec
- Decode Speed P50: > 5 tokens/sec

## Image Generation
- Target: < 30000ms (512x512, 20 steps)
- P95: < 45000ms

## Model Download
- Target: > 5 Mbps average download speed
- P50: > 3 Mbps

## Database Queries
- Complex queries: < 100ms
- Simple queries: < 10ms
- P95 complex: < 200ms

## Compaction
- Target: < 10000ms per conversation
- Compression ratio: > 2x
- P95: < 20000ms

## Screen Rendering
- Target: < 500ms to first paint
- P95: < 1000ms

## Measurement Methodology

- **Cold Start**: MainActivity.onCreate() → onResume()
- **Model Init**: Start of model loading → model ready for inference
- **TTFT**: Message sent → first token received
- **Decode Speed**: Total tokens / total time (excluding TTFT)
- **Image Gen**: Prompt submitted → image bitmap ready
- **Download**: Download start → file written to disk
- **DB Queries**: Query method entry → result returned
- **Compaction**: Compaction start → new summary stored
- **Screen Render**: Composable first composition → UI stable

## Monitoring

All metrics tracked via Firebase Performance custom traces.
View real-time data in Firebase Console → Performance.

## Alerting Thresholds

- P95 exceeds target by 2x: Warning
- P95 exceeds target by 3x: Critical
- Any operation > 60s: Timeout error

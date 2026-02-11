# Qwen2.5-Omni-7B LiteRT Conversion Guide

## Executive Summary

**Status**: ⚠️ **Qwen2.5-Omni-7B conversion to LiteRT is COMPLEX**

The model uses advanced multimodal architecture (Thinker-Talker, Whisper audio encoder, TMRoPE for video sync) that isn't yet fully supported by AI Edge Torch Generative API.

## Three Approaches

### Approach 1: Use Pre-Converted MNN Model ⭐ FASTEST
**Timeline**: Immediate
**Difficulty**: Medium (requires app architecture change)

```bash
# Model already available on HuggingFace
https://huggingface.co/taobao-mnn/Qwen2.5-Omni-7B-MNN
https://huggingface.co/taobao-mnn/Qwen2.5-Omni-3B-MNN
```

**Pros**:
- ✅ Already converted and optimized (4-bit quantization)
- ✅ Tested and working on mobile
- ✅ Full multimodal support (text + vision + audio + video)

**Cons**:
- ❌ Uses MNN framework instead of LiteRT
- ❌ Requires rewriting inference layer in your app
- ❌ Different API from current MediaPipe/LiteRT stack

**Viability**: **60%** - Would work but requires significant refactoring

---

### Approach 2: Convert LiteRT Yourself 🔧 COMPLEX
**Timeline**: 1-2 weeks
**Difficulty**: Hard (requires ML engineering expertise)

Use the provided `convert_qwen_omni_to_litert.py` script as starting point.

**Steps**:
1. Install dependencies:
   ```bash
   pip install ai-edge-torch torch transformers safetensors
   ```

2. Run conversion:
   ```bash
   python convert_qwen_omni_to_litert.py
   ```

3. Expected challenges:
   - Audio encoder (Whisper-based) requires custom integration
   - Vision encoder may need separate conversion
   - Thinker-Talker architecture needs model authoring
   - Streaming speech generation not supported in LiteRT yet

**Pros**:
- ✅ Native LiteRT integration (works with your current app)
- ✅ Full control over quantization and optimization

**Cons**:
- ❌ Requires deep ML expertise
- ❌ May need to wait for AI Edge Torch updates
- ❌ Multimodal components need custom authoring
- ❌ Time-intensive (1-2 weeks minimum)

**Viability**: **30%** - Technically possible but very difficult

---

### Approach 3: Start with Qwen2.5-VL-7B 🎯 RECOMMENDED
**Timeline**: 2-3 days
**Difficulty**: Medium (manageable)

Use **Qwen2.5-VL-7B** (vision-only) instead of Omni variant.

**Why VL instead of Omni**:
- ✅ Simpler architecture (text + vision only, no audio complexity)
- ✅ Better AI Edge Torch support (transformer-based, standard ViT)
- ✅ Pre-quantized versions available:
  - `neuralmagic/Qwen2.5-VL-7B-Instruct-quantized.w4a16`
  - `RedHatAI/Qwen2.5-VL-7B-Instruct-quantized.w4a16`
- ✅ Can add Whisper separately for audio (modular approach)

**Steps**:
1. Download pre-quantized Qwen2.5-VL-7B
2. Convert using AI Edge Torch (simpler than Omni)
3. Upload to your HuggingFace
4. Add Whisper-Tiny separately for audio input
5. Test integration

**Timeline**:
- Day 1: Download and convert Qwen2.5-VL-7B
- Day 2: Create .litertlm bundle, upload to HF
- Day 3: Test integration with app

**Viability**: **90%** - Highly likely to succeed

---

## Recommended Path Forward

### Phase 1: Quick Win with Qwen2.5-VL-7B (This Week)

1. **Convert Qwen2.5-VL-7B** (vision-only, simpler):
   ```bash
   # Use pre-quantized version
   MODEL=neuralmagic/Qwen2.5-VL-7B-Instruct-quantized.w4a16
   python convert_qwen_vl_to_litert.py --model $MODEL
   ```

2. **Host on HuggingFace**:
   ```bash
   huggingface-cli repo create on-device-ai-inc/qwen2.5-vl-7b-litert-lm
   huggingface-cli upload on-device-ai-inc/qwen2.5-vl-7b-litert-lm ./qwen_vl_litert/
   ```

3. **Update model_allowlist.json**:
   ```json
   {
     "name": "Qwen2.5-VL-7B",
     "modelId": "on-device-ai-inc/qwen2.5-vl-7b-litert-lm",
     "llmSupportImage": true,
     "llmSupportAudio": false,
     "defaultConfig": {
       "maxTokens": 8192,
       "contextWindow": 131072
     }
   }
   ```

4. **Add Whisper separately** (optional):
   - Use WhisperKitAndroid SDK
   - Audio → Whisper → Text → Qwen2.5-VL
   - Modular approach (2 models: 40MB + 1.8GB)

### Phase 2: Full Multimodal with Omni (Future)

Wait for:
- AI Edge Torch Generative API updates for complex multimodal
- Official LiteRT-LM support from Google/Qwen team
- Or consider MNN integration if LiteRT proves too difficult

---

## Technical Details

### System Requirements

**For Conversion**:
- 16GB RAM minimum (32GB recommended)
- 50GB disk space
- Python 3.10+
- CUDA GPU optional (speeds up conversion)

**For Deployment**:
- Android device with 12GB+ RAM
- ~2GB storage for quantized model
- GPU/NPU acceleration recommended

### File Sizes

| Model | Base Size | Int4 Quantized | w4a16 Quantized |
|-------|-----------|----------------|-----------------|
| Qwen2.5-Omni-7B | ~14GB | ~3.5GB | ~1.8GB |
| Qwen2.5-VL-7B | ~14GB | ~3.5GB | ~1.8GB |
| Whisper-Tiny | ~150MB | ~40MB | ~40MB |

### Conversion Time Estimates

| Task | CPU Only | With GPU |
|------|----------|----------|
| Download model | 30-60 min | 30-60 min |
| Quantization | 2-4 hours | 30-60 min |
| TFLite conversion | 1-2 hours | 20-40 min |
| Bundle creation | 10 min | 10 min |
| **Total** | **4-7 hours** | **1.5-3 hours** |

---

## Next Steps

### Immediate (Recommended):

1. **Convert Qwen2.5-VL-7B using pre-quantized version** ⭐
   ```bash
   cd scripts/
   python convert_qwen_vl_to_litert.py
   ```

2. **Upload to HuggingFace**
   - Create repo: `on-device-ai-inc/qwen2.5-vl-7b-litert-lm`
   - Upload converted .litertlm file

3. **Update app**
   - Add to model_allowlist.json
   - Test on device

### Alternative (If you have time):

1. **Try full Omni conversion** (experimental)
   ```bash
   python convert_qwen_omni_to_litert.py
   ```

2. **Expect issues and iterate**
   - May need custom model authoring
   - Audio/video components need special handling
   - Could take 1-2 weeks to resolve

### Fallback (If conversion fails):

1. **Use MNN version**
   - Download taobao-mnn/Qwen2.5-Omni-7B-MNN
   - Refactor app to use MNN runtime
   - Full multimodal support but different framework

---

## References

- [AI Edge Torch Generative API](https://github.com/google-ai-edge/ai-edge-torch)
- [LiteRT-LM Documentation](https://github.com/google-ai-edge/LiteRT-LM)
- [Qwen2.5-Omni GitHub](https://github.com/QwenLM/Qwen2.5-Omni)
- [MNN-LLM Paper](https://arxiv.org/html/2506.10443)
- [Multimodal Model Quantization](https://developers.redhat.com/articles/2025/02/19/multimodal-model-quantization-support-through-llm-compressor)

---

## Support

If conversion fails or you encounter issues:
1. Check GitHub issues: [ai-edge-torch/issues](https://github.com/google-ai-edge/ai-edge-torch/issues)
2. Qwen team support: [Qwen2.5-Omni/issues](https://github.com/QwenLM/Qwen2.5-Omni/issues)
3. Consider hiring ML engineer with mobile deployment experience

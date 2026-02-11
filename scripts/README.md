# Model Conversion Scripts

This directory contains scripts and documentation for converting models to LiteRT format for Android deployment.

## Files

- **`convert_qwen_omni_to_litert.py`** - Qwen2.5-Omni-7B conversion script (experimental)
- **`CONVERSION_GUIDE.md`** - Comprehensive guide with 3 conversion approaches
- **`README.md`** - This file

## Quick Start

### Option 1: Recommended Path (Qwen2.5-VL-7B)

The simpler, more reliable approach is to convert Qwen2.5-VL-7B (vision-only) first:

```bash
# Install dependencies
pip install ai-edge-torch torch transformers huggingface-hub

# Create conversion script for VL variant
# (Script to be created based on convert_qwen_omni_to_litert.py template)

# Run conversion
python convert_qwen_vl_to_litert.py

# Upload to HuggingFace
huggingface-cli login
huggingface-cli repo create on-device-ai-inc/qwen2.5-vl-7b-litert-lm
huggingface-cli upload on-device-ai-inc/qwen2.5-vl-7b-litert-lm ./qwen_vl_litert/
```

### Option 2: Full Multimodal (Qwen2.5-Omni-7B)

⚠️ **Advanced** - Requires ML expertise and may not work without custom model authoring

```bash
# Try conversion (experimental)
python convert_qwen_omni_to_litert.py

# If it fails (likely), see CONVERSION_GUIDE.md for alternatives
```

## Current Status

✅ **Completed**:
- Research and documentation
- Conversion script template
- model_allowlist.json updated with Qwen2.5-Omni-7B entry

⏳ **Pending**:
- Actual model conversion (requires GPU machine with 16GB+ RAM)
- Upload to HuggingFace
- Integration testing with Android app

🔄 **In Progress**:
- Evaluating conversion feasibility
- Determining best approach (VL vs Omni)

## Next Steps

### For You (User):

1. **Decide on approach**:
   - **Quick win**: Convert Qwen2.5-VL-7B (2-3 days)
   - **Full multimodal**: Attempt Qwen2.5-Omni-7B (1-2 weeks, may fail)
   - **Alternative**: Use MNN pre-converted model (requires app refactor)

2. **Set up conversion environment**:
   ```bash
   # Create virtual environment
   python -m venv litert_env
   source litert_env/bin/activate  # Linux/Mac
   # or: litert_env\Scripts\activate  # Windows

   # Install dependencies
   pip install -r requirements.txt  # (create this file)
   ```

3. **Run conversion** on a machine with:
   - 16GB+ RAM (32GB recommended)
   - 50GB disk space
   - GPU optional but speeds up process

4. **Upload to HuggingFace**:
   ```bash
   # Create account if needed: https://huggingface.co/join
   # Create new model repository
   # Upload .litertlm file + tokenizer + metadata
   ```

5. **Test integration**:
   ```bash
   # Build and install app
   cd ../
   ./gradlew assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk

   # Try downloading new model in app
   # Test multimodal capabilities
   ```

### For Me (AI Assistant):

1. **Create requirements.txt** with all dependencies
2. **Create Qwen2.5-VL-7B conversion script** (simpler variant)
3. **Provide testing checklist** for app integration
4. **Document any issues encountered**

## Requirements

### System Requirements (for conversion):

```
Python >= 3.10
PyTorch >= 2.0
ai-edge-torch >= 0.2.0
transformers >= 4.40.0
safetensors >= 0.4.0
huggingface-hub >= 0.20.0
```

### Android Requirements (for deployment):

```
Min SDK: 24 (Android 7.0)
Target SDK: 34 (Android 14)
Device RAM: 12GB+ (for Qwen2.5-Omni-7B)
Device RAM: 8GB+ (for Qwen2.5-VL-7B)
Storage: 2-4GB free
```

## Troubleshooting

### Conversion fails with "unsupported operation"
→ Check CONVERSION_GUIDE.md for workarounds or use pre-quantized models

### Out of memory during conversion
→ Increase RAM or use smaller model variant (3B instead of 7B)

### Model doesn't load in app
→ Verify .litertlm format is correct, check LiteRT-LM version compatibility

### Download fails in app
→ Check HuggingFace URL is publicly accessible, verify SHA256 hash

## Support

- **Conversion issues**: [AI Edge Torch GitHub](https://github.com/google-ai-edge/ai-edge-torch/issues)
- **Model issues**: [Qwen2.5-Omni GitHub](https://github.com/QwenLM/Qwen2.5-Omni/issues)
- **App integration**: Check `../app/README.md` or create GitHub issue

## References

- [CONVERSION_GUIDE.md](./CONVERSION_GUIDE.md) - Detailed conversion guide
- [AI Edge Torch Documentation](https://ai.google.dev/edge/litert/models/edge_generative)
- [LiteRT-LM GitHub](https://github.com/google-ai-edge/LiteRT-LM)
- [Qwen2.5-Omni Documentation](https://qwenlm.github.io/blog/qwen2.5-omni/)

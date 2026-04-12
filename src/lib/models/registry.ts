export const MODEL_REGISTRY = {
  lite: {
    id: 'lite',
    sizeBytes: 612_270_080,
    displayName: 'Lite (584 MB)',
    r2Key: 'models/lite/model.bin',
  },
  standard: {
    id: 'standard',
    sizeBytes: 1_288_490_189,
    displayName: 'Standard (1.2 GB)',
    r2Key: 'models/standard/model.bin',
  },
  multimodal: {
    id: 'multimodal',
    sizeBytes: 3_972_844_134,
    displayName: 'Multimodal (3.7 GB)',
    r2Key: 'models/multimodal/model.bin',
  },
  max: {
    id: 'max',
    sizeBytes: 5_260_827_648,
    displayName: 'Max (4.9 GB)',
    r2Key: 'models/max/model.bin',
  },
} as const

export type ModelId = keyof typeof MODEL_REGISTRY

export function isValidModelId(id: string): id is ModelId {
  return id in MODEL_REGISTRY
}

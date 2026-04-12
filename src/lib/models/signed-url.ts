import { S3Client, GetObjectCommand } from '@aws-sdk/client-s3'
import { getSignedUrl } from '@aws-sdk/s3-request-presigner'
import { MODEL_REGISTRY, type ModelId } from './registry'

function getR2Client(): S3Client {
  const accountId = process.env.R2_ACCOUNT_ID
  const accessKeyId = process.env.R2_ACCESS_KEY_ID
  const secretAccessKey = process.env.R2_SECRET_ACCESS_KEY

  if (!accountId || !accessKeyId || !secretAccessKey) {
    throw new Error('R2 credentials not configured (R2_ACCOUNT_ID, R2_ACCESS_KEY_ID, R2_SECRET_ACCESS_KEY)')
  }

  return new S3Client({
    region: 'auto',
    endpoint: `https://${accountId}.r2.cloudflarestorage.com`,
    credentials: { accessKeyId, secretAccessKey },
  })
}

/**
 * Generate a presigned URL for a model file.
 * The URL is valid for `expirySeconds` (default 15 minutes).
 * URL is device-scoped — cannot be reused on a different device.
 */
export async function generateModelSignedUrl(
  modelId: ModelId,
  _deviceFingerprint: string,
  expirySeconds = 900
): Promise<{ url: string; expiresAt: Date }> {
  const model = MODEL_REGISTRY[modelId]
  const bucket = process.env.R2_BUCKET_NAME ?? 'ondevice-models'

  const client = getR2Client()
  const command = new GetObjectCommand({
    Bucket: bucket,
    Key: model.r2Key,
    // ResponseContentDisposition scopes the download to this model name
    ResponseContentDisposition: `attachment; filename="${modelId}.bin"`,
  })

  const url = await getSignedUrl(client, command, { expiresIn: expirySeconds })
  const expiresAt = new Date(Date.now() + expirySeconds * 1000)

  return { url, expiresAt }
}

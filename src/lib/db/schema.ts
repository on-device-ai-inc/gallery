import { pgTable, text, integer, timestamp, boolean, index, unique } from 'drizzle-orm/pg-core'

// Pre-launch waitlist — email capture before March 26 launch
export const waitlistEntries = pgTable('waitlist_entries', {
  id: text('id').primaryKey(),
  email: text('email').notNull().unique(),
  source: text('source'),                                   // 'website' | 'direct'
  subscribedAt: timestamp('subscribed_at').notNull().defaultNow(),
}, (t) => [
  index('idx_waitlist_email').on(t.email),
])

export const orders = pgTable('orders', {
  id: text('id').primaryKey(),                         // cuid generated app-side
  email: text('email').notNull(),
  phone: text('phone'),                                // required for M-Pesa
  amountCents: integer('amount_cents').notNull(),      // in smallest currency unit
  currency: text('currency').notNull(),                // ISO 4217
  amountUsdCents: integer('amount_usd_cents').notNull(), // for reporting
  provider: text('provider').notNull(),                // stripe | intasend | flutterwave | mpesa
  providerRef: text('provider_ref').notNull(),         // provider's transaction id
  status: text('status').notNull().default('pending'), // pending | paid | failed
  createdAt: timestamp('created_at').notNull().defaultNow(),
  paidAt: timestamp('paid_at'),
})

export const downloadTokens = pgTable('download_tokens', {
  id: text('id').primaryKey(),
  orderId: text('order_id').notNull().references(() => orders.id),
  tokenHash: text('token_hash').notNull().unique(),   // SHA-256 of the JWT
  expiresAt: timestamp('expires_at').notNull(),
  usedAt: timestamp('used_at'),                       // null = not yet used
  createdAt: timestamp('created_at').notNull().defaultNow(),
})

// Promo code redemptions — one redemption per code per email
export const promoRedemptions = pgTable('promo_redemptions', {
  id: text('id').primaryKey(),
  code: text('code').notNull(),
  email: text('email').notNull(),
  orderId: text('order_id').notNull().references(() => orders.id),
  redeemedAt: timestamp('redeemed_at').notNull().defaultNow(),
}, (t) => [
  unique('uniq_promo_code_email').on(t.code, t.email),
  index('idx_promo_redemptions_code').on(t.code),
])

// Device-bound licenses — one per order per device
export const deviceLicenses = pgTable('device_licenses', {
  id: text('id').primaryKey(),                              // cuid
  orderId: text('order_id').notNull().unique().references(() => orders.id),
  deviceFingerprint: text('device_fingerprint').notNull(),  // SHA-256(ANDROID_ID+MODEL+MANUFACTURER)
  licenseKey: text('license_key').notNull().unique(),       // UUID v4, primary credential
  activatedAt: timestamp('activated_at').notNull().defaultNow(),
  lastVerifiedAt: timestamp('last_verified_at'),
  reactivationCount: integer('reactivation_count').notNull().default(0),
  revoked: boolean('revoked').notNull().default(false),
  revokedAt: timestamp('revoked_at'),
  createdAt: timestamp('created_at').notNull().defaultNow(),
}, (t) => [
  index('idx_device_licenses_order_id').on(t.orderId),
  index('idx_device_licenses_license_key').on(t.licenseKey),
  index('idx_device_licenses_fingerprint').on(t.deviceFingerprint),
])

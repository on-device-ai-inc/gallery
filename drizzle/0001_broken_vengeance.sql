CREATE TABLE "device_licenses" (
	"id" text PRIMARY KEY NOT NULL,
	"order_id" text NOT NULL,
	"device_fingerprint" text NOT NULL,
	"license_key" text NOT NULL,
	"activated_at" timestamp DEFAULT now() NOT NULL,
	"last_verified_at" timestamp,
	"reactivation_count" integer DEFAULT 0 NOT NULL,
	"revoked" boolean DEFAULT false NOT NULL,
	"revoked_at" timestamp,
	"created_at" timestamp DEFAULT now() NOT NULL,
	CONSTRAINT "device_licenses_license_key_unique" UNIQUE("license_key")
);
--> statement-breakpoint
ALTER TABLE "device_licenses" ADD CONSTRAINT "device_licenses_order_id_orders_id_fk" FOREIGN KEY ("order_id") REFERENCES "public"."orders"("id") ON DELETE no action ON UPDATE no action;--> statement-breakpoint
CREATE INDEX "idx_device_licenses_order_id" ON "device_licenses" USING btree ("order_id");--> statement-breakpoint
CREATE INDEX "idx_device_licenses_license_key" ON "device_licenses" USING btree ("license_key");--> statement-breakpoint
CREATE INDEX "idx_device_licenses_fingerprint" ON "device_licenses" USING btree ("device_fingerprint");
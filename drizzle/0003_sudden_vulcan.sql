CREATE TABLE "promo_redemptions" (
	"id" text PRIMARY KEY NOT NULL,
	"code" text NOT NULL,
	"email" text NOT NULL,
	"order_id" text NOT NULL,
	"redeemed_at" timestamp DEFAULT now() NOT NULL,
	CONSTRAINT "uniq_promo_code_email" UNIQUE("code","email")
);
--> statement-breakpoint
ALTER TABLE "promo_redemptions" ADD CONSTRAINT "promo_redemptions_order_id_orders_id_fk" FOREIGN KEY ("order_id") REFERENCES "public"."orders"("id") ON DELETE no action ON UPDATE no action;--> statement-breakpoint
CREATE INDEX "idx_promo_redemptions_code" ON "promo_redemptions" USING btree ("code");
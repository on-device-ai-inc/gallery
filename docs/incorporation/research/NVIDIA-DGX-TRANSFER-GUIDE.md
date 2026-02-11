# NVIDIA DGX Spark: Personal to Corporate Entity Transfer Guide
**Researched:** February 10, 2026
**Status:** ACTION REQUIRED — Contact NVIDIA Support

---

## The Bottom Line

There is **no self-service portal or documented process** for transferring DGX Spark registration from a personal account to a corporate entity. This must be handled through direct engagement with NVIDIA support.

---

## What the Legal Agreements Say

### DGX End User License Agreement (Software)
- License is **non-exclusive, non-transferable, without right to sublicense**
- Assignment requires **NVIDIA's written permission** — any unauthorized assignment is void

### DGX Support Services Terms & Conditions
- Support services are **non-transferable, non-assignable**
- **Terminated when product is transferred** to another party

### NVIDIA Manufacturer's Warranty
- Warranty is for **original owner only**
- **Non-transferable** — voided when product changes hands

---

## Step-by-Step Transfer Process

### Step 1: Gather Documentation
Before contacting NVIDIA, prepare:
- [x] DGX Spark **serial number**: **1983925014476**
- [ ] **NVIDIA Marketplace order number** + confirmation email
- [ ] **Original invoice/receipt** (request from support if not available)
- [ ] **Certificate of Incorporation** (OnDevice AI Inc.)
- [ ] **Bill of Sale** documenting transfer from personal to corporate entity
- [ ] Company legal info: name, incorporation number, business address

### Step 2: Open NVIDIA Support Ticket

| Channel | Contact |
|---------|---------|
| Support Ticket | https://nvidia.custhelp.com/app/ask |
| Live Chat | http://nvidia.custhelp.com/app/chat/chat_launch/ |
| Phone (US toll-free) | +1 (800) 421-5048 |
| Enterprise Support (no login) | https://enterprise-support.nvidia.com/s/create-case |

**Suggested ticket text:**
> Subject: Transfer DGX Spark registration from personal to corporate entity
>
> I purchased an NVIDIA DGX Spark (Serial: 1983925014476, hostname: ondeviceai-spark) under my personal NVIDIA account. I need to transfer the device registration, warranty, software entitlements, and support to my corporate entity OnDevice AI Inc.
>
> I understand per the DGX EULA that license assignment requires NVIDIA's written permission, and I am formally requesting that permission.
>
> I have prepared: order confirmation, device serial number, corporate Certificate of Incorporation, and a bill of sale documenting the transfer.
>
> Please advise on the process and any additional documentation required.

### Step 3: Set Up Corporate NVIDIA Enterprise Account
- Register at: https://enterpriseproductregistration.nvidia.com/
- Need **Product Activation Key (PAK)** from entitlement certificate email

### Step 4: Set Up Corporate NGC Organization
- DGX Spark does **NOT require NGC hardware activation or serial number registration**
- Create NGC account at: https://ngc.nvidia.com/
- Individual org can be **converted to enterprise org** via NGC Activate Subscription portal
- Generate API key at: https://org.ngc.nvidia.com/setup/api-keys

### Step 5: Handle Warranty
- Check current status: https://enterprise-support.nvidia.com/s/rma-serial-check
- Register warranty: https://enterpriseproductregistration.nvidia.com/warrantyclaim
- Request NVIDIA make exception for personal-to-corporate transfer

---

## Important Considerations

1. **Support entitlements will likely be terminated** — may need new purchase under corporate entity
2. **Warranty is technically voided** — request exception (common scenario: individual buys, then incorporates)
3. **DGX Spark is $3,999** — if transfer proves too complex, consider fresh purchase under corporate entity
4. **Invoice availability is limited** — not downloadable from Marketplace; request from support
5. **NGC org conversion is a defined path** — simpler than hardware ownership transfer
6. **No self-service account merge** — entirely manual, support-driven process

---

## Key URLs

| Resource | URL |
|----------|-----|
| DGX Spark Support | https://www.nvidia.com/en-us/support/dgx-spark/ |
| Open Support Ticket | https://nvidia.custhelp.com/app/ask |
| Enterprise Product Registration | https://enterpriseproductregistration.nvidia.com/ |
| Warranty Check | https://enterprise-support.nvidia.com/s/rma-serial-check |
| NGC API Keys | https://org.ngc.nvidia.com/setup/api-keys |
| Marketplace Account | https://marketplace.nvidia.com/en-us/account/ |
| DGX Support T&C | https://images.nvidia.com/content/technologies/deep-learning/pdf/NVIDIA-DGX-Support-Services-02-07-20.pdf |
| DGX EULA | https://images.nvidia.com/content/technologies/deep-learning/pdf/DGX-End-User-License-Agreement-09-30-22.pdf |

---

## Sources
- NVIDIA DGX Spark Support Page
- NVIDIA Enterprise Customer Support
- NVIDIA Developer Forums (DGX Spark/GB10)
- DGX Support Services Terms & Conditions (PDF)
- DGX End User License Agreement (PDF)
- NGC User Guide
- Enterprise Support Services User Guide (PDF)

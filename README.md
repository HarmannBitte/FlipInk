# FlipInk: Detachable E-Ink Secondary Display for Nothing Phone (1)

**FlipInk** is an open-source hardware & software blueprint for an ultra-thin, detachable E-Ink secondary display designed for the **Nothing Phone (1)**, mounted via the **Peak Design SlimLink / Universal Adapter** ecosystem.

---

## 📑 Table of Contents
1. [Executive Summary & System Architecture](#system-architecture)
2. [Mechanical & Physical Mount Design (Peak Design SlimLink)](#mechanical-design)
3. [Power Delivery: Reverse Wireless Charging (Qi 5W) & Battery Strategy](#power-delivery)
4. [Hardware Component Selection & Schematics](#hardware-selection)
5. [Firmware Design (ESP32-S3 + E-Paper Controller)](#firmware-architecture)
6. [Android Companion App & Screen Streaming Architecture](#android-app-architecture)
7. [BlackScreen OLED Battery Saver Overlay](#blackscreen-module)
8. [E-Ink Optimizations (Dithering, Partial Refresh, Ghosting Mitigation)](#e-ink-optimizations)
9. [BOM (Bill of Materials) & Build Guide](#bill-of-materials)

---

## 1. System Architecture <a name="system-architecture"></a>

```
┌────────────────────────────────────────────────────────────────────────┐
│                          NOTHING PHONE (1)                             │
│                                                                        │
│   ┌───────────────────────────┐      ┌──────────────────────────────┐  │
│   │   FlipInk Companion App   │      │   BlackScreen OLED Overlay   │  │
│   │                           │      │                              │  │
│   │  • MediaProjection API    │      │  • WindowManager overlay     │  │
│   │    (or VirtualDisplay)    │      │  • FLAG_LAYOUT_NO_LIMITS     │  │
│   │  • 1-bit Floyd-Steinberg  │      │  • Dim / Blackout active     │  │
│   │    or 4-level Grayscale   │      │  • Proximity / Flip trigger  │  │
│   │  • Dirty-rect detection   │      │  • Volume button page-turn   │  │
│   └─────────────┬─────────────┘      └──────────────────────────────┘  │
│                 │ (BLE 5.0 / High-Speed Wi-Fi ESP-NOW)                 │
│                 ▼                                                      │
│   ┌───────────────────────────┐                                        │
│   │  5W Reverse Wireless Tx   │ (Nothing Phone 1 Qi Coil)              │
│   └─────────────┬─────────────┘                                        │
└─────────────────┼──────────────────────────────────────────────────────┘
                  │ Wireless Power & RF Link
                  │ (or SlimLink physical pass-through)
┌─────────────────┼──────────────────────────────────────────────────────┐
│                 ▼                                                      │
│   ┌───────────────────────────┐      ┌──────────────────────────────┐  │
│   │   Qi 5W Rx Coil & PMIC    │──────▶   Tiny 250-400mAh LiPo /     │  │
│   │   (TI BQ51013B / IP6816)  │      │   Supercapacitor Buffer      │  │
│   └─────────────┬─────────────┘      └──────────────┬───────────────┘  │
│                 ▼                                   ▼                  │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │                     ESP32-S3 Microcontroller                    │  │
│   │                                                                 │  │
│   │  • BLE 5 / ESP-NOW Receiver                                     │  │
│   │  • Differential Framebuffer (LUT Partial Refresh)               │  │
│   │  • SPI driver to E-Paper Driver IC                              │  │
│   └─────────────────────────────┬───────────────────────────────────┘  │
│                                 ▼                                      │
│   ┌─────────────────────────────────────────────────────────────────┐  │
│   │            4.2" ~ 4.26" Fast Partial-Refresh E-Paper            │  │
│   │                     (e.g., Good Display GDEQ0426T82)            │  │
│   │                     400x300, 30-40ms fast partial               │  │
│   └─────────────────────────────────────────────────────────────────┘  │
│                         FLIPINK DETACHABLE MODULE                      │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Mechanical Design & Peak Design SlimLink Attachment <a name="mechanical-design"></a>

### 2.1 The Challenge & Geometry
The **Nothing Phone (1)** dimensions:
* Height: 159.2 mm
* Width: 75.8 mm
* Thickness: 8.3 mm
* Qi wireless charging coil: centered on the back, illuminated by the central circular Glyph LED ring.

The **Peak Design Universal Adapter**:
* Exterior footprint: $56\text{ mm} \times 56\text{ mm}$
* Thickness: $2.6\text{ mm}$
* Features: SlimLink magnetic + mechanical locking aperture in the center (approx. $20\text{ mm} \times 20\text{ mm}$ square with spring catches).

### 2.2 Dual-Mount / Pass-Through Architecture
To allow your phone to clip into your bike handlebar mount **and** clip the E-Ink display on/off:
1. **Pass-Through "Sandwich" Design (Option A - Recommended)**:
   * **Phone-facing side of FlipInk**: Features a male SlimLink locking boss with 4 embedded NdFeB magnets ($6\text{ mm} \times 3\text{ mm}$, N52) and mechanical retention tabs. It locks directly into the Peak Design Universal Adapter on your phone.
   * **Outer-facing side of FlipInk**: Features a female Peak Design Universal Adapter (or an exact 3D-printed SlimLink receiver recess).
   * **Benefit**: You can snap the FlipInk module onto your phone, and then snap the entire assembly onto your Peak Design bike mount! Or detach FlipInk and lock the phone straight onto the bike mount.
2. **Side-Rail / Corner Clip with Cutout (Option B)**:
   * FlipInk clips around the phone edges with a square window ($58\text{ mm} \times 58\text{ mm}$) exposing the phone's native Peak Design adapter to the outside world.

---

## 3. Power Delivery Strategy <a name="power-delivery"></a>

### 3.1 Reverse Wireless Charging (Nothing Phone 1 - 5W Qi)
The Nothing Phone (1) supports **5W reverse wireless charging** via Battery Share:
* Operating frequency: 110 kHz – 205 kHz.
* Transmission distance: Maximum 3–5 mm before severe magnetic decoupling.

#### Critical Constraint with Peak Design Adapter:
The Peak Design Universal Adapter on the back is 2.6 mm thick and sits right over the charging coil.
* **Direct Inductive Powering**: If the E-ink module is mounted behind the Peak Design adapter, the air gap reaches 3–4 mm (adapter + shell). While Qi can bridge 3–4 mm, magnetic efficiency drops to ~50–60% (generating heat on the phone back).
* **FlipInk Hybrid Power Strategy**:
  1. **Ultraslim Internal Battery (300 mAh 3.7V LiPo, ~3.5 mm thick)**: Gives **15–30 hours of continuous reading** or weeks of static display (E-ink uses 0W when static!).
  2. **Qi Wireless Receiver Coil (0.3 mm ultra-thin)**: Positioned on the inner face of FlipInk facing the phone coil. When attached and Reverse Wireless Charging is enabled on the Nothing Phone, FlipInk trickles charges at ~300–500 mA.
  3. **Super-Low Standby (< 15 µA)**: When no new pages are received, the ESP32-S3 goes into Deep Sleep with timer/BLE wakeup.

---

## 4. Hardware Component Selection <a name="hardware-selection"></a>

| Component | Part Number / Model | Key Specs | Reason for Selection |
|---|---|---|---|
| **E-Paper Panel** | Good Display **GDEQ0426T82** (or GDEW042T2) | 4.26-inch, 800×480 (or 4.2" 400×300), partial refresh 0.3s | Ideal aspect ratio for mobile reading; supports fast 1-bit partial refresh. |
| **MCU & Wireless** | **ESP32-S3-WROOM-1** (or ESP32-C3) | Dual-core Xtensa 240MHz, 8MB PSRAM, Wi-Fi 4 + BLE 5.0 | Hardware crypto, enough RAM for full display framebuffers & fast image decompression. |
| **Wireless Rx Coil & IC** | **TI BQ51013B** or **IP6816** Qi 5W Rx module | 5V / 1A output, Qi 1.2 compliant, ultra-thin ferrite backing | Integrates rectifier, regulator, and Qi communication packets. |
| **Battery Management** | **TP4056** or **TI BQ24040** | 4.2V CC/CV LiPo charger with thermal protection | Clean charging from wireless 5V rail. |
| **LDO Regulator** | **RT9080** / **AP2112K-3.3** | Ultra-low quiescent current ($2\,\mu\text{A}$), 600mA out | Ensures deep sleep doesn't drain the battery. |
| **Sensors** | **LIS3DH** 3-axis accelerometer | I2C ultra-low power | Detects phone flip: automatically triggers BlackScreen on front and updates E-Ink on back! |

---

## 5. Firmware Architecture (ESP32-S3) <a name="firmware-architecture"></a>

See implementation in `firmware/main/`:
* **BLE GATT Service** for control commands (page flip, refresh mode, battery status).
* **ESP-NOW or BLE 2M PHY** for chunked binary bitmap transmission.
* **Partial Refresh LUT Manager**: Prevents full-screen flashing during reading; executes a full refresh every 10–20 page turns to clear ghosting.

---

## 6. Android Companion App Architecture <a name="android-app-architecture"></a>

Located in `android/`:
1. **Screen Capture Service**:
   * Uses Android's `MediaProjection` API (or `VirtualDisplay` if using Shizuku/ADB) to capture frames of any book reader (Moon+ Reader, Kindle, KOReader, Tachiyomi).
2. **Image Processing Pipeline**:
   * Downsampling to E-Ink resolution ($800 \times 480$ or $400 \times 300$).
   * High-speed Floyd-Steinberg error diffusion dithering (or thresholding for pure text).
   * Dirty-rectangle calculation: Only send bytes that changed.
3. **BlackScreen OLED Overlay Module**:
   * Displays an absolute `#000000` view with `TYPE_APPLICATION_OVERLAY`.
   * On OLED screens (Nothing Phone 1 has a flexible OLED display), pure black pixels draw **0 current**, saving up to 80–90% of screen battery.
   * Intercepts hardware Volume Up/Down buttons to send page-turn packets to the E-Ink screen and reader app via `AccessibilityService`.

---

## 7. Open Source BlackScreen Alternatives
If you prefer standalone apps rather than building into FlipInk:
* **BlackScreen / Extinguish (Shizuku-based)**: Turns off physical panel backlight while apps run.
* **Black Overlay / ScreenDimmer**: Standard Android overlay apps drawing 100% black overlays with touch pass-through.

# FlipInk Hardware Bill of Materials (BOM) & Sourcing Guide

This document specifies the exact component selection, dimensions, pinouts, and sourcing links for building the **FlipInk** detachable E-Ink secondary display.

---

## 1. Component Selection & Sourcing Table

| Subsystem | Component / Part Number | Key Specifications | Physical Dimensions | Est. Cost (USD) | Sourcing / Manufacturer |
|---|---|---|---|---|---|
| **E-Ink Panel** | **Good Display GDEQ0426T82** (or GDEY042T81) | 4.26" (800×480), SSD1677, 0.42s fast partial refresh, SPI | Outer: 105.3 × 62.4 × 1.8 mm; Active: 92.8 × 55.7 mm | ~$18 - $24 | Good Display / Buy-LCD / AliExpress |
| **Alternative E-Ink** | **Good Display GDEY037T03** (compact 3.7") | 3.7" (416×240), UC8253, 0.3s fast partial refresh, SPI | Outer: 93.0 × 53.0 × 1.0 mm | ~$14 - $18 | Good Display / AliExpress |
| **Microcontroller** | **Seeed Studio XIAO ESP32-S3** | Dual-core 240MHz, 8MB PSRAM, Wi-Fi/BLE 5.0, built-in LiPo charging & fuel gauge | **21.0 × 17.8 × 3.5 mm** | ~$7.50 | Seeed Studio, Mouser, DigiKey |
| **EPD Driver Board** | **DESPI-C02** (or custom discrete booster) | 24-Pin 0.5mm FPC breakout with onboard booster circuit (SI1308EDL / MBR0530) | **41.0 × 22.0 × 2.5 mm** | ~$4.00 | Good Display / AliExpress |
| **Qi Wireless Rx** | **5V 1A Ultra-Thin Qi Receiver Module** (TI BQ51013B or IP6816 based) | WPC 1.2 Qi compliant, 5W output (5V @ 1A), ultra-thin ferrite backing | PCB: ~28 × 15 × 1.8 mm; Coil: ~48 × 32 × 0.4 mm | ~$4 - $6 | Tindie / AliExpress / Amazon |
| **Battery** | **402030 LiPo Cell** | 3.7V, 200–250 mAh, built-in PCM protection board | **30.0 × 20.0 × 4.0 mm** | ~$3 - $5 | Adafruit, AliExpress, Amazon |
| **Magnets** | **N52 Neodymium Disc Magnets** (Qty: 4 to 8) | High-temperature grade, for SlimLink magnetic alignment | **6.0 mm dia. × 3.0 mm thick** | ~$3.00 (pack) | K&J Magnetics / Amazon |
| **Mounting Hardware** | **M3 × 25mm machine screw + compression spring** | For compliant SlimLink push-button latching mechanism | Standard hardware | ~$1.00 | Local hardware / McMaster |

---

## 2. Spatial Layout & Enclosure Packaging

Total phone rear footprint: **159.2 mm × 75.8 mm**  
Peak Design Universal Adapter footprint: **56 mm × 56 mm × 2.6 mm**

```
+--------------------------------------------------------+
|  TOP: Dual Camera Island Clearance (18mm)             |
+--------------------------------------------------------+
|                                                        |
|   4.26" E-Ink Panel (GDEQ0426T82: 105.3 x 62.4 mm)    |
|   Faces outward (away from phone)                      |
|                                                        |
+--------------------------------------------------------+
|  INTERNAL LAYER (Between E-Ink and Phone):            |
|                                                        |
|   [ XIAO ESP32-S3 ]          [ 402030 LiPo Battery ]   |
|   (21 x 17.8 x 3.5 mm)       (30 x 20 x 4.0 mm)        |
|                                                        |
|   [ DESPI-C02 Driver ]       [ 5V Qi Wireless Rx PCB ] |
|   (41 x 22 x 2.5 mm)         (28 x 15 x 1.8 mm)        |
|                                                        |
|   --------------------------------------------------   |
|   CENTER RECESSED APERTURE:                            |
|   Male SlimLink Boss + 4x (6x3mm N52 Magnets)          |
|   Surrounded by Ultra-Thin Qi Receiver Coil            |
|   Snaps flush into the 56x56mm Peak Design Adapter     |
+--------------------------------------------------------+
```

### Total Thickness Breakdown:
* E-Ink Glass + Bezel: **1.8 mm**
* Internal Electronics / Battery Layer: **4.0 mm**
* SlimLink Latching Plate + Case Shell: **1.8 mm**
* **Total FlipInk Module Thickness**: **~7.6 mm** (excluding the male boss that recesses inside the Peak Design adapter).

---

## 3. Electrical Interconnection Schematic

```
[ Phone Wireless Tx Coil (Nothing Phone 1) ]
                 ))) 110-205 kHz inductive coupling
[ Ultra-Thin Qi Rx Coil (0.4mm) ]
                 │
                 ▼
[ Qi Rx PCB (TI BQ51013B / IP6816) ]
                 │ 5V DC Output
                 ▼
[ Seeed Studio XIAO ESP32-S3 (VIN / 5V Pad) ]
                 │
                 ├──▶ Integrated LiPo Charger (BAT+ / BAT- pads) ──▶ [ 402030 3.7V LiPo ]
                 │
                 └──▶ 3.3V LDO Output
                             │
                             ▼
                 [ DESPI-C02 Booster & Level Shift ]
                             │ SPI Bus (CS, DC, RST, BUSY, SCK, MOSI)
                             ▼
                 [ 4.26" Good Display GDEQ0426T82 E-Paper ]
```

### Pin Assignment for XIAO ESP32-S3:
* `GPIO 10` / `D8`: SPI `SCK`
* `GPIO 9`  / `D10`: SPI `MOSI`
* `GPIO 8`  / `D7`: EPD `CS`
* `GPIO 7`  / `D6`: EPD `DC`
* `GPIO 6`  / `D5`: EPD `RST`
* `GPIO 5`  / `D4`: EPD `BUSY`
* `VBAT`: LiPo battery positive with internal analog divider on `GPIO 1` for battery telemetry.

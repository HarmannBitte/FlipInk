# FlipInk 3D Mechanical & Mounting Design (Peak Design SlimLink)

This guide provides the CAD/3D printing specifications for attaching the FlipInk E-Ink display to a **Nothing Phone (1)** equipped with a **Peak Design Universal Adapter**.

---

## 1. Dimensional Constraints

### Nothing Phone (1)
* **Body Dimensions**: 159.2 mm × 75.8 mm × 8.3 mm
* **Rear Profile**: Flat Gorilla Glass back with semi-transparent elements & central Glyph wireless charging ring.

### Peak Design Universal Adapter
* **Outer Footprint**: 56 mm × 56 mm
* **Thickness**: 2.6 mm
* **SlimLink Retention Mechanism**:
  * Central magnetic ring (works with MagSafe / PD SlimLink magnets)
  * Two lateral spring-loaded mechanical push-button retention catches (slot size: ~20 mm × 20 mm).

---

## 2. Mounting Architectures

### Architecture A: "Pass-Through Sandwich" (Recommended for Bike Mounting)
If you frequently use a Peak Design bike handlebar mount, you don't want to remove FlipInk every time you bike:
* **Inner Face (facing phone)**:
  * 3D printed male SlimLink boss with 4 embedded $6\text{ mm} \times 3\text{ mm}$ N52 Neodymium magnets.
  * Mechanical retention teeth that snap securely into the Universal Adapter on the phone.
* **Outer Face (facing world)**:
  * A female Peak Design Universal Adapter glued/embedded into the rear casing, OR a 3D printed female SlimLink receiver.
  * Allows clipping the entire Phone + FlipInk assembly straight into the **Peak Design Out-Front Bike Mount** (or Motorcycle Mount).

```
   [ Nothing Phone 1 ]
           │
     (2.6mm PD Universal Adapter)
           │
   [ FlipInk Module ]
     ├─ Male SlimLink connector (snaps to phone)
     ├─ Qi Receiver Coil (aligned with phone coil)
     ├─ ESP32-S3 + LiPo + GDEQ0426 E-Paper
     └─ Female SlimLink receiver (facing outward)
           │
   [ Peak Design Bike Handlebar Mount ]
```

### Architecture B: Quick-Latch Snap-On Case / Wrap-Around
* Uses a 2-piece snap shell clasping around the phone corners.
* Leaves a square $58\text{ mm} \times 58\text{ mm}$ window cut out for the Universal Adapter.
* Detaches in < 2 seconds via tension clip.

---

## 3. Recommended 3D Printing Settings

* **Filament**: PETG, ABS, or PA-CF (Carbon Fiber Nylon) for UV and temperature resistance on bicycle handlebars. (Avoid standard PLA due to thermal softening in direct sunlight).
* **Layer Height**: 0.16 mm or 0.20 mm.
* **Infill**: 30% Gyroid or Honeycomb.
* **Perimeters/Walls**: 4 perimeters for mechanical integrity of the latching tabs.
* **Magnets**: 4x (or 8x) $6\text{ mm} \times 3\text{ mm}$ N52 disc magnets, embedded by pausing print at layer height $z = 4.2\text{ mm}$.

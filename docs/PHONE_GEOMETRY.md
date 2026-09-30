# Geometric Analysis of Peak Design Adapter on Nothing Phone (1)

Based on the actual phone layout (Nothing Phone 1 in White):

```
+-----------------------------------+
|  [O] [O] Camera Module            |
|                                   |
|        /-----------------\        |  <-- Circular Glyph LED / Qi Tx Coil
|       /                   \       |
|      |    +-----------+    |      |
|      |    |           |    |      |
|      |    | PeakDesign|    |      |  <-- Centered directly inside the coil!
|      |    |  Adapter  |    |      |
|      |    | 56 x 56mm |    |      |
|      |    +-----------+    |      |
|       \                   /       |
|        \-----------------/        |
|                                   |
|  [NOTHING]                        |
+-----------------------------------+
```

## Key Observations & Mechanical Implications:

1. **Centering over Qi Coil**:
   * The Peak Design Universal Adapter ($56 \times 56\text{ mm}$) is positioned directly concentric with the circular central Glyph strip, which is where the internal Qi induction coil is located.
   * **Advantage**: The physical attachment point and the wireless charging center share the exact same axis $(X_c, Y_c)$.

2. **Clearance Below the Camera Island**:
   * The camera pill is located in the top-left corner.
   * The top edge of the Peak Design adapter sits comfortably below the camera bump with $\approx 18\text{--}22\text{ mm}$ of clearance.
   * An E-ink display enclosure can either:
     - End flush just under the camera lenses (accommodating a ~3.7" to 4.2" screen).
     - Extend to the top right while having a L-shaped corner notch around the dual cameras.

3. **Inductive Gap & Coil Strategy**:
   * The Peak Design adapter is $2.6\text{ mm}$ thick, with a nylon canvas outer shell.
   * To achieve maximum reverse-charging transfer efficiency without magnetic distortion from the SlimLink steel/magnet ring:
     - Place an annular Qi receiver coil ($45\text{--}50\text{ mm}$ outer diameter with an open center hole) on the inner mating face of FlipInk.
     - The SlimLink male latching core passes straight through the center hole of the receiver coil, minimizing the distance between the phone's coil and FlipInk's coil.

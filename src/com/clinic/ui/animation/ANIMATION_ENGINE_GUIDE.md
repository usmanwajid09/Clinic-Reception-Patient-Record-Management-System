# 60 FPS Motion, Animation & Modern Design System Guide

## Overview & Architecture
To eliminate the rigid look of traditional desktop Swing templates, this application features a bespoke, high-performance animation framework:
- **60 FPS Timing Engine (`Animator.java`)**:
  - Ticks at ~16ms (`1000ms / 60fps`) using lightweight `javax.swing.Timer`.
  - Non-blocking execution with mathematical easing curves:
    - `EASE_OUT_CUBIC` (f(t) = (t-1)^3 + 1)
    - `EASE_OUT_QUAD` (f(t) = t * (2 - t))
    - `EASE_IN_OUT_CUBIC`
    - `EASE_OUT_BACK` (subtle playful bounce)
- **RGB/Alpha Interpolation (`ColorInterpolator.java`)**:
  - Smooth linear blending between resting and hover colors across interactive controls.
- **Floating Slide-in Toast Notifications (`ToastNotification.java`)**:
  - Mounts onto the window's `JLayeredPane` (above content panels).
  - Slides down with ease-out cubic motion from top-right.
  - Automatically dismisses after 3.2 seconds by fading out and sliding up.
- **Micro-Interactions (`ModernButton.java` & `StatCard.java`)**:
  - Smooth background color lerp over 140ms.
  - Dual-pass Gaussian alpha drop shadows that elevate dynamically on mouse hover.
- **Live Status Badges (`StatusBadge.java`)**:
  - Table cell pill renderer displaying colored badges with status indicator dots (`● AVAILABLE`, `● SCHEDULED`, `● COMPLETED`, `● PAID`, `● NO_SHOW`).


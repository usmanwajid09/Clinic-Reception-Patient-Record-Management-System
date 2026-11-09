package com.clinic.ui.animation;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

/**
 * High-performance 60 FPS animation utility for smooth Swing desktop transitions.
 * Supports cubic easing curves and asynchronous completion callbacks.
 */
public final class Animator {
    private static final int FRAME_RATE = 60;
    private static final int FRAME_DELAY = 1000 / FRAME_RATE; // ~16ms per frame

    private Animator() {}

    /**
     * Easing curve interpolation.
     */
    public enum Easing {
        LINEAR {
            @Override
            public float ease(float t) {
                return t;
            }
        },
        EASE_OUT_QUAD {
            @Override
            public float ease(float t) {
                return t * (2 - t);
            }
        },
        EASE_IN_OUT_CUBIC {
            @Override
            public float ease(float t) {
                return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
            }
        },
        EASE_OUT_CUBIC {
            @Override
            public float ease(float t) {
                float f = t - 1.0f;
                return f * f * f + 1.0f;
            }
        },
        EASE_OUT_BACK {
            @Override
            public float ease(float t) {
                float c1 = 1.70158f;
                float c3 = c1 + 1.0f;
                float f = t - 1.0f;
                return 1.0f + c3 * f * f * f + c1 * f * f;
            }
        };

        public abstract float ease(float t);
    }

    /**
     * Executes an animation over the specified duration.
     *
     * @param durationMs total animation length in milliseconds
     * @param easing     easing function to apply
     * @param onStep     callback invoked on every frame with eased progress (0.0f -> 1.0f)
     * @param onComplete optional callback when animation reaches 1.0f
     * @return the running Swing Timer instance (can be stopped if interrupted)
     */
    public static Timer animate(int durationMs, Easing easing, Consumer<Float> onStep, Runnable onComplete) {
        final long startTime = System.currentTimeMillis();
        final Timer[] timerHolder = new Timer[1];

        timerHolder[0] = new Timer(FRAME_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float rawProgress = Math.min(1.0f, (float) elapsed / durationMs);
                float easedProgress = easing.ease(rawProgress);

                if (onStep != null) {
                    onStep.accept(easedProgress);
                }

                if (rawProgress >= 1.0f) {
                    timerHolder[0].stop();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        });

        timerHolder[0].setRepeats(true);
        timerHolder[0].start();
        return timerHolder[0];
    }

    public static Timer animate(int durationMs, Easing easing, Consumer<Float> onStep) {
        return animate(durationMs, easing, onStep, null);
    }

    public static Timer animate(int durationMs, Consumer<Float> onStep) {
        return animate(durationMs, Easing.EASE_OUT_CUBIC, onStep, null);
    }
}

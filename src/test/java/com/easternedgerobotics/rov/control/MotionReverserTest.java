package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.value.MotionValue;

import org.junit.Assert;
import org.junit.Test;

public final class MotionReverserTest {
    @Test
    public final void applyDoesNotReverseMotionIfNotToggled() {
        final MotionValue reversed = MotionReverser.apply(new MotionValue(1, 1, 1, 1, 1, 1), false);
        Assert.assertEquals(new MotionValue(1, 1, 1, 1, 1, 1), reversed);
    }

    @Test
    public final void applyDoesReverseRollMotionIfToggled() {
        final MotionValue reversed = MotionReverser.apply(new MotionValue(0, 0, 0, 0, 0, 1), true);
        Assert.assertEquals(-1, reversed.getRoll(), 0f);
    }

    @Test
    public final void applyDoesReverseSurgeMotionIfToggled() {
        final MotionValue reversed = MotionReverser.apply(new MotionValue(0, 0, 1, 0, 0, 0), true);
        Assert.assertEquals(-1, reversed.getSurge(), 0f);
    }

    @Test
    public final void applyDoesReverseSwayMotionIfToggled() {
        final MotionValue reversed = MotionReverser.apply(new MotionValue(0, 1, 0, 0, 0, 0), true);
        Assert.assertEquals(-1, reversed.getSway(), 0f);
    }

    @Test
    public final void applyDoesNotReverseHeaveMotionIfToggled() {
        final MotionValue reversed = MotionReverser.apply(new MotionValue(1, 0, 0, 0, 0, 0), true);
        Assert.assertEquals(1, reversed.getHeave(), 0f);
    }

    @Test
    public final void applyDoesNotReverseYawMotionIfToggled() {
        final MotionValue reversed = MotionReverser.apply(new MotionValue(0, 0, 0, 0, 1, 0), true);
        Assert.assertEquals(1, reversed.getYaw(), 0f);
    }
}

package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.value.MotionValue;

import org.junit.Assert;
import org.junit.Test;

public final class MotionReverserTest {
    @Test
    public final void applyDoesNotReverseMotionIfNotToggled() {
        final MotionReverser fn = new MotionReverser();
        final MotionValue reversed = fn.apply(new MotionValue(1, 1, 1, 1, 1, 1));
        Assert.assertEquals(new MotionValue(1, 1, 1, 1, 1, 1), reversed);
    }

    @Test
    public final void applyDoesReverseRollMotionIfToggled() {
        final MotionReverser fn = new MotionReverser();
        fn.toggle();
        final MotionValue reversed = fn.apply(new MotionValue(0, 0, 0, 0, 0, 1));
        Assert.assertEquals(-1, reversed.getRoll(), 0f);
    }

    @Test
    public final void applyDoesReverseSurgeMotionIfToggled() {
        final MotionReverser fn = new MotionReverser();
        fn.toggle();
        final MotionValue reversed = fn.apply(new MotionValue(0, 0, 1, 0, 0, 0));
        Assert.assertEquals(-1, reversed.getSurge(), 0f);
    }

    @Test
    public final void applyDoesReverseSwayMotionIfToggled() {
        final MotionReverser fn = new MotionReverser();
        fn.toggle();
        final MotionValue reversed = fn.apply(new MotionValue(0, 1, 0, 0, 0, 0));
        Assert.assertEquals(-1, reversed.getSway(), 0f);
    }

    @Test
    public final void applyDoesNotReverseHeaveMotionIfToggled() {
        final MotionReverser fn = new MotionReverser();
        fn.toggle();
        final MotionValue reversed = fn.apply(new MotionValue(1, 0, 0, 0, 0, 0));
        Assert.assertEquals(1, reversed.getHeave(), 0f);
    }

    @Test
    public final void applyDoesNotReverseYawMotionIfToggled() {
        final MotionReverser fn = new MotionReverser();
        fn.toggle();
        final MotionValue reversed = fn.apply(new MotionValue(0, 0, 0, 0, 1, 0));
        Assert.assertEquals(1, reversed.getYaw(), 0f);
    }
}

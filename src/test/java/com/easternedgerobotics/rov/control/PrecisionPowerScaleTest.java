package com.easternedgerobotics.rov.control;

import com.easternedgerobotics.rov.value.MotionValue;
import com.easternedgerobotics.rov.value.PrecisionPowerValue;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings({"checkstyle:magicnumber"})
public class PrecisionPowerScaleTest {
    @Test
    public final void applyDoesNotScaleMotionIfNotEnabled() {
        final PrecisionPowerScale precision = new PrecisionPowerScale();
        final PrecisionPowerValue power = new PrecisionPowerValue(.5f, true, true, true, true, true, true);
        final MotionValue scaled = precision.apply(new MotionValue(1, 1, 1, 1, 1, 1), power);
        Assert.assertEquals(new MotionValue(1, 1, 1, 1, 1, 1), scaled);
    }

    @Test
    public final void applyDoesScaleMotionIfEnabled() {
        final PrecisionPowerScale precision = new PrecisionPowerScale();
        final PrecisionPowerValue power = new PrecisionPowerValue(.5f, true, true, true, true, true, true);
        precision.setEnabled(true);
        final MotionValue scaled = precision.apply(new MotionValue(1, 1, 1, 1, 1, 1), power);
        Assert.assertEquals(new MotionValue(.5f, .5f, .5f, .5f, .5f, .5f), scaled);
    }

    @Test
    public final void applyDoesScaleNone() {
        final PrecisionPowerScale precision = new PrecisionPowerScale();
        final PrecisionPowerValue power = new PrecisionPowerValue(.5f, false, false, false, false, false, false);
        precision.setEnabled(true);
        final MotionValue scaled = precision.apply(new MotionValue(1, 1, 1, 1, 1, 1), power);
        Assert.assertEquals(new MotionValue(1, 1, 1, 1, 1, 1), scaled);
    }

    @Test
    public final void applyDoesScaleJustHeave() {
        final PrecisionPowerScale precision = new PrecisionPowerScale();
        final PrecisionPowerValue power = new PrecisionPowerValue(.5f, true, false, false, false, false, false);
        precision.setEnabled(true);
        final MotionValue scaled = precision.apply(new MotionValue(1, 1, 1, 1, 1, 1), power);
        Assert.assertEquals(new MotionValue(.5f, 1, 1, 1, 1, 1), scaled);
    }

    @Test
    public final void applyDoesScaleJustSway() {
        final PrecisionPowerScale precision = new PrecisionPowerScale();
        final PrecisionPowerValue power = new PrecisionPowerValue(.5f, false, true, false, false, false, false);
        precision.setEnabled(true);
        final MotionValue scaled = precision.apply(new MotionValue(1, 1, 1, 1, 1, 1), power);
        Assert.assertEquals(new MotionValue(1, .5f, 1, 1, 1, 1), scaled);
    }

    @Test
    public final void applyDoesScaleJustSurge() {
        final PrecisionPowerScale precision = new PrecisionPowerScale();
        final PrecisionPowerValue power = new PrecisionPowerValue(.5f, false, false, true, false, false, false);
        precision.setEnabled(true);
        final MotionValue scaled = precision.apply(new MotionValue(1, 1, 1, 1, 1, 1), power);
        Assert.assertEquals(new MotionValue(1, 1, .5f, 1, 1, 1), scaled);
    }

    @Test
    public final void applyDoesScaleJustPitch() {
        final PrecisionPowerScale precision = new PrecisionPowerScale();
        final PrecisionPowerValue power = new PrecisionPowerValue(.5f, false, false, false, true, false, false);
        precision.setEnabled(true);
        final MotionValue scaled = precision.apply(new MotionValue(1, 1, 1, 1, 1, 1), power);
        Assert.assertEquals(new MotionValue(1, 1, 1, .5f, 1, 1), scaled);
    }

    @Test
    public final void applyDoesScaleJustYaw() {
        final PrecisionPowerScale precision = new PrecisionPowerScale();
        final PrecisionPowerValue power = new PrecisionPowerValue(.5f, false, false, false, false, true, false);
        precision.setEnabled(true);
        final MotionValue scaled = precision.apply(new MotionValue(1, 1, 1, 1, 1, 1), power);
        Assert.assertEquals(new MotionValue(1, 1, 1, 1, .5f, 1), scaled);
    }

    @Test
    public final void applyDoesScaleJustRoll() {
        final PrecisionPowerScale precision = new PrecisionPowerScale();
        final PrecisionPowerValue power = new PrecisionPowerValue(.5f, false, false, false, false, false, true);
        precision.setEnabled(true);
        final MotionValue scaled = precision.apply(new MotionValue(1, 1, 1, 1, 1, 1), power);
        Assert.assertEquals(new MotionValue(1, 1, 1, 1, 1, .5f), scaled);
    }
}

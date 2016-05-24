package com.easternedgerobotics.rov.io.pololu;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class MaestroTest {
    @Test
    public final void hashCodeIsConsistentAcrossMultipleCallsWithoutAnyChangesToTheObject() {
        final List<?> maestro = new Maestro<>(null /* this shouldn't matter */, (byte) 1);
        final int a = maestro.hashCode();
        final int b = maestro.hashCode();
        Assert.assertTrue("List#hashCode() should be consistent across calls", a == b);
    }
}

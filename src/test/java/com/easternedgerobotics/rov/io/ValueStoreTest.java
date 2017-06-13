package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.io.files.ValueStore;
import com.easternedgerobotics.rov.test.OrgPwmTinylogSuppressionRule;
import com.easternedgerobotics.rov.test.SunUtilLoggingSuppressionRule;
import com.easternedgerobotics.rov.value.MotionPowerValue;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class ValueStoreTest {
    private static final String TEST_NODE = "test-value-store";

    private static final int STORE_COUNT = 9;

    @Rule
    public final TestRule motionPowerProfileLoggerRule = new OrgPwmTinylogSuppressionRule(ValueStore.class);

    @Rule
    public final TestRule javaUtilPrefsRule = new SunUtilLoggingSuppressionRule("java.util.prefs");

    @Test
    public final void doProfilesStartEmpty() {
        try {
            initPrefs();
            final ValueStore<MotionPowerValue> motionPowerStore = ValueStore.of(MotionPowerValue.class, TEST_NODE);

            for (int index = 0; index < STORE_COUNT; index++) {
                final Optional<MotionPowerValue> motionPower = motionPowerStore.get(index);
                if (motionPower.isPresent()) {
                    Assert.fail(String.format(
                        "Values should not exist by default. Returned %d: %s", index, motionPower.get()));
                }
            }
        } finally {
            deletePrefs();
        }
    }

    @Test
    public final void doProfilesPersistRAM() {
        try {
            initPrefs();
            final ValueStore<MotionPowerValue> motionPowerStore = ValueStore.of(MotionPowerValue.class, TEST_NODE);
            final MotionPowerValue testValue = new MotionPowerValue(.1f, .2f, .3f, .4f, .5f, .6f, .7f);
            for (int index = 0; index < STORE_COUNT; index++) {
                motionPowerStore.set(index, testValue);
            }
            for (int index = 0; index < STORE_COUNT; index++) {
                final Optional<MotionPowerValue> motionPower = motionPowerStore.get(index);
                if (!motionPower.isPresent()) {
                    Assert.fail(String.format("Values should exist after setting. Missing %d", index));
                } else if (!motionPower.get().equals(testValue)) {
                    Assert.fail(String.format("Value %d should be %s value. Returned %s from RAM",
                        index, testValue, motionPower.get()));
                }
            }
        } finally {
            deletePrefs();
        }
    }

    @Test
    @SuppressWarnings({"checkstyle:AvoidNestedBlocks"})
    public final void doProfilesPersistDisk() {
        try {
            initPrefs();
            final MotionPowerValue testValue = new MotionPowerValue(.1f, .2f, .3f, .4f, .5f, .6f, .7f);
            {
                final ValueStore<MotionPowerValue> motionPowerStore = ValueStore.of(MotionPowerValue.class, TEST_NODE);
                for (int index = 0; index < STORE_COUNT; index++) {
                    motionPowerStore.set(index, testValue);
                }
            }
            System.gc();
            {
                final ValueStore<MotionPowerValue> motionPowerStore = ValueStore.of(MotionPowerValue.class, TEST_NODE);
                for (int index = 0; index < STORE_COUNT; index++) {
                    final Optional<MotionPowerValue> motionPower = motionPowerStore.get(index);
                    if (!motionPower.isPresent()) {
                        Assert.fail(String.format("Values should exist after setting. Missing %d", index));
                    } else if (!motionPower.get().equals(testValue)) {
                        Assert.fail(String.format("Value %d should be %s value. Returned %s from RAM",
                            index, testValue, motionPower.get()));
                    }
                }
            }
        } finally {
            deletePrefs();
        }
    }

    private static void initPrefs() {
        try {
            if (Preferences.userRoot().node(TEST_NODE).keys().length > 0) {
                Assert.fail(String.format("Testing preferences %s should be empty.", TEST_NODE));
            }
        } catch (final BackingStoreException e) {
            Assert.fail(String.format("Testing preferences %s could not be access.", TEST_NODE));
        }
    }

    private static void deletePrefs() {
        try {
            Preferences.userRoot().node(TEST_NODE).clear();
        } catch (final BackingStoreException e) {
            Assert.fail(String.format("Testing preferences %s could not be cleared.", TEST_NODE));
        }
    }
}

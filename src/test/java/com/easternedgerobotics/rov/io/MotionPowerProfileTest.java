package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.value.MotionPowerValue;

import org.junit.Assert;
import org.junit.Test;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class MotionPowerProfileTest {
    private static final String PROFILE_TEST_NODE = "test-profiles";

    private static final int PROFILE_COUNT = 9;

    @Test
    public final void doProfilesStartEmpty() {
        try {
            initPrefs();
            final MotionPowerProfile profile = new MotionPowerProfile(PROFILE_TEST_NODE);

            final MotionPowerValue defaultMotion = new MotionPowerValue();
            for (int index = 0; index < PROFILE_COUNT; index++) {
                final MotionPowerValue profileMotion = profile.get(index);
                if (!profileMotion.equals(defaultMotion)) {
                    Assert.fail(String.format(
                        "Motion profile %d should be default value. Returned %s", index, profileMotion));
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
            final MotionPowerProfile profile = new MotionPowerProfile(PROFILE_TEST_NODE);
            final MotionPowerValue testValue = new MotionPowerValue(.1f, .2f, .3f, .4f, .5f, .6f, .7f);
            for (int index = 0; index < PROFILE_COUNT; index++) {
                profile.set(index, testValue);
            }
            for (int index = 0; index < PROFILE_COUNT; index++) {
                final MotionPowerValue profileMotion = profile.get(index);
                if (!profileMotion.equals(testValue)) {
                    Assert.fail(String.format("Motion profile %d should be %s value. Returned %s from RAM",
                        index, testValue, profileMotion));
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
                final MotionPowerProfile profile = new MotionPowerProfile(PROFILE_TEST_NODE);
                for (int index = 0; index < PROFILE_COUNT; index++) {
                    profile.set(index, testValue);
                }
            }
            {
                final MotionPowerProfile profile = new MotionPowerProfile(PROFILE_TEST_NODE);
                for (int index = 0; index < PROFILE_COUNT; index++) {
                    final MotionPowerValue profileMotion = profile.get(index);
                    if (!profileMotion.equals(testValue)) {
                        Assert.fail(String.format("Motion profile %d should be %s value. Returned %s from RAM",
                            index, testValue, profileMotion));
                    }
                }
            }
        } finally {
            deletePrefs();
        }
    }

    private static void initPrefs() {
        try {
            if (Preferences.userRoot().node(PROFILE_TEST_NODE).keys().length > 0) {
                Assert.fail(String.format("Testing preferences %s should be empty.", PROFILE_TEST_NODE));
            }
        } catch (final BackingStoreException e) {
            Assert.fail(String.format("Testing preferences %s could not be access.", PROFILE_TEST_NODE));
        }
    }

    private static void deletePrefs() {
        try {
            Preferences.userRoot().node(PROFILE_TEST_NODE).clear();
        } catch (final BackingStoreException e) {
            Assert.fail(String.format("Testing preferences %s could not be cleared.", PROFILE_TEST_NODE));
        }
    }
}

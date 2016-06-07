package com.easternedgerobotics.rov.io;

import com.easternedgerobotics.rov.value.MotionPowerValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

public final class MotionPowerProfile {
    /**
     * The directory which contains the power profiles.
     */
    private final Preferences profileHome;

    /**
     * A copy of power profiles to exist in RAM.
     */
    private final Map<Integer, MotionPowerValue> profiles = new HashMap<>();

    /**
     * Create a power profile loader/ setter which works in the specified directory.
     *
     * @param profile the preferences directory.
     */
    public MotionPowerProfile(final String profile) {
        this.profileHome = Preferences.userRoot().node(profile);
    }

    /**
     * Get the latest MotionPowerValue associated with the index.
     * Fetches from disk if not loaded.
     *
     * @param index the power profile index.
     * @return MotionPowerValue
     */
    public MotionPowerValue get(final int index) {
        return profiles.computeIfAbsent(index, i -> {
            final String profileName = String.format("profile-%d", i);
            final String profileStr = profileHome.get(profileName, "DEFAULT");
            if (profileStr.equals("DEFAULT")) {
                Logger.warn("The profile {} does not exist.", profileName);
                return new MotionPowerValue();
            }
            try {
                return new ObjectMapper().readValue(profileStr, MotionPowerValue.class);
            } catch (final IOException e) {
                Logger.warn("The profile {} could not be parsed from '{}': {}", profileName, profileStr, e);
                return new MotionPowerValue();
            }
        });
    }

    /**
     * Update the MotionPowerValue profile for a given index and write this value to disk.
     *
     * @param index the power profile index.
     * @param profile MotionPowerValue to be saved to disk.
     */
    public void set(final int index, final MotionPowerValue profile) {
        profiles.put(index, profile);
        final String profileName = String.format("profile-%d", index);
        try {
            profileHome.put(profileName, new ObjectMapper().writeValueAsString(profile));
        } catch (final IOException e) {
            Logger.warn("The profile {} could not be saved to {}: {}", profile, profileName, e);
        }
    }
}

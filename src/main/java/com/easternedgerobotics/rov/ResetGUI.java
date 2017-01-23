package com.easternedgerobotics.rov;

import com.easternedgerobotics.rov.fx.ViewLoader;

import java.util.prefs.BackingStoreException;

public final class ResetGUI {
    private ResetGUI() {

    }

    public static void main(final String[] args) throws BackingStoreException {
        ViewLoader.dropPreferences();
    }
}

package com.easternedgerobotics.rov.config;

public interface TopsidesConfig {
    String pilotPanelName();

    String pilotPanelPort();

    int pilotPanelTimeOut();

    int pilotPanelBaud();

    int pilotPanelHeartbeatInterval();

    int pilotPanelHeartbeatTimeout();

    byte[] pilotPanelOutputs();

    byte[] pilotPanelInputPullups();

    int joystickRecoveryInterval();

    byte emergencyStopButtonAddress();

    int profileSwitchDuration();

    String profilePref();

    int profileSaveFlashCount();

    int profileSaveFlashDuration();

    int heartbeatLostInterval();
}

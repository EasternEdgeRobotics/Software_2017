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

    long profileSwitchDuration();

    String profilePref();

    String mpv();
}

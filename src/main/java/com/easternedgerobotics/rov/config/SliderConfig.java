package com.easternedgerobotics.rov.config;

public interface SliderConfig {
    byte globalPowerSliderAddress();

    byte heavePowerSliderAddress();

    byte swayPowerSliderAddress();

    byte surgePowerSliderAddress();

    byte yawPowerSliderAddress();

    byte rollPowerSliderAddress();

    byte lightPowerSliderAddress();
}

package com.easternedgerobotics.rov.io.pololu;

import com.easternedgerobotics.rov.io.devices.I2C;
import com.easternedgerobotics.rov.io.devices.MockI2CBus;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.List;

@SuppressWarnings({"checkstyle:MagicNumber"})
public final class AltIMU10v3Test {
    @Test
    public void doesEnableJustLowDevices() {
        final List<I2C> bus = new MockI2CBus();

        final I2C l3gLow = bus.get(AltIMU10v3.L3GD20H_SA0_LOW_ADDRESS);
        final I2C l3gHigh = bus.get(AltIMU10v3.L3GD20H_SA0_HIGH_ADDRESS);
        final I2C lpsLow = bus.get(AltIMU10v3.LPS331AP_SA0_LOW_ADDRESS);
        final I2C lpsHigh = bus.get(AltIMU10v3.LPS331AP_SA0_HIGH_ADDRESS);
        final I2C lsmLow = bus.get(AltIMU10v3.LSM303D_SA0_LOW_ADDRESS);
        final I2C lsmHigh = bus.get(AltIMU10v3.LSM303D_SA0_HIGH_ADDRESS);

        final AltIMU10v3 lowImu = new AltIMU10v3(bus, false);

        Assert.assertEquals(l3gLow.read(L3GD20H.CTRL1), L3GD20H.CTRL1_ENABLE);
        Assert.assertEquals(l3gHigh.read(L3GD20H.CTRL1), 0);

        Assert.assertEquals(lpsLow.read(LPS331AP.CTRL_REG1), LPS331AP.CTRL1_ENABLE);
        Assert.assertEquals(lpsHigh.read(LPS331AP.CTRL_REG1), 0);

        Assert.assertEquals(lsmLow.read(LSM303D.CTRL1), LSM303D.CTRL1_VAL);
        Assert.assertEquals(lsmHigh.read(LSM303D.CTRL1), 0);
        Assert.assertEquals(lsmLow.read(LSM303D.CTRL2), LSM303D.CTRL2_VAL);
        Assert.assertEquals(lsmHigh.read(LSM303D.CTRL2), 0);
        Assert.assertEquals(lsmLow.read(LSM303D.CTRL5), LSM303D.CTRL5_VAL);
        Assert.assertEquals(lsmHigh.read(LSM303D.CTRL5), 0);
        Assert.assertEquals(lsmLow.read(LSM303D.CTRL6), LSM303D.CTRL6_VAL);
        Assert.assertEquals(lsmHigh.read(LSM303D.CTRL6), 0);
        Assert.assertEquals(lsmLow.read(LSM303D.CTRL7), LSM303D.CTRL7_VAL);
        Assert.assertEquals(lsmHigh.read(LSM303D.CTRL7), 0);
    }

    @Test
    public void doesEnableJustHighDevices() {
        final List<I2C> bus = new MockI2CBus();

        final I2C l3gLow = bus.get(AltIMU10v3.L3GD20H_SA0_LOW_ADDRESS);
        final I2C l3gHigh = bus.get(AltIMU10v3.L3GD20H_SA0_HIGH_ADDRESS);
        final I2C lpsLow = bus.get(AltIMU10v3.LPS331AP_SA0_LOW_ADDRESS);
        final I2C lpsHigh = bus.get(AltIMU10v3.LPS331AP_SA0_HIGH_ADDRESS);
        final I2C lsmLow = bus.get(AltIMU10v3.LSM303D_SA0_LOW_ADDRESS);
        final I2C lsmHigh = bus.get(AltIMU10v3.LSM303D_SA0_HIGH_ADDRESS);

        final AltIMU10v3 highImu = new AltIMU10v3(bus, true);

        Assert.assertEquals(l3gLow.read(L3GD20H.CTRL1), 0);
        Assert.assertEquals(l3gHigh.read(L3GD20H.CTRL1), L3GD20H.CTRL1_ENABLE);

        Assert.assertEquals(lpsLow.read(LPS331AP.CTRL_REG1), 0);
        Assert.assertEquals(lpsHigh.read(LPS331AP.CTRL_REG1), LPS331AP.CTRL1_ENABLE);

        Assert.assertEquals(lsmLow.read(LSM303D.CTRL1), 0);
        Assert.assertEquals(lsmHigh.read(LSM303D.CTRL1), LSM303D.CTRL1_VAL);
        Assert.assertEquals(lsmLow.read(LSM303D.CTRL2), 0);
        Assert.assertEquals(lsmHigh.read(LSM303D.CTRL2), LSM303D.CTRL2_VAL);
        Assert.assertEquals(lsmLow.read(LSM303D.CTRL5), 0);
        Assert.assertEquals(lsmHigh.read(LSM303D.CTRL5), LSM303D.CTRL5_VAL);
        Assert.assertEquals(lsmLow.read(LSM303D.CTRL6), 0);
        Assert.assertEquals(lsmHigh.read(LSM303D.CTRL6), LSM303D.CTRL6_VAL);
        Assert.assertEquals(lsmLow.read(LSM303D.CTRL7), 0);
        Assert.assertEquals(lsmHigh.read(LSM303D.CTRL7), LSM303D.CTRL7_VAL);
    }

    @Test
    public void doesReadCorrectPressure() {
        final List<I2C> bus = new MockI2CBus();
        final AltIMU10v3 imu = new AltIMU10v3(bus, false);
        Assert.assertEquals(0, imu.pressure().getPressure(), 0.001);
        final float pressure = 111;
        final int pressureRaw = (int) (pressure * LPS331AP.PRESSURE_SCALAR);
        final byte[] bytes = ByteBuffer.allocate(4).putInt(pressureRaw).array();
        for (int i = 0; i < 2; i++) {
            final byte temp = bytes[i];
            bytes[i] = bytes[3 - i];
            bytes[3 - i] = temp;
        }
        bus.get(AltIMU10v3.LPS331AP_SA0_LOW_ADDRESS).write(LPS331AP.PRESS_POUT_XL_REH, bytes);
        Assert.assertEquals(pressure, imu.pressure().getPressure(), 0.001);
        bus.get(AltIMU10v3.LPS331AP_SA0_LOW_ADDRESS).write(LPS331AP.PRESS_POUT_XL_REH, new byte[] {0, 0, 0, 0});
        Assert.assertEquals(0, imu.pressure().getPressure(), 0.001);
    }

    @Test
    public void doesReadCorrectTemperature() {
        final List<I2C> bus = new MockI2CBus();
        final AltIMU10v3 imu = new AltIMU10v3(bus, false);
        Assert.assertEquals(LPS331AP.TEMP_OFFSET, imu.temperature().getTemperature(), 0.001);
        final float temperature = 33;
        final short temperatureRaw = (short) ((temperature - LPS331AP.TEMP_OFFSET) * LPS331AP.TEMP_SCALAR);
        final byte[] bytes = ByteBuffer.allocate(2).putShort(temperatureRaw).array();
        final byte temp = bytes[0];
        bytes[0] = bytes[1];
        bytes[1] = temp;
        bus.get(AltIMU10v3.LPS331AP_SA0_LOW_ADDRESS).write(LPS331AP.TEMP_OUT_L, bytes);
        Assert.assertEquals(temperature, imu.temperature().getTemperature(), 0.001);
        bus.get(AltIMU10v3.LPS331AP_SA0_LOW_ADDRESS).write(LPS331AP.TEMP_OUT_L, new byte[] {0, 0, 0, 0});
        Assert.assertEquals(LPS331AP.TEMP_OFFSET, imu.temperature().getTemperature(), 0.001);
    }

    @Test
    public void doesReadCorrectGyro() {
        final List<I2C> bus = new MockI2CBus();
        final AltIMU10v3 imu = new AltIMU10v3(bus, false);
        Assert.assertEquals(0, imu.angularVelocity().getX(), 0.001);
        Assert.assertEquals(0, imu.angularVelocity().getY(), 0.001);
        Assert.assertEquals(0, imu.angularVelocity().getZ(), 0.001);
        final float x = 1111, y = 2222, z = 3333;
        final byte[] bytes = ByteBuffer.allocate(6).putShort((short) z).putShort((short) y).putShort((short) x).array();
        for (int i = 0; i < 3; i++) {
            final byte temp = bytes[i];
            bytes[i] = bytes[5 - i];
            bytes[5 - i] = temp;
        }
        bus.get(AltIMU10v3.L3GD20H_SA0_LOW_ADDRESS).write(L3GD20H.OUT_X_L, bytes);
        Assert.assertEquals(x, imu.angularVelocity().getX(), 0.001);
        Assert.assertEquals(y, imu.angularVelocity().getY(), 0.001);
        Assert.assertEquals(z, imu.angularVelocity().getZ(), 0.001);
        bus.get(AltIMU10v3.L3GD20H_SA0_LOW_ADDRESS).write(L3GD20H.OUT_X_L, new byte[] {0, 0, 0, 0, 0, 0});
        Assert.assertEquals(0, imu.angularVelocity().getX(), 0.001);
        Assert.assertEquals(0, imu.angularVelocity().getY(), 0.001);
        Assert.assertEquals(0, imu.angularVelocity().getZ(), 0.001);
    }

    @Test
    public void doesReadCorrectMagnetometer() {
        final List<I2C> bus = new MockI2CBus();
        final AltIMU10v3 imu = new AltIMU10v3(bus, false);
        Assert.assertEquals(0, imu.rotation().getX(), 0.001);
        Assert.assertEquals(0, imu.rotation().getY(), 0.001);
        Assert.assertEquals(0, imu.rotation().getZ(), 0.001);
        final float x = 1111, y = 2222, z = 3333;
        final byte[] bytes = ByteBuffer.allocate(6).putShort((short) z).putShort((short) y).putShort((short) x).array();
        for (int i = 0; i < 3; i++) {
            final byte temp = bytes[i];
            bytes[i] = bytes[5 - i];
            bytes[5 - i] = temp;
        }
        bus.get(AltIMU10v3.LSM303D_SA0_LOW_ADDRESS).write(LSM303D.OUT_X_L_M, bytes);
        Assert.assertEquals(x, imu.rotation().getX(), 0.001);
        Assert.assertEquals(y, imu.rotation().getY(), 0.001);
        Assert.assertEquals(z, imu.rotation().getZ(), 0.001);
        bus.get(AltIMU10v3.LSM303D_SA0_LOW_ADDRESS).write(LSM303D.OUT_X_L_M, new byte[] {0, 0, 0, 0, 0, 0});
        Assert.assertEquals(0, imu.angularVelocity().getX(), 0.001);
        Assert.assertEquals(0, imu.angularVelocity().getY(), 0.001);
        Assert.assertEquals(0, imu.angularVelocity().getZ(), 0.001);
    }

    @Test
    public void doesReadCorrectAccelerometer() {
        final List<I2C> bus = new MockI2CBus();
        final AltIMU10v3 imu = new AltIMU10v3(bus, false);
        Assert.assertEquals(0, imu.acceleration().getX(), 0.001);
        Assert.assertEquals(0, imu.acceleration().getY(), 0.001);
        Assert.assertEquals(0, imu.acceleration().getZ(), 0.001);
        final float x = 1111, y = 2222, z = 3333;
        final byte[] bytes = ByteBuffer.allocate(6).putShort((short) z).putShort((short) y).putShort((short) x).array();
        for (int i = 0; i < 3; i++) {
            final byte temp = bytes[i];
            bytes[i] = bytes[5 - i];
            bytes[5 - i] = temp;
        }
        bus.get(AltIMU10v3.LSM303D_SA0_LOW_ADDRESS).write(LSM303D.OUT_X_L_A, bytes);
        Assert.assertEquals(x, imu.acceleration().getX(), 0.001);
        Assert.assertEquals(y, imu.acceleration().getY(), 0.001);
        Assert.assertEquals(z, imu.acceleration().getZ(), 0.001);
        bus.get(AltIMU10v3.LSM303D_SA0_LOW_ADDRESS).write(LSM303D.OUT_X_L_A, new byte[] {0, 0, 0, 0, 0, 0});
        Assert.assertEquals(0, imu.acceleration().getX(), 0.001);
        Assert.assertEquals(0, imu.acceleration().getY(), 0.001);
        Assert.assertEquals(0, imu.acceleration().getZ(), 0.001);
    }
}

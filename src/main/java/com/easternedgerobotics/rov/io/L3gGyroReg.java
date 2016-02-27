package com.easternedgerobotics.rov.io;

public enum L3gGyroReg {
    // Device identification register.
    WHO_AM_I((byte) 0x0F),

    /**
     * CTRL1 Format: DR1 DR0 BW1 BW0 PD Zen Xen Yen
     * DR1 DR0, Output data rate selection. Refer to Table 21
     * BW1 BW0, Bandwidth selection. Refer to Table 21
     * PD, Power mode. Default value: 0. 0= Power Down 1= Normal Mode
     * Zen Xen Yen, 0: axis disabled; 1: axis enabled
     */
    CTRL1((byte) 0x20),
    CTRL_REG1((byte) 0x20),

    /**
     * CTRL2 Format: EXTRen LVLen HPM1 HPM0 HPCF3 HPCF2 HPCF1 HPCF0
     * EXTRen Edge sensitive trigger Enable: Default value: 0
     * LVLen Level sensitive trigger Enable: Default value: 0
     * HPM1-HPM0 High Pass filter Mode Selection. Default value: 00
     * HPCF3-HPCF0 High Pass filter Cut Off frequency selection. Default value: 0000
     */
    CTRL2((byte) 0x21),
    CTRL_REG2((byte) 0x21),

    /**
     * CTRL3 Format: INT1_IG INT1_Boot H_Lactive PP_OD INT2_DRDY INT2_FTH INT2_ORun INT2_Empty
     * INT1_IG Interrupt enable on INT1 pin. Default value 0. (0: disable; 1: enable)
     * INT1_Boot Boot status available on INT1 pin. Default value 0. (0: disable; 1: enable)
     * H_Lactive Interrupt active configuration on INT. Default value 0. (0: high; 1:low)
     * PP_OD Push- Pull / Open drain. Default value: 0. (0: push-pull; 1: open drain)
     * INT2_DRDY Date Ready on DRDY/INT2 pin. Default value 0. (0: disable; 1: enable)
     * INT2_FTH FIFO Threshold interrupt on DRDY/INT2 pin. Default value: 0. (0: disable; 1: enable)
     * INT2_ORun FIFO Overrun interrupt on DRDY/INT2 pin. Default value: 0. (0: disable; 1: enable)
     * INT2_Empty FIFO Empty interrupt on DRDY/INT2 pin. Default value: 0. (0: disable; 1: enable)
     */
    CTRL3((byte) 0x22),
    CTRL_REG3((byte) 0x22),

    /**
     * CTRL4 Format: BDU BLE FS1 FS0 IMPen ST2 ST1 SIM
     * BDU Block data update. Default value: 0
     *     (0: continuos update; 1: output registers not updated until MSB and LSB reading)
     * BLE Big/little endian data selection. Default value 0.(0: Data LSB @ lower address; 1: Data MSB @ lower address)
     * FS1-FS0 Full scale selection. Default value: 00 (00: 245 dps; 01: 500 dps; 1x: 2000 dps)
     * IMPen Level sensitive latched enable. Default value: 0
     *     (0: level sensitive latched disabled; 1: level sensitive latched enabled)
     * ST2-ST1 Self-test enable. Default value: 00
     *     00 = normal mode (default) 01 = self-test 0 (+) 10 = unused 11 = self-test 1(-)
     * SIM SPI Serial Interface Mode selection. Default value: 0 (0: 4-wire interface; 1: 3-wire interface).
     */
    CTRL4((byte) 0x23),
    CTRL_REG4((byte) 0x23),

    /**
     * CTRL5 Format: BOOT FIFO_EN StopOnFTH HPen IG_Sel1 IG_Sel0 Out_Sel1 Out_Sel0
     * BOOT Reboot memory content. Default value: 0 (0: normal mode; 1: reboot memory content(1))
     *      1. Boot request is executed as soon as internal oscillator is turned-on.
     *         It is possible to set bit while in Powerdown
     * mode, in this case it will be served at the next normal mode or sleep mode.
     * FIFO_EN FIFO enable. Default value: 0 (0: FIFO disable; 1: FIFO Enable)
     * StopOnFTH Sensing chain FIFO stop values memorization at FIFO Threshold. Default value: 0
     * 0 = FIFO depth is not limited (32 digital words per axis)
     * 1 = FIFO depth is limited to FIFO Threshold which is defined in FIFO_CTRL (2Eh Register)
     * HPen High Pass filter Enable. Default value: 0 (0: HPF disabled; 1: HPF enabled see Figure 23.)
     * IG_Sel1-IG_Sel0 INT Generator selection configuration. Default value: 00 (See Figure 23.)
     * Out_Sel1-Out_Sel0 Out selection configuration. Default value: 00 (See Figure 23.)
     */
    CTRL5((byte) 0x24),
    CTRL_REG5((byte) 0x24),

    // Digital high pass filter reference value. Default value: 0
    REFERENCE((byte) 0x25),

    // Temperature data (-1LSB/deg with 8 bit resolution). The value is expressed as two’s complement.
    OUT_TEMP((byte) 0x26),

    /**
     * STATUS Format: ZYXOR ZOR YOR XOR ZYXDA ZDA YDA XDA
     * ZYXOR X, Y, Z -axis data overrun. Default value: 0
     *     (0: no overrun has occurred; 1: new data has overwritten the previous one before it was read)
     * ZOR Z axis data overrun.
     * YOR Y axis data overrun.
     * XOR X axis data overrun.
     * ZYXDA X, Y, Z -axis new data available. Default value: 0
     *     (0: a new set of data is not yet available; 1: a new set of data is available)
     * ZDA Z axis new data available.
     * YDA Y axis new data available.
     * XDA X axis new data available.
     *
     * X/Y/ZOR Default value: 0 (0: no overrun has occurred;
     *                          1: a new data for the Z-axis has overwritten the previous one)
     * X/Y/ZDA Default value: 0 (0: a new data for the Z-axis is not yet available;
     *                          1: a new data for the Z-axis is available)
     */
    STATUS((byte) 0x27),
    STATUS_REG((byte) 0x27),

    // X-axis angular rate data. The value is expressed as two’s complement.
    OUT_X_L((byte) 0x28),
    OUT_X_H((byte) 0x29),

    //Y-axis angular rate data. The value is expressed as two’s complement.
    OUT_Y_L((byte) 0x2A),
    OUT_Y_H((byte) 0x2B),

    // Z-axis angular rate data. The value is expressed as two’s complement.
    OUT_Z_L((byte) 0x2C),
    OUT_Z_H((byte) 0x2D),

    /**
     * FIFO_CTRL Format: FM2 FM1 FM0 FTH4 FTH3 FTH2 FTH1 FTH0
     * FM2-FM0 FIFO mode selection. Default value: 000 (see Table 41)
     * FTH4-FTH0 FIFO threshold setting. Default value: 0
     * FM2   FM1    FM0    FIFO mode
     * 0     0      0      Bypass mode
     * 0     0      1      FIFO mode
     * 0     1      0      Stream mode
     * 0     1      1      Stream-to-FIFO mode
     * 1     0      0      Bypass-to-stream mode
     * 1     1      0      Dynamic stream mode
     * 1     1      1      Bypass-to-FIFO mode
     */
    FIFO_CTRL((byte) 0x2E),
    FIFO_CTRL_REG((byte) 0x2E),

    /**
     * FIFO_SRC Format: FTH OVRN EMPTY FSS4 FSS3 FSS2 FSS1 FSS0
     * FTH FIFO threshold status.
     *     (0: FIFO filling is lower than FTH level; 1: FIFO filling is equal or higher than FTH level)
     * OVRN Overrun bit status. (0: FIFO is not completely filled; 1:FIFO is completely filled)
     * EMPTY FIFO empty bit. ( 0: FIFO not empty; 1: FIFO empty)
     * FSS4-FSS0 FIFO stored data level of the unread samples
     */
    FIFO_SRC((byte) 0x2F),
    FIFO_SRC_REG((byte) 0x2F),

    /**
     * IG_CFG Format: AND/OR LIR ZHIE ZLIE YHIE YLIE XHIE XLIE
     * AND/OR AND/OR combination of Interrupt events. Default value: 0
     *     (0: OR combination of interrupt events 1: AND combination of interrupt * events
     * LIR Latch Interrupt Request. Default value: 0
     *     (0: interrupt request not latched; 1: interrupt request latched) Cleared by reading IG_SRC * reg.
     * ZHIE Enable interrupt generation on Z high event.
     * ZLIE Enable interrupt generation on Z low event.
     * YHIE Enable interrupt generation on Y high event.
     * YLIE Enable interrupt generation on Y low event.
     * XHIE Enable interrupt generation on X high event.
     * XLIE Enable interrupt generation on X low event.
     * <p>
     * Default value: 0 (0: disable interrupt request;
     *                   1: enable interrupt request onmeasured angular rate value higher than preset threshold)
     */
    IG_CFG((byte) 0x30),
    INT1_CFG((byte) 0x30),

    /**
     * IG_SRC Format: 0 IA ZH ZL YH YL XH XL\
     * IA Interrupt active. Default value: 0
     *     (0: no interrupt has been generated; 1: one or more interrupts have been generated)
     * ZH Z high. Default value: 0 (0: no interrupt, 1: Z High event has occurred)
     * ZL Z low. Default value: 0 (0: no interrupt; 1: Z Low event has occurred)
     * YH Y high. Default value: 0 (0: no interrupt, 1: Y High event has occurred)
     * YL Y low. Default value: 0 (0: no interrupt, 1: Y Low event has occurred)
     * XH X high. Default value: 0 (0: no interrupt, 1: X High event has occurred)
     * XL X low. Default value: 0 (0: no interrupt, 1: X Low event has occurred)
     * <p>
     * Interrupt source register. Read only register.
     * Reading at this address clears IG_SRC IA bit (and eventually the interrupt signal on INT1 pin) and
     * allows the refresh of data in the IG_SRC register if the latched option was chosen.
     */
    IG_SRC((byte) 0x31),
    INT1_SRC((byte) 0x31),

    /**
     * IG_THS_XH Format: DCRM THSX14 THSX13 THSX12 THSX11 THSX10 THSX9 THSX8
     * DCRM Interrupt generation counter mode selection. Default value: 0 0 = Reset 1 = Decrement
     * THSX14 - THSX8 Interrupt threshold on X axis. Default value: 000 0000
     */
    IG_THS_XH((byte) 0x32),
    INT1_THS_XH((byte) 0x32),

    //  Interrupt threshold on X axis. Default value: 0000 0000
    IG_THS_XL((byte) 0x33),
    INT1_THS_XL((byte) 0x33),

    // 7 bit Interrupt threshold on Y axis. Default value: -000 0000
    IG_THS_YH((byte) 0x34),
    INT1_THS_YH((byte) 0x34),

    // Interrupt threshold on Y axis. Default value: 0000 0000
    IG_THS_YL((byte) 0x35),
    INT1_THS_YL((byte) 0x35),

    // 7 bit  Interrupt threshold on Z axis. Default value: -000 0000
    IG_THS_ZH((byte) 0x36),
    INT1_THS_ZH((byte) 0x36),

    // Interrupt threshold on Z axis. Default value: 0000 0000
    IG_THS_ZL((byte) 0x37),
    INT1_THS_ZL((byte) 0x37),

    /**
     * IG_DURATION Format: WAIT D6 D5 D4 D3 D2 D1 D0
     * WAIT WAIT enable. Default value: 0 (0: disable; 1: enable)
     * D6 - D0 Duration value. Default value: 000 0000
     * <p>
     * D6 - D0 bits set the minimum duration of the Interrupt event to be recognized.
     * Duration steps and maximum values depend on the ODR chosen.
     * <p>
     * Wait =’0’: the interrupt falls immediately if signal crosses the selected threshold
     * Wait =’1’: if signal crosses the selected threshold, the interrupt falls after a number of
     * samples equal to the duration counter register value.
     */
    IG_DURATION((byte) 0x38),
    INT1_DURATION((byte) 0x38),

    /**
     * LOW_ODR Format: -- -- DRDY_HL 0 I2C_dis SW_RES 0 Low_ODR
     * DRDY_HL  DRDY/INT2 pin active level. Default value: 0 0 = DRDY active high 1 = DRDY active low
     * I2C_dis 0 = both the I2C and SPI interfaces enabled (default) 1 = SPI only
     * SW_RES Software reset. Default value: 0 0 = Normal Mode 1 = Reset Device
     *     (this bit is cleared by hardware after next flash boot)
     * Low_ODR Low speed ODR. Default value: 0 0 = Low Speed ODR disabled 1 = Low Speed ODR enabled
     * <p>
     * <p>
     * Refer to Table 21 DR and BW configuration setting: for ODR and Bandwidth configuration on CTRL1 register
     * The bits labels zero  must be set to ‘0’ for proper working of the device.
     */
    LOW_ODR((byte) 0x39);

    private final byte address;

    L3gGyroReg(final byte address) {
        this.address = address;
    }

    public byte getAddress() {
        return address;
    }
}

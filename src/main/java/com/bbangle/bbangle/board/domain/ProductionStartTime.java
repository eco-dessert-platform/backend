package com.bbangle.bbangle.board.domain;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.time.LocalTime;
import java.util.Arrays;
import lombok.Getter;

@Getter
public enum ProductionStartTime {

    T_03_04("03:00~04:00", LocalTime.of(3, 0)),
    T_04_05("04:00~05:00", LocalTime.of(4, 0)),
    T_05_06("05:00~06:00", LocalTime.of(5, 0)),
    T_06_07("06:00~07:00", LocalTime.of(6, 0)),
    T_07_08("07:00~08:00", LocalTime.of(7, 0)),
    T_08_09("08:00~09:00", LocalTime.of(8, 0)),
    T_09_10("09:00~10:00", LocalTime.of(9, 0)),
    T_10_11("10:00~11:00", LocalTime.of(10, 0)),
    T_11_12("11:00~12:00", LocalTime.of(11, 0)),
    T_12_13("12:00~13:00", LocalTime.of(12, 0)),
    T_13_14("13:00~14:00", LocalTime.of(13, 0)),
    T_14_15("14:00~15:00", LocalTime.of(14, 0)),
    T_15_16("15:00~16:00", LocalTime.of(15, 0)),
    T_16_17("16:00~17:00", LocalTime.of(16, 0)),
    T_17_18("17:00~18:00", LocalTime.of(17, 0)),
    T_18_19("18:00~19:00", LocalTime.of(18, 0)),
    T_19_20("19:00~20:00", LocalTime.of(19, 0)),
    T_20_21("20:00~21:00", LocalTime.of(20, 0)),
    T_21_22("21:00~22:00", LocalTime.of(21, 0)),
    T_22_23("22:00~23:00", LocalTime.of(22, 0)),
    T_23_00("23:00~00:00", LocalTime.of(23, 0)),
    T_00_01("00:00~01:00", LocalTime.of(0, 0)),
    T_01_02("01:00~02:00", LocalTime.of(1, 0)),
    T_02_03("02:00~03:00", LocalTime.of(2, 0));

    private final String label;
    private final LocalTime startTime;

    ProductionStartTime(String label, LocalTime startTime) {
        this.label = label;
        this.startTime = startTime;
    }

    public static ProductionStartTime from(String value) {
        return Arrays.stream(values())
            .filter(time -> time.name().equals(value))
            .findFirst()
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.INVALID_PRODUCTION_START_TIME));
    }
}
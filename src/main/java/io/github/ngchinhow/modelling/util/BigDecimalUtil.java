package io.github.ngchinhow.modelling.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalUtil {
    public static final BigDecimal ZERO = valueOf(0d);
    public static final BigDecimal ONE = valueOf(1d);
    public static final BigDecimal TWO = valueOf(2d);
    public static final BigDecimal THREE = valueOf(3d);
    public static final BigDecimal FOUR = valueOf(4d);

    public static BigDecimal valueOf(double value) {
        // Setting default scale to the same as max for Oracle database
        return BigDecimal.valueOf(value).setScale(127, RoundingMode.HALF_UP);
    }
}

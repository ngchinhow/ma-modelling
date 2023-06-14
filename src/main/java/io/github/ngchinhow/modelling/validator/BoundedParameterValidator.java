package io.github.ngchinhow.modelling.validator;

import org.apache.commons.math3.fitting.leastsquares.ParameterValidator;
import org.apache.commons.math3.linear.RealVector;

public class BoundedParameterValidator implements ParameterValidator {

    @Override
    public RealVector validate(RealVector realVector) {
        int size = realVector.getDimension();
        for (int i = 0; i < size; i++) {
            realVector.setEntry(i, this.gutter(realVector.getEntry(i)));
        }
        return realVector;
    }

    private double gutter(double x) {
        x = Math.abs(x);
        return x > 1 ? 1 : x;
    }
}

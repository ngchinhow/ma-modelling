package io.github.ngchinhow.modelling.model;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEMAModel {

    protected final int dataSize;
    protected final int scale;
    protected final BigDecimal alpha;
    protected final List<BigDecimal> observedValues;
    // fValues[k][i] = fki
    protected final List<List<BigDecimal>> fValues = new ArrayList<>();
    // jValues[k][i][j] = Jkij
    protected final List<List<List<BigDecimal>>> jValues = new ArrayList<>();
    protected int iteratorCounter = 0;

    protected AbstractEMAModel(int scale, BigDecimal alpha, List<BigDecimal> observedValues) {
        this.scale = scale;
        this.alpha = alpha;
        this.observedValues = observedValues.stream()
            .map(d -> d.setScale(127, RoundingMode.HALF_UP))
            .toList();
        this.dataSize = observedValues.size();
    }

    public abstract MultivariateVectorFunction function();

    public abstract MultivariateMatrixFunction jacobian();

    protected abstract void reinitializeFValues(BigDecimal... parameters);

    protected abstract void reinitializeJValues();

}

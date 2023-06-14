package io.github.ngchinhow.modelling.model;

import io.github.ngchinhow.modelling.util.BigDecimalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
public class DoubleEMAModel extends AbstractEMAModel {
    private final static int PARAMETER_SIZE = 1;

    public DoubleEMAModel(int scale, BigDecimal alpha, List<BigDecimal> observedValues) {
        super(scale, alpha, observedValues);
    }

    @Override
    public MultivariateVectorFunction function() {
        return params -> {
            this.reinitializeFValues();
            double[] functionValues = new double[dataSize];
            BigDecimal beta = BigDecimalUtil.valueOf(params[0]);
            iteratorCounter++;
            log.info("beta estimate for evaluation {} is {}", iteratorCounter, beta);
            functionValues[0] = fValues.get(0).get(0).doubleValue();
            for (int i = 1; i < dataSize; i++) {
                List<BigDecimal> previousPoint = fValues.get(i - 1);
                BigDecimal f1Prev = previousPoint.get(0);
                BigDecimal f2Prev = previousPoint.get(1);
                BigDecimal f1Cur = alpha.multiply(observedValues.get(i))
                    .add(BigDecimalUtil.ONE.subtract(alpha).multiply(f1Prev.add(f2Prev)));
                BigDecimal f2Cur = beta.multiply(f1Cur.subtract(f1Prev))
                    .add(BigDecimalUtil.ONE.subtract(beta).multiply(f2Prev));
                fValues.add(List.of(f1Cur, f2Cur));
                functionValues[i] = f1Cur.setScale(scale, RoundingMode.HALF_UP).doubleValue();
            }
            return functionValues;
        };
    }

    @Override
    public MultivariateMatrixFunction jacobian() {
        return params -> {
            this.reinitializeJValues();
            BigDecimal beta = BigDecimalUtil.valueOf(params[0]);
            double[][] jacobianMatrix = new double[dataSize][PARAMETER_SIZE];
            jacobianMatrix[0][0] = jValues.get(0).get(0).get(0).doubleValue();
            for (int i = 1; i < dataSize; i++) {
                List<BigDecimal> previousPoint = fValues.get(i - 1);
                BigDecimal f1Prev = previousPoint.get(0);
                BigDecimal f2Prev = previousPoint.get(1);
                List<List<BigDecimal>> previousJacobian = jValues.get(i - 1);
                BigDecimal j11Prev = previousJacobian.get(0).get(0);
                BigDecimal j21Prev = previousJacobian.get(1).get(0);
                BigDecimal j11Cur = BigDecimalUtil.ONE.subtract(alpha)
                    .multiply(j11Prev.add(j21Prev));
                BigDecimal j21Cur = beta.multiply(j11Cur.subtract(j11Prev))
                    .add(BigDecimalUtil.ONE.subtract(beta).multiply(j21Prev))
                    .add(fValues.get(i).get(0))
                    .subtract(f1Prev)
                    .subtract(f2Prev);
                jValues.add(List.of(List.of(j11Cur), List.of(j21Cur)));
                jacobianMatrix[i][0] = j11Cur.doubleValue();
            }
            return jacobianMatrix;
        };
    }

    @Override
    protected void reinitializeFValues(BigDecimal... parameters) {
        fValues.clear();
        assert observedValues.size() > 3;
        BigDecimal g0 = observedValues.get(0);
        BigDecimal g1 = observedValues.get(1);
        BigDecimal g2 = observedValues.get(2);
        BigDecimal f20 = g1.multiply(BigDecimalUtil.FOUR)
            .subtract(g2)
            .subtract(g0.multiply(BigDecimalUtil.THREE))
            .divide(BigDecimalUtil.TWO, RoundingMode.HALF_UP);
        fValues.add(List.of(g0, f20));
    }

    @Override
    protected void reinitializeJValues() {
        jValues.clear();
        BigDecimal f10 = fValues.get(0).get(0);
        BigDecimal f11 = fValues.get(1).get(0);
        BigDecimal f20 = fValues.get(0).get(1);
        BigDecimal j0Estimate = f11.subtract(f10).subtract(f20);
        jValues.add(List.of(List.of(BigDecimalUtil.ZERO), List.of(j0Estimate)));
    }
}

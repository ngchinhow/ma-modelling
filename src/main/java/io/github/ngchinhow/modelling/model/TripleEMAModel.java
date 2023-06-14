package io.github.ngchinhow.modelling.model;

import io.github.ngchinhow.modelling.util.BigDecimalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class TripleEMAModel extends AbstractEMAModel {
    private static final int PARAMETER_SIZE = 2;
    private final int cycleLength;
    private final List<BigDecimal> f3InitialList = new ArrayList<>();

    protected TripleEMAModel(int scale, BigDecimal alpha, List<BigDecimal> observedValues, int cycleLength) {
        super(scale, alpha, observedValues);
        this.cycleLength = cycleLength;
        this.initializeF3List();
    }

    public void initializeF3List() {
        List<BigDecimal> A = new ArrayList<>();
        BigDecimal sumMean = BigDecimalUtil.ZERO;
        for (int i = 0; i < dataSize; i++) {
            sumMean = sumMean.add(observedValues.get(i));
            if (i % cycleLength == cycleLength - 1) {
                A.add(sumMean.divide(BigDecimalUtil.valueOf(cycleLength), RoundingMode.HALF_UP));
                sumMean = BigDecimalUtil.ZERO;
            }
        }
        BigDecimal N = BigDecimalUtil.valueOf(A.size());
        for (int i = 0; i < cycleLength; i++) {
            BigDecimal sumCycles = BigDecimalUtil.ZERO;
            for (int j = i; j < dataSize; j = j + cycleLength) {
                sumCycles = sumCycles.add(observedValues.get(j).divide(A.get(j / cycleLength), RoundingMode.HALF_UP));
            }
            f3InitialList.add(sumCycles.divide(N, RoundingMode.HALF_UP));
        }
    }

    @Override
    public MultivariateVectorFunction function() {
        return params -> {
            iteratorCounter++;
            double[] functionValues = new double[dataSize];
            BigDecimal beta = BigDecimalUtil.valueOf(params[0]);
            BigDecimal gamma = BigDecimalUtil.valueOf(params[1]);
            this.reinitializeFValues(gamma);
            log.debug("beta and gamma estimates for evaluation {} is {} and {}", iteratorCounter, beta, gamma);
            functionValues[0] = fValues.get(0).get(0).doubleValue();
            for (int i = 1; i < dataSize; i++) {
                @SuppressWarnings("DuplicatedCode")
                List<BigDecimal> previousPoint = fValues.get(i - 1);
                BigDecimal f1Prev = previousPoint.get(0);
                BigDecimal f2Prev = previousPoint.get(1);
                BigDecimal f3Prev = i < cycleLength ? f3InitialList.get(i) : fValues.get(i - cycleLength).get(2);
                BigDecimal f1Cur = alpha.multiply(observedValues.get(i))
                    .divide(f3Prev, RoundingMode.HALF_UP)
                    .add(BigDecimalUtil.ONE.subtract(alpha).multiply(f1Prev.add(f2Prev)));
                BigDecimal f2Cur = beta.multiply(f1Cur.subtract(f1Prev))
                    .add(BigDecimalUtil.ONE.subtract(beta).multiply(f2Prev));
                BigDecimal f3Cur = gamma.multiply(observedValues.get(i))
                    .divide(f1Cur, RoundingMode.HALF_UP)
                    .add(BigDecimalUtil.ONE.subtract(gamma).multiply(f3Prev));
                fValues.add(List.of(f1Cur, f2Cur, f3Cur));
                functionValues[i] = f1Prev.setScale(scale, RoundingMode.HALF_UP).doubleValue();
            }
            return functionValues;
        };
    }

    @Override
    public MultivariateMatrixFunction jacobian() {
        return params -> {
            this.reinitializeJValues();
            BigDecimal beta = BigDecimalUtil.valueOf(params[0]);
            BigDecimal gamma = BigDecimalUtil.valueOf(params[1]);
            double[][] jacobianMatrix = new double[dataSize][PARAMETER_SIZE];
            jacobianMatrix[0][0] = jValues.get(0).get(0).get(0).doubleValue();
            for (int i = 1; i < dataSize; i++) {
                BigDecimal x = observedValues.get(i);
                @SuppressWarnings("DuplicatedCode")
                List<BigDecimal> previousPoint = fValues.get(i - 1);
                BigDecimal f1Prev = previousPoint.get(0);
                BigDecimal f2Prev = previousPoint.get(1);
                BigDecimal f3Prev = i < cycleLength ? f3InitialList.get(i) : fValues.get(i - cycleLength).get(2);
                BigDecimal f1Cur = fValues.get(i).get(0);
                List<List<BigDecimal>> previousJacobian = jValues.get(i - 1);
                BigDecimal j11Prev = previousJacobian.get(0).get(0);
                BigDecimal j12Prev = previousJacobian.get(0).get(1);
                BigDecimal j21Prev = previousJacobian.get(1).get(0);
                BigDecimal j22Prev = previousJacobian.get(1).get(1);
                // Jacobian of t < 0 is assumed to be 0
                BigDecimal j31Prev = i < cycleLength ? BigDecimalUtil.ZERO : jValues.get(i - cycleLength).get(2).get(0);
                BigDecimal j32Prev = i < cycleLength ? BigDecimalUtil.ZERO : jValues.get(i - cycleLength).get(2).get(1);
                BigDecimal j1Multiplicand = alpha.negate()
                    .multiply(x)
                    .divide(f3Prev.pow(2), RoundingMode.HALF_UP);
                BigDecimal j3Multiplicand = gamma.negate()
                    .multiply(x)
                    .divide(f1Cur.pow(2), RoundingMode.HALF_UP);
                BigDecimal j11Cur = j1Multiplicand
                    .multiply(j31Prev)
                    .add(BigDecimalUtil.ONE.subtract(alpha).multiply(j11Prev.add(j21Prev)));
                BigDecimal j12Cur = j1Multiplicand
                    .multiply(j32Prev)
                    .add(BigDecimalUtil.ONE.subtract(alpha).multiply(j12Prev.add(j22Prev)));
                BigDecimal j21Cur = beta.multiply(j11Cur.subtract(j11Prev))
                    .add(BigDecimalUtil.ONE.subtract(beta).multiply(j21Prev))
                    .add(f1Cur)
                    .subtract(f1Prev)
                    .subtract(f2Prev);
                BigDecimal j22Cur = beta.multiply(j12Cur.subtract(j12Prev))
                    .add(BigDecimalUtil.ONE.subtract(beta).multiply(j22Prev));
                BigDecimal j31Cur = j3Multiplicand
                    .multiply(j11Cur)
                    .add(BigDecimalUtil.ONE.subtract(gamma).multiply(j31Prev));
                BigDecimal j32Cur = j3Multiplicand
                    .multiply(j12Cur)
                    .add(BigDecimalUtil.ONE.subtract(gamma).multiply(j32Prev))
                    .add(x.divide(f1Cur, RoundingMode.HALF_UP))
                    .subtract(f3Prev);
                jValues.add(List.of(
                    List.of(j11Cur, j12Cur),
                    List.of(j21Cur, j22Cur),
                    List.of(j31Cur, j32Cur)
                ));
                jacobianMatrix[i][0] = j11Cur.doubleValue();
                jacobianMatrix[i][1] = j12Cur.doubleValue();
            }
            return jacobianMatrix;
        };
    }

    @Override
    protected void reinitializeFValues(BigDecimal... parameters) {
        fValues.clear();
        final BigDecimal gamma = Arrays.stream(parameters).findFirst().orElseThrow();
        final BigDecimal f10Est = observedValues.get(0);
        BigDecimal f20Est = BigDecimalUtil.ZERO;
        for (int i = 0; i < cycleLength; i++) {
            BigDecimal addend = observedValues.get(i + cycleLength)
                .subtract(observedValues.get(i))
                .divide(BigDecimalUtil.valueOf(cycleLength).pow(2), RoundingMode.HALF_UP);
            f20Est = f20Est.add(addend);
        }
        BigDecimal f30Est = gamma.add(BigDecimalUtil.ONE.subtract(gamma).multiply(f3InitialList.get(0)));
        fValues.add(List.of(f10Est, f20Est, f30Est));
    }

    @Override
    protected void reinitializeJValues() {
        jValues.clear();
        // Jacobian of t < 0 is assumed to be 0
        jValues.add(List.of(
            List.of(BigDecimalUtil.ZERO, BigDecimalUtil.ZERO),
            List.of(
                fValues.get(1).get(0).subtract(fValues.get(0).get(0)).subtract(fValues.get(0).get(1)),
                BigDecimalUtil.ZERO
            ),
            List.of(
                BigDecimalUtil.ZERO,
                observedValues.get(0).divide(fValues.get(0).get(0), RoundingMode.HALF_UP).add(f3InitialList.get(0))
            )
        ));
    }
}

package io.github.ngchinhow.modelling.model;

import io.github.ngchinhow.modelling.util.BigDecimalUtil;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public abstract class AbstractEMAProblemTest {
    protected final LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
    protected int scale;
    protected BigDecimal alpha;
    protected List<BigDecimal> observedValuesList;
    protected RealVector observedValuesVector;

    protected void prepareData(int period, int scale, String testFileName) {
        this.scale = scale;
        this.alpha = BigDecimalUtil.valueOf(20)
            .divide(BigDecimalUtil.valueOf(period).add(BigDecimalUtil.ONE), RoundingMode.HALF_UP);
        File file;
        try {
            file = ResourceUtils.getFile("classpath:" + testFileName);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            this.observedValuesList = br.lines().map(BigDecimal::new).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int size = observedValuesList.size();
        this.observedValuesVector = new ArrayRealVector(size);
        for (int i = 0; i < size; i++)
            observedValuesVector.setEntry(i, observedValuesList.get(i).doubleValue());
    }
}

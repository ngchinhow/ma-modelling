package io.github.ngchinhow.modelling.model;

import io.github.ngchinhow.modelling.util.BigDecimalUtil;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.junit.platform.commons.util.ClassLoaderUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public abstract class AbstractEMAProblemTest {
    protected final LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
    protected int scale;
    protected BigDecimal alpha;
    protected List<BigDecimal> observedValuesList;
    protected RealVector observedValuesVector;

    protected void prepareData(int period, int scale, String testFileName) throws URISyntaxException, IOException {
        this.scale = scale;
        this.alpha = BigDecimalUtil.valueOf(20)
            .divide(BigDecimalUtil.valueOf(period).add(BigDecimalUtil.ONE), RoundingMode.HALF_UP);
        URL url = ClassLoaderUtils.getDefaultClassLoader().getResource(testFileName);
        assert url != null;
        Path path = Paths.get(url.toURI());
        this.observedValuesList = Files.readAllLines(path, StandardCharsets.UTF_8)
            .stream()
            .map(BigDecimal::new)
            .toList();
        int size = observedValuesList.size();
        this.observedValuesVector = new ArrayRealVector(size);
        for (int i = 0; i < size; i++)
            observedValuesVector.setEntry(i, observedValuesList.get(i).doubleValue());
    }
}

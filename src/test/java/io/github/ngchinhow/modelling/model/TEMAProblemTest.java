package io.github.ngchinhow.modelling.model;

import io.github.ngchinhow.modelling.aggregator.TEMAProblemAggregator;
import io.github.ngchinhow.modelling.util.Constant;
import io.github.ngchinhow.modelling.validator.BoundedParameterValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.net.URISyntaxException;

@Slf4j
public class TEMAProblemTest extends AbstractEMAProblemTest {

    private int cycleLength;

    @BeforeEach
    public void beforeEach() throws URISyntaxException, IOException {
        final int period = 400;
        final int scale = 1;
        final int timeGap = 30;
        this.cycleLength = Constant.MINUTES_IN_DAY / timeGap;
        final String testFileName = "TEMA_test_data.txt";
        this.prepareData(period, scale, testFileName);
    }

    @ParameterizedTest
    @CsvSource({
        "0.001,0.001",
        "0.01,0.01",
        "0.1,0.1",
        "0.5,0.5"
    })
    @Disabled
    public void givenOptimizer_whenDoTEMAProblem_thenFindParameters(@AggregateWith(TEMAProblemAggregator.class) double[] initialGuesses) {
        TripleEMAModel model = new TripleEMAModel(scale, alpha, observedValuesList, cycleLength);
        LeastSquaresProblem problem = new LeastSquaresBuilder()
            .model(model.function(), model.jacobian())
            .start(initialGuesses)
            .maxEvaluations(1000)
            .maxIterations(1000)
            .target(observedValuesVector)
            .parameterValidator(new BoundedParameterValidator())
            .build();
        LeastSquaresOptimizer.Optimum optimum = optimizer.optimize(problem);
        log.info(
            "number of evaluations: {}, number of iterations: {}",
            optimum.getEvaluations(),
            optimum.getIterations()
        );
        int pointsDimension = optimum.getPoint().getDimension();
        for (int i = 0; i < pointsDimension; i++) {
            log.info("parameter {} value: {}", i + 1, optimum.getPoint().getEntry(i));
        }
        int residualsDimension = optimum.getResiduals().getDimension();
        double sum = 0d;
        for (int i = 0; i < residualsDimension; i++) {
            sum += Math.pow(optimum.getResiduals().getEntry(i), 2);
        }
        log.info("sum residual: {}", sum);
    }
}

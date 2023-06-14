package io.github.ngchinhow.modelling.model;

import io.github.ngchinhow.modelling.validator.BoundedParameterValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

@Slf4j
public class DEMAProblemTest extends AbstractEMAProblemTest {

    @BeforeEach
    public void beforeEach() throws URISyntaxException, IOException {
        final int period = 75;
        final int scale = 1;
        final String testFileName = "DEMA_test_data.txt";
        this.prepareData(period, scale, testFileName);
    }

    @Test
    @Disabled
    public void givenOptimizer_whenDoDEMAProblem_thenFindParameters() {
        DoubleEMAModel model = new DoubleEMAModel(scale, alpha, observedValuesList);
        LeastSquaresProblem problem = new LeastSquaresBuilder()
            .model(model.function(), model.jacobian())
            .start(new double[]{0.001d})
            .maxEvaluations(1000)
            .maxIterations(1000)
            .target(observedValuesVector)
            .parameterValidator(new BoundedParameterValidator())
            .build();
        LeastSquaresOptimizer.Optimum optimum = optimizer.optimize(problem);
        log.info(
            """
                    
                    number of evaluations: {},
                    number of iterations: {},
                    points: {},
                    residuals: {}
                """,
            optimum.getEvaluations(),
            optimum.getIterations(),
            optimum.getPoint(),
            optimum.getResiduals()
        );
    }
}

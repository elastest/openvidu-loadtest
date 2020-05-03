package io.openvidu.load.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElastestBaseTest {
    protected static final Logger logger = LoggerFactory
            .getLogger(ElastestBaseTest.class);

    @BeforeEach
    public void setupTest(final TestInfo info){
        final String testName = info.getTestMethod().get().getName();
        logger.info("##### Start test: {}", testName);
    }

    @AfterEach
    public void teardown(final TestInfo info) {
        final String testName = info.getTestMethod().get().getName();
        logger.info("##### Finish test: {}", testName);
    }

}
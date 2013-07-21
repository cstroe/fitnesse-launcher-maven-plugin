package uk.co.javahelp.maven.plugin.fitnesse.responders.run;

import java.io.IOException;

import util.TimeMeasurement;
import fitnesse.responders.run.CompositeExecutionLog;
import fitnesse.responders.run.ResultsListener;
import fitnesse.responders.run.TestPage;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;

public class DelegatingResultsListener implements ResultsListener {

    private final ResultsListener[] delegates;

    public DelegatingResultsListener(final ResultsListener... delegates) {
        this.delegates = delegates;
    }

    public final void allTestingComplete(final TimeMeasurement totalTimeMeasurement)
            throws IOException {
        for(ResultsListener delegate : this.delegates) {
            delegate.allTestingComplete(totalTimeMeasurement);
        }
    }

    public final void setExecutionLogAndTrackingId(final String stopResponderId, final CompositeExecutionLog log) {
        for(ResultsListener delegate : this.delegates) {
            delegate.setExecutionLogAndTrackingId(stopResponderId, log);
        }
    }

    public final void announceNumberTestsToRun(final int testsToRun) {
        for(ResultsListener delegate : this.delegates) {
            delegate.announceNumberTestsToRun(testsToRun);
        }
    }

    public final void testSystemStarted(final TestSystem testSystem, final String testSystemName, final String testRunner) {
        for(ResultsListener delegate : this.delegates) {
            delegate.testSystemStarted(testSystem, testSystemName, testRunner);
        }
    }

    public final void newTestStarted(final TestPage test, final TimeMeasurement timeMeasurement)
            throws IOException {
        for(ResultsListener delegate : this.delegates) {
            delegate.newTestStarted(test, timeMeasurement);
        }
    }

    public final void testOutputChunk(final String output) throws IOException {
        for(ResultsListener delegate : this.delegates) {
            delegate.testOutputChunk(output);
        }
    }

    public final void testComplete(final TestPage test, final TestSummary testSummary, final TimeMeasurement timeMeasurement)
            throws IOException {
        for(ResultsListener delegate : this.delegates) {
            delegate.testComplete(test, testSummary, timeMeasurement);
        }
    }

    public final void errorOccured() {
        for(ResultsListener delegate : this.delegates) {
            delegate.errorOccured();
        }
    }
}

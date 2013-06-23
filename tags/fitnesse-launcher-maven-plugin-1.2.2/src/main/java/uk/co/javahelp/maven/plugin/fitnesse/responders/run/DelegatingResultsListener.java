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

    public DelegatingResultsListener(ResultsListener... delegates) {
        this.delegates = delegates;
    }

    public void allTestingComplete(TimeMeasurement totalTimeMeasurement)
            throws IOException {
        for(ResultsListener delegate : this.delegates) {
            delegate.allTestingComplete(totalTimeMeasurement);
        }
    }

    public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {
        for(ResultsListener delegate : this.delegates) {
            delegate.setExecutionLogAndTrackingId(stopResponderId, log);
        }
    }

    public void announceNumberTestsToRun(int testsToRun) {
        for(ResultsListener delegate : this.delegates) {
            delegate.announceNumberTestsToRun(testsToRun);
        }
    }

    public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) {
        for(ResultsListener delegate : this.delegates) {
            delegate.testSystemStarted(testSystem, testSystemName, testRunner);
        }
    }

    public void newTestStarted(TestPage test, TimeMeasurement timeMeasurement)
            throws IOException {
        for(ResultsListener delegate : this.delegates) {
            delegate.newTestStarted(test, timeMeasurement);
        }
    }

    public void testOutputChunk(String output) throws IOException {
        for(ResultsListener delegate : this.delegates) {
            delegate.testOutputChunk(output);
        }
    }

    public void testComplete(TestPage test, TestSummary testSummary, TimeMeasurement timeMeasurement)
            throws IOException {
        for(ResultsListener delegate : this.delegates) {
            delegate.testComplete(test, testSummary, timeMeasurement);
        }
    }

    public void errorOccured() {
        for(ResultsListener delegate : this.delegates) {
            delegate.errorOccured();
        }
    }
}

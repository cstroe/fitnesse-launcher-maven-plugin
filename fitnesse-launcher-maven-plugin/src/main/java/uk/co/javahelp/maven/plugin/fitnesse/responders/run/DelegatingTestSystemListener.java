package uk.co.javahelp.maven.plugin.fitnesse.responders.run;

import java.io.IOException;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.ExecutionLog;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;

public class DelegatingTestSystemListener implements TestSystemListener<WikiTestPage> {

    private final TestSystemListener<WikiTestPage>[] delegates;

    public DelegatingTestSystemListener(final TestSystemListener<WikiTestPage>... delegates) {
        this.delegates = delegates;
    }

	@Override
    public final void testSystemStarted(final TestSystem testSystem) {
        for(TestSystemListener<WikiTestPage> delegate : this.delegates) {
            delegate.testSystemStarted(testSystem);
        }
    }

	@Override
	public void testSystemStopped(final TestSystem testSystem, final ExecutionLog executionLog, final Throwable cause) {
        for(TestSystemListener<WikiTestPage> delegate : this.delegates) {
            delegate.testSystemStopped(testSystem, executionLog, cause);
        }
	}

	@Override
    public final void testStarted(final WikiTestPage test)
            throws IOException {
        for(TestSystemListener<WikiTestPage> delegate : this.delegates) {
            delegate.testStarted(test);
        }
    }

	@Override
    public final void testOutputChunk(final String output) throws IOException {
        for(TestSystemListener<WikiTestPage> delegate : this.delegates) {
            delegate.testOutputChunk(output);
        }
    }

	@Override
	public void testAssertionVerified(final Assertion assertion, final TestResult testResult) {
        for(TestSystemListener<WikiTestPage> delegate : this.delegates) {
            delegate.testAssertionVerified(assertion, testResult);
        }
	}

	@Override
	public void testExceptionOccurred(final Assertion assertion, final ExceptionResult exceptionResult) {
        for(TestSystemListener<WikiTestPage> delegate : this.delegates) {
            delegate.testExceptionOccurred(assertion, exceptionResult);
        }
	}

	@Override
    public final void testComplete(final WikiTestPage test, final TestSummary testSummary)
            throws IOException {
        for(TestSystemListener<WikiTestPage> delegate : this.delegates) {
            delegate.testComplete(test, testSummary);
        }
    }
}

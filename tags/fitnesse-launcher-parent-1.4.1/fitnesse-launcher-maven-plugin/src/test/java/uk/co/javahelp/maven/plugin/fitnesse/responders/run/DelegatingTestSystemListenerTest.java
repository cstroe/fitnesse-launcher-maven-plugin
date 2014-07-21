package uk.co.javahelp.maven.plugin.fitnesse.responders.run;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableHeader;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;
import org.junit.Before;
import org.junit.Test;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.CommandRunnerExecutionLog;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.testsystems.slim.HtmlTable;
import fitnesse.testsystems.slim.SlimTestContextImpl;
import fitnesse.testsystems.slim.results.SlimExceptionResult;
import fitnesse.testsystems.slim.results.SlimTestResult;
import fitnesse.testsystems.slim.tables.QueryTable;
import fitnesse.testsystems.slim.tables.SyntaxError;
import fitnesse.wiki.WikiPage;

public class DelegatingTestSystemListenerTest {
	
	private static final int DELEGATE_COUNT = 3;

	private DelegatingTestSystemListener delegatingListener;
	
    private TestSystemListener<WikiTestPage>[] delegates;
    
    @SuppressWarnings("unchecked")
	@Before
    public void setUp() {
    	delegates = new TestSystemListener[DELEGATE_COUNT];
    	
    	for(int i = 0 ; i < DELEGATE_COUNT ; i++) {
    		delegates[i] = mock(TestSystemListener.class);
    	}
    	
    	delegatingListener = new DelegatingTestSystemListener(delegates);
    }
    
    @Test
    public void testTestSystemStarted() throws Exception {
    	TestSystem testSystem = mock(TestSystem.class);
    	
    	delegatingListener.testSystemStarted(testSystem);
    	
    	for(int i = 0 ; i < DELEGATE_COUNT ; i++) {
    		verify(delegates[i], times(1)).testSystemStarted(testSystem);
    	}
    }
    
    @Test
    public void testTestSystemStopped() throws Exception {
    	TestSystem testSystem = mock(TestSystem.class);
    	CommandRunnerExecutionLog execLog = new CommandRunnerExecutionLog(null);
    	Throwable throwable = new Throwable();
    	
    	delegatingListener.testSystemStopped(testSystem, execLog, throwable);
    	
    	for(int i = 0 ; i < DELEGATE_COUNT ; i++) {
    		verify(delegates[i], times(1)).testSystemStopped(testSystem, execLog, throwable);
    	}
    }
    
    @Test
    public void testStarted() throws Exception {
    	WikiTestPage test = new WikiTestPage((WikiPage)null);
    	
    	delegatingListener.testStarted(test);
    	
    	for(int i = 0 ; i < DELEGATE_COUNT ; i++) {
    		verify(delegates[i], times(1)).testStarted(test);
    	}
    }
    
    @Test
    public void testTestOutputChunk() throws Exception {
    	String output = "output";
    	
    	delegatingListener.testOutputChunk(output);
    	
    	for(int i = 0 ; i < DELEGATE_COUNT ; i++) {
    		verify(delegates[i], times(1)).testOutputChunk(output);
    	}
    }
    
    @Test
    public void testTestAssertionVerified() throws SyntaxError {
    	Assertion assertion = assertion();
    	TestResult testResult = new SlimTestResult(ExecutionResult.PASS);
    	
    	delegatingListener.testAssertionVerified(assertion, testResult);
    	
    	for(int i = 0 ; i < DELEGATE_COUNT ; i++) {
    		verify(delegates[i], times(1)).testAssertionVerified(assertion, testResult);
    	}
    }
    
    @Test
    public void testTestExceptionOccurred() throws Exception {
    	Assertion assertion = assertion();
    	ExceptionResult exceptionResult = new SlimExceptionResult("resultKey", "exceptionValue");
    	
    	delegatingListener.testExceptionOccurred(assertion, exceptionResult);
    	
    	for(int i = 0 ; i < DELEGATE_COUNT ; i++) {
    		verify(delegates[i], times(1)).testExceptionOccurred(assertion, exceptionResult);
    	}
    }
    
    @Test
    public void testTestComplete() throws Exception {
    	WikiTestPage test = new WikiTestPage((WikiPage)null);
        TestSummary testSummary = new TestSummary();
    	
    	delegatingListener.testComplete(test, testSummary);
    	
    	for(int i = 0 ; i < DELEGATE_COUNT ; i++) {
    		verify(delegates[i], times(1)).testComplete(test, testSummary);
    	}
    }
    
    private Assertion assertion() throws SyntaxError {
		NodeList headerColumns = new NodeList();
		headerColumns.add(new TableColumn());
		TableHeader tableHeader = new TableHeader();
		tableHeader.setChildren(headerColumns);
		NodeList rowColumns = new NodeList();
		rowColumns.add(new TableColumn());
		TableRow tableRow = new TableRow();
		tableRow.setChildren(rowColumns);
		NodeList rows = new NodeList();
		rows.add(tableHeader);
		rows.add(tableRow);
		TableTag tableTag = new TableTag();
		tableTag.setChildren(rows);
    	List<? extends Assertion> list = new QueryTable(new HtmlTable(tableTag), "id", new SlimTestContextImpl()).getAssertions();
    	return list.get(0);
    }
}

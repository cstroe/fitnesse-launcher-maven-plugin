package fitnesse.slim.powermock;

import junit.test.powermock.PowermockTest;

import org.powermock.modules.agent.support.PowerMockAgentTestInitializer;

/**
 * See Issue #24. http://code.google.com/p/fitnesse-launcher-maven-plugin/issues/detail?id=24
 */
public class PowermockFixture {
	
	private static String staticResult;

	private String ctorResult;

	private String methodResult;
	
	static {
		staticResult = init();
	}

	public PowermockFixture() {
        this.ctorResult = init();
	}
	
    public String staticInit() {
    	return staticResult;
    }
    
    public String ctorInit() {
    	return ctorResult;
    }
    
    public String methodInit() {
    	methodResult = init();
    	return methodResult;
    }
    
    private static String init() {
		try {
			PowerMockAgentTestInitializer.initialize(PowermockTest.class);
			return "OK";
		} catch (Throwable e) {
			return "FAIL";
		}
    }
}

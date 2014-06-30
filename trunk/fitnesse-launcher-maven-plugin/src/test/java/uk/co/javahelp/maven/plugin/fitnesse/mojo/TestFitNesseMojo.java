package uk.co.javahelp.maven.plugin.fitnesse.mojo;

public class TestFitNesseMojo extends AbstractFitNesseMojo {

    public Execution[] calledWith = null;

	public TestFitNesseMojo() {
		this.executions = new Execution[0];
	}

	@Override
	protected void executeInternal(Execution... executions) {
		this.calledWith = executions;
	}
}

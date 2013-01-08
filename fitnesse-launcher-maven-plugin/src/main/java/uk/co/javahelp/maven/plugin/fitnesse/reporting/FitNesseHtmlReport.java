package uk.co.javahelp.maven.plugin.fitnesse.reporting;

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;


/**
 * @goal html-report
 * @phase site
 */
public class FitNesseHtmlReport extends AbstractMavenReport {
	
    /**
     * Directory where reports will go.
     *
     * @parameter expression="${project.reporting.outputDirectory}"
     * @required
     * @readonly
     */
    private String outputDirectory;
 
    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
 
    /**
     * @component
     * @required
     * @readonly
     */
    private Renderer siteRenderer;

	@Override
	public String getOutputName() {
		return "html-report";
	}

	@Override
	public String getName(Locale locale) {
		return getBundle( locale ).getString( "report.name" );
	}

	@Override
	public String getDescription(Locale locale) {
		return getBundle( locale ).getString( "report.description" );
	}

	@Override
	protected Renderer getSiteRenderer() {
		return this.siteRenderer;
	}

	@Override
	protected String getOutputDirectory() {
		return this.outputDirectory;
	}

	@Override
	protected MavenProject getProject() {
		return this.project;
	}
	
	private ResourceBundle getBundle( Locale locale ) {
	    return ResourceBundle.getBundle( "html-report", locale, this.getClass().getClassLoader() );
	}

	@Override
	protected void executeReport(Locale locale) throws MavenReportException {
		// TODO Auto-generated method stub

	}

}

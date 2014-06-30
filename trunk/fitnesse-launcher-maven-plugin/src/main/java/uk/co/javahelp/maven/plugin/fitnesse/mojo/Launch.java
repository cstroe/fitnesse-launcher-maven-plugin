package uk.co.javahelp.maven.plugin.fitnesse.mojo;

import java.util.Arrays;

import uk.co.javahelp.maven.plugin.fitnesse.util.Utils;
import fitnesse.junit.TestHelper;

public class Launch {

    /**
     * @parameter property="fitnesse.suite"
     */
    private String suite;

    /**
     * @parameter property="fitnesse.test"
     */
    private String test;

    /**
     * @see <a href="http://fitnesse.org/FitNesse.UserGuide.TestSuites.TagsAndFilters">Suite Tags</a>
     * @parameter property="fitnesse.suiteFilter"
     */
    private String suiteFilter;

    /**
     * @see <a href="http://fitnesse.org/FitNesse.UserGuide.TestSuites.TagsAndFilters">Suite Tags</a>
     * @parameter property="fitnesse.excludeSuiteFilter"
     */
    private String excludeSuiteFilter;

	public Launch() {
	}
		
	public Launch(final String suite, final String test) {
		this(suite, test, null, null);
	}
	
	public Launch(final String suite, final String test, final String suiteFilter, final String excludeSuiteFilter) {
		this.suite = suite;
		this.test = test;
		this.suiteFilter = suiteFilter;
		this.excludeSuiteFilter = excludeSuiteFilter;
	}

	public String getSuite() {
		return this.suite;
	}

	public String getTest() {
		return this.test;
	}

	public String getSuiteFilter() {
		return this.suiteFilter;
	}

	public String getExcludeSuiteFilter() {
		return this.excludeSuiteFilter;
	}

    public String[] calcPageNameAndType() {
        final boolean haveSuite = !Utils.isBlank(this.suite);
        final boolean haveTest = !Utils.isBlank(this.test);
        if (!haveSuite && !haveTest) {
            throw new IllegalArgumentException("No suite or test page specified");
        } else if (haveSuite && haveTest) {
            throw new IllegalArgumentException("Suite and test page parameters are mutually exclusive");
        }

        final String pageName = (haveSuite) ? this.suite : this.test;
        final String pageType = (haveSuite) ? TestHelper.PAGE_TYPE_SUITE : TestHelper.PAGE_TYPE_TEST;

        return new String[] { pageName, pageType };
    }

	@Override
	public int hashCode() {
		return Arrays.hashCode(getArray());
	}

	@Override
	public boolean equals(final Object that) {
		if (this == that) return true;
		if (that == null) return false;
		if (this.getClass() != that.getClass()) return false;
		return Arrays.equals(this.getArray(), ((Launch) that).getArray());
	}
	
	private String[] getArray() {
		return toArray(this.suite, this.test, this.suiteFilter, this.excludeSuiteFilter);
	}
	
	private String[] toArray(final String... array) {
		return array;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
	    appendField(sb, "suite", this.suite);
	    appendField(sb, "test", this.test);
	    appendField(sb, "suiteFilter", this.suiteFilter);
	    appendField(sb, "excludeSuiteFilter", this.excludeSuiteFilter);
		return sb.toString();
	}
	
	private void appendField(final StringBuilder sb, final String name, final String field) {
		if(field != null) {
			sb.append(name);
			sb.append(":");
			sb.append(field);
			sb.append(" ");
		}
	}
}

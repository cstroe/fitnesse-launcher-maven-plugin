package uk.co.javahelp.maven.plugin.fitnesse.util;

public final class Utils {

	private Utils() { }

    public static boolean isBlank(final String string) {
        return string == null || string.trim().equals("");
    }
    
    public static boolean isWindows() {
        return System.getProperty("os.name", "unknown").toLowerCase().startsWith("windows");
    }
}

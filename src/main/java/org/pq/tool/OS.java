package org.pq.tool;

public class OS {

    public static String osFamily() {
        final String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac")) {
            return "macos";
        } else if (osName.contains("windows")) {
            return "windows";
        } else if (osName.contains("linux") || osName.contains("unix")) {
            return "linux";
        } else {
            throw new RuntimeException(String.format("unknown os family: %s", osName));
        }
    }
    
    public static void main(String... args) {
        final String family = osFamily();
        final String arch = System.getProperty("os.arch");
        System.out.printf("%s_%s%n", family, arch);
    }

}

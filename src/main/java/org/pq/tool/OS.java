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

    public static String osArch() {
        return System.getProperty("os.arch");
    }

    public static String prefix() {
        return String.format("%s_%s", osFamily(), osArch());
    }
    
    public static void main(String... args) {
        System.out.print(prefix());
    }

}

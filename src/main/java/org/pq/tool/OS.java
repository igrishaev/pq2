package org.pq.tool;

public class OS {

    public enum OSFamily {
        MAC("mac", "dylib"),
        LINUX("linux", "so"),
        WINDOWS("windows", "dll.a");
        public final String tag;
        public final String ext;
        OSFamily(final String tag, final String ext) {
            this.tag = tag;
            this.ext = ext;
        }
    }

    public static OSFamily osFamily() {
        final String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac")) {
            return OSFamily.MAC;
        } else if (osName.contains("windows")) {
            return OSFamily.WINDOWS;
        } else if (osName.contains("linux") || osName.contains("unix")) {
            return OSFamily.LINUX;
        } else {
            throw new RuntimeException(String.format("unsupported OS: %s", osName));
        }
    }

    public static String osArch() {
        return System.getProperty("os.arch");
    }

    public static String libName() {
        final OSFamily osFamily = osFamily();
        return String.format("%s_%s.%s", osFamily.tag, osArch(), osFamily.ext);
    }
    
    public static void main(String... args) {
        System.out.print(libName());
    }

}

package org.pq.tool;

public class OS {

    public enum OSFamily {
        MAC("macos", "dylib"),
        LINUX("linux", "so"),
        WINDOWS("windows", "dll");
        public final String tag;
        public final String ext;
        OSFamily(final String tag, final String ext) {
            this.tag = tag;
            this.ext = ext;
        }
    }

    public static OSFamily getOSFamily() {
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

    public static final OSFamily osFamily;
    public static final String libPrefix;
    static {
        osFamily = getOSFamily();
        libPrefix = String.format("%s_%s", osFamily.tag, osArch());
    }

    public static String libName(final String name) {
        return String.format("%s_%s.%s", libPrefix, name, osFamily.ext);
    }


    public static void main(String... args) {
        System.out.print(libPrefix);
    }

}

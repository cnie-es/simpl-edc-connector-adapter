package eu.europa.ec.simpl.edcconnectoradapter.util;

public final class PathUtil {

    private PathUtil() {}

    public static String checkSuffix(String suffix, String path) {
        if (path == null) {
            return null;
        }
        if (path.endsWith(suffix)) {
            return path;
        }
        return path + suffix;
    }
}

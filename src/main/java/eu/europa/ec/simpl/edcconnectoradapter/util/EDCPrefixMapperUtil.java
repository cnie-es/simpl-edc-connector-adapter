package eu.europa.ec.simpl.edcconnectoradapter.util;

public final class EDCPrefixMapperUtil {

    private EDCPrefixMapperUtil() {}

    public static String formatOdrlValue(String value) {
        return value.replace("odrl:", "");
    }

    public static String formatEDCValue(String value) {
        value = value.replace("@", "");
        return value.replace("edc:", "");
    }
}

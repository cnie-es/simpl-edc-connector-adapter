package eu.europa.ec.simpl.edcconnectoradapter.constant;

public final class RequestMappingV1 {

    public static final String VERSION = "/v1";

    public static final String BASE = VERSION;

    public static final String CONFIGURATION = BASE + "/configs";
    public static final String SELF_DESCRIPTIONS = BASE + "/selfDescriptions";
    public static final String TRANSFER_PROCESS = BASE + "/transfers";

    private RequestMappingV1() {}
}

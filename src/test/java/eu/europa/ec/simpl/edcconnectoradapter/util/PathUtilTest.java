package eu.europa.ec.simpl.edcconnectoradapter.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class PathUtilTest {

    private static final String PATH = "/path";
    private static final String SUFFIX = "/suffix";
    private static final String PATH_SUFFIX = PATH + SUFFIX;

    @Test
    void testCheckSuffixWithNullPath() {
        assertNull(PathUtil.checkSuffix(SUFFIX, null));
    }

    @Test
    void testCheckSuffixWithSamePathSuffix() {
        assertEquals(PATH_SUFFIX, PathUtil.checkSuffix(SUFFIX, PATH_SUFFIX));
    }

    @Test
    void testCheckSuffixWithoutPathSuffix() {
        assertEquals(PATH_SUFFIX, PathUtil.checkSuffix(SUFFIX, PATH));
    }
}

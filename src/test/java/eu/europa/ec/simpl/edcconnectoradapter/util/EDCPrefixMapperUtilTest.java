package eu.europa.ec.simpl.edcconnectoradapter.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EDCPrefixMapperUtilTest {

    @Test
    void testFormatOdrlValueRemovesOdrlPrefix() {
        assertEquals("permission", EDCPrefixMapperUtil.formatOdrlValue("odrl:permission"));
    }

    @Test
    void testFormatOdrlValueWithoutOdrlPrefixReturnsSameValue() {
        assertEquals("permission", EDCPrefixMapperUtil.formatOdrlValue("permission"));
    }

    @Test
    void testFormatEDCValueRemovesEdcPrefix() {
        assertEquals("contract", EDCPrefixMapperUtil.formatEDCValue("edc:contract"));
    }

    @Test
    void testFormatEDCValueRemovesAtSymbol() {
        assertEquals("contract", EDCPrefixMapperUtil.formatEDCValue("@contract"));
    }

    @Test
    void testFormatEDCValueRemovesEdcPrefixAndAtSymbol() {
        assertEquals("contract", EDCPrefixMapperUtil.formatEDCValue("@edc:contract"));
    }

    @Test
    void testFormatEDCValueWithoutEdcPrefixOrAtSymbolReturnsSameValue() {
        assertEquals("contract", EDCPrefixMapperUtil.formatEDCValue("contract"));
    }

    @Test
    void testFormatOdrlValueWithEmptyStringReturnsEmptyString() {
        assertEquals("", EDCPrefixMapperUtil.formatOdrlValue(""));
    }

    @Test
    void testFormatEDCValueWithEmptyStringReturnsEmptyString() {
        assertEquals("", EDCPrefixMapperUtil.formatEDCValue(""));
    }
}

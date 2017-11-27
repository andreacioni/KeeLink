package it.andreacioni.kp2a.plugin.keelink;

import org.junit.Test;

import java.security.PublicKey;

import static org.junit.Assert.*;


public class RSACryptoTest {
    @Test
    public void testPEMToString() throws Exception {
        String key = "-----BEGIN PUBLIC KEY-----\n" +
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCQhj0nQeXr/pep+sU11D+Y2Kz7\n" +
                "5zlR+Vv70XbCKwKf/cRzY11mEAvOCb8D49wYvwoz6xcXuhvE8QAjSvOwAvC839bh\n" +
                "SrQQTwCSLEYRsVAOYsxOBZxiyivmyzSrI9OlrphsJFIgDCrtDhB+x1YUwiGw0eTi\n" +
                "jWFUX5HFO0c7W0fWOQIDAQAB\n" +
                "-----END PUBLIC KEY-----";

        String expect = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCQhj0nQeXr/pep+sU11D+Y2Kz75zlR+Vv70XbCKwKf/cRzY11mEAvOCb8D49wYvwoz6xcXuhvE8QAjSvOwAvC839bhSrQQTwCSLEYRsVAOYsxOBZxiyivmyzSrI9OlrphsJFIgDCrtDhB+x1YUwiGw0eTijWFUX5HFO0c7W0fWOQIDAQAB";

        String pemString = KeeLinkUtils.PEMtoBase64String(key);

        assertEquals(expect, pemString);
    }
}
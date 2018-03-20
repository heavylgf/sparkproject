package com.ctdc.conf;

import junit.framework.TestCase;

/**
 * Created by CTWLPC on 2018/1/18.
 */
public class ConfigurationManagerTest extends TestCase {

    public void testGetProperty() throws Exception {
        String testvalue1 = ConfigurationManager.getProperty("testkey1");
        System.out.println(testvalue1);
    }

}
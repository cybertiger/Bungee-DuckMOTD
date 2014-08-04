/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.motdduck;

import java.io.StringReader;
import net.md_5.bungee.api.ServerPing;
import org.cyberiantiger.minecraft.motdduck.config.DuckProtocol;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

/**
 *
 * @author antony
 */
public class MainTest {
    
    public MainTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    private static final String PROTOCOL_YAML = "name: 1.7.10\nversion: 5";

    @Test
    public void testDuckProtocol() throws Exception {
        Yaml configLoader = new Yaml(new CustomClassLoaderConstructor(DuckProtocol.class, getClass().getClassLoader()));
        configLoader.setBeanAccess(BeanAccess.FIELD);
        DuckProtocol proto = configLoader.loadAs(new StringReader(PROTOCOL_YAML), DuckProtocol.class);
        ServerPing.Protocol bungeeProto = proto.asProtocol();
        assertEquals(5, bungeeProto.getProtocol());
        assertEquals("1.7.10", bungeeProto.getName());
    }
}
package com.claimvantage.force.jdbc;

import java.sql.SQLException;
import java.util.Properties;

import junit.framework.TestCase;

public class CredentialsTest extends TestCase {
    
    private static final String U = "jane.doe";
    private static final String P = "abcd4321";
    
    private static Properties PROPERTIES = new Properties();
    static {
        PROPERTIES.put("custom", "false");
        PROPERTIES.put("includes", "User,Task,Event");
    }

    public void testColonSeparatedUrl() throws SQLException {
        
        Credentials c = new Credentials("jdbc:claimvantage:force:" + U + ":" + P, PROPERTIES);
        assertCredentials(c);
    }
    
    public void testSemiColonSeparatedUrl() throws SQLException {
        
        Credentials c = new Credentials("jdbc:claimvantage:force;user=" + U + ";password=" + P, PROPERTIES);
        assertCredentials(c);
    }
    
    public void testSemiColonSeparatedUrlWithExtraProperties() throws SQLException {
        
        Credentials c = new Credentials("jdbc:claimvantage:force;user=" + U + ";password=" + P + ";extra=123", PROPERTIES);
        assertCredentials(c);
        assertEquals("123", c.getProperties().get("extra"));
    }
    
    public void testCredentialsInPropertiesOnly() throws SQLException {
        
        Properties p = new Properties();
        p.putAll(PROPERTIES);
        p.put("user", U);
        p.put("password", P);
        
        Credentials c = new Credentials("jdbc:claimvantage:force", p);
        assertCredentials(c);
    }
    
    public void testSemiColonSeparatedUrlHasPriorityOverProperties() throws SQLException {
        
        Properties p = new Properties();
        p.putAll(PROPERTIES);
        p.put("user", "otherUser");
        p.put("password", "otherPassword");
        
        Credentials c = new Credentials("jdbc:claimvantage:force;user=" + U + ";password=" + P, p);
        assertCredentials(c);
    }
    
    public void testBadUrls() throws SQLException {
        
        try {
            new Credentials("jdbc:claimvantage:force", null);
            fail("exception expected");
        } catch (SQLException e) {
            System.out.println(e);
        }
        
        try {
            new Credentials("jdbc:claimvantage:force;user=;;", null);
            fail("exception expected");
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
    
    private void assertCredentials(Credentials c) {
        
        assertEquals(U, c.getUsername());
        assertEquals(P, c.getPassword());
        
        // Ensure other properties are preserved
        Properties p = c.getProperties();
        for (Object key : PROPERTIES.keySet()) {
            assertEquals("expected=" + PROPERTIES + " actual=" + p, PROPERTIES.get(key), p.get(key));
        }
    }
}

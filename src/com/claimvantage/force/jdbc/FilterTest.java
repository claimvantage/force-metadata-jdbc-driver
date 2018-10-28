package com.claimvantage.force.jdbc;

import java.util.Properties;

import junit.framework.TestCase;

import com.sforce.soap.partner.DescribeGlobalSObjectResult;

public class FilterTest extends TestCase {

    public void testDefault() {
        
        Filter f = new Filter(null);
        
        assertEquals(false, f.accept(createStandard("Abc")));
        assertEquals(true, f.accept(createCustom("Abc")));
        assertEquals(false, f.accept(createCustom("USER")));
        assertEquals(true, f.accept(createCustom("event")));
    }
    
    public void testStandard() {
        
        Properties p = new Properties();
        p.put("excludes", "User,Event");
        p.put("standard", "true");
        p.put("custom", "false");
        Filter f = new Filter(p);
        
        assertEquals(true, f.accept(createStandard("Abc")));
        assertEquals(false, f.accept(createCustom("Abc")));
        assertEquals(false, f.accept(createStandard("User")));
        assertEquals(false, f.accept(createStandard("Event")));
    }
    
    public void testCustom() {
        
        Properties p = new Properties();
        p.put("excludes", " User, Event ");
        p.put("standard", " fALsE");
        p.put("custom", " tRuE ");
        Filter f = new Filter(p);

        assertEquals(false, f.accept(createStandard("Abc")));
        assertEquals(true, f.accept(createCustom("Abc")));
        assertEquals(false, f.accept(createStandard("uSeR")));
        assertEquals(false, f.accept(createStandard("EvEnt")));
    }
    
    public void testNoExcludes() {
        
        Properties p = new Properties();
        p.put("standard", "true");
        p.put("excludes", "");
        Filter f = new Filter(p);

        assertEquals(true, f.accept(createStandard("User")));
    }
    
    public void testIncludes() {
        
        Properties p = new Properties();
        p.put("standard", "false");
        // Excludes takes priority so default excludes must be removed
        p.put("excludes", "");
        p.put("includes", "User,Event");
        Filter f = new Filter(p);

        assertEquals(true, f.accept(createStandard("User")));
        assertEquals(true, f.accept(createStandard("Event")));
        assertEquals(false, f.accept(createStandard("Other")));
    }

    private DescribeGlobalSObjectResult createStandard(String name) {
        DescribeGlobalSObjectResult d = new DescribeGlobalSObjectResult();
        d.setName(name);
        d.setCustom(false);
        return d;
    }
    
    private DescribeGlobalSObjectResult createCustom(String name) {
        DescribeGlobalSObjectResult d = new DescribeGlobalSObjectResult();
        d.setName(name);
        d.setCustom(true);
        return d;
    }
}

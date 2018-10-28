package com.claimvantage.force.jdbc;

import junit.framework.TestCase;

public class ColumnMapTest extends TestCase {
    
    public void test() {
        
        ColumnMap<String, Object> cols = new ColumnMap<String, Object>();
    
        cols.put("col1", "value1");
        cols.put("col2", "value2");
        cols.put("col3", "value3");
        
        assertEquals("value3", cols.getByIndex(3));
        assertEquals("value2", cols.getByIndex(2));
        assertEquals("value1", cols.getByIndex(1));
        
        assertEquals("value3", cols.get("col3"));
        assertEquals("value2", cols.get("col2"));
        assertEquals("value1", cols.get("col1"));
        
        // Doesn't support e.g. remove
    }
}

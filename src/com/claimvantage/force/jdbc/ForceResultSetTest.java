package com.claimvantage.force.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class ForceResultSetTest extends TestCase {

    public void testNumberConversion() throws SQLException {
        
        ColumnMap<String, Object> map = new ColumnMap<String, Object>();
        
        map.put("NULL", null);
        map.put("INVALID", "xyz");
        
        map.put("SHORT-SHORT", (short) 5);
        map.put("INT-SHORT", (short) 6);
        map.put("LONG-SHORT", (short) 7);

        map.put("SHORT-INT", 5);
        map.put("INT-INT", 6);
        map.put("LONG-INT", 7);
        
        map.put("SHORT-LONG", (long) 5);
        map.put("INT-LONG", (long) 6);
        map.put("LONG-LONG", (long) 7);
        
        List<ColumnMap<String, Object>> maps = new ArrayList<ColumnMap<String, Object>>();
        maps.add(map);
        
        ForceResultSet rs = new ForceResultSet(maps);
        assertEquals(true, rs.first());
        
        // Part of JDBC interface, null mapped to zero
        assertEquals(0, rs.getShort("NULL"));
        assertEquals(0, rs.getInt("NULL"));
        assertEquals(0, rs.getLong("NULL"));
        
        assertEquals(5, rs.getShort("SHORT-SHORT"));
        assertEquals(6, rs.getInt("INT-SHORT"));
        assertEquals(7, rs.getLong("LONG-SHORT"));
        
        assertEquals(5, rs.getShort("SHORT-INT"));
        assertEquals(6, rs.getInt("INT-INT"));
        assertEquals(7, rs.getLong("LONG-INT"));
        
        assertEquals(5, rs.getShort("SHORT-LONG"));
        assertEquals(6, rs.getInt("INT-LONG"));
        assertEquals(7, rs.getLong("LONG-LONG"));
        
        try {
            rs.getShort("INVALID");
            fail("exception expected");
        } catch (SQLException e) {
            System.out.println(e);
        }
        try {
            rs.getInt("INVALID");
            fail("exception expected");
        } catch (SQLException e) {
            System.out.println(e);
        }
        try {
            rs.getLong("INVALID");
            fail("exception expected");
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}

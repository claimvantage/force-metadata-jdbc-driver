package com.claimvantage.force.jdbc;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.sforce.soap.partner.DescribeGlobalSObjectResult;

public class Filter {
    
    private boolean keepStandard;
    private boolean keepCustom;
    private Set<String> inclusionNames;
    private Set<String> exclusionNames;

    public Filter(Properties info) {
        
        // Default to custom only and no User object as all objects are associated with that
        // which makes the graphs a mess and kills performance
        keepStandard = false;
        keepCustom = true;
        inclusionNames = new HashSet<String>();
        exclusionNames = new HashSet<String>();
        exclusionNames.add("User".toLowerCase());
        
        if (info != null) {
            for (Object o : info.keySet()) {
                String key = ((String) o).trim();
                String value = info.getProperty(key).trim();
                if (key.equals("includes")) {
                    inclusionNames = createNameSet(value);
                } else if (key.equals("excludes")) {
                    // Replace the default to allow no exclusions
                    exclusionNames = createNameSet(value);
                } else if (key.equals("standard")) {
                    keepStandard = Boolean.parseBoolean(value);
                } else if (key.equals("custom")) {
                    keepCustom = Boolean.parseBoolean(value);
                }
            }
        }
    }
    
    private Set<String> createNameSet(String value) {
        Set<String> set = new HashSet<String>();
        if (value != null && value.trim().length() > 0) {
            String[] names = value.split(",");
            for (String name : names) {
                if (name.trim().length() != 0) {
                    set.add(name.trim().toLowerCase());
                }
            }
        }
        return set;
    }
    
    public boolean accept(DescribeGlobalSObjectResult sob) {
        
        String name = sob.getName().toLowerCase();
        if (exclusionNames.contains(name)) {
            // Exclusion takes precedence
            return false;
        }
        if (sob.isCustom() && !keepCustom) {
            // Inclusion overrides general flag
            return inclusionNames.contains(name);
        }
        if (!sob.isCustom() && !keepStandard) {
            // Inclusion overrides general flag
            return inclusionNames.contains(name);
        }
        return true;
    }

    @Override
    public String toString() {
        return "Filter [exclusionNames=" + exclusionNames
                + ", inclusionNames=" + inclusionNames
                + ", keepCustom=" + keepCustom
                + ", keepStandard=" + keepStandard
                + "]";
    }
}

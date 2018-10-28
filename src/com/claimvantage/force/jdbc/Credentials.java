package com.claimvantage.force.jdbc;

import java.sql.SQLException;
import java.util.Properties;

/**
 * Support various ways of a accepting the user and password.
 */
public class Credentials {
    
    private static final String U = "user";
    private static final String P = "password";
    
    private String user;
    private String password;
    private Properties properties = new Properties();

    public Credentials(String driverUrl, Properties info) throws SQLException {
        
        String[] parts = driverUrl.split(":");
        
        // Lowest priority: from info
        if (info != null) {
            user = info.getProperty(U);
            password = info.getProperty(P);
            properties.putAll(info);
        }
        
        // New format
        if (parts.length == 3) {
            String s = parts[2].substring("force".length());
            Properties urlProperties = parse(s);
            if (urlProperties.containsKey(U)) {
                user = urlProperties.getProperty(U);
            }
            if (urlProperties.containsKey(P)) {
                password = urlProperties.getProperty(P);
            }
            properties.putAll(urlProperties);
        // Older format
        } else if (parts.length == 5) {
            user = parts[3];
            password = parts[4];
        }
        
        if (user == null || password == null) {
            throw new SQLException("user or password missing; URL must be of form"
                    + " 1) \"jdbc:claimvantage:force:myUser:myPassword\" where \"user\" and \"password\" are defined in the url"
                    + " or"
                    + " 2) \"jdbc:claimvantage:force[(;[name=value])+]\" where \"user\" and \"password\" are defined in the url"
                    + " (e.g. \"jdbc:claimvantage:force;user=myUser;password=myPassword\") or in the connection properties"
                    );
        }
    }
    
    public String getUsername() {
        return user;
    }
    
    public String getPassword() {
        return password;
    }
    
    public Properties getProperties() {
        return properties;
    }
    
    private Properties parse(String s) {
        Properties p = new Properties();
        String[] pairs = s.split(";");
        for (String pair : pairs) {
            String[] parts = pair.split("=");
            if (parts.length >= 2) {
                p.put(parts[0], parts[1]);
            }
        }
        return p;
    }
}

package com.claimvantage.force.jdbc;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

/**
 * A JDBC driver that wraps the Force.com enterprise web service API to obtain
 * enough information for SchemaSpy or Open ModelSphere to be able to create their output.
 * So it is just a few methods of DatabaseMetaData that are implemented.
 */
public class ForceMetaDataDriver implements Driver {
    
	static {
	    try {
	        DriverManager.registerDriver(new ForceMetaDataDriver());
	    } catch (SQLException e) {
	        // lame, but better then nothing
	    	e.printStackTrace();
	    }
	}

    private static final String URL = "jdbc:claimvantage:force";
    
    public boolean acceptsURL(String url) throws SQLException {
        return url.startsWith(URL);
    }

    public Connection connect(String driverUrl, Properties info) throws SQLException {
        
        Credentials credentials = new Credentials(driverUrl, info);
        
        // Optional - set this property to not use the default Force.com login URL
        String forceUrl = info != null ? info.getProperty("url") : null;

        ResultSetFactory factory;
        try {
            WscService service = new WscService(credentials.getUsername(), credentials.getPassword(), forceUrl, new Filter(info));
            factory = service.createResultSetFactory();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            throw new SQLException(sw.toString());
        }
        return new ForceConnection(factory);
    }

    public int getMajorVersion() {
        return Versions.DRIVER_MAJOR;
    }

    public int getMinorVersion() {
        return Versions.DRIVER_MINOR;
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[] {};
    }

    public boolean jdbcCompliant() {
        return false;
    }
}

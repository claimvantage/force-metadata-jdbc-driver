package com.claimvantage.force.jdbc;

import java.util.List;

public class Table {

    private String name;
    private String comments;
    private List<Column> columns;

    public Table(String name, String comments, List<Column> columns) {
        this.name = name;
        this.comments = comments;
        this.columns = columns; 
        for (Column c : columns) {
            c.setTable(this);
        }
    }
    
    public String getName() {
        return name;
    }

    public String getComments() {
        return comments;
    }

    public List<Column> getColumns() {
        return columns;
    }
}

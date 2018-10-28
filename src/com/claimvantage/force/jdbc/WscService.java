package com.claimvantage.force.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sforce.soap.partner.ChildRelationship;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.PicklistEntry;
import com.sforce.soap.partner.RecordTypeInfo;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * Wraps the Force.com describe calls web service outputting simple data objects.
 * 
 * Uses WSC.
 */
public class WscService {

    private final Filter filter;
    private final PartnerConnection partnerConnection;

    public WscService(String un, String pw, String url, Filter filter) throws ConnectionException {

        this.filter = filter;

        ConnectorConfig partnerConfig = new ConnectorConfig();

        if (System.getProperty("http.auth.ntlm.domain") != null) {
            partnerConfig.setNtlmDomain(System.getProperty("http.auth.ntlm.domain"));
        }
        if ((System.getProperty("http.proxyHost") != null) && (System.getProperty("http.proxyPort") != null)) {
            partnerConfig.setProxy(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort")));
        }
        if (System.getProperty("http.proxyUser") != null) {
            partnerConfig.setProxyUsername(System.getProperty("http.proxyUser"));
        }
        if (System.getProperty("http.proxyPassword") != null) {
            partnerConfig.setProxyPassword(System.getProperty("http.proxyPassword"));
        }

        partnerConfig.setUsername(un);
        partnerConfig.setPassword(pw);
        if (url != null && url.length() > 0) {
            partnerConfig.setAuthEndpoint(url);
            log("Force.com connection url " + url);
        }
        partnerConfig.setConnectionTimeout(60 * 1000);

        log(filter.toString());

        partnerConnection = Connector.newConnection(partnerConfig);
    }

    private void log(String message) {
        System.out.println("ForceMetaDataDriver: " + message);
    }

    /**
     * Grab the describe data and return it wrapped in a factory.
     */
    public ResultSetFactory createResultSetFactory() throws ConnectionException {

        ResultSetFactory factory = new ResultSetFactory();
        Map<String, String> childParentReferenceNames = new HashMap<String, String>();
        Map<String, Boolean> childCascadeDeletes = new HashMap<String, Boolean>();
        List<String> typesList = getSObjectTypes();
        Set<String> typesSet = new HashSet<String>(typesList);
        List<String[]> batchedTypes = batch(typesList);

        // Need all child references so run through the batches first
        for (String[] batch : batchedTypes) {
            DescribeSObjectResult[] sobs = partnerConnection.describeSObjects(batch);
            if (sobs != null) {
                for (DescribeSObjectResult sob : sobs) {
                    ChildRelationship[] crs = sob.getChildRelationships();
                    if (crs != null) {
                        for (ChildRelationship cr : crs) {
                            if (typesSet.contains(cr.getChildSObject())) {
                                String qualified = cr.getChildSObject() + '.' + cr.getField();
                                childParentReferenceNames.put(qualified, cr.getRelationshipName());
                                childCascadeDeletes.put(qualified, cr.isCascadeDelete());
                            }
                        }
                    }
                }
            }
        }

        // Run through the batches again now the child references are available
        for (String[] batch : batchedTypes) {
            DescribeSObjectResult[] sobs = partnerConnection.describeSObjects(batch);
            if (sobs != null) {
                for (DescribeSObjectResult sob : sobs) {
                    Field[] fields = sob.getFields();
                    List<Column> columns = new ArrayList<Column>(fields.length);
                    for (Field field : fields) {
                        if (keep(field)) {
                            Column column = new Column(field.getName(), getType(field));
                            columns.add(column);

                            column.setLength(getLength(field));

                            List<String> comments = new ArrayList<String>();
                            if (field.getLabel() != null) {
                                comments.add("Label: " + field.getLabel());
                            }
                            if (field.getDefaultValueFormula() != null) {
                                comments.add("Default: " + field.getDefaultValueFormula());
                            }
                            if (field.getCalculatedFormula() != null) {
                                comments.add("Formula: " + field.getCalculatedFormula());
                            }
                            if (field.getInlineHelpText() != null) {
                                comments.add("Help: " + field.getInlineHelpText());
                            }
                            String picklist = getPicklistValues(field.getPicklistValues());
                            if (picklist != null) {
                                comments.add("Picklist: " + picklist);
                            }
                            if ("reference".equals(field.getType().toString())) {
                                // MasterDetail vs Reference apparently not
                                // in API; cascade delete is though
                                String qualified = sob.getName() + "." + field.getName();
                                String childParentReferenceName = childParentReferenceNames.get(qualified);
                                Boolean cascadeDelete = childCascadeDeletes.get(qualified);
                                if (childParentReferenceName != null && cascadeDelete != null) {
                                    comments.add("Referenced: " + childParentReferenceName + (cascadeDelete ? " (cascade delete)" : ""));
                                }
                            }
                            column.setComments(separate(comments, "\n"));

                            // Booleans have this as false so not too
                            // helpful; leave off
                            column.setNillable(false);

                            // NB Not implemented; see comment in
                            // ResultSetFactory class
                            column.setCalculated(field.isCalculated() || field.isAutoNumber());

                            String[] referenceTos = field.getReferenceTo();
                            if (referenceTos != null) {
                                for (String referenceTo : referenceTos) {
                                    if (typesSet.contains(referenceTo)) {
                                        column.setReferencedTable(referenceTo);
                                        column.setReferencedColumn("Id");
                                    }
                                }
                            }
                        }
                    }

                    List<String> comments = new ArrayList<String>();
                    comments.add("Labels: " + sob.getLabel() + " | " + sob.getLabelPlural());
                    String recordTypes = getRecordTypes(sob.getRecordTypeInfos());
                    if (recordTypes != null) {
                        comments.add("Record Types: " + recordTypes);
                    }

                    Table table = new Table(sob.getName(), separate(comments, "\n"), columns);
                    factory.addTable(table);
                }
            }
        }

        return factory;
    }

    private String getType(Field field) {
        String s = field.getType().toString();
        // WSC adds this prefix for some types
        if (s.startsWith("_")) {
            s = s.substring("_".length());
        }
        return s.equalsIgnoreCase("double") ? "decimal" : s;
    }

    private int getLength(Field field) {
        if (field.getLength() != 0) {
            return field.getLength();
        } else if (field.getPrecision() != 0) {
            return field.getPrecision();
        } else if (field.getDigits() != 0) {
            return field.getDigits();
        } else if (field.getByteLength() != 0) {
            return field.getByteLength();
        } else {
            // SchemaSpy expects a value
            return 0;
        }
    }

    private String getPicklistValues(PicklistEntry[] entries) {
        if (entries != null && entries.length > 0) {
            List<String> values = new ArrayList<String>(128);
            for (PicklistEntry entry : entries) {
                values.add(entry.getValue());
            }
            if (values.size() > 0) {
                return separate(values, " | ");
            }
        }
        return null;
    }

    private String getRecordTypes(RecordTypeInfo[] rts) {
        if (rts != null && rts.length > 0) {
            List<String> values = new ArrayList<String>(16);
            for (RecordTypeInfo rt : rts) {
                // Master always present
                if (!rt.getName().equalsIgnoreCase("Master")) {
                    values.add(rt.getName() + (rt.isDefaultRecordTypeMapping() ? " (default)" : ""));
                }
            }
            if (values.size() > 0) {
                return separate(values, " | ");
            }
        }
        return null;
    }

    // Avoid EXCEEDED_MAX_TYPES_LIMIT on call by breaking into batches
    private List<String[]> batch(List<String> types) {

        List<String[]> batchedTypes = new ArrayList<String[]>();

        final int batchSize = 100;
        for (int batch = 0; batch < (types.size() + batchSize - 1) / batchSize; batch++) {
            int from = batch * batchSize;
            int to = (batch + 1) * batchSize;
            if (to > types.size()) {
                to = types.size();
            }
            List<String> t = types.subList(from, to);
            String[] a = new String[t.size()];
            t.toArray(a);
            batchedTypes.add(a);
        }

        return batchedTypes;
    }

    private List<String> getSObjectTypes() throws ConnectionException {

        DescribeGlobalSObjectResult[] sobs = partnerConnection.describeGlobal().getSobjects();

        List<String> list = new ArrayList<String>();
        for (DescribeGlobalSObjectResult sob : sobs) {
            if (keep(sob)) {
                list.add(sob.getName());
            }
        }
        return list;
    }

    private boolean keep(DescribeGlobalSObjectResult sob) {
        // Filter tables.
        // Normally want the User table filtered as all objects are associated with that
        // so the graphs become a mess and very slow to generate.
        return filter.accept(sob);
    }

    private boolean keep(Field field) {
        // Keeping all fields
        return true;
    }

    private String separate(List<String> terms, String separator) {
        StringBuilder sb = new StringBuilder(2048);
        for (String term : terms) {
            if (sb.length() > 0) {
                sb.append(separator);
            }
            sb.append(term);
        }
        return sb.toString();
    }
}

package com.canoo.ant.table.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.canoo.ant.table.APropertyTable;

public class InMemoryPropertyTable extends APropertyTable {

    private static final Logger LOG = Logger.getLogger(InMemoryPropertyTable.class);
    private static Map sTables = new HashMap(); // maps tablename to List of Strings

    public InMemoryPropertyTable() {
    }

    public static void addTo(String tablename, String[] entries){
        if (!sTables.containsKey(tablename)) {
            sTables.put(tablename, new LinkedList());
        }
        List rows = (List) sTables.get(tablename);
        rows.add(entries);
    }
    public static void reset(){
        sTables = new HashMap();
    }

    protected List read(final String tablename) throws IOException {
        List result = new LinkedList();
        List rows = (List) sTables.get(tablename);
        if (null == rows){
            LOG.error("no such table " + tablename);
            return result;
        }
        Iterator eachrow = rows.iterator();
        if (!eachrow.hasNext()){
            LOG.error("no header line in table "+tablename);
            return result;
        }
        String[] header = (String[]) eachrow.next();
        while (eachrow.hasNext()) {
            String[] row = (String[]) eachrow.next();
            Properties entry = new Properties();
            for (int i = 0; i < header.length; i++) {
                entry.setProperty(header[i], row[i]);
            }
            result.add(entry);
        }
        return result;
    }
}

package com.canoo.ant.table;

import com.canoo.ant.filter.AllEqualsFilter;
import com.canoo.ant.filter.AllFilter;
import com.canoo.ant.filter.ITableFilter;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

public abstract class APropertyTable implements IPropertyTable {

    private static final Logger LOG = Logger.getLogger(APropertyTable.class);
    private static final int MAX_DEPTH = 10; // max recursion depth
    private static final ThreadLocal DEPTH = new ThreadLocal();

    private File fContainer;
    private String fTable;
    private String fPrefix;
    private ITableFilter fFilter;
    private List fRawTable;
    private List fMetaTable;
    protected static final String EMPTY = "";
    protected static final String KEY_JOIN = "JOIN";

    protected APropertyTable() {
        fFilter = new AllFilter();
        if( DEPTH.get() == null ) {
            setDepth(0);
        }
    }

    private static void setDepth(int depth){
        DEPTH.set(new Integer(depth));
    }
    private static int getDepth(){
        return((Integer)DEPTH.get()).intValue();
    }

    /**
     * @return columnName -> expander (Type IPropertyTable)
     */
    public Map getColumnInfo() {
        List meta = getMetaTable();
        Map result = new HashMap(meta.size()); // smaller is likely
        // find all properties for this table
        List tableSpecificColumnInfo = new AllEqualsFilter(TableFactory.KEY_TABLE).filter(meta, getTable());
        for (Iterator eachColumnInfo = tableSpecificColumnInfo.iterator(); eachColumnInfo.hasNext();) {
            Properties colInfo = (Properties) eachColumnInfo.next();

            try {
                // tableClass defaults to the current class
                IPropertyTable table = TableFactory.createTable(colInfo, getClass().getName());
                ITableFilter filter = TableFactory.createFilter(colInfo);
                final File container;
                if (colInfo.getProperty(TableFactory.KEY_CONTAINER, "").length() > 0) {
                	container = new File(getContainer().getParentFile(), colInfo.getProperty(TableFactory.KEY_CONTAINER));
                	colInfo.remove(TableFactory.KEY_CONTAINER); // to be sure that it doesn't get used with wrong path
                }
                else {
                	container = getContainer();
                }
                
                String key = colInfo.getProperty(TableFactory.KEY_NAME); // no default possible
                TableFactory.initOrDefault(table, filter, colInfo, container, key);
                result.put(key, table);
            } catch (Exception e) {
                LOG.error("cannot work with Property: " + colInfo.toString(), e);
                throw new RuntimeException("Cannot work with Property: " + colInfo.toString(), e);
            }
        }
        return result;
    }

    public List getPropertiesList(final String filterValue, final String prefix) {
        // start with copy of initial table
        // if current filter concerns extension keys, filter before extending
        // filtering in advance also lowers memory consumption in the average
        List result = getFilter().filter(getRawTable(), filterValue);
        if (getDepth() > MAX_DEPTH){
            LOG.error("processing grounded due to excessive recursion calls: "+getDepth());
            return result;
        }
        setDepth(getDepth()+1);

        final Map colInfo = getColumnInfo();
        // only go over entries in the colInfo.
        // (property names without colInfo info are not expanded)
        for (Iterator eachExpandable = colInfo.keySet().iterator(); eachExpandable.hasNext();) {
            String expansionName = (String) eachExpandable.next();
            expandName(result, expansionName, colInfo);
        }

        setDepth(getDepth()-1);

        // filter a second time to allow filters to work on expansions
        result = getFilter().filter(result, filterValue);
        // prefix is processed after filtering
        if (prefix!=null && prefix.length()>0){
            result = mapPrefix(result, prefix);
        }

        return result;
    }

    // like a ruby map!
    private List mapPrefix(List result, final String prefix) {
        List collect = new ArrayList(result.size());
        for (Iterator eachProps = result.iterator(); eachProps.hasNext();) {
            Properties props = (Properties) eachProps.next();
            Properties mapped = new Properties();
            for (Iterator eachKey = props.keySet().iterator(); eachKey.hasNext();) {
                String key = (String) eachKey.next();
                String value = props.getProperty(key);
                mapped.setProperty(prefix+"."+key, value);
            }
            collect.add(mapped);
        }
        return collect;
    }

    protected void expandName(List result, String expansionName, Map colInfo) {
        List expansions = new LinkedList(); // cannot add while iterating. store and add later
        for (Iterator eachProperties = result.iterator(); eachProperties.hasNext();) {
            Properties props = (Properties) eachProperties.next();
            List newExpansions = expandProps(props, expansionName, colInfo);
            // default behaviour: like OUTER join, we do not shrink if nothing found
            if (newExpansions.size() > 0) {
                eachProperties.remove();
                expansions.addAll(newExpansions);
            }
        }
        result.addAll(expansions);
    }

    protected List expandProps(Properties props, String expansionName, Map colInfo) {
        String value = props.getProperty(expansionName);
        List propExpansions = new LinkedList();
        IPropertyTable expansionTable = (IPropertyTable) colInfo.get(expansionName);
        // recursive call
        List expandWith = expansionTable.getPropertiesList(value, expansionTable.getPrefix());
        for (Iterator eachExpansion = expandWith.iterator(); eachExpansion.hasNext();) {
            Properties expandProps = (Properties) eachExpansion.next();
            // merge expansion with current line
            expandProps.putAll(props);
            // store for later adding
            propExpansions.add(expandProps);
        }
        return propExpansions;
    }

    //-------------- field accessors ------------------

    public File getContainer() {
        return fContainer;
    }

    public void setContainer(File container) {
        fContainer = container;
    }

    public String getTable() {
        return fTable;
    }

    public void setTable(String table) {
        fTable = table;
    }

    public ITableFilter getFilter() {
        return fFilter;
    }

    public void setFilter(ITableFilter filter) {
        fFilter = filter;
    }

    public String getPrefix() {
        return fPrefix;
    }

    public void setPrefix(String prefix) {
        fPrefix = prefix;
    }

    //-------------- how to read specifics ------------------

    /** lazy getter, cached */
    public List getRawTable() {
        fRawTable = getCachedTable(getTable(), fRawTable);
        return fRawTable;
    }

    /** lazy getter, cached */
    public List getMetaTable() {
    	if (hasJoinTable()) {
    		fMetaTable = getCachedTable(KEY_JOIN, fMetaTable);
    	}
    	else {
    		fMetaTable = Collections.EMPTY_LIST;
    	}
        return fMetaTable;
    }

    /**
     * Indicates if the table container has a JOIN table.
     * @return default is <code>true</code>
     */
    protected boolean hasJoinTable() {
		return true;
	}

	protected List getCachedTable(final String table, List tableCache) {
        if (tableCache != null) {
            return tableCache;
        }

        try {
            tableCache = read(table);
        }
        catch (final IOException e) {
            LOG.error("Cannot read " + getContainer() + " " + table, e);
            String message = "Cannot read container >" + getContainer() + "<";
            if (table != null)
            	message += " (table " + table + ")";
            message += ": " + e.getMessage();
            throw new RuntimeException(message, e);
        }

        if (tableCache.isEmpty()) {
            LOG.debug("no entry in " + getContainer() + "/" + table);
        }
        LOG.debug(tableCache.size()+" entries in "+getContainer()+ " " + table);
        return tableCache;
    }
    protected abstract List read(String table) throws IOException;
}


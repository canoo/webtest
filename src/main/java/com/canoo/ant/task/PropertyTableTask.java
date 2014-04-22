package com.canoo.ant.task;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.taskdefs.MacroInstance;

import com.canoo.ant.filter.ITableFilter;
import com.canoo.ant.table.APropertyTable;
import com.canoo.ant.table.ExcelPropertyTable;
import com.canoo.ant.table.IPropertyTable;
import com.canoo.ant.table.TableFactory;

/**
 * 
 * @author Dierk König
 * @author Marc Guillemot
 * @webtest.step
 *   category="Extension"
 *   name="dataDriven"
 *   description="This is an Ant task that calls its nested element as often as specified by the tableContainer.
 *   In each run a new set of properties from the the tableContainer is made available to the nested steps"
 */
public class PropertyTableTask extends Task implements TaskContainer {
    private static final Logger LOG = Logger.getLogger(PropertyTableTask.class);

    private List fTasks = new LinkedList();
    private Properties fProps = new Properties();
    private String fName;
    private String fValue;
    private boolean fFailASAP = false;
    private File fTableContainer;
    private boolean replaceProperties;

    /**
     * @webtest.parameter required="no"
     * default="no"
     * description="Specifies whether properties contained in the table's values
     * should be expanded or not"
     * @param replaceProperties
     */
    public void setReplaceProperties(boolean replaceProperties) {
        this.replaceProperties = replaceProperties;
    }

    public void setValue(final String value) {
        fValue = value;
    }

    public void setName(String name) {
        fName = name;
    }

    public void setTableclass(String tableClass) {
        fProps.setProperty(TableFactory.KEY_TABLE_CLASS, tableClass);
    }

    /**
     * @webtest.parameter required="no" default="FirstEquals"
     * description="The filter to apply on the foreign table after the lookup.
     * One of \"All\", \"Empty\", \"FirstEquals\", \"AllEquals\" or \"Group\"."
     */
    public void setFilterclass(String filterClass) {
        fProps.setProperty(TableFactory.KEY_FILTER_CLASS, filterClass);
    }

    /**
     * @webtest.parameter required="no"
     * default="the first table"
     * description="The name of the source table in the current container. 
     * For instance the name of the sheet when the table container is an Excel document."
     */
    public void setTable(final String table) {
        fProps.setProperty(TableFactory.KEY_FOREIGN_TABLE, table); // unlucky naming in this context
    }

    /**
    * @webtest.parameter required="yes"
    * description="The container for the table containing the data.
    * Typically this is the path to an Excel file but this can also be a directory structure."
    */
    public void setTableContainer(final File container) throws IOException {
    	fTableContainer = container;
    }
    public void setContainer(final File container) throws IOException {
    	setTableContainer(container);
    }

    /**
     * @webtest.parameter required="no"
     * default="false"
     * description="Indicates if the task should fail at the first failure of the nested content (when set to true)
     * or continue the execution for all records and then throw a BuildException when one or more error occurred (when set to false)."
     */
    public void setFailASAP(final boolean newValue) {
    	fFailASAP = newValue;
    }

    /**
     * Called by Ant to add nested tasks
     * @see org.apache.tools.ant.TaskContainer#addTask(org.apache.tools.ant.Task)
     * @webtest.nested.parameter required="yes"
     * description="Any Ant task."
     */
    public void addTask(final Task task) {
        fTasks.add(task);
    }

    public void execute() throws BuildException {
        final IPropertyTable table;
        final ITableFilter filter;
        try {
            table = TableFactory.createTable(fProps, ExcelPropertyTable.class.getName());
            filter = TableFactory.createFilter(fProps);
        } 
        catch (final Exception e) {
            throw new BuildException("cannot create container", e, getLocation());
        }

        TableFactory.initOrDefault(table, filter, fProps, fTableContainer, fName);

        final List propertiesList = table.getPropertiesList(fValue, null);
        LOG.debug("propertiesList.size() = " + propertiesList.size());
        if (propertiesList.isEmpty()) {
            LOG.warn("no match found in table " + table.getClass().getName() +
                    " with filter " + table.getFilter().getClass().getName() +
                    " and settings " + fProps.toString() +
                    " raw data:" + ((APropertyTable) table).getRawTable());
        }

        int nbFailures = 0;
        int nbRuns = 0;
        for (final Iterator eachPropSet = propertiesList.iterator(); eachPropSet.hasNext();) {
        	++nbRuns;
            final Properties propSet = (Properties) eachPropSet.next();
            for (final Iterator eachKey = propSet.keySet().iterator(); eachKey.hasNext();) {
                final String key = (String) eachKey.next();
                String value = propSet.getProperty(key);
                if (replaceProperties) {
                    value = getProject().replaceProperties(value);
                }
                LOG.debug("setting key/value " + key + "/" + value);
                getProject().setInheritedProperty(key, value);
            }
            
            for (final Iterator eachTask = fTasks.iterator(); eachTask.hasNext();) {
            	Task task = (Task) eachTask.next();
                // It's probably allways the case
                if (task instanceof UnknownElement)
                {
                	task = copy((UnknownElement) task);
                }
                try {
                	task.perform();
                }
                catch (final BuildException e)
                {
                	if (fFailASAP)
                		throw e;
                	else
                		nbFailures++;
                }
            }
        }
        if (nbFailures > 0) {
        	throw new BuildException("" + nbFailures + "/" + nbRuns + " executions of nested tasks failed");
        }
    }

    /**
     * Performs a fresh copy of the element. Inspired from {@link MacroInstance#copy}
     * Ideally Ant should offer a method for it
     * See https://issues.apache.org/bugzilla/show_bug.cgi?id=44795
     */
    private UnknownElement copy(UnknownElement ue) {
        UnknownElement ret = new UnknownElement(ue.getTag());
        ret.setNamespace(ue.getNamespace());
        ret.setProject(getProject());
        ret.setQName(ue.getQName());
        ret.setTaskType(ue.getTaskType());
        ret.setTaskName(ue.getTaskName());
        ret.setLocation(ue.getLocation());
        if (getOwningTarget() == null) {
            Target t = new Target();
            t.setProject(getProject());
            ret.setOwningTarget(t);
        } else {
            ret.setOwningTarget(getOwningTarget());
        }
        RuntimeConfigurable rc = new RuntimeConfigurable(
            ret, ue.getTaskName());
        rc.setPolyType(ue.getWrapper().getPolyType());
        final Map m = ue.getWrapper().getAttributeMap();
        for (Iterator i = m.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            rc.setAttribute(
                (String) entry.getKey(),
                (String) entry.getValue());
        }
        rc.addText(ue.getWrapper().getText().toString());

        final Enumeration e = ue.getWrapper().getChildren();
        while (e.hasMoreElements()) {
            RuntimeConfigurable r = (RuntimeConfigurable) e.nextElement();
            UnknownElement unknownElement = (UnknownElement) r.getProxy();
            String tag = unknownElement.getTaskType();
            if (tag != null) {
                tag = tag.toLowerCase(Locale.US);
            }

            UnknownElement child = copy(unknownElement);
            rc.addChild(child.getWrapper());
            ret.addChild(child);

        }
        return ret;
    }
}

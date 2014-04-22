package com.canoo.ant.table;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class DirectoryPropertyTable extends APropertyTable {

    private static final Logger LOG = Logger.getLogger(DirectoryPropertyTable.class);
    private static final String PROP_FILE_SUFFIX = ".properties";

    public DirectoryPropertyTable() {
    }

    protected List read(final String subdir) throws IOException {
        List result = new LinkedList();
        File containerDir = new File(getContainer() + File.separator + subdir);
        if (!containerDir.isDirectory()) {
            if (subdir.equals(KEY_JOIN)){
                LOG.debug("no meta info available in " + getContainer());
            } else {
                LOG.error(containerDir.getCanonicalPath() + " is not a directory");
            }
            return result;
        }
        File[] files = containerDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(PROP_FILE_SUFFIX);
            }
        });
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            Properties props = new Properties();
            props.load(new FileInputStream(file));
            props.setProperty(subdir+".file.name", simpleName(file));
            LOG.debug("loaded " + file.getCanonicalPath() + " with values " + props.toString());
            result.add(props);
        }
        return result;
    }

    private String simpleName(File file) {
        String name = file.getName();
        return name.substring(0, name.length()-PROP_FILE_SUFFIX.length());
    }
}

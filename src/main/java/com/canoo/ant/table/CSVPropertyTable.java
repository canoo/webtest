package com.canoo.ant.table;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class CSVPropertyTable extends APropertyTable {

    private static final Logger LOG = Logger.getLogger(DirectoryPropertyTable.class);
    private static final String CSV_SUFFIX = ".csv";

    public CSVPropertyTable() {
    }
    
    protected boolean hasJoinTable() {
        return false;
    }

    protected List read(final String filename) throws IOException {
        List result = new LinkedList();
        File containerDir;
        if ( filename == null ) {
            containerDir = getContainer();
        } else {
            containerDir = new File(getContainer() + File.separator + filename);
        }
        File[] files;
        if (containerDir.isDirectory()) {
            files = containerDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(CSV_SUFFIX);
                }
            });
        } else {
            files = new File[1];
            files[0] = containerDir;
        }
        
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            FileReader filereader = new FileReader(file);
            CSVReader csvFile = new CSVReader(filereader);
            
            // Read Header
            String[] headers = csvFile.readNext();
            
            String[] data; 
            while ((data = csvFile.readNext()) != null) {
                Properties props = new Properties();
                props.setProperty(".file.name", simpleName(file));
                LOG.debug("loaded " + file.getCanonicalPath() + " with values " + props.toString());
                if( data.length > 0 ) {
                    for ( int j = 0; j < headers.length; j++ ) {
                        props.setProperty(headers[j].trim(), data[j]);
                    }
                }
                result.add(props);
            }
            csvFile.close();
            filereader.close();
        }
        return result;
    }

    private String simpleName(File file) {
        String name = file.getName();
        return name.substring(0, name.length()-CSV_SUFFIX.length());
    }
}

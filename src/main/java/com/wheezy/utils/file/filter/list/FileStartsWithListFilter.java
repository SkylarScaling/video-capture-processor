package com.wheezy.utils.file.filter.list;

import java.io.File;
import java.io.FileFilter;

public class FileStartsWithListFilter implements FileFilter
{
    private final String filenameStartsWith;

    public FileStartsWithListFilter(String filenameStartsWith)
    {
        super();
        this.filenameStartsWith = filenameStartsWith;
    }

    @Override
    public boolean accept(File file)
    {
        return !file.isDirectory() && file.getName().startsWith(filenameStartsWith);
    }
}

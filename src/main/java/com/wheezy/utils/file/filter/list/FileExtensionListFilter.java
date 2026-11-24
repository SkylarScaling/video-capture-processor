package com.wheezy.utils.file.filter.list;

import java.io.File;
import java.io.FileFilter;

public final class FileExtensionListFilter implements FileFilter
{
    private final String extension;

    public FileExtensionListFilter(String ext)
    {
        if (!ext.startsWith("."))
        {
            extension = '.' + ext.toUpperCase();
        }
        else
        {
            extension = ext.toUpperCase();
        }
    }

    @Override
    public boolean accept(File file)
    {
        return file.getName().toUpperCase().endsWith(extension);
    }
}
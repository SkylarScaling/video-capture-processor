package com.wheezy.utils.file.filter.list;

import java.io.File;
import java.io.FileFilter;

public class FileOnlyListFilter implements FileFilter
{
    @Override
    public boolean accept(File file)
    {
        return !file.isDirectory();
    }
}

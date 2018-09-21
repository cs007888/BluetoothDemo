package com.example.transfer.adapter.util;

import java.io.File;

public final class deletefile {
    /**
     * 递归删除文件和文件夹
     * @param file    要删除的根目录
     */
    public static void DeleteFile(File file){
        if(file.isFile()){
            file.delete();
            return;
        }
        if(file.isDirectory()){
            File[] childFile = file.listFiles();
            if(childFile == null || childFile.length == 0){
                file.delete();
                return;
            }
            for(File f : childFile){
               DeleteFile(f);
            }
            file.delete();
        }
    }
}
package org.AutomateFeatureSwitchCleanups.FileHandling;

import java.io.File;

public class DeleteTheFile {
    public void DeleteFile(File file){
        if (file.delete()){
            System.out.println();
        }
        else{
            System.out.println();
        }
    }
}

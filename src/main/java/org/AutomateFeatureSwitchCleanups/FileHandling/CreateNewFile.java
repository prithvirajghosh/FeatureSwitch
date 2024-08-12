package org.AutomateFeatureSwitchCleanups.FileHandling;

import java.io.File;
import java.io.IOException;

public class CreateNewFile {
    public File CreateFile(String fileName) {
        File myFile;
        try {
            myFile = new File(fileName);
            if (myFile.createNewFile()) {
                System.out.println();
            } else {
                System.out.println();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return myFile;
    }
}

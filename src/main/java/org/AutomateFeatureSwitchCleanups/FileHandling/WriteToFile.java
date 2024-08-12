package org.AutomateFeatureSwitchCleanups.FileHandling;

import java.io.*;

public class WriteToFile {
    public void WriteFile(File file, String DataToWrite){
        try{
            FileWriter Writer = new FileWriter(file, false);
            BufferedWriter bufferedWriter = new BufferedWriter(Writer);
            bufferedWriter.write(DataToWrite);
            bufferedWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

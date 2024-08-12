package org.AutomateFeatureSwitchCleanups.FileHandling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ReadFromFile {
    public String ReadFile(File file)throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(file));
        return reader.readLine();
    }
}

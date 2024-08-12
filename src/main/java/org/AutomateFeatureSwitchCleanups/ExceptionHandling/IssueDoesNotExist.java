package org.AutomateFeatureSwitchCleanups.ExceptionHandling;

public class IssueDoesNotExist extends Exception{
    public IssueDoesNotExist(String errorMessage){
        super(errorMessage);
    }
}

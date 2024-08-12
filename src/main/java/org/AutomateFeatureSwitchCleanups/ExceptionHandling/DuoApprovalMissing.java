package org.AutomateFeatureSwitchCleanups.ExceptionHandling;

public class DuoApprovalMissing extends Exception {
    public DuoApprovalMissing(String errorMessage){
        super(errorMessage);
    }
}
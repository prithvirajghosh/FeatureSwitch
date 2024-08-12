package org.AutomateFeatureSwitchCleanups;

import com.jcraft.jsch.JSchException;
import org.AutomateFeatureSwitchCleanups.ConnectDB.CheckIfFeatureSwitchPresent;
import org.AutomateFeatureSwitchCleanups.ConnectJIRA.ConnectToIssuePage;
import org.AutomateFeatureSwitchCleanups.ConnectRepo.ConnectBitbucket;
import org.AutomateFeatureSwitchCleanups.ConnectServer.InsertAndRemoveFeatureSwitch;
import org.AutomateFeatureSwitchCleanups.ExceptionHandling.IssueDoesNotExist;
import org.AutomateFeatureSwitchCleanups.ExtractionService.ExtractFeatureSwitch;
import org.AutomateFeatureSwitchCleanups.ExtractionService.ExtractJiraComponents;
import org.AutomateFeatureSwitchCleanups.FileHandling.CreateNewFile;
import org.AutomateFeatureSwitchCleanups.FileHandling.DeleteTheFile;
import org.AutomateFeatureSwitchCleanups.FileHandling.ReadFromFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class main {
    public static void main(String[] args) throws IssueDoesNotExist, IOException {
        Scanner sc = new Scanner(System.in);
        //STEP 1
        String JiraIssueNumber = InputJiraNumber(sc);
        CreateNewFile createNewFileobj = new CreateNewFile();
        File fileToStorePrePost = createNewFileobj.CreateFile("PreOrPost");

        //STEP 2 - Extracts the components e.g. oper1a, sortshipdb
        try {
            List<String> nameOfComponents = getStrings(JiraIssueNumber); //private function
            System.out.println("Component/s Present : " + nameOfComponents);
            //counter variable to keep track if all issues have been removed from table
            int qcpassed = -1;

            //STEP 3 - loop to iterate over the components separately
            for (String comp : nameOfComponents) {
                System.out.println("\nStarting for " + comp + ":"); //oper1a or sort_ship_db

                //STEP 4 - Extracts the issue names
                ArrayList<String> extractedIssues = getStrings(comp, JiraIssueNumber, fileToStorePrePost); //private function
                System.out.println("Extracted Issues : " + extractedIssues);

                //STEP 5 - Read from the file if it is predeploy or postdeploy (code changes required)
                ReadFromFile readFromFileobj = new ReadFromFile();
                String PreOrPost = readFromFileobj.ReadFile(fileToStorePrePost);

                //STEP 6 - Connect to Database to check the feature table
                CheckIfFeatureSwitchPresent connectDBobj = new CheckIfFeatureSwitchPresent();
                qcpassed += connectDBobj.ConnectToDatabase(extractedIssues, comp); //Check if the issues are removed or not

                //STEP 7 - Validating the script in case of oper1a component
                if (comp.equals("oper1a") && qcpassed == -1) {
                    System.out.println("\nDo you want to test the script by inserting a feature switch?");
                    String choice = sc.next();
                    if (choice.matches("^(?i)(yes|y)$")) {
                        try {
                            InsertAndRunScript(extractedIssues, JiraIssueNumber, PreOrPost);
                            System.out.println("\nValidating again for " + comp);
                            qcpassed += connectDBobj.ConnectToDatabase(extractedIssues, comp);
                        } catch (JSchException e) {
                            System.err.println("JSchException : Could not connect to host");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (!nameOfComponents.isEmpty() && qcpassed == -1) {
                System.out.println("All feature switches removed");
            } else {
                System.err.println("Feature switch still present");
            }
        }
        catch (IOException | IssueDoesNotExist | NullPointerException e) {
            System.err.println("Could not connect to repository");
        }
        DeleteTheFile deleteTheFileobj = new DeleteTheFile();
        deleteTheFileobj.DeleteFile(fileToStorePrePost);
    }


    private static List<String> getStrings(String JiraIssueNumber) throws IssueDoesNotExist {               //JIRA component
        try {
            ConnectToIssuePage connectToIssuePageobj = new ConnectToIssuePage();
            String componentsOfIssue = connectToIssuePageobj.ConnectIssuePage(JiraIssueNumber); //returns the JIRA api result in string format
            //Parse the string into Json, extract the components section of the JIRA issue e.g., oper1a or sort_ship_db
            ExtractJiraComponents extractJiraComponentsObj = new ExtractJiraComponents();
            return extractJiraComponentsObj.extractComponentNames(componentsOfIssue); //returns the components stored in the list
        } catch (IssueDoesNotExist ie) {
            throw ie;
        }

    } //extract components of issues from JIRA api


    private static ArrayList<String> getStrings(String comp, String JiraIssueNumber, File file) throws IOException, IssueDoesNotExist, NullPointerException {      //BITBUCKET

        ConnectBitbucket connectBitbucketObj = new ConnectBitbucket(); //object of connectBitbucket created
        try{
            String responseFromBitbucket = connectBitbucketObj.connectRepo(JiraIssueNumber, comp, file);
            ExtractFeatureSwitch extractFeatureSwitchObj = new ExtractFeatureSwitch();
            return extractFeatureSwitchObj.extractKeywordsWithXWMLWM(responseFromBitbucket); //returns the issues stored in the list
        } catch (IOException | IssueDoesNotExist | NullPointerException e) {
            throw e;
        }
    } //extract list of issues from Bitbucket api


    private static void InsertAndRunScript(ArrayList<String> extractedIssues, String JiraIssueNumber, String PreOrPost) throws JSchException {
        try {
            InsertAndRemoveFeatureSwitch insertAndRemoveFeatureSwitchobj = new InsertAndRemoveFeatureSwitch();
            insertAndRemoveFeatureSwitchobj.ConnectToOper2a(extractedIssues, JiraIssueNumber, PreOrPost);
        } catch (JSchException e) {
            throw e; //thrown back so that the main function catches it
        } catch (Exception ex) {
            System.out.println("Error in Inserting or Running Script");
        }
    }//inserts any one issue from the issue list randomly and runs the script

    private static String InputJiraNumber(Scanner sc) {
        System.out.println("Enter Jira Issue number (e.g. XWM-1234) : ");
        //Take Input of JIRA issue number e.g. XWM-1234
        String JiraIssueNumber = sc.nextLine();
        JiraIssueNumber = JiraIssueNumber.toUpperCase();
        return JiraIssueNumber;
    }
}

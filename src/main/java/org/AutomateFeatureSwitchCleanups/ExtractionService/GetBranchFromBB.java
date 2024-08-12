package org.AutomateFeatureSwitchCleanups.ExtractionService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.AutomateFeatureSwitchCleanups.ExceptionHandling.IssueDoesNotExist;
import org.AutomateFeatureSwitchCleanups.FileHandling.WriteToFile;
import org.json.JSONArray;
import org.json.JSONObject;

public class GetBranchFromBB {
    private static final String PERSONAL_ACCESS_TOKEN = "NzUyMzU4NTc0NTM3Olu90eNXb3s28pj3ia3BU0g2iFrZ";

    public String ExtractBranchNameBB(String JiraIssueNumber, File file) throws IOException, IssueDoesNotExist {

        String baseURL = "https://bitbucket.fastenal.com/rest/api/1.0/projects/WMS/repos/oper1a";
        String PreOrPostDeploy = "predeploy/";
        String branchesURL = baseURL + "/branches";

        try { //adding all the fetched branches to allBranches list
            List<String> allBranches = new ArrayList<>();
            boolean isLastPage = false;
            int start = 0;
            while (!isLastPage) {
                JSONObject jsonResponse = getJsonObject(branchesURL, start);
                JSONArray branches = jsonResponse.getJSONArray("values");
                for (int i = 0; i < branches.length(); i++) {
                    String branchName = branches.getJSONObject(i).getString("displayId");
                    allBranches.add(branchName);
                }
                isLastPage = jsonResponse.getBoolean("isLastPage");
                if (!isLastPage) {
                    start = jsonResponse.getInt("nextPageStart");
                }
            }
            // Filter branches by known prefix
            String branchName = GetBranchName(allBranches, JiraIssueNumber);
            return GetBranchURL(branchName, baseURL, JiraIssueNumber, PreOrPostDeploy, file);
        } catch (IssueDoesNotExist e) {
            throw e;
        } catch (IOException e) {
            System.out.println("An error has occurred.");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private String GetBranchName(List<String> allBranches, String JiraIssueNumber) {
        String branchNameFinal = null;
        for (String branchName : allBranches) {
            if (branchName.startsWith(JiraIssueNumber)) {
                branchNameFinal = branchName;
                return branchNameFinal;
            }
        }
        return null;
    } //returns branch name if found


    private String GetBranchURL(String branchName, String baseURL, String JiraIssueNumber, String PreOrPostDeploy, File file) throws IOException, IssueDoesNotExist {
        String fileURL = baseURL + "/browse/deployment_hook/" + PreOrPostDeploy + JiraIssueNumber + ".sh?at=refs/heads/" + branchName;
        WriteToFile writeToFileobj = new WriteToFile();

        try {
            URL url = new URL(fileURL);
            HttpURLConnection connectionCheck = (HttpURLConnection) url.openConnection();
            connectionCheck.setRequestMethod("GET");
            connectionCheck.setRequestProperty("Authorization", "Bearer " + PERSONAL_ACCESS_TOKEN);
            if (connectionCheck.getResponseCode() == 200) {
                writeToFileobj.WriteFile(file, PreOrPostDeploy);
                System.out.println(fileURL);
                return fileURL;
            } else if (PreOrPostDeploy.equals("predeploy/")) {
                PreOrPostDeploy = "postdeploy/";
                return GetBranchURL(branchName, baseURL, JiraIssueNumber, PreOrPostDeploy, file);
            } else {
                throw new IssueDoesNotExist("Could not find the Bitbucket page with this issue number. This branch might have been merged with master branch.");
            }
        } catch (IssueDoesNotExist e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    } //extracts branchURL


    private static JSONObject getJsonObject(String branchesURL, int start) throws IOException {
        String paginatedURL = branchesURL + "?start=" + start;
        HttpURLConnection conn = (HttpURLConnection) new URL(paginatedURL).openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + PERSONAL_ACCESS_TOKEN);
        int status = conn.getResponseCode();
        if (status != 200) {
            throw new RuntimeException("HTTP GET Request Failed with Error code : " + status);
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        conn.disconnect();
        JSONObject jsonResponse = new JSONObject(content.toString());
        return jsonResponse;
    }
}
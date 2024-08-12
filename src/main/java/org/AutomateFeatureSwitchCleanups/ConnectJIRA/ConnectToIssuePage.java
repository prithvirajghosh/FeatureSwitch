package org.AutomateFeatureSwitchCleanups.ConnectJIRA;


import com.google.gson.JsonObject;
import org.AutomateFeatureSwitchCleanups.ExceptionHandling.IssueDoesNotExist;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ConnectToIssuePage {
    //This will connect to JIRA Api and return jsonobject
    private static final String API_TOKEN = ""; //enter your jira personal access token here

    public String ConnectIssuePage(String jira_url_issue) throws IssueDoesNotExist {
        String JIRA_URL;
        JIRA_URL = "https://jira.fastenal.com/rest/api/2/issue/" + jira_url_issue + "?fields=components";
        JsonObject jsonObject = null;
        String responseBody = null;
        try {
            // Create the HttpClient
            HttpClient client = HttpClient.newHttpClient();
            //System.out.println("Requesting URI: " + JIRA_URL);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(JIRA_URL)).header("Authorization", "Bearer " + API_TOKEN).build();
            // Send the request and get the response
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    // Log the entire response body
                    responseBody = response.body();
                } else {
                    throw new IssueDoesNotExist("Wrong Issue number");
                }
            } catch (IssueDoesNotExist ie) {
                throw ie;
            }
        } catch (ConnectException ce) {
            System.err.println("Could not connect to JIRA");
        } catch (IssueDoesNotExist ie) {
            throw ie;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseBody;
    }

}

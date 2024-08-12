package org.AutomateFeatureSwitchCleanups.ConnectRepo;

import org.AutomateFeatureSwitchCleanups.ExceptionHandling.IssueDoesNotExist;
import org.AutomateFeatureSwitchCleanups.ExtractionService.GetBranchFromBB;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class ConnectBitbucket {
    private static final String PERSONAL_ACCESS_TOKEN = ""; //enter your bitbucket personal access token here

    public String connectRepo(String IssueNumber, String IssueComponentType, File file) throws IOException, IssueDoesNotExist, NullPointerException {
        String BITBUCKET_URL = null;

        if (IssueComponentType.equals("oper1a")) {
            GetBranchFromBB getBranchFromBBobj = new GetBranchFromBB();
            try {
                BITBUCKET_URL = getBranchFromBBobj.ExtractBranchNameBB(IssueNumber, file);
                //BITBUCKET_URL = "https://bitbucket.fastenal.com/rest/api/1.0/projects/WMS/repos/oper1a/browse/deployment_hook/predeploy/" + IssueNumber + ".sh";
              }
//            catch (IssueDoesNotExist | IOException e) {
//                throw e;
//            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (IssueComponentType.equals("sort_ship_db")) {
            BITBUCKET_URL = "https://bitbucket.fastenal.com/rest/api/1.0/projects/WMS/repos/sort_ship_db/browse/db/schema/WMS_PICKY/scripts/liquibase/scripts/" + IssueNumber + ".sql";
        } else {
            System.err.println(IssueComponentType + ": Need to test it manually");
        }
        String responseBody;
        try {
            HttpResponse response = HttpConnectionToBB(BITBUCKET_URL);
            if (response.statusCode() == 200) {
                responseBody = (String) response.body();
                return responseBody;

            } else {
                return null;
            }
        } catch (NullPointerException npe) {
            throw npe;
        } catch (ConnectException ce) {
            System.err.println("Could not connect to Bitbucket");
        }
        return null;
    }

    private HttpResponse HttpConnectionToBB(String BitbucketURL) throws ConnectException, NullPointerException {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = null;
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BitbucketURL)).header("Authorization", "Bearer " + PERSONAL_ACCESS_TOKEN).build();
            // Send the request and get the response
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (NullPointerException npe) {
            throw npe;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}

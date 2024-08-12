package org.AutomateFeatureSwitchCleanups.ConnectServer;
import com.jcraft.jsch.*;
import org.AutomateFeatureSwitchCleanups.ExceptionHandling.DuoApprovalMissing;
import org.AutomateFeatureSwitchCleanups.ExceptionHandling.Oper2aConnectionError;

import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
public class InsertAndRemoveFeatureSwitch {
    public void ConnectToOper2a(ArrayList<String> ExtractedOper1aIssues, String issue_number, String PreOrPost) throws JSchException {
        String host = "oper2a";
        String user = "pghosh";
        int index = RandomNumberGenerator(ExtractedOper1aIssues.size());
        String IssueToInsert = ExtractedOper1aIssues.get(index);
        //PreOrPost = "predeploy/"; //hardcoding postdeploy for test purpose
        String scriptPath = "/usr0/deploy/oper1a/deployment_hook/" + PreOrPost + issue_number + ".sh";
        try {
            Session session = getSession(user, host);
            System.out.println("Connecting to the server...");
            session.connect();
            System.out.println("Please approve DUO Mobile Authentication");
            String combinedCommand = "/usr0/bin/feature_switch_add --id=" + IssueToInsert + " && sh " + scriptPath;
            executeCommand(session, combinedCommand);
            System.out.println("\nFeature Switch was added for " + IssueToInsert + " and executed the script which removed " + IssueToInsert + " thus validating the script\n");
            session.disconnect();
        } catch (DuoApprovalMissing dam) {
            System.out.println("Error: " + dam.getMessage());
        } catch (JSchException jse) {
            throw jse; // thrown back so that it is caught where it is called
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static Session getSession(String user, String host) throws JSchException {
        JSch jsch = new JSch(); // JSch library provided implementation of SSH in Java
        String knownHostsFilePath = System.getProperty("user.home") + "/.ssh/known_hosts";
        // Ensure the known_hosts file exists
        try {
            Files.createDirectories(Paths.get(knownHostsFilePath).getParent());
            if (Files.notExists(Paths.get(knownHostsFilePath))) {
                Files.createFile(Paths.get(knownHostsFilePath));
            }
            jsch.setKnownHosts(knownHostsFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Session session = jsch.getSession(user, host, 22);
        // Set user info for handling interactive authentication (password prompt from user)
        UserInfo ui = new MyUserInfo();
        session.setUserInfo(ui);
        // Java properties to avoid unknown host confirmation prompt
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "yes"); //setting it to yes for secure connection
        session.setConfig(config);
        return session;
    }
    private static void executeCommand(Session session, String command) throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel("exec"); // opens a channel of type exec on the oper2a session
        channel.setCommand(command);
        channel.setInputStream(null);
        channel.setErrStream(System.err);
        InputStream in = channel.getInputStream(); // input stream obtained from the channel
        channel.connect();
        byte[] tmp = new byte[1024]; //
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) break;
                System.out.print(new String(tmp, 0, i)); // reads chunks of data from input stream and converts them to a string and then prints it
            }
            if (channel.isClosed()) {
                if (channel.getExitStatus() == 1) { // if DUO is denied, then exit status will become 1, so we have handled the exception here
                    channel.disconnect();
                    session.disconnect();
                    throw new DuoApprovalMissing("Duo approval rejected");
                }
                if (in.available() > 0) continue; // if there is data in the input stream, it continues reading
                // System.out.println("Exit status: " + channel.getExitStatus());
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
        channel.disconnect();
    }
    private int RandomNumberGenerator(int size) {
        java.util.Random rand = new Random();
        return rand.nextInt(size);
    }

    private static String ExtractPassword (){
        System.out.println("Validating Password");
        String filePath = "C:/Users/pghosh/.key/pghosh.txt";
        String password = null;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            password = br.readLine();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return password;
    }
    // Custom UserInfo implementation for handling password
    public static class MyUserInfo implements UserInfo {
        private String password;
        @Override
        public String getPassword() {
            return password;
        }
        @Override
        public boolean promptYesNo(String message) {
            return true; // Automatically accept
        }
        @Override
        public String getPassphrase() {
            return null;
        }
        @Override
        public boolean promptPassphrase(String message) {
            return false;
        }
        @Override
        public boolean promptPassword(String message) {
            password = ExtractPassword();
            return true;
        }
        @Override
        public void showMessage(String message) {
            System.out.println(message);
        }
    }
}
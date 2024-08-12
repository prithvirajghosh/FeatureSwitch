package org.AutomateFeatureSwitchCleanups.ConnectDB;


import java.sql.*;
import java.util.ArrayList;

public class CheckIfFeatureSwitchPresent {
    public int ConnectToDatabase(ArrayList<String> listOfIssues, String IssueComponentType) {
        String jdbcUrl = "jdbc:oracle:thin:@WMS.DB.TST.FASTENAL.COM:1521:WMST"; // Replace with your actual database URL
        String sqlQuery = null;
        String username = null;
        String password = null;
        if (IssueComponentType.equals("oper1a")) {
            username = "DC";
            password = ""; //enter the password here;
            sqlQuery = "SELECT count(*) AS ftCount FROM feature WHERE feature_id IN (?)";
        } else if (IssueComponentType.equals("sort_ship_db")) {
            username = "QATST";
            password = ""; //enter the password here;
            sqlQuery = "SELECT count(*) AS ftCount FROM wms.wms_component_property WHERE property_name IN (?)";
        }

        int finalCount = 0;
        try {
            // Establish a connection to the Oracle database using DriverManager method from java.sql
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

            System.out.println("Total Number of Feature Switches : " + listOfIssues.size());
            for (String item : listOfIssues) { //iterate through each of the feature switches
                System.out.print("Feature Switch : " + item + "\t");

                // Create a PreparedStatement
                PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
                // Set any parameters (if needed)
                //can iterate over the arraylist - I want arraylist from my
                preparedStatement.setString(1, item); // Example: Department ID 30

                // Execute the query
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    // Process the result set
                    while (resultSet.next()) { //processes each row in the result set
                        int countFeature = resultSet.getInt("ftCount"); // it retrieves the value of the ftCount column
                        if (countFeature != 0) {
                            System.out.println("Present in table");
                        } else {
                            System.out.println("Not present in table");
                        }
                        finalCount = finalCount + countFeature; //stores the total count of the feature switches, should be zero if no feature switch is present

                    }
                }
            }
            // Close the connections
            connection.close();

        } catch (SQLRecoverableException e) {
            System.err.println("Check VPN Connection");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return finalCount;
    }

}

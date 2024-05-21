import org.testng.annotations.DataProvider; 
import org.testng.annotations.Test; 
import org.testng.annotations.BeforeClass; 
import org.testng.annotations.AfterClass; 
import org.testng.Assert; 
import io.restassured.RestAssured; 
import io.restassured.response.Response; 
import com.aventstack.extentreports.ExtentReports; 
import com.aventstack.extentreports.ExtentTest; 
import com.aventstack.extentreports.reporter.ExtentSparkReporter; 
import com.aventstack.extentreports.reporter.configuration.Theme; 
// Here is the Explanation For What Each Method Do  .
/*setup():  It sets up the Extent Reports for reporting test results .

userEndpoints():  It returns an array of endpoint URLs along with a boolean indicating whether the endpoint is expected to pass or fail.

testGetUser(String endpoint, boolean pass):test the functionality of getting user details by sending GET requests to different user endpoints. 

userData():  test data for user details testing. It returns an array of user IDs and their corresponding first names, including both success and failure cases.

testUserData(int userId, String FirstName): test user data by sending GET requests to retrieve user details.  

createUserData():  test data for creating user data. It returns an array of JSON request bodies for creating users along with the expected status codes.

testCreateUser(String requestBody, int expectedStatusCode):  tests creating a new user with various scenarios. 

updateUserData(): This method provides test data for updating user data. It returns an array of user IDs, JSON request bodies for updating users, and the expected status codes.

testUpdateUser(String userId, String requestBody, int expectedStatusCode): This method is annotated with @Test and tests updating an existing user with various scenarios. It logs request and response details and performs assertions on the response status code and body.

patchUserData(): This method provides test data for partially updating user data. It returns an array of user IDs, JSON request bodies for patching users, and the expected status codes.

testPatchUser(String userId, String requestBody, int expectedStatusCode): tests partially updating an existing user with various scenarios. 

deleteUserData(): test data for deleting user data. It returns an array of user IDs and their corresponding expected status codes after deletion.

testDeleteUser(String userId, int expectedStatusCode):  tests deleting a user with various scenarios. 

tearDown():  It flushes the Extent Reports, ensuring all test results are properly recorded.*/

public class APITest {
    ExtentReports extent; // ExtentReports object to manage the reporting
    ExtentTest test; // ExtentTest object to represent each test case
    
    // Method to set up Extent Reports for reporting test results.
    @BeforeClass
    public void setup() {
        // Set up Extent Reports
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter("extent.html");
        sparkReporter.config().setTheme(Theme.DARK);
        sparkReporter.config().setDocumentTitle("Test Report");
        sparkReporter.config().setReportName("API Automation Test Report using RestAssured");

        extent = new ExtentReports();
        extent.attachReporter(sparkReporter); // Attach ExtentSparkReporter to ExtentReports
    }

    // DataProvider for testing endpoints.
    @DataProvider(name = "userEndpoints")
    public Object[][] userEndpoints() {
        return new Object[][] {
            { "https://reqres.in/api/users/1", true }, // Valid endpoint for user with ID 1
            { "https://reqres.in/api/users/10", false } // Invalid endpoint
        };
    }

    // Test method for testing getting user details
    @Test(dataProvider = "userEndpoints")
    public void testGetUser(String endpoint, boolean pass) {
        test = extent.createTest("testGetUser", "Test to get user details");

        // Log request details
        test.info("Request: GET " + endpoint);

        // Send GET request
        Response response = RestAssured.get(endpoint);

        // Log response details
        test.info("Request URL: " + endpoint);
        test.info("Request Method: GET");
        test.info("Response Status Code: " + response.getStatusCode());
        test.info("Response Time: " + response.getTime() + " ms");
        test.info("Response Body: " + response.getBody().asString());

        // Assertions
        if (pass) {
            Assert.assertEquals(response.getStatusCode(), 200);
            Assert.assertTrue(response.getBody().asString().contains("data"));
            test.pass("Status code is 200 and Response body contains 'data'");
        } else {
            Assert.assertNotEquals(response.getStatusCode(), 200);
            test.fail("Status code is not 200 as expected");
        }
    }

    // DataProvider for testing user data
    @DataProvider(name = "userData")
    public Object[][] userData() {
        return new Object[][] {
            // Success case
            { 3, " Emma" },
            // Failure case
            { 10, "" } // Invalid user ID
        };
    }

    // Test method for testing user data
   @Test(dataProvider = "userData")
    public void testUserData(int userId, String FirstName) {
        test = extent.createTest("testUserData", "Test user details");

        // Log request details
        test.info("Request: GET https://reqres.in/api/users/" + userId);

        // Send GET request
        Response response = RestAssured.get("https://reqres.in/api/users/" + userId);

        // Log response details
        test.info("Response Status Code: " + response.getStatusCode());
        test.info("Response Body: " + response.getBody().asString());

        // Assertions
        if (response.getStatusCode() == 200) {
            Assert.assertTrue(response.getBody().asString().contains(FirstName));
            test.pass("User details contain expected Name: " + FirstName);
        } else {
            test.fail("Failed to retrieve user details");
        }
    }
    

    // DataProvider for testing creating user data
    @DataProvider(name = "createUserData")
    public Object[][] createUserData() {
        return new Object[][] {
            { "{ \"first_name\": \"Toka\", \"last_name\": \"Ibrahem\" }", 201 }, // Successful creation
            { "{ \"first_name\": \"\", \"email\": \"invalid-email\" }", 400 }, // Invalid email
            { "{ \"last_name\": \"smith\" }", 400 } // Missing required field
            
        };
    }

    // Test method for testing creating a new user
    @Test(dataProvider = "createUserData")
    public void testCreateUser(String requestBody, int expectedStatusCode) {
        test = extent.createTest("testCreateUser", "Test creating a new user with various scenarios");

        // Log request details
        test.info("Request: POST https://reqres.in/api/users");
        test.info("Request Body: " + requestBody);

        // Send POST request
        Response response = RestAssured
            .given()
            .header("Content-Type", "application/json")
            .body(requestBody)
            .post("https://reqres.in/api/users");

        // Log response details
        test.info("Response Status Code: " + response.getStatusCode());
        test.info("Response Body: " + response.getBody().asString());
     // Assertions
        Assert.assertEquals(response.getStatusCode(), expectedStatusCode);
        if (response.getStatusCode() == expectedStatusCode) {
            test.pass("Status code is " + expectedStatusCode);

            // If the status code is 201 (Created), verify the new user ID
            if (expectedStatusCode == 201) {
                // Extract the ID from the response body
                int userId = response.jsonPath().getInt("id");
                int expectedUserId = userId + 1; // Expected user ID is the returned ID incremented by 1
                Assert.assertEquals(userId, expectedUserId, "New user ID is not incremented by 1");
            }
        } else {
            test.fail("Expected status code " + expectedStatusCode + " but got " + response.getStatusCode());
        }
    }

    // DataProvider for testing updating user data
    @DataProvider(name = "updateUserData")
    public Object[][] updateUserData() {
        return new Object[][] {
            { "1", "{ \"first_name\": \"morpheus\", \"last_name\": \"zion resident\" }", 200 }, // Successful update
            { "2", "{ \"last_name\": \"smith\" }", 400 } // Missing required field
           
        };
    }

    // Test method for testing updating an existing user
    @Test(dataProvider = "updateUserData")
    public void testUpdateUser(String userId, String requestBody, int expectedStatusCode) {
        test = extent.createTest("testUpdateUser", "Test updating an existing user with various scenarios");

        //        // Log request details
        test.info("Request: PUT https://reqres.in/api/users/" + userId);
        test.info("Request Body: " + requestBody);

        // Send PUT request
       
        Response response = RestAssured
        		.given()
        		.header("Content-Type", "application/json")
        		.body(requestBody)
        		.put("https://reqres.in/api/users/" + userId);    // Log response details
        test.info("Response Status Code: " + response.getStatusCode());
        test.info("Response Body: " + response.getBody().asString());

        // Assertions
        Assert.assertEquals(response.getStatusCode(), expectedStatusCode);
        if (response.getStatusCode() == expectedStatusCode) {
            test.pass("Status code is " + expectedStatusCode);
        } else {
            test.fail("Expected status code " + expectedStatusCode + " but got " + response.getStatusCode());
        }
    }

    // DataProvider for testing patching user data
    @DataProvider(name = "patchUserData")
    public Object[][] patchUserData() {
        return new Object[][] {
            { "1", "{ \"first_name\": \"neo\" }", 200 }, // Successful patch
            { "3", "{ \"last_name\": \"smith\" }", 400 } // Missing required field
                   };
    }

    // Test method for testing partially updating an existing user
    @Test(dataProvider = "patchUserData")
    public void testPatchUser(String userId, String requestBody, int expectedStatusCode) {
        test = extent.createTest("testPatchUser", "Test partially updating an existing user with various scenarios");

        // Log request details
        test.info("Request: PATCH https://reqres.in/api/users/" + userId);
        test.info("Request Body: " + requestBody);

        // Send PATCH request
        Response response = RestAssured
            .given()
            .header("Content-Type", "application/json")
            .body(requestBody)
            .patch("https://reqres.in/api/users/" + userId);

        // Log response details
        test.info("Response Status Code: " + response.getStatusCode());
        test.info("Response Body: " + response.getBody().asString());

        // Assertions
        Assert.assertEquals(response.getStatusCode(), expectedStatusCode);
        if (response.getStatusCode() == expectedStatusCode) {
            test.pass("Status code is " + expectedStatusCode);
        } else {
            test.fail("Expected status code " + expectedStatusCode + " but got " + response.getStatusCode());
        }
    }

    // DataProvider for testing deleting user data
    @DataProvider(name = "deleteUserData")
    public Object[][] deleteUserData() {
        return new Object[][] {
            { "1", 204 }, // Successful deletion
            { "10", 404 } // Non-existent user
                   };
    }

    // Test method for testing deleting a user
    @Test(dataProvider = "deleteUserData")
    public void testDeleteUser(String userId, int expectedStatusCode) {
        test = extent.createTest("testDeleteUser", "Test deleting a user with various scenarios");

        // Log request details
        test.info("Request: DELETE https://reqres.in/api/users/" + userId);

        // Send DELETE request
        Response response = RestAssured.delete("https://reqres.in/api/users/" + userId);

        // Log response details
        test.info("Response Status Code: " + response.getStatusCode());
        test.info("Response Body: " + response.getBody().asString());

        // Assertions
        Assert.assertEquals(response.getStatusCode(), expectedStatusCode);
        if (response.getStatusCode() == expectedStatusCode) {
            test.pass("Status code is " + expectedStatusCode);
        } else {
            test.fail("Expected status code " + expectedStatusCode + " but got " + response.getStatusCode());
        }
    }

    // AfterClass method to flush Extent Reports
    @AfterClass
    public void tearDown() {
        extent.flush();
    }}








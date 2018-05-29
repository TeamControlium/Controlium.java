package TeamControlium.Controlium.Test;

import TeamControlium.Controlium.*;
import TeamControlium.Utilities.TestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SeleniumServerTests {

    @BeforeEach
    void setUp() {
        TestData.setItem("Selenium", "SeleniumServerFolder","./TestServer");
    }

    @AfterEach
    void tearDown() {
    }

    // Verify Selenum can be launched locally
    @org.junit.jupiter.api.Test
    void VerifySeleniumDriverLaunchesChromeAsDefault() {
        int chromeCountBefore=0;
        int chromeCountAfter=0;
        for (String[] line: getProcessList()) {
            if (line[0].equalsIgnoreCase("chromedriver.exe")) chromeCountBefore++;
        }

        SeleniumDriver seleniumDriver = new SeleniumDriver(false);

        try {Thread.sleep(2000);} catch(Exception e) {}
        for (String[] line: getProcessList()) {
            if (line[0].equalsIgnoreCase("chromedriver.exe")) chromeCountAfter++;
        }
        Assertions.assertEquals(chromeCountBefore,chromeCountAfter-1,"Number of Chrome Driver instances increments");
    }

    // Verify Selenium can kill other instances of Server on startup
    @org.junit.jupiter.api.Test
    void VerifySeleniumDriverCanKillPreviousInstances() {
        int chromeCountBefore=0;
        int chromeCountAfter=0;
        for (String[] line: getProcessList()) {
            if (line[0].equalsIgnoreCase("chromedriver.exe")) chromeCountBefore++;
        }

        SeleniumDriver seleniumDriver1 = new SeleniumDriver(false);

        SeleniumDriver seleniumDriver2 = new SeleniumDriver(true);

        try {Thread.sleep(2000);} catch(Exception e) {}

        for (String[] line: getProcessList()) {
            if (line[0].equalsIgnoreCase("chromedriver.exe")) chromeCountAfter++;
        }
        Assertions.assertEquals(1,chromeCountAfter,"Number of Chrome Driver instances exactly 1");

        seleniumDriver1.CloseDriver();
        seleniumDriver2.CloseDriver();
    }


    private List<String[]> getProcessList() {
        boolean startLogging = false;
        // MAT GET THIS WORKING - Driver executable filesnames.........

        List<String[]> list = new ArrayList<String[]>();
        try {
            String line;
            //Process p = Runtime.getRuntime().exec("ps -e");
            Process p = Runtime.getRuntime().exec (System.getenv("windir") +"\\system32\\"+"tasklist.exe");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                String[] arrayLine = line.split("\\s+");
                if (startLogging) list.add(arrayLine);
                if (line.contains("=======")) startLogging=true;
            }
            input.close();
        } catch (Exception err) {Assertions.fail("Error getting process list: ",err);
        }
        return list;
    }
}

package TeamControlium.Controlium.Test;

import TeamControlium.Controlium.*;
import TeamControlium.Utilities.Logger;
import TeamControlium.Utilities.TestData;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class BasicNavigationAndFindTests {

    private SeleniumDriver seleniumDriver=null;
    @BeforeEach
    void setUp() {
        TestData.setItem("Selenium", "SeleniumServerFolder", "./TestServer");
        if (seleniumDriver!=null) seleniumDriver.CloseDriver();
        seleniumDriver = new SeleniumDriver(true);

    }

    @AfterEach
    void tearDown() {
        if (seleniumDriver!=null) seleniumDriver.CloseDriver();
    }

    // Verify we can browse to a URL with Selenium
    @org.junit.jupiter.api.Test
    void VerifyBrowseToWorks() {
        seleniumDriver.gotoURL("http://www.google.com");

        String pageTitle = seleniumDriver.getPageTitle();
        Assertions.assertEquals("Google", pageTitle, "Page title correct");
    }

    // Verify we can find an element
    @org.junit.jupiter.api.Test
    void VerifyBasicFindElement() {
        seleniumDriver.gotoURL("http://www.google.com");
        HTMLElement element=null;
        try {
            element = seleniumDriver.findElement(new ObjectMapping("//input[@id='lst-ib']"));
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown finding Google search testbox element: ",e.getMessage());
        }
        assertNotNull(element,"Returned element not null");
    }

    // Verify timeout when finding non-existant element
    @org.junit.jupiter.api.Test
    void VerifyFindElementTimeout() {
        seleniumDriver.setFindTimeout(Duration.ofMillis(10000));
        seleniumDriver.gotoURL("http://www.google.com");
        HTMLElement element=null;
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            element = seleniumDriver.findElement(new ObjectMapping("//input[@id='wont match']"));
        }
        catch( Exception e) {
            stopWatch.stop();
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown finding Google search testbox element: ",e.getMessage());
        }

        int actualTime = (int)stopWatch.getTime(TimeUnit.SECONDS);

        assertEquals(true,(actualTime>9 && actualTime<12), String.format("Timeout ([%d]) approx 10 seconds",actualTime));
        assertNull(element,"Returned element null");
    }

    // Verify error after correct timeout when requiring single element but find logic returns multiple
    @org.junit.jupiter.api.Test
    void VerifyFindElementFailForMultiple() {
        seleniumDriver.setFindTimeout(Duration.ofMillis(10000));
        seleniumDriver.gotoURL("http://www.google.com");
        HTMLElement element=null;
        String errorMessage=null;
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            element = seleniumDriver.findElement(new ObjectMapping("//input","Find logic returning mutiple elements"),true,false);
        }
        catch( Exception e) {
            stopWatch.stop();
            errorMessage=e.getMessage();
        }

        int actualTime = (int)stopWatch.getTime(TimeUnit.SECONDS);

        assertEquals(true,(actualTime>9 && actualTime<12), String.format("Timeout ([%d]) approx 10 seconds",actualTime));
        assertNull(element,"Returned element null");
        assertEquals(true,(errorMessage!=null && errorMessage.startsWith("Found 9 matching elements using [//input] ([Find logic returning mutiple elements]) from [DOM Top Level]. Not allowing mutiple matches and timeout reached after")),"Correct error message");
    }

    // Verify we can find multiple elements
    @org.junit.jupiter.api.Test
    void VerifyBasicFindMultipleElements() {
        seleniumDriver.gotoURL("http://www.google.com");
        List<HTMLElement> elements=null;
        try {
            elements = seleniumDriver.findElements(null,new ObjectMapping("//input"));
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown finding Google search testbox element: ",e.getMessage());
        }
        assertEquals(9,elements.size(),"Verify 9 input elements on Google search site");
    }

    // Verify correct error message when find fails (No friendly name set)
    @org.junit.jupiter.api.Test
    void VerifyCorrectErrorWithBadFindLogicNoFriendlyName() {
        seleniumDriver.gotoURL("http://www.google.com");
        HTMLElement element=null;
        String errorMessage=null;
        try {
            element = seleniumDriver.findElement(new ObjectMapping("//wibble'"));
        }
        catch( Exception e) {
            errorMessage = e.getMessage();
        }
        assertNull(element,"No returned element");
        assertEquals("Selenium Driver error. Find Logic [//wibble'] for [//wibble'] is invalid!",errorMessage,"Correct error message");
    }

    // Verify correct error message when find files (Friendly name set)
    @org.junit.jupiter.api.Test
    void VerifyCorrectErrorWithBadFindLogicWithFriendlyName() {
        seleniumDriver.gotoURL("http://www.google.com");
        HTMLElement element=null;
        String errorMessage=null;
        try {
            element = seleniumDriver.findElement(new ObjectMapping("//wibble'","Non-sense find logic"));
        }
        catch( Exception e) {
            errorMessage = e.getMessage();
        }
        assertNull(element,"No returned element");
        assertEquals("Selenium Driver error. Find Logic [//wibble'] for [Non-sense find logic] is invalid!",errorMessage,"Correct error message");
    }

    // Verify find fails when needing element to be stable and it is not
    @org.junit.jupiter.api.Test
    void VerifyUnstableElementTimeout() {
        seleniumDriver.setFindTimeout(Duration.ofMillis(10000));
        //
        // At bouncejs.com  there is an element in the left preferences panel that is constantly moving up and down.  So it is NEVER stable.  So we can check that
        // findElement fails if told to wait untile stable.
        //
        ObjectMapping movingElement = new ObjectMapping("//div[@id='preferences']/div[@class='empty-message']","Moving element in left preferences panel");
        seleniumDriver.gotoURL("http://bouncejs.com/");
        HTMLElement element=null;
        String errorMessage=null;
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            element = seleniumDriver.findElement(movingElement,true);
        }
        catch( Exception e) {
            stopWatch.stop();
            errorMessage = e.getMessage();
        }

        int actualTime = (int)stopWatch.getTime(TimeUnit.SECONDS);

        assertEquals(true,(actualTime>9 && actualTime<12), String.format("Timeout ([%d]) approx 10 seconds",actualTime));
        assertEquals(true,(errorMessage!=null && errorMessage.startsWith("From [DOM Top Level], find [//div[@id='preferences']/div[@class='empty-message'] (Moving element in left preferences panel)] returned 1 matches (Not Allowing multiple matches). Element NOT stable after timeout reached")),"Correct Error message");
        assertNull(element,"No returned element");
    }

}

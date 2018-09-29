package TeamControlium.Controlium.Test;

import TeamControlium.Controlium.*;
import TeamControlium.Controlium.Exception.InvalidElementState;
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

public class ElementInteractionTests {

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

    // Verify we can enter and readback text
    @org.junit.jupiter.api.Test
    void VerifyDataEntryAndReadBack() {
        String testText = "My entered text";
        seleniumDriver.gotoURL("https://www.w3schools.com/angular/tryit.asp?filename=try_ng_example1");
        HTMLElement inputElement=null;
        HTMLElement outputElement=null;
        try {
            HTMLElement iframe = seleniumDriver.findElement(new ObjectMapping("//iframe[@id='iframeResult']"));
            seleniumDriver.setIFrame(iframe);
            inputElement = seleniumDriver.findElement(new ObjectMapping("//input[@ng-model='name']"));
            outputElement = seleniumDriver.findElement(new ObjectMapping("//p[@class='ng-binding' and starts-with(.,'You wrote')]"));
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown finding textbox element: ",e.getMessage());
            assertNotNull(inputElement,"Returned element must not be null!!");
        }

        inputElement.setText(testText);
        String textBack = outputElement.getText();

        assertEquals(true,textBack.contains("My entered text"),String.format("Output label contains entered text [%s]",testText));
    }

    // Verify we can enter and readback text
    @org.junit.jupiter.api.Test
    void CheckClickOverlayProcessing() {
        String testText = "My entered text";
        ObjectMapping overlayButtonFindLogic = new ObjectMapping("//button[.='Turn on the overlay effect']", "Overlay effect button");

        final HTMLElement overlayButton;
        seleniumDriver.gotoURL("https://www.w3schools.com/howto/howto_css_overlay.asp");

        // Find the button
        try {
            overlayButton = seleniumDriver.findElement(overlayButtonFindLogic);
        } catch (Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation, "Error thrown finding [%s]: %s", overlayButtonFindLogic.getFriendlyName(), e.getMessage());
            throw new RuntimeException(String.format("Error thrown finding [%s]", overlayButtonFindLogic.getFriendlyName()), e);
        }

        // Click it
        try {
            overlayButton.click();
        } catch (Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation, "Error thrown clicking [%s]: %s", overlayButton.getFriendlyName(), e.getMessage());
            throw new RuntimeException(String.format("Error thrown clicking [%s]", overlayButton.getFriendlyName()), e);
        }

        //
        // We now have the overlay shown, so lets try clicking it again.... :-)
        //

        //
        // Check correct exception when we try clicking
        //
        Exception exceptionThrown=null;
        try {
            overlayButton.click();
        }
        catch (Exception ex) {
            exceptionThrown=ex;
        }

        // Check we have the right exception thrown
        assertEquals(InvalidElementState.class, exceptionThrown.getClass());

        // And make sure the correct offending element was identified
        assertEquals(true,exceptionThrown.getMessage().contains("div id=\"overlay\""),"Cause string identifies correct element");
    }

}

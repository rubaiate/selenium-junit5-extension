package org.nde.selenium.extension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

@ExtendWith(SeleniumDriverExtension.class)
class SeleniumDriverExtensionTest {
    @Test
    public void testWebDriverAsParameter(WebDriver driver) {
        driver.get("https://junit.org");
    }
}
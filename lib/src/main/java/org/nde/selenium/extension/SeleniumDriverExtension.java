package org.nde.selenium.extension;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.junit.jupiter.api.extension.*;

import static org.junit.jupiter.api.extension.ExtensionContext.*;

public class SeleniumDriverExtension implements ParameterResolver, AfterEachCallback, TestExecutionExceptionHandler {
    final static Namespace NAMESPACE = Namespace.create(SeleniumDriverExtension.class);
    final static String DRIVER_KEY = "selenium-driver";

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(WebDriver.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        WebDriver driver = createDriver();
        extensionContext.getStore(NAMESPACE).put(DRIVER_KEY, driver);
        return driver;
    }

    @Override
    public void afterEach(ExtensionContext extensionContext){
        quitWebDriver(extensionContext);
    }

    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable) throws Throwable {
        quitWebDriver(extensionContext);
        throw throwable;
    }

    private void quitWebDriver(ExtensionContext extensionContext) {
        Store store = extensionContext.getStore(NAMESPACE);
        WebDriver driver = (WebDriver) store.get(DRIVER_KEY);
        if (driver != null) {
            driver.quit();
            store.remove(DRIVER_KEY);
        }
    }

    private WebDriver createDriver(){
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setHeadless(true);
        return new ChromeDriver(chromeOptions);
    }
}
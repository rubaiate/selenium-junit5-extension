package org.nde.selenium.extension;

import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.junit.jupiter.api.extension.*;

import static org.junit.jupiter.api.extension.ExtensionContext.*;

public class SeleniumDriverExtension extends TypeBasedParameterResolver<WebDriver> implements AfterEachCallback, TestExecutionExceptionHandler {
    public final static Namespace NAMESPACE = Namespace.create(SeleniumDriverExtension.class);
    public final static String DRIVER_KEY = "selenium-driver";

    @Override
    public WebDriver resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        boolean headless = getHeadless(extensionContext);

        WebDriver driver = createDriver(headless);

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
        WebDriver driver = store.remove(DRIVER_KEY, WebDriver.class);
        if (driver != null) {
            driver.quit();
        }
    }

    private WebDriver createDriver(boolean headless){
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setHeadless(headless);
        return new ChromeDriver(chromeOptions);
    }

    private boolean getHeadless(ExtensionContext extensionContext) {
        Headless headless = extensionContext.getTestMethod().get().getDeclaredAnnotation(Headless.class);
        if(headless!=null){
            return headless.value();
        }
        return true;
    }
}
### Selenium WebDriver
It is possible to find great articles about setting up selenium and running tests. 
But I find it is tricky to manage WebDriver, specially when you need test cases not to be polluted by utility codes.

WebDriver is the main api for interfacing browsers, while testing using selenium. 
Following is a very basic selenium test which visits https://junit.org.

```java
    @Test
    public void basicTest() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setHeadless(true);
        WebDriver driver = new ChromeDriver(chromeOptions);
        driver.get("https://junit.org");
    }
```
 
 If your set up is correct this will run without any issue, but you will find many chrome process running in the background.
 You can check the process manager to verify.
 ```shell script
ps -ef | grep chrome
```
In linux you can kill running chrome process by following command. (Note: If you are running chrome browser it may get kill by this command)
```shell script
pkill -f chrome
```

#### Calling quit...
Quick solution will fix the issue. Just quit the driver after running tests.
```java
    ...
    driver.get("https://junit.org");
    driver.quit()
```
You can check the background processes again. No new additional chrome processes will be running in the background.
So is issue fixed? Not yet. 

Even correct selenium tests can be failed due to many reasons. So what happened if a test get failed.
We can simulate failed test using invalid url.
```java
    ...
    driver.get("https://junit345.org");
    driver.quit()
```
Oh.. the driver not quit properly, and the process still is running. We can simply fix the issue by wrapping test withing try, catch, finally block, but we are looking for elegant solutions.
Junit 5 extensions will give us helping hand here. 

#### [Junit 5 Extensions](https://junit.org/junit5/docs/current/user-guide/#extensions)
After going through available features, I came up with following plan.
1. Inject WebDriver into each test method using [**ParameterResolver**](https://junit.org/junit5/docs/current/user-guide/#extensions-parameter-resolution).
We are going to use [**TypeBasedParameterResolver**](https://github.com/junit-team/junit5/blob/r5.7.1/junit-jupiter-api/src/main/java/org/junit/jupiter/api/extension/support/TypeBasedParameterResolver.java) which is more convenient.
2. Attach WebDriver into **ExtensionContext** using *store* while resolving the parameter.
3. Retrieve WebDriver from  **ExtensionContext** and call quit [*afterEach*](https://junit.org/junit5/docs/current/user-guide/#extensions-lifecycle-callbacks) method.
   Or If exceptions have thrown do the same to release the driver.

##### Injecting and WebDriver
```java
    @Override
    public WebDriver resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        WebDriver driver = createDriver(true);

        extensionContext.getStore(NAMESPACE).put(DRIVER_KEY, driver);
        return driver;
    }
```

##### Quit WebDriver afterEach
```java
    @Override
    public void afterEach(ExtensionContext extensionContext){
        quitWebDriver(extensionContext);
    }

    private void quitWebDriver(ExtensionContext extensionContext) {
         Store store = extensionContext.getStore(NAMESPACE);
         WebDriver driver = store.remove(DRIVER_KEY, WebDriver.class);
         if (driver != null) {
             driver.quit();
         }
    }
```

##### Quit WebDriver on handleTestExecutionException
```java
    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable) throws Throwable {
        quitWebDriver(extensionContext);
        throw throwable;
    }
```

##### Registering the Extension
ExtendWith annotation can be used to register Junit5 extensions.
```java
@ExtendWith(SeleniumDriverExtension.class)
```

##### Test code updated with SeleniumDriverExtension
So test code will be elegant without resource leaks.
```java
@ExtendWith(SeleniumDriverExtension.class)
class SeleniumDriverExtensionTest {
    @Test
    public void testWebDriverAsParameter(WebDriver driver) {
        driver.get("https://junit.org");
    }
}
```

#### Disable headless
In a ci/cd pipeline test will be run as headless, most of the time, 
but when running locally it is useful to disable *headless* specially for debugging purpose.
Custom annotation **Headless** is created to indicate value for the headless parameter.
```java
    private boolean getHeadless(ExtensionContext extensionContext) {
        Headless headless = extensionContext.getTestMethod().get().getDeclaredAnnotation(Headless.class);
        if(headless!=null){
            return headless.value();
        }
        return true;
    }
```
##### Using **Headless** annotation to disable headless
```java
    @Test
    @Headless(false)
    public void testWebDriverAsParameter(WebDriver driver) {
    ...
    }
```
### Having a global web driver...
You may be thinking about having a global WebDriver, but I find that is troublesome when your tests become more complex. 
Also it will create dependencies between test methods.
package selenium.jobcan;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import io.github.bonigarcia.wdm.WebDriverManager;

public class Web {
    public static void main(String[] args) {
        
        WebDriverManager.firefoxdriver().arch64().setup();
        WebDriver driver = WebDriverManager.getInstance().getWebDriver();
        
        System.out.println(driver != null); // fail
        
        try {
            driver = WebDriverManager.firefoxdriver().create();
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }
        System.out.println(driver != null); // success
        try {
            driver  = WebDriverManager.firefoxdriver().browserInDocker().create();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        System.out.println(driver != null); // success

        FirefoxOptions options = new FirefoxOptions();
        options.setCapability("acceptInsecureCerts", true);
        options.addArguments("--start-maximized");
      
        options.addArguments("enable-automation"); // https://stackoverflow.com/a/43840128/1689770
        options.addArguments("--headless"); // only if you are ACTUALLY running headless
        options.addArguments("--no-sandbox"); //https://stackoverflow.com/a/50725918/1689770
        options.addArguments("--disable-dev-shm-usage"); //https://stackoverflow.com/a/50725918/1689770
        options.addArguments("--disable-browser-side-navigation"); //https://stackoverflow.com/a/49123152/1689770
        options.addArguments("--disable-gpu"); //https://stackoverflow.com/questions/51959986/how-to-solve-selenium-chromedriver-timed-out-receiving-message-from-renderer-exc
        
        try {
            driver = new FirefoxDriver(options); //success
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        System.out.println(driver != null);

    }
}

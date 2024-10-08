package selenium.jobcan;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.fasterxml.jackson.databind.ObjectMapper;

import selenium.enums.MyProperties;

public class Jobcan {

    WebDriver driver;
    JavascriptExecutor js;
    WebDriverWait waiter;

    Map<String, String> properties;

    public Jobcan(WebDriver webDriver, Map<String, String> properties) throws Exception {
        this.driver = webDriver;
        this.js = (JavascriptExecutor) webDriver;
        this.waiter = new WebDriverWait(driver, Duration.of(3000, ChronoUnit.MILLIS));
        this.properties = properties;
    }

    public static void main(String[] args) throws Exception {

        // 1. properties loading and check date
        String propertiesPath = args[0];
        System.out.println("!");
        ZonedDateTime now = ZonedDateTime.now();
        System.out.println("=================================================================");
        System.out.println(now.getDayOfWeek().toString() + "\tJob Started!!!\n\n" + "\t\t" + now.toString());

        Map<String, String> map = MyUtils.loadProperties(propertiesPath);

        boolean show = MyProperties.SHOW.getBooleanValue(map);
        boolean sleepYN = MyProperties.SLEEP.getBooleanValue(map);

        boolean go = checkListBeforeStart(now, map);
        if (!go) {
            return;
        }

        WebDriver driver = null;
        switch (map.getOrDefault("os", "chrome")) {
            case "chrome" -> {
                System.out.println("chrome");
                System.setProperty("webdriver.chrome.driver", map.get("driver"));
                driver = MyUtils.getChromeDriver(show);
            }
            case "firefox" -> {
                System.out.println("firefox");
                System.setProperty("webdriver.gecko.driver", map.get("geckodriver"));
                driver = MyUtils.getFirefoxDriver(show);
            }
            case "webdriver_chrome" -> {
                // webdrivermanager는 chrome버전 114까지만 가능.
                System.out.println("webdriver manager -> chrome");
                ChromeOptions options = new ChromeOptions();
                options.setCapability("acceptInsecureCerts", true);
                options.addArguments("--start-maximized");
                if (!show) {
                    options.addArguments("enable-automation"); // https://stackoverflow.com/a/43840128/1689770
                    options.addArguments("--headless"); // only if you are ACTUALLY running headless
                    options.addArguments("--no-sandbox"); //https://stackoverflow.com/a/50725918/1689770
                    options.addArguments("--disable-dev-shm-usage"); //https://stackoverflow.com/a/50725918/1689770
                    options.addArguments("--disable-browser-side-navigation"); //https://stackoverflow.com/a/49123152/1689770
                    options.addArguments("--disable-gpu"); //https://stackoverflow.com/questions/51959986/how-to-solve-selenium-chromedriver-timed-out-receiving-message-from-renderer-exc
                }
                driver = io.github.bonigarcia.wdm.WebDriverManager.chromedriver().capabilities(options).create();
            }
            default -> {
                System.out.println("webdriver manager -> firefox");
                FirefoxOptions options = new FirefoxOptions();
                options.setCapability("acceptInsecureCerts", true);
                options.addArguments("--start-maximized");
                if (!show) {
                    options.addArguments("enable-automation"); // https://stackoverflow.com/a/43840128/1689770
                    options.addArguments("--headless"); // only if you are ACTUALLY running headless
                    options.addArguments("--no-sandbox"); //https://stackoverflow.com/a/50725918/1689770
                    options.addArguments("--disable-dev-shm-usage"); //https://stackoverflow.com/a/50725918/1689770
                    options.addArguments("--disable-browser-side-navigation"); //https://stackoverflow.com/a/49123152/1689770
                    options.addArguments("--disable-gpu"); //https://stackoverflow.com/questions/51959986/how-to-solve-selenium-chromedriver-timed-out-receiving-message-from-renderer-exc
                }
                driver = io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver().capabilities(options).create();
            }
        }

        Jobcan jobcan = new Jobcan(driver, map);
        //3. random timer		
        if (sleepYN) {
            Random random = new Random();
            int sleep = (random.nextInt(120) + 1) * 1000;
            System.out.println("sleep time : " + sleep);
            MyUtils.sleep(sleep);
        }

        //4. web page works
        jobcan.excute();
        System.out.println("=================================================================\n");
    }

    private static boolean checkListBeforeStart(ZonedDateTime now, Map<String, String> map) {
        String[] holidays = MyProperties.HOLIDAY.getValue(map).replaceAll(" ", "").split(","); // 공백 제거 후 쪼갬
        String today = now.format(DateTimeFormatter.ofPattern("MMdd"));
        int dayOfWeek = now.getDayOfWeek().getValue();

        // check day of week which is Saturday or Sunday
        if (dayOfWeek == DayOfWeek.SATURDAY.getValue() || dayOfWeek == DayOfWeek.SUNDAY.getValue()) {
            System.out.println("\t\t\t[It is " + DayOfWeek.of(dayOfWeek).toString() + "]");
            return false;
        }
        // check holiday which written in properties file.
        for (String holiday : holidays) {
            if (today.startsWith(holiday)) { // start with로 바꾸는것 테스트해봐야제
                System.out.println("\t\t\t[It is happy holiday!!!]");
                return false;
            }
        }
        return true;
    }

    public List<String> openWindows() {
        WebElement sslButton = driver.findElement(By.cssSelector("#jbc-app-links > ul > li:nth-child(3) > a"));
        sslButton.click();

        for (String wind : driver.getWindowHandles()) {
            if (driver.getWindowHandle().equals(wind)) {
                continue;
            }
            driver.switchTo().window(wind);
            MyUtils.sleep(5000);
            js.executeScript("window.open('/employee/attendance')");
            // js.executeScript("window.open('/employee/adit/modify')");
            MyUtils.sleep(3000);
        }
        List<String> windows = new ArrayList<>();

        // 0 main , 1 employee, 2 list 3. adit page
        for (String wind : driver.getWindowHandles()) {
            windows.add(wind);
        }

        driver.switchTo().window(windows.get(2));
        waiter
                .ignoring(NoSuchElementException.class)
                .withTimeout(Duration.ofSeconds(30))
                .pollingEvery(Duration.ofSeconds(3))
                .until(t -> {
                    return t.findElement(By.cssSelector("#search-result")) != null;
                });

        // driver.switchTo().window(windows.get(3));
        // waiter
        // .ignoring(NoSuchElementException.class)
        // .withTimeout(Duration.ofSeconds(10))
        // .pollingEvery(Duration.ofSeconds(1))
        // .until(t -> {
        // 	return t.findElement(By.cssSelector("#logs-table > div > table > tbody > tr")) !=null;
        // });
        driver.switchTo().window(windows.get(0));
        return windows;
    }
    //Deprecated

    public boolean aditCheck(WebDriver page) {
        boolean flag = false;
        String nowStr = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HHmm"));
        int now = Integer.parseInt(nowStr);
        List<WebElement> list = page.findElements(By.cssSelector("#logs-table > div > table > tbody > tr"));

        boolean on = false;
        list.forEach(t -> {
            System.out.println(t.findElement(By.cssSelector("td:nth-child(1)")).getText());
        });
        if (now <= 1000 && list.size() == 0) {
            // 출근 가능
        } else if (1700 <= now) {
            // 출근 표시가 있고 퇴근 표시가 없어야 한다.
        } else {

        }

        return flag;
    }

    public boolean attendanceCheck(WebDriver page) {
        WebElement el = page.findElement(By.cssSelector(String.format("#search-result > div.table-responsive.text-nowrap > table > tbody > tr:nth-child(%d)", ZonedDateTime.now().getDayOfMonth())));
        //0 날짜 MM/dd(DoW)
        //1 휴일구분 
        //2 근무스케줄시간 hh:mm~hh:mm
        //3 출근시각 hh:mm
        //4 퇴근시각 
        List<WebElement> list = el.findElements(By.cssSelector("td"));
        String scheduled_onoff = list.get(2).getText().replaceAll("[^0-9]", "");
        String real_on = list.get(3).getText().replaceAll("[^0-9]", "");
        String real_off = list.get(4).getText().replaceAll("[^0-9]", "");

        String nowStr = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HHmm"));
        String onStr = scheduled_onoff.substring(0, 4);
        String offStr = scheduled_onoff.substring(4);

        int now = Integer.parseInt(nowStr);
        int on = Integer.parseInt(onStr);
        int off = Integer.parseInt(offStr);

        System.out.println("\n" + el.getText());
        System.out.println("time now : " + now);

        boolean flag = false;
        if (now <= 1000 && now <= on && "".equals(real_on)) {
            // 출근 가능
            System.out.println("now it's time to work");
            flag = true;
        } else if (1700 <= now && off <= now && !"".equals(real_on) && "".equals(real_off)) {
            // 퇴근 가능
            System.out.println("now you are free to go home");
            flag = true;
        } else {
            // 출퇴근 가능 시간이 아님
            System.out.println("cannot check both get_on and get_off");
        }
        return flag;

    }

    public void buttonClick(WebDriver page, String id) {
        boolean click = MyProperties.CLICK.getBooleanValue(properties);
        String time = page.findElement(By.cssSelector("#clock")).getText();
        int i = 0;
        while ("00:00:00".equals(time) && i++ < 3) {
            time = page.findElement(By.cssSelector("#clock")).getText();
            MyUtils.sleep(1000);
        }
        System.out.println(time);
        WebElement registerBtn = page.findElement(By.cssSelector("#adit-button-push"));

        if (click) {
            registerBtn.click();
            System.out.println(registerBtn.getText() + "clicked !!!");
            telegramSendMessage("[" + id + "] jobcan clicked : " + time);
        }
    }

    public void excute() {
        // telegramSendMessage("test");
        driver.get(MyProperties.URL.getValue(properties));
        driver.manage().window().maximize();
        login(driver);

        // 사용자 사번 U00000
        // WebElement workerNumber = driver.findElement(By.cssSelector("body > div.wrapper > div > section.content > div > div > div:nth-child(1) > div.box-body > table > tbody > tr:nth-child(3) >td"));
        WebElement workerNumber = MyWaiter.wait(driver, By.cssSelector("body > div.wrapper > div > section.content > div > div > div:nth-child(1) > div.box-body > table > tbody > tr:nth-child(3) >td"), 3);
        String id = "## getting ID failed  ##";
        if (workerNumber != null) {
            id = workerNumber.getText();
        }
        System.out.println("uracle id => " + id);

        // 0 main, 1 employee, 2 attendance
        List<String> windows = openWindows();

        boolean okay = attendanceCheck(driver.switchTo().window(windows.get(2)));
        boolean force = MyProperties.FORCE.getBooleanValue(properties);

        if (okay || force) {
            buttonClick(driver.switchTo().window(windows.get(1)), id);
        }

        boolean quit = MyProperties.QUIT.getBooleanValue(properties);
        if (quit) {
            driver.quit();
        }
    }

    public void login(WebDriver driver) {
        // TODO Auto-generated method stub
        driver.findElement(By.cssSelector("#user_email")).sendKeys(MyProperties.ID.getValue(properties));
        driver.findElement(By.cssSelector("#user_password")).sendKeys(MyProperties.PW.getValue(properties));
        driver.findElement(By.cssSelector("#login_button")).click();
        MyUtils.sleep(500);
    }

    public void telegramSendMessage(String text) {

        String Token = properties.get("token");
        String chat_id = properties.get("chatId");

        BufferedReader in = null;

        try {
            URL obj = new URL("https://api.telegram.org/bot" + Token + "/sendmessage?chat_id=" + chat_id + "&text=" + text); // 호출할 url

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

            String line = "";
            StringBuilder sb = new StringBuilder();
            while ((line = in.readLine()) != null) { // response를 차례대로 출력
                sb.append(line);
            }
            String rst = sb.toString();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = mapper.readValue(rst, Map.class);
            System.out.println("Telegram Message : " + ((Map<String, Object>) map.get("result")).get("text"));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) try {
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

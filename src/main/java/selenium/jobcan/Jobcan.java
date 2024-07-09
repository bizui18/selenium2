package selenium.jobcan;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import selenium.enums.MyProperties;

public class Jobcan {
	WebDriver driver;
	JavascriptExecutor js;
	WebDriverWait waiter;

	String chromePath;
	Map<String,String> properties;
	public Jobcan(WebDriver webDriver,Map<String,String> properties) throws Exception {
		this.driver = webDriver;
		this.js = (JavascriptExecutor) webDriver;
		this.waiter = new WebDriverWait(driver, Duration.of(3000, ChronoUnit.MILLIS));
		this.properties = properties;
	}
	
	public static void main(String[] args) throws Exception {

		String properiesPath = "d:/dot.properties";
		
        ZonedDateTime now = ZonedDateTime.now();
        System.out.println("=================================================================");
        System.out.println("Job Started!!!     "+now.toString());
        
        Map<String, String> map = MyUtils.loadProperties(properiesPath);
        
        String[] holidays = MyProperties.HOLIDAY.getValue(map).replaceAll(" ", "").split(","); // 공백 제거 후 쪼갬
		boolean show = MyProperties.SHOW.getBooleanValue(map);
		boolean sleepYN = MyProperties.SLEEP.getBooleanValue(map);
        String today = now.format(DateTimeFormatter.ofPattern("MMdd"));
		for (String holiday : holidays) {
			if(today.equals(holiday)){
				System.out.println("\t\t\tit is happy holiday!!!");
				return;
			}
		}

		WebDriverManager manager = new WebDriverManager();
		manager.setChromePath();
		manager.downloadDriver();
				
		Jobcan jobcan = new Jobcan(MyUtils.getWebDriver(show),map);
		
		// random timer		
		if(sleepYN){
			Random random = new Random();
			int sleep = (random.nextInt(300) + 1) * 1000;
			System.out.println("sleep time : "+sleep);
			MyUtils.sleep(sleep);
		}
		
		jobcan.excute();
		System.out.println("=================================================================\n");
	}

	public void excute() {
		driver.get(MyProperties.URL.getValue(properties));
		driver.manage().window().maximize();
		login(driver);
		
		boolean click = MyProperties.CLICK.getBooleanValue(properties);
		boolean quit = MyProperties.QUIT.getBooleanValue(properties);
		// 체크인/아웃 페이지 이동
		driver.findElement(By.cssSelector("#jbc-app-links > ul > li:nth-child(3) > a")).click();

		String mainWindow = driver.getWindowHandle();
		for (String window : driver.getWindowHandles()) {
			if (mainWindow.equals(window))
				continue;
			driver.switchTo().window(window);
			MyUtils.sleep(3000);
			String time = driver.findElement(By.cssSelector("#clock")).getText();
			int i = 0;
			while ("00:00:00".equals(time) && i++ < 3) {
				time = driver.findElement(By.cssSelector("#clock")).getText();
				MyUtils.sleep(1000);
			}
			System.out.println(time);
			WebElement registerBtn = driver.findElement(By.cssSelector("#adit-button-push"));
			
			if(click){
				registerBtn.click();
				System.out.println(registerBtn.getText() + "clicked !!!");
			}
		}

		if(quit){
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

}

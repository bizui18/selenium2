package selenium.jobcan;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;

public class Jobcan {
	WebDriver driver;
	JavascriptExecutor js;

	WebDriverWait waiter;

	String chromePath;

	String id = "jhchoi01@uracle.co.kr";
	String pw = "1q2w3e4r";

	public Jobcan(WebDriver webDriver) {
		this.driver = webDriver;
		this.js = (JavascriptExecutor) webDriver;
		this.waiter = new WebDriverWait(driver, Duration.of(3000, ChronoUnit.MILLIS));

	}

	public static void main(String[] args) throws Exception {

		System.out.println("=================================================================");
		System.out.println("Job Started!!!     " + ZonedDateTime.now().toString());

		Map<String, String> map = MyUtils.loadProperties("d:/jobcan.properties");
		
		String rM = map.get("restMonth"); 
		String rD = map.get("restDay");
		String[] dayList = rD.split(",");
		System.out.println("rest M D : " + rM + " / " + Arrays.toString(dayList));
		
		LocalDate now = LocalDate.now();
		int month = now.getMonthValue();
		int day = now.getDayOfMonth();
		System.out.println("Today : " + month + " / " + day);
		
		boolean checkDay = true;
		for (String restDay : dayList) {
			System.out.println("M : " + rM +" / "+ Integer.toString(month));
			System.out.println("D : " + restDay + " / " + Integer.toString(day));
			
			if(rM.equals(Integer.toString(month)) && restDay.equals(Integer.toString(day))) {
				checkDay = false;
			}else {
			}
		}
		
		System.out.println("checkDay : " + checkDay);
		checkDay = false;
		if(checkDay) {
			// System.out.printf("id : %s pw : %s \n",args[0],args[1]);
			WebDriverManager manager = new WebDriverManager();
			manager.setChromePath();
			
			// manager.downloadDriver("d:/chromedriver/");
			manager.downloadDriver();
			
			System.setProperty("webdriver.chrome.driver",
					String.format("%s%s", manager.getChromeDriverPath(), "/chromedriver.exe"));
			
			Jobcan jobcan = new Jobcan(MyUtils.getWebDriver(false));
			
			// random timer
			Random random = new Random();
			
			int sleep = random.nextInt(10) + 1 * 1000;
			System.out.println(sleep);
			MyUtils.sleep(sleep);
			
			jobcan.excute();
		}
		System.out.println("=================================================================\n");
	}

	public void excute() {
		driver.get("https://id.jobcan.jp/users/sign_in?lang=ko");
		driver.manage().window().maximize();
		login(driver);

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
			System.out.println(registerBtn.getText());
			// registerBtn.click();
		}

		driver.quit();
	}

	public void login(WebDriver driver) {
		// TODO Auto-generated method stub
		driver.findElement(By.cssSelector("#user_email")).sendKeys(id);
		driver.findElement(By.cssSelector("#user_password")).sendKeys(pw);
		driver.findElement(By.cssSelector("#login_button")).click();
		MyUtils.sleep(500);
	}

}

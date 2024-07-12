package selenium.jobcan;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import selenium.enums.MyProperties;
import selenium.enums.OS;

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

		// 1. properties loading and check date
		String properiesPath = "d:/dot.properties";
		// String properiesPath = args[0];
		
        ZonedDateTime now = ZonedDateTime.now();
        System.out.println("=================================================================");
        System.out.println("Job Started!!!\n"+now.getDayOfWeek().toString() +"\t"+ now.toString());
        
        Map<String, String> map = MyUtils.loadProperties(properiesPath);

        String[] holidays = MyProperties.HOLIDAY.getValue(map).replaceAll(" ", "").split(","); // 공백 제거 후 쪼갬
		boolean show = MyProperties.SHOW.getBooleanValue(map);
		boolean sleepYN = MyProperties.SLEEP.getBooleanValue(map);
        String today = now.format(DateTimeFormatter.ofPattern("MMdd"));
		int dayOfWeek = now.getDayOfWeek().getValue();
		
		// check day of week which is Saturday or Sunday
		if(dayOfWeek == DayOfWeek.SATURDAY.getValue() || dayOfWeek == DayOfWeek.SUNDAY.getValue()){
			System.out.println("\t\t\tit is " + DayOfWeek.of(dayOfWeek).toString());
			return;
		}
		// check holiday which written in properties file.
		for (String holiday : holidays) {
			if(today.equals(holiday)){
				System.out.println("\t\t\tit is happy holiday!!!");
				return;
			}
		}

		if(!OS.get().equals(OS.Window))show = false;
		System.setProperty("webdriver.chrome.driver", map.get("driver"));

		Jobcan jobcan = new Jobcan(MyUtils.getWebDriver(show),map);
		
		//3. random timer		
		if(sleepYN){
			Random random = new Random();
			int sleep = (random.nextInt(300) + 1) * 1000;
			System.out.println("sleep time : "+sleep);
			MyUtils.sleep(sleep);
		}
		
		//4. web page works
		jobcan.excute();
		System.out.println("=================================================================\n");
	}
	public List<String> openWindows(){
		WebElement sslButton = driver.findElement(By.cssSelector("#jbc-app-links > ul > li:nth-child(3) > a"));
		sslButton.click();		
		
		for (String wind : driver.getWindowHandles()) {
			if(driver.getWindowHandle().equals(wind))continue;
			driver.switchTo().window(wind);
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
			return t.findElement(By.cssSelector("#search-result")) !=null;
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

	public boolean aditCheck(WebDriver page){
		boolean flag = false;
		String nowStr = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HHmm"));
		int now = Integer.parseInt(nowStr);
		List<WebElement> list = page.findElements(By.cssSelector("#logs-table > div > table > tbody > tr"));
		
		boolean on = false;
		list.forEach(t -> {
			System.out.println(t.findElement(By.cssSelector("td:nth-child(1)")).getText());
		});
		if(now <=1000 && list.size() == 0 ){
			// 출근 가능
		}else if(1700 <= now){
			// 출근 표시가 있고 퇴근 표시가 없어야 한다.
		}else{

		}

		return flag;
	}
	public boolean attendanceCheck(WebDriver page){
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

		System.out.println(el.getText());
		System.out.println("time now : "+now);

		boolean flag = false;
		if(now <=1000 && now <= on &&"".equals(real_on)){
			// 출근 가능
			System.out.println("now it's time to work");
			flag = true;
		}else if(1700<=now && off <= now && !"".equals(real_on) && "".equals(real_off)){
			// 퇴근 가능
			System.out.println("now you are free to go home");
			flag = true;
		}else{
			// 출퇴근 가능 시간이 아님
			System.out.println("cannot check both get_on and get_off");
		}
		return flag;


		
	}
	public void buttonClick(WebDriver page){
		boolean click = MyProperties.CLICK.getBooleanValue(properties);
		String time = page.findElement(By.cssSelector("#clock")).getText();
			int i = 0;
			while ("00:00:00".equals(time) && i++ < 3) {
				time = page.findElement(By.cssSelector("#clock")).getText();
				MyUtils.sleep(1000);
			}
			System.out.println(time);
			WebElement registerBtn = page.findElement(By.cssSelector("#adit-button-push"));
			
			if(click){
				registerBtn.click();
				System.out.println(registerBtn.getText() + "clicked !!!");
			}
	}

	public void excute() {
		driver.get(MyProperties.URL.getValue(properties));
		driver.manage().window().maximize();
		login(driver);
		

		// 사용자 사번 U00000
		WebElement workerNumber = driver.findElement(By.cssSelector("body > div.wrapper > div > section.content > div > div > div:nth-child(1) > div.box-body > table > tbody > tr:nth-child(3) >td"));
		System.out.println("uracle id => "+workerNumber.getText());
		
		// 0 main, 1 employee, 2 attendance
		List<String> windows = openWindows();
		
		boolean okay = attendanceCheck(driver.switchTo().window(windows.get(2)));
		
		if(okay){
			buttonClick(driver.switchTo().window(windows.get(1)));
		}
		
		boolean quit = MyProperties.QUIT.getBooleanValue(properties);
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

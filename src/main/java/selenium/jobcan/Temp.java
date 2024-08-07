package selenium.jobcan;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Temp {
    public static void main(String[] args) throws Exception {
        System.out.println("asf");
        System.setProperty("webdriver.chrome.driver", "D:/chromedriver-win64/chromedriver.exe");
        temp();
        WebDriver driver = new ChromeDriver();
        driver.get("www.naver.com");
    }

    public static void temp() throws Exception{
        Map<String, String> properties = MyUtils.loadProperties("d:/dot.properties");

        String Token = properties.get("token");
		String chat_id = properties.get("chatId");
		
		BufferedReader in = null;
		
		 try {
			 URL obj = new URL("https://api.telegram.org/bot" + Token + "/sendmessage?chat_id=" + chat_id + "&text=" + "123"); // 호출할 url
			 
			 HttpURLConnection con = (HttpURLConnection)obj.openConnection();
			 con.setRequestMethod("GET");
			 in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			 
			 String line = "";
			 StringBuilder sb = new StringBuilder();
			 while((line = in.readLine()) != null) { // response를 차례대로 출력
				 sb.append(line);
			 }
			 String rst = sb.toString();
			 ObjectMapper mapper = new ObjectMapper();
			 Map<String, Object> map = mapper.readValue(rst, Map.class);
			 System.out.println("Telegram Message : " + ((Map<String,Object>) map.get("result")).get("text"));
			 
		 } catch(Exception e) {
			 e.printStackTrace();
		 } finally {
			 if(in != null) try { in.close(); } catch(Exception e) { e.printStackTrace(); }
		 }
    }
}

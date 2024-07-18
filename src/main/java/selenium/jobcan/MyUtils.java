package selenium.jobcan;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class MyUtils {
   
   public static WebDriver getWebDriver(){
      // if("window".equals(System.getenv("os"))){
         // }else{
            // }
      System.setProperty("webdriver.chrome.driver", "D:/chromedriver-win64/chromedriver.exe");
      // System.setProperty("webdriver.chrome.driver", "/root/selenium/chromedriver");

      ChromeOptions options = new ChromeOptions();
      options.setCapability("acceptInsecureCerts", true);
      
      // options.setCapability("ignoreProtectedModeSettings", true);
      options.addArguments("--start-maximized");

      

      options.addArguments("enable-automation"); // https://stackoverflow.com/a/43840128/1689770
      options.addArguments("--headless"); // only if you are ACTUALLY running headless
      options.addArguments("--no-sandbox"); //https://stackoverflow.com/a/50725918/1689770
      options.addArguments("--disable-dev-shm-usage"); //https://stackoverflow.com/a/50725918/1689770
      options.addArguments("--disable-browser-side-navigation"); //https://stackoverflow.com/a/49123152/1689770
      options.addArguments("--disable-gpu"); //https://stackoverflow.com/questions/51959986/how-to-solve-selenium-chromedriver-timed-out-receiving-message-from-renderer-exc

      return new ChromeDriver(options);
   }
   public static WebDriver getWebDriver(boolean show){ // true 

      ChromeOptions options = new ChromeOptions();
      options.setCapability("acceptInsecureCerts", true);
      options.addArguments("--start-maximized");
      
      if(!show){
         options.addArguments("enable-automation"); // https://stackoverflow.com/a/43840128/1689770
         options.addArguments("--headless"); // only if you are ACTUALLY running headless
         options.addArguments("--no-sandbox"); //https://stackoverflow.com/a/50725918/1689770
         options.addArguments("--disable-dev-shm-usage"); //https://stackoverflow.com/a/50725918/1689770
         options.addArguments("--disable-browser-side-navigation"); //https://stackoverflow.com/a/49123152/1689770
         options.addArguments("--disable-gpu"); //https://stackoverflow.com/questions/51959986/how-to-solve-selenium-chromedriver-timed-out-receiving-message-from-renderer-exc
      }
      
      ChromeDriver driver = null;
      try {
         driver = new ChromeDriver(options);
      } catch (Exception e) {
         // TODO: handle exception
         System.out.println(e.getMessage());
      }
      return driver;
   }
   public static String fileDownload(String spec,String outputDir){
      InputStream is = null;
      FileOutputStream os = null;
      String fileName = "";
        try{
            URL url = new URL(spec);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();

            // System.out.println("responseCode " + responseCode);

            // Status 가 200 일 때
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String disposition = conn.getHeaderField("Content-Disposition");
                String contentType = conn.getContentType();
                
                // 일반적으로 Content-Disposition 헤더에 있지만 
                // 없을 경우 url 에서 추출해 내면 된다.
                if (disposition != null) {
                    String target = "filename=";
                    int index = disposition.indexOf(target);
                    if (index != -1) {
                        fileName = disposition.substring(index + target.length() + 1);
                    }
                } else {
                    fileName = spec.substring(spec.lastIndexOf("/") + 1);
                }

                // System.out.println("Content-Type = " + contentType);
                // System.out.println("Content-Disposition = " + disposition);
               //  System.out.println("fileName = " + fileName);
            File saveDir = new File(outputDir);
            if(!saveDir.exists()){
               saveDir.mkdirs();
            }
                is = conn.getInputStream();
                os = new FileOutputStream(new File(outputDir, fileName));

                final int BUFFER_SIZE = 4096;
                int bytesRead;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.close();
                is.close();
                // System.out.println("File downloaded");
            } else {
                System.out.println("No file to download. Server replied HTTP code: " + responseCode);
            }
            conn.disconnect();
        } catch (Exception e){
            System.out.println("An error occurred while trying to download a file.");
            e.printStackTrace();
            try {
                if (is != null){
                    is.close();
                }
                if (os != null){
                    os.close();
                }
            } catch (IOException e1){
                e1.printStackTrace();
            }
        }
      return fileName;
   }
   
   public static String fileDownload(String spec){
        String outputDir = "D:/temp";
        return fileDownload(spec, outputDir);
   }
   public static Date toDate(SimpleDateFormat format,String str) throws ParseException {
      return format.parse(str);
   }
   
   public static Map<String,String> loadProperties(String fileUrl) throws Exception {
      Properties properties = new Properties();
      properties.load(new BufferedInputStream(new FileInputStream(new File(fileUrl))));
      
      Map<String,String> propertiesMap = new HashMap<>();

      System.out.println();
      System.out.println("\t\t[ Checking your properties file ]");
      for (Object ob : properties.keySet()) {
         System.out.println("\t\t" + ob + " : \t "
               + new String(properties.getProperty(ob.toString()).getBytes("ISO-8859-1"), "utf-8"));
         propertiesMap.put((String) ob,
               new String(properties.getProperty(ob.toString()).getBytes("ISO-8859-1"), "utf-8"));
      }
      System.out.println();
      
      return propertiesMap;
   }
   private String convertEncoding(String iso) {
      String a = "";
      try {
         a = new String(iso.getBytes("ISO-8859-1"), "utf-8");
      } catch (UnsupportedEncodingException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return a;
   }

   public static void sleep(long mil) {
      try {
         Thread.sleep(mil);
      } catch (InterruptedException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   public static void deleteFiles(File[] files){
      Arrays.stream(files).filter(t -> {
         return t!=null;
      }).forEach(File::deleteOnExit);
   }
   public static void deleteFiles(File[][] file2d){
      for (File[] files : file2d) {
         Arrays.stream(files).filter(t -> {
            return t!=null;
         }).forEach(File::deleteOnExit);
      }
   }
   public static List<List<String>> divideList(List<String> list, int n ){
      if(n<=0) n =1;
      List<List<String>> result = new ArrayList<>();
      for (int i = 0; i < n; i++) {
         result.add(new ArrayList<>());
      }
      int size = list.size();
      int num=0;
      a:while(num < size){
         for (int i = 0; i < n; i++) {
            result.get(i).add(list.get(num++));
            if(num == size) {
               break a;
            }
         }
      }
      System.out.println("");
      return result;
   }

   public static void copy(FileInputStream is, FileOutputStream os) {
      byte[] b = new byte[1024];
      int readData = 0;
      try {
         while((readData = is.read(b))>0) {
            os.write(b, 0, readData);
         }
      } catch (Exception e) {
         // TODO: handle exception
      }finally {
         try {
            is.close();
            os.close();
         } catch (Exception e2) {
            // TODO: handle exception
            System.out.println("close fail!");
         }
      }
   }

   public static BufferedImage getBufferedImage(File file) {
      BufferedImage tempImage=null;
      try {
         tempImage = ImageIO.read(file);
      } catch (Exception e) {
         // TODO: handle exception
         e.printStackTrace();
         System.out.println("image read fail");
      }
      return tempImage;
   }

   public static void unCompressZip(String filepath, String zipName) throws Exception{
     File zipFile = new File(filepath, zipName);

     BufferedInputStream in = new BufferedInputStream(new FileInputStream(zipFile));
     ZipInputStream zipInputStream = new ZipInputStream(in);
     ZipEntry zipEntry = null;
     while((zipEntry = zipInputStream.getNextEntry()) != null){
         int length = 0;
       File save = new File(filepath,zipEntry.getName());
       if(!save.getParentFile().exists()) save.getParentFile().mkdirs();
         BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(save));
         while((length = zipInputStream.read()) != -1){
             out.write(length);
         }

       out.close();
         zipInputStream.closeEntry();
       }
      zipInputStream.close();
   }
}

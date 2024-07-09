package selenium.jobcan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

// only for chrome...
public class WebDriverManager {
    private String jsonUrl;
    private String defaultChromeDriverDir;
    private String chromeDriverPath;
    
    // 초기화...
    public WebDriverManager(){
        jsonUrl = "https://github.com/GoogleChromeLabs/chrome-for-testing/blob/main/data/latest-versions-per-milestone-with-downloads.json";
        defaultChromeDriverDir = String.format("d:/chromeDriver/%s/", ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss")));
    }

    private String getChromeVersion(){

        String chromePath =System.getProperty("chrome.path");
        if(chromePath == null){
            setChromePath();
            chromePath = System.getProperty("chrome.path");
        }
        File file = new File(chromePath);
        String version = file.listFiles(pathname -> pathname.isDirectory() && !"".equals(pathname.getName().replaceAll("[^1-9]","")))[0].getName();

        return version;
    }
    // chrome driver download url compilation
    private Map<String,String> getDownloadUrl() throws IOException{
        Map<String,String> map = new HashMap<>();

        String dir = "d:/";
        String fileName = MyUtils.fileDownload(jsonUrl, dir);
        
        File file = new File(dir,fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

        StringBuilder sb = new StringBuilder();
        String line = "";
        while((line = reader.readLine())!=null){
            sb.append(line).append("\n");
        }

        // enum으로 수정해야 한다.

        String suffix = "chromedriver-win64";
        String regex = String.format("\\bhttps://[\\w.-]+(?:\\.[\\w.-]+)+(?:/[^\\s]*)?%s\\.zip\\b", suffix);

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sb.toString());

        while(matcher.find()){
            String key = matcher.group().replaceAll("[^1-9]", "").substring(0, 3);
            map.put(key, matcher.group());
        }

        if(reader !=null){
            reader.close();
            file.deleteOnExit();
        }
        return map;
    }

    public void downloadDriver(String dir) throws Exception{
        this.chromeDriverPath = dir;

        Map<String,String> urlMap = getDownloadUrl();
        String version = getChromeVersion().substring(0, 3);

        String path = MyUtils.fileDownload(urlMap.get(version), dir);
        System.out.println(path);
        
        new File(path).mkdirs();

        MyUtils.unCompressZip(dir,path);
        Files.deleteIfExists(Path.of(dir,path));
        mvFromDir(dir);
        System.setProperty("webdriver.chrome.driver",String.format("%s%s", dir,"/chromedriver.exe"));

    }

    public void downloadDriver() throws Exception{
        downloadDriver(defaultChromeDriverDir);
    }
    
    public String getChromeDriverPath(){
        return chromeDriverPath;
    }

    public void recursiveFileFinder(File parent, List<File> result){
        File[] list = parent.listFiles();
        for (File file : list) {
            if(file.isDirectory()){
                recursiveFileFinder(file, result);
            }else if(file.isFile()){
                result.add(file);
            }
        }
    }
    public void mvFromDir(String path){
        File file = new File(path);
        if(!file.exists() || !file.isDirectory()) return;
        if(!path.endsWith("/") || !path.endsWith("\\")){
            path += "/";
        }
        List<File> list = new ArrayList<>();
        recursiveFileFinder(file, list);
        
        list.forEach(t -> {
            try {
                MyUtils.copy(new FileInputStream(t), new FileOutputStream(file.getPath()+"/"+t.getName()));
                
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        list.forEach(t -> {
            t.deleteOnExit();
        });
        
    }

    public void setChromePath(){
      System.setProperty("chrome.path", "C:\\Program Files\\Google\\Chrome\\Application");
   }

   public void setChromePath(String path){
      System.setProperty("chrome.path", path);
   }
    
}

package selenium.jobcan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import selenium.enums.OS;

// only for chrome...
public class WebDriverManager {
    private String jsonUrl;
    private String defaultChromeDriverDir;
    private String chromeDriverPath;
    private OS os;
    // 초기화...
    public WebDriverManager(){
        os = OS.get();
        jsonUrl = "https://github.com/GoogleChromeLabs/chrome-for-testing/blob/main/data/latest-versions-per-milestone-with-downloads.json";
        defaultChromeDriverDir = String.format("%s%s",os.getDir(), "/"+ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss")));
    }

    private String getChromeVersion(){
        String version = "";
        if(os.equals(OS.Window)){

            String chromePath =System.getProperty("chrome.path");
            if(chromePath == null){
                setChromePath();
                chromePath = System.getProperty("chrome.path");
            }
            File file = new File(chromePath);
            version = file.listFiles(pathname -> pathname.isDirectory() && !"".equals(pathname.getName().replaceAll("[^1-9]","")))[0].getName();
        }else if(os.equals(OS.Linux)){
            try {
                Process p = Runtime.getRuntime().exec("chrome --version");
                byte[] bytes = p.getInputStream().readAllBytes();
                if(bytes == null){
                    bytes = new byte[0];
                }
                version = new String(bytes).replaceAll("[^1-9]","");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return version;
    }
    // chrome driver download url compilation
    private Map<String,String> getDownloadUrl() throws IOException{
        Map<String,String> map = new HashMap<>();

        String dir = os.getDir();
        String fileName = MyUtils.fileDownload(jsonUrl, dir);
        
        File file = new File(dir,fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

        StringBuilder sb = new StringBuilder();
        String line = "";
        while((line = reader.readLine())!=null){
            sb.append(line).append("\n");
        }

        // enum으로 수정해야 한다.

        // String suffix = "chromedriver-win64";
        String suffix = os.getSuffix();
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

        String zipFile = MyUtils.fileDownload(urlMap.get(version), dir);
        System.out.printf("chrome driver save dir ==>\t%s %s\n",dir,"/"+zipFile);
        
        new File(dir).mkdirs();

        MyUtils.unCompressZip(dir,zipFile);
        Files.deleteIfExists(Path.of(dir,zipFile));
        mvFromDir(dir);
        
        setChromeDriver(String.format("%s%s", dir,"/"+os.getExe()));
    }
    public void setChromeDriver(){
        System.setProperty("webdriver.chrome.driver",String.format("%s%s", this.chromeDriverPath,"/"+os.getExe()));
    }
    public void setChromeDriver(String path){
        System.setProperty("webdriver.chrome.driver",path);
    }
    public void deleteRecursive(File root) throws IOException{
        for (File file : root.listFiles(t-> t.isFile())) {
            Files.deleteIfExists(file.toPath());
        }
        File[] list = root.listFiles(t ->t.isDirectory()) ;
        for (File dir : list) {
            deleteRecursive(dir);
        }
        Files.deleteIfExists(root.toPath());
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
            // t.deleteOnExit(); 즉시 삭제가 아닌것 같다.
            try {
                Files.deleteIfExists(t.toPath()); // 즉시 삭제
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        
    }

    // 윈도우에서 크롬 버전을 확인하기 위해서 사용한다.
    // 이때 확인된 크롬 버전으로 driver를 다운 받는다.
    public void setChromePath(){
        System.setProperty("chrome.path", "C:/Program Files/Google/Chrome/Application");
    }

    public void setChromePath(String path){
        System.setProperty("chrome.path", path);
    }
    
}

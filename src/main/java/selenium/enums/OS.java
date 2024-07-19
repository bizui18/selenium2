package selenium.enums;

import lombok.Getter;

@Getter
public enum OS {
    Linux("linux","/root/chromeDriver","chromedriver","chromedriver-linux64"),
    Window("windows","d:/chromeDriver","chromedriver.exe","chromedriver-win64"),
    Mac("unix","","",""), // 테스트 기기 없음...
    ;
    
    private String osName;
    private String dir;
    private String exe;
    private String suffix;
    OS(String name, String dir, String exe, String suffix) {
		//TODO Auto-generated constructor stub
        this.osName = name;
        this.dir = dir;
        this.exe = exe;
        this.suffix = suffix;
	}

    // default 는 Linux
    public static OS get(){
        String osStr = System.getProperty("os.name").toLowerCase();
        
        for (OS os : OS.values()) {
            if(osStr.contains(os.getOsName())){
                return os;
            }
        }

        return OS.Linux;
    }
    
}

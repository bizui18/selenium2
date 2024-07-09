package selenium.enums;

import java.util.Map;

public enum MyProperties {
    ID("id"),
    PW("pw"),
    HOLIDAY("holiday"),
    SHOW("show"),
    QUIT("quit"),
    URL("url"),
    CLICK("click"),
    SLEEP("sleep"),
    ;

    private String key;

    private MyProperties(String key) {
        this.key = key;
    }

    public String getKey(){
        return this.key;
    }
    public String getValue(Map<String,String> properties){
        return properties.getOrDefault(this.key,"");
    }
    public boolean getBooleanValue(Map<String,String> properties){
        return properties.getOrDefault(this.key,"").toLowerCase().equals("y");
    }
}

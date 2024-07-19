package selenium.jobcan;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class MyWaiter {
    public static boolean wait(WebDriver driver,By by,int trial){
        if(trial < 3){
            trial = 3;
        }
        WebElement el = null;
        
        for (int i = 0; i < trial; i++) {
            try {
                if(el == null){
                    el = driver.findElement(by);
                }else{
                    return true;
                }
            } catch (NoSuchElementException e) {
                // TODO: handle exception
                System.out.printf("waiting element is created, [%s / %s]\n",i+1,trial);
            }
            MyUtils.sleep(2000);
        }
        return el != null;
    }
}

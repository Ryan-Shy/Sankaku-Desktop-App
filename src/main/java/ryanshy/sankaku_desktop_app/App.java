package ryanshy.sankaku_desktop_app;

import ryanshy.sankaku_desktop_app.util.ZipUtil;

public class App {
	
	public static String VERSION_TAG = "v0.0.1-alpha";
	
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());
        /*
        ZipUtil.unzip("./zip.zip", "../lib/");
        System.out.println("Updated");
        */
    }
    
    
}

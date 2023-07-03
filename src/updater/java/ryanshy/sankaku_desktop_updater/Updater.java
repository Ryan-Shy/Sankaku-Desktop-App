package ryanshy.sankaku_desktop_updater;

import java.io.IOException;

public class Updater {

	public static void main(String[] args) {
		// collect args
		boolean cleanUpdate = false;
		for(int i = 0; i < args.length; i++) {
			String arg = args[i];
			if(arg.equals("--clean")) cleanUpdate = true;
		}
		// run updater
		UpdateHelper updater = new UpdateHelper();
        boolean updateAvailable = updater.isUpdateAvailable();
        System.out.println(updateAvailable);
        
        updater.update(cleanUpdate);
        // start App
        try {
			Runtime.getRuntime().exec("cmd /c start \"\" .\\bin\\"+UpdateHelper.REPO+".bat");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

}

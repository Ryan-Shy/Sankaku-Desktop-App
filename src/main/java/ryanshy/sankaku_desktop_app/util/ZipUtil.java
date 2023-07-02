package ryanshy.sankaku_desktop_app.util;

import java.io.IOException;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class ZipUtil {
	public static void unzip(String source, String destination, String password) {
		try (ZipFile zipFile = new ZipFile(source)) {
			if(zipFile.isEncrypted()) {
				zipFile.setPassword(password.toCharArray());
			}
			zipFile.extractAll(destination);
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void unzip(String source, String destination) {
		unzip(source, destination, "");
	}
	
}

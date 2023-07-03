package ryanshy.sankaku_desktop_updater;

import java.io.IOException;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class ZipUtil {
	public static boolean unzip(String source, String destination, String password) {
		try (ZipFile zipFile = new ZipFile(source)) {
			if(zipFile.isEncrypted()) {
				zipFile.setPassword(password.toCharArray());
			}
			zipFile.extractAll(destination);
			return true;
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean unzip(String source, String destination) {
		return unzip(source, destination, "");
	}
	
}

package ryanshy.sankaku_desktop_updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpdateHelper {

	//TODO replace this with better solution
	public static final String VERSION_TAG = "v0.0.1-alpha";
	public static final String VERSION_TAG_KEY = "versiontag";
	
	// constants
	public static final String REPO = "Sankaku-Desktop-App";
	public static final String OWNER = "Ryan-Shy";
	
	public static final String ASSET_NAME = REPO+".zip";
	public static final String DOWNLOAD_DIR = System.getenv("LOCALAPPDATA")+"Low"+"/ryanshy/temp/";
	private static final int CONNECT_TIMEOUT = 10000;
	private static final int READ_TIMEOUT = 10000;
	
	// github api keys
	public static final String TAG_NAME_KEY = "tag_name";
	public static final String ASSETS_KEY = "assets";
	public static final String ASSET_NAME_KEY = "name";
	public static final String DOWNLOAD_URL_KEY = "browser_download_url";
	
	
	// variables
	private HttpClient client;
	private JsonNode latestRelease;
	private Properties updateProps;
	
	public UpdateHelper() {
		this.client = HttpClient.newHttpClient();
		this.latestRelease = null;
		this.updateProps = new Properties();
	}
	
	public boolean isUpdateAvailable() {
    	String latestTagName = getLatestReleaseTag();
    	if(latestTagName == null) return false;
    	
    	String version_tag;
		try {
			version_tag = getCurrentReleaseTag();
			if(version_tag == null || version_tag.isBlank()) return false;
	    	return !version_tag.equals(latestTagName);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return false;
    }
	
	private String getLatestReleaseTag() {
		if(latestRelease == null) getLatestRelease();
		if(latestRelease == null) return null;
		JsonNode tagNameNode = latestRelease.get(TAG_NAME_KEY);
		String tagName = tagNameNode.asText();
		if(tagName == null || tagName.isEmpty()) return null;
		return tagName;
	}
	
	private JsonNode getLatestRelease() {
		String uri = "https://api.github.com/repos/"+OWNER+"/"+REPO+"/releases/latest";
		
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(uri))
				.header("Accept", "application/vnd.github+json")
				.GET()
				.build();
		HttpResponse<String> response;
		try {
			response = client.send(request, BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		if(response.statusCode() != 200) {
			return null;
		}
		String body = response.body();
		ObjectMapper mapper = new ObjectMapper();
		try {
			latestRelease = mapper.readTree(body);
			return latestRelease;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void loadProperties() throws IOException, URISyntaxException {
		String rootPath = (new File(Updater.class.getProtectionDomain().getCodeSource().getLocation().toURI())).getParent();
		FileInputStream propertiesFile = FileUtils.openInputStream(new File(rootPath+"/updater.properties"));
		updateProps.load(propertiesFile);
	}
	
	private void storeProperties() throws IOException, URISyntaxException {
		String rootPath = (new File(Updater.class.getProtectionDomain().getCodeSource().getLocation().toURI())).getParent();
		FileOutputStream propertiesFile = FileUtils.openOutputStream(new File(rootPath+"/updater.properties"));
		updateProps.store(propertiesFile, null);
	}
	
	private String getCurrentReleaseTag() throws IOException, URISyntaxException {
		loadProperties();
		return updateProps.getProperty(VERSION_TAG_KEY);
	}
	
	private void setCurrentReleaseTag(String newTag) {
		try {
			loadProperties();
			updateProps.setProperty(VERSION_TAG_KEY, newTag);
			storeProperties();
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public void update(boolean cleanUpdate) {
		if(!isUpdateAvailable()) return;
		if(latestRelease == null) getLatestRelease();
		if(latestRelease == null) return;
		
		// get assets
		if(!latestRelease.has(ASSETS_KEY) || !latestRelease.get(ASSETS_KEY).isArray()) return;
		JsonNode assets = latestRelease.get(ASSETS_KEY);
		String downloadUrl = null;
		for(int i = 0; i < assets.size(); i++) {
			JsonNode asset = assets.get(i);
			if(asset.has(ASSET_NAME_KEY) && ASSET_NAME.equals(asset.get(ASSET_NAME_KEY).asText())) {
				if(asset.has(DOWNLOAD_URL_KEY)) {
					downloadUrl = asset.get(DOWNLOAD_URL_KEY).asText();
					break;
				}
			}
		}
		if(downloadUrl == null || downloadUrl.isBlank()) return;
		
		// download Files	
		try {
			// clear download folder
			FileUtils.deleteDirectory(new File(DOWNLOAD_DIR));
			// download
			FileUtils.copyURLToFile(
					new URL(downloadUrl), 
					new File(DOWNLOAD_DIR+ASSET_NAME),
					CONNECT_TIMEOUT,
					READ_TIMEOUT);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		// unzip Files
		if(!ZipUtil.unzip(DOWNLOAD_DIR+ASSET_NAME, DOWNLOAD_DIR)) return;
		
		// replace Files
		try {
			FileUtils.delete(new File(DOWNLOAD_DIR+ASSET_NAME));
			File libFolder = new File(DOWNLOAD_DIR+REPO+"/lib");
			if(cleanUpdate) FileUtils.deleteDirectory(new File("./lib"));
			FileUtils.copyDirectory(libFolder, new File("./lib"));
			FileUtils.deleteDirectory(libFolder);
			File binFolder = new File(DOWNLOAD_DIR+REPO+"/bin");
			if(cleanUpdate) FileUtils.deleteDirectory(new File("./bin"));
			FileUtils.copyDirectory(binFolder, new File("./bin"));
			FileUtils.deleteDirectory(binFolder);
			FileUtils.deleteDirectory(new File(DOWNLOAD_DIR));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}			
		// update app version tag
		String latestTag = getLatestReleaseTag();
		if(latestTag == null || latestTag.isBlank()) return;
		setCurrentReleaseTag(latestTag);
	}
	
}

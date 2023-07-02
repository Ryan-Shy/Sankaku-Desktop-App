package ryanshy.sankaku_desktop_app.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ryanshy.sankaku_desktop_app.App;

public class UpdateHelper {

	// constants
	public static final String REPO = "Sankaku-Desktop-App";
	public static final String OWNER = "Ryan-Shy";
	
	public static final String ASSET_NAME = "Sankaku-Desktop-App.zip";
	public static final String DOWNLOAD_DIR = "%localappdata%low/ryanshy/temp/";
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
	
	public UpdateHelper() {
		this.client = HttpClient.newHttpClient();
		this.latestRelease = null;
	}
	
	public boolean isUpdateAvailable() {
    	String latestTagName = getLatestReleaseTag();
    	if(latestTagName == null) return false;
    	
    	return !App.VERSION_TAG.equals(latestTagName);
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
	
	public void update() {
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
		
		try {
			FileUtils.copyURLToFile(
					new URL(downloadUrl), 
					new File(DOWNLOAD_DIR+ASSET_NAME),
					CONNECT_TIMEOUT,
					READ_TIMEOUT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//TODO replace and restart
	}
	
}

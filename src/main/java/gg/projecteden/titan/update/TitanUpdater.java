package gg.projecteden.titan.update;

import com.google.gson.Gson;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.utils.Utils;
import net.minecraft.client.MinecraftClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class TitanUpdater {

	public static UpdateStatus updateStatus = UpdateStatus.NONE;
	public static Date buildDate;
	public static final String mcVersion = MinecraftClient.getInstance().getGameVersion();

	public static void checkForUpdates() {
		updateBuildDate();

		Titan.log("Checking for Modrinth update");

		final ModrinthVersion[] modrinthVersionsResponse;
		try {
			modrinthVersionsResponse = getModrinthVersions();
		} catch (Exception ignore) {
			return;
		}
		if (modrinthVersionsResponse == null)
			return;
		List<ModrinthVersion> modrinthVersions = Arrays.stream(modrinthVersionsResponse)
				.filter(version -> Arrays.asList(version.getGame_versions()).contains(mcVersion))
				.toList();

		Titan.log("Found " + modrinthVersions.size() + " possible version" + (modrinthVersions.size() == 1 ? "" : "s"));

		ModrinthVersion modrinthVersion = modrinthVersions.stream()
				.max(Comparator.comparing(ModrinthVersion::getDatePublished))
				.orElse(null);

		if (modrinthVersion == null)
			return;

		if (modrinthVersion.getVersion_number().equals(Titan.version()))
			return;

		if (modrinthVersion.getDatePublished().before(buildDate))
			return;

		Titan.log("Found Modrinth update!");

		updateStatus = UpdateStatus.AVAILABLE;
	}

	private static void updateBuildDate() {
		if (buildDate == null) {
			String date = Utils.getManifestAttribute("Build-Timestamp");
            buildDate = Utils.ISODate(date);
        }
	}

	private static ModrinthVersion[] getModrinthVersions() {
		Timeout timeout = Timeout.ofMilliseconds(5000);
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(timeout)
				.setConnectionRequestTimeout(timeout)
				.build();

		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpGet request = new HttpGet("https://api.modrinth.com/v2/project/" + Titan.MODRINTH_SLUG +"/version");
			request.setConfig(requestConfig);
			request.addHeader("Accept", "application/json");
			request.addHeader("Authorization", Titan.MODRINTH_TOKEN);
			CloseableHttpResponse response = client.execute(request);
			return new Gson().fromJson(EntityUtils.toString(response.getEntity()), ModrinthVersion[].class);
		} catch (Exception e) {
			Titan.log("Error while getting Modrinth version");
			e.printStackTrace();
		}
		return new ModrinthVersion[0];
	}

}

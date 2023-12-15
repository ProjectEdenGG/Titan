package gg.projecteden.titan.update;

import com.google.gson.Gson;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.Utils;
import net.minecraft.client.MinecraftClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TitanUpdater {

	public static UpdateStatus updateStatus = UpdateStatus.NONE;
	public static Date buildDate;
	public static final String mcVersion = MinecraftClient.getInstance().getGameVersion();

	public static void checkForUpdates() {
		updateBuildDate();

		Titan.log("Checking for Modrinth update");

		List<ModrinthVersion> modrinthVersions = Arrays.stream(getModrinthVersions())
				.filter(version -> Arrays.asList(version.getGame_versions()).contains(mcVersion))
				.toList();

		Titan.log("Found " + modrinthVersions.size() + " possible version" + (modrinthVersions.size() == 1 ? "" : "s"));

		ModrinthVersion modrinthVersion = modrinthVersions.stream()
				// Build Date will always be before the modrinth publish date. Check that the new version is at least 2 minutes older than current
				.filter(version -> {
					long diffInMillies = Math.abs(buildDate.getTime() - version.getDatePublished().getTime());
					long diff = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
					return diff >= 2;
				})
				.min(Comparator.comparing(ModrinthVersion::getDatePublished))
				.orElse(null);

		if (modrinthVersion == null)
			return;

		if (buildDate.after(modrinthVersion.getDatePublished()))
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
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpGet request = new HttpGet("https://api.modrinth.com/v2/project/" + Titan.MODRINTH_SLUG +"/version");
			request.addHeader("Accept", "application/json");
			request.addHeader("Authorization", Titan.MODRINTH_TOKEN);
			CloseableHttpResponse response = client.execute(request);
			return new Gson().fromJson(EntityUtils.toString(response.getEntity()), ModrinthVersion[].class);
		} catch (Exception e) {
			Titan.log("Error while getting Modrinth version");
			e.printStackTrace();
		}
		return null;
	}

}

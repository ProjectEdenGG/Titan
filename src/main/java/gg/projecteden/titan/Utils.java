package gg.projecteden.titan;

import com.google.gson.Gson;
import joptsimple.internal.Strings;
import lombok.SneakyThrows;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Utils {

	@SneakyThrows
	public static String bash(String command, File directory) {
		InputStream result = Runtime.getRuntime().exec(command, null, directory).getInputStream();
		StringBuilder builder = new StringBuilder();
		new Scanner(result).forEachRemaining(string -> builder.append(string).append(" "));
		return builder.toString().trim();
	}

	public static boolean isOnEden() {
		final ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();
		if (handler == null)
			return false;

		final String address = handler.getConnection().getAddress().toString();
		if (address == null)
			return false;

		return address.contains("projecteden.gg") || address.contains("51.222.11.194");
	}

	public static String camelCase(String text) {
		if (Strings.isNullOrEmpty(text))
			return text;

		return Arrays.stream(text.replaceAll("_", " ").split(" "))
				.map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
				.collect(Collectors.joining(" "));
	}

	public static <T> T getGitResponse(String get, Class<T> type) {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			HttpGet request = new HttpGet("https://api.github.com/repos/ProjectEdenGG/" + get);
			request.addHeader("Accept", "application/vnd.github+json");
			request.addHeader("User-Agent", "Googlebot/2.1 (+http://www.google.com/bot.html)");
			CloseableHttpResponse response = client.execute(request);
			return new Gson().fromJson(EntityUtils.toString(response.getEntity()), type);
		} catch (Throwable ex) {
			Titan.log("An error occurred while checking git versioning. Rate limit reached?");
			ex.printStackTrace();
		}
		return null;
	}

}

package gg.projecteden.titan.utils;

import com.google.gson.Gson;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.creative.CustomCreativeItem;
import net.minecraft.client.MinecraftClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class NexusAPI {

    public static final Timeout TIMEOUT = Timeout.ofMilliseconds(5000);

    public static <T> T get(String endpoint, Class<T> returnType) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(TIMEOUT)
                .setConnectionRequestTimeout(TIMEOUT)
                .build();

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet("https://mcapi.projecteden.gg/" + endpoint);
            request.setConfig(requestConfig);
            request.addHeader("Accept", "application/json");
            request.setHeader("Accept-Charset", "UTF-8");
            CloseableHttpResponse response = client.execute(request);
            return new Gson().fromJson(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8), returnType);
        } catch (Exception e) {
            Titan.log("Error while getting Custom Blocks");
            e.printStackTrace();
        }
        return null;
    }

    public static CustomCreativeItem[] getItems() {
        CustomCreativeItem[] items = get("titan/creative/items/" + getUUID(), CustomCreativeItem[].class);
        if (items == null)
            return new CustomCreativeItem[0];
        return items;
    }

    public static CustomCreativeItem[] getCategories() {
        CustomCreativeItem[] categories = get("titan/creative/categories/" + getUUID(), CustomCreativeItem[].class);
        if (categories == null)
            return new CustomCreativeItem[0];
        return categories;
    }

    private static String getUUID() {
        UUID uuid = MinecraftClient.getInstance().getSession().getUuidOrNull();
        if (uuid == null)
            return UUID.randomUUID().toString();
        return uuid.toString();
    }

}

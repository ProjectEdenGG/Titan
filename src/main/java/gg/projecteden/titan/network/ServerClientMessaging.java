package gg.projecteden.titan.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import gg.projecteden.titan.Titan;
import gg.projecteden.titan.network.models.PluginMessage;
import gg.projecteden.titan.network.models.Serverbound;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static gg.projecteden.titan.Titan.MOD_ID;

public class ServerClientMessaging {

	public record TitanPacket(String packet) implements CustomPacketPayload {
		private static final Identifier NETWORKING_CHANNEL = Identifier.fromNamespaceAndPath(MOD_ID, "networking");

		public static final CustomPacketPayload.Type<TitanPacket> PACKET_ID = new CustomPacketPayload.Type<>(NETWORKING_CHANNEL);
		public static final StreamCodec<RegistryFriendlyByteBuf, TitanPacket> PACKET_CODEC = StreamCodec.composite(new StreamCodec<ByteBuf, String>() {
			public String decode(ByteBuf byteBuf) {
				Titan.debug("Decoding...");
				Titan.debug("Readable bytes: " + byteBuf.readableBytes());
				byte[] bytes = new byte[byteBuf.readableBytes()];
				byteBuf.readBytes(bytes);
				Titan.debug("Raw: " + Arrays.toString(bytes));
				String string = new String(bytes);
				Titan.debug("String: " + new String(bytes));
				string = string.substring(string.indexOf("{"));
				return string;
			}

			public void encode(ByteBuf byteBuf, String string) {
				Titan.debug("Encoding...");
				Utf8String.write(byteBuf, string, 10000);
			}
		}, TitanPacket::packet, TitanPacket::new);

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return PACKET_ID;
		}

		public String getPacket() {
			return packet;
		}

		public void receive() {
			Titan.debug("Received server message: " + getPacket());
			Titan.debug("Raw: " + packet);
			JsonObject json = GSON.fromJson(getPacket(), JsonObject.class);

			if (json == null || json.isEmpty()) {
				Titan.debug("JSON is empty");
				return;
			}

			int processed = 0;
			for (PluginMessage message : PluginMessage.values()) {
				if (json.has(message.name().toLowerCase())) {
					message.receive(json.getAsJsonObject(message.name().toLowerCase()));
					processed++;
				}
			}
			Titan.debug("Processed %d messages".formatted(processed));
		}
	}

	public final static Gson GSON = new GsonBuilder().create();

	public static final List<Serverbound> toSend = new ArrayList<>();

	public static void send(Serverbound serverbound) {
		if (Minecraft.getInstance() != null && Minecraft.getInstance().getConnection() != null)
			toSend.add(serverbound);
		else
			Titan.debug("Cannot send packets while not online");
	}

	private static void flush() {
		if (toSend.isEmpty()) return;
		if (Minecraft.getInstance() == null || Minecraft.getInstance().getConnection() == null) return;

		Collections.reverse(toSend); // Prefer newer messages

		JsonObject json = new JsonObject();
		toSend.forEach(serverbound -> {
			String type = serverbound.getType().name().toLowerCase();
			Titan.debug("Sending " + type);

			if (json.has(type)) { // Combine like messages
				JsonObject original = json.getAsJsonObject(type);
				JsonObject duplicate = GSON.fromJson(serverbound.getJson(), JsonObject.class);
				duplicate.keySet().forEach(key -> {
					if (original.has(key))
						return;
					original.add(key, duplicate.get(key));
				});
			}
			else
				json.add(serverbound.getType().name().toLowerCase(), GSON.fromJson(serverbound.getJson(), JsonObject.class));
		});

		ClientPlayNetworking.send(new TitanPacket(GSON.toJson(json)));

		toSend.forEach(Serverbound::onSend);
		toSend.clear();
	}

	public static void init() {
		PayloadTypeRegistry.playC2S().register(TitanPacket.PACKET_ID, TitanPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(TitanPacket.PACKET_ID, TitanPacket.PACKET_CODEC);

		ClientPlayNetworking.registerGlobalReceiver(TitanPacket.PACKET_ID, (payload, context) -> {
			context.client().execute(payload::receive);
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> ServerClientMessaging.flush());
	}

}


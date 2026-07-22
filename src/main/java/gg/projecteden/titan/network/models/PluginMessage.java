package gg.projecteden.titan.network.models;

import com.google.gson.JsonObject;
import gg.projecteden.titan.network.ServerClientMessaging;
import gg.projecteden.titan.network.clientbound.BackpackConfig;
import gg.projecteden.titan.network.clientbound.ResetMinigame;
import gg.projecteden.titan.network.clientbound.SaturnUpdate;
import gg.projecteden.titan.network.clientbound.UpdateState;
import gg.projecteden.titan.network.serverbound.Handshake;
import gg.projecteden.titan.network.serverbound.Scroll;
import gg.projecteden.titan.network.serverbound.TitanConfig;
import gg.projecteden.titan.network.serverbound.Versions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public enum PluginMessage {
    HANDSHAKE(Handshake.class),
    SATURN_UPDATE(SaturnUpdate.class),
    TITAN_CONFIG(TitanConfig.class),
    VERSIONS(Versions.class),
    UPDATE_STATE(UpdateState.class),
    RESET_MINIGAME(ResetMinigame.class),
    BACKPACK_CONFIG(BackpackConfig.class),
    SCROLL(Scroll.class),
    ;

    final @NonNull Class<? extends Message> clazz;

    public void receive(JsonObject object) {
        if (object == null) return;
        Message message = ServerClientMessaging.GSON.fromJson(object, getClazz());
        if (message instanceof Clientbound clientbound)
            clientbound.onReceive();
    }




}

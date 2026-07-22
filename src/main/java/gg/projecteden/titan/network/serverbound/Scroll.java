package gg.projecteden.titan.network.serverbound;


import gg.projecteden.titan.network.models.PluginMessage;
import gg.projecteden.titan.network.models.Serverbound;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Scroll extends Serverbound {

    boolean up;

    @Override
    public PluginMessage getType() {
        return PluginMessage.SCROLL;
    }
}

package gg.projecteden.titan.network.clientbound;

import gg.projecteden.titan.network.models.Clientbound;
import gg.projecteden.titan.utils.InventoryOverlay;
import lombok.Getter;

public class BackpackConfig extends Clientbound {

    public static Entry[] ENTRIES;
    public Entry[] entries;

    @Override
    public void onReceive() {
        ENTRIES = entries;
    }

    public static class Entry {
        @Getter
        String type;
        @Getter
        int rows;

        public InventoryOverlay.InventoryRenderType getRenderType() {
            return InventoryOverlay.InventoryRenderType.ofRows(this.rows);
        }
    }

}

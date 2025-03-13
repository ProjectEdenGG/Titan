package gg.projecteden.titan.mixin;

import gg.projecteden.titan.creative.CustomCreativeTabs;
import net.fabricmc.fabric.api.client.itemgroup.v1.FabricCreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin {

    @Shadow protected abstract void setSelectedTab(ItemGroup group);

    @Inject(method = "init", at = @At("TAIL"))
    void init(CallbackInfo ci) {
        if (this instanceof FabricCreativeInventoryScreen fabricCreativeInventoryScreen) {
            if (CustomCreativeTabs.GROUPS.values().stream().noneMatch(ItemGroup::shouldDisplay)) {
                fabricCreativeInventoryScreen.switchToPreviousPage();
                this.setSelectedTab(ItemGroups.getDefaultTab());
            }
        }
    }

}

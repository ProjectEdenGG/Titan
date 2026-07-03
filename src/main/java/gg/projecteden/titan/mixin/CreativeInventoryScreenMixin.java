package gg.projecteden.titan.mixin;

import gg.projecteden.titan.creative.CustomCreativeTabs;
import net.fabricmc.fabric.api.client.creativetab.v1.FabricCreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin {

    @Shadow protected abstract void selectTab(CreativeModeTab group);

    @Inject(method = "init", at = @At("TAIL"))
    void init(CallbackInfo ci) {
        if (this instanceof FabricCreativeModeInventoryScreen fabricCreativeInventoryScreen) {
            if (CustomCreativeTabs.GROUPS.values().stream().noneMatch(CreativeModeTab::shouldDisplay)) {
                fabricCreativeInventoryScreen.switchToPreviousPage();
                this.selectTab(CreativeModeTabs.getDefaultTab());
            }
        }
    }

}

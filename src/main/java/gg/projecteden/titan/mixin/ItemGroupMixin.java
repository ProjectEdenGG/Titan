package gg.projecteden.titan.mixin;

import gg.projecteden.titan.creative.CustomCreativeTabs;
import gg.projecteden.titan.utils.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeTab.class)
public abstract class ItemGroupMixin {

    @Shadow public abstract Component getDisplayName();

    @Shadow public abstract boolean hasAnyItems();

    @Inject(at = @At("HEAD"), method = "shouldDisplay", cancellable = true)
    void shouldDisplay(CallbackInfoReturnable<Boolean> cir) {
        if (this.getDisplayName() == null)
            return;
        if (CustomCreativeTabs.GROUPS.containsKey(this.getDisplayName().getString().toLowerCase().replace(" ", "_"))) {
            if (!Utils.isOnEden() || !hasAnyItems())
                cir.setReturnValue(false);
        }
    }

}

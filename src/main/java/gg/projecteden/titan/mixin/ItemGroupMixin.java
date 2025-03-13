package gg.projecteden.titan.mixin;

import gg.projecteden.titan.creative.CustomCreativeTabs;
import gg.projecteden.titan.utils.Utils;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemGroup.class)
public abstract class ItemGroupMixin {

    @Shadow public abstract Text getDisplayName();

    @Shadow public abstract boolean hasStacks();

    @Inject(at = @At("HEAD"), method = "shouldDisplay", cancellable = true)
    void shouldDisplay(CallbackInfoReturnable<Boolean> cir) {
        if (this.getDisplayName() == null)
            return;
        if (CustomCreativeTabs.GROUPS.containsKey(this.getDisplayName().getString().toLowerCase().replace(" ", "_"))) {
            if (!Utils.isOnEden() || !hasStacks())
                cir.setReturnValue(false);
        }
    }

}

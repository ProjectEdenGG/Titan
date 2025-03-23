package gg.projecteden.titan.mixin;

import gg.projecteden.titan.config.ConfigItem;
import gg.projecteden.titan.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.ScheduledTickView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoteBlock.class)
public class NoteBlockMixin extends Block {

    public NoteBlockMixin(Settings settings) {
        super(settings);
    }

    @Unique
    public boolean shouldCancel() {
        return Utils.isOnEden() && ConfigItem.STOP_CUSTOM_BLOCK_FLASHING.getValue();
    }

    @Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
    public void getStateForPlacement(ItemPlacementContext context, CallbackInfoReturnable<BlockState> cir) {
        if (shouldCancel())
            cir.setReturnValue(this.getDefaultState());
    }

    @Inject(method = "getStateForNeighborUpdate", at = @At("HEAD"), cancellable = true)
    public void updateShape(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random, CallbackInfoReturnable<BlockState> cir) {
        if (shouldCancel())
            cir.setReturnValue(state);
    }

    @Inject(method = "neighborUpdate", at = @At("HEAD"), cancellable = true)
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, WireOrientation wireOrientation, boolean notify, CallbackInfo ci) {
        if (shouldCancel()) {
            ci.cancel();
            world.setBlockState(pos, state, Block.FORCE_STATE);
        }
    }

    @Inject(method = "onUseWithItem", at = @At("HEAD"), cancellable = true)
    public void onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (shouldCancel())
            if (state.get(NoteBlock.INSTRUMENT) == NoteBlockInstrument.HARP)
                cir.setReturnValue(ActionResult.SUCCESS);
            else
                cir.setReturnValue(ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION);
    }

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    public void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (shouldCancel())
            if (state.get(NoteBlock.INSTRUMENT) == NoteBlockInstrument.HARP)
                cir.setReturnValue(ActionResult.SUCCESS);
            else
                cir.setReturnValue(ActionResult.CONSUME);
    }

}

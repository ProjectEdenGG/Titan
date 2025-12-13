package gg.projecteden.titan.mixin;

import gg.projecteden.titan.config.ConfigItem;
import gg.projecteden.titan.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoteBlock.class)
public class NoteBlockMixin extends Block {

    public NoteBlockMixin(Properties settings) {
        super(settings);
    }

    @Unique
    public boolean shouldCancel() {
        return Utils.isOnEden() && ConfigItem.STOP_CUSTOM_BLOCK_FLASHING.getValue();
    }

    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    public void getStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        if (shouldCancel())
            cir.setReturnValue(this.defaultBlockState());
    }

    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    public void updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random, CallbackInfoReturnable<BlockState> cir) {
        if (shouldCancel())
            cir.setReturnValue(state);
    }

    @Inject(method = "neighborChanged", at = @At("HEAD"), cancellable = true)
    public void neighborUpdate(BlockState state, Level world, BlockPos pos, Block sourceBlock, Orientation wireOrientation, boolean notify, CallbackInfo ci) {
        if (shouldCancel()) {
            ci.cancel();
            world.setBlock(pos, state, Block.UPDATE_KNOWN_SHAPE);
        }
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    public void onUseWithItem(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (shouldCancel())
            if (state.getValue(NoteBlock.INSTRUMENT) == NoteBlockInstrument.HARP)
                cir.setReturnValue(InteractionResult.SUCCESS);
            else
                cir.setReturnValue(InteractionResult.TRY_WITH_EMPTY_HAND);
    }

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    public void onUse(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (shouldCancel())
            if (state.getValue(NoteBlock.INSTRUMENT) == NoteBlockInstrument.HARP)
                cir.setReturnValue(InteractionResult.SUCCESS);
            else
                cir.setReturnValue(InteractionResult.CONSUME);
    }

}

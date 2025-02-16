package supersymmetry.api.metatileentity.multiblock;

import cam72cam.immersiverailroading.IRBlocks;
import gregtech.api.pattern.PatternStringError;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import gregtech.common.blocks.BlockColored;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraftforge.fml.common.Loader;
import org.jetbrains.annotations.NotNull;
import supersymmetry.SuSyValues;
import supersymmetry.common.blocks.BlockCoolingCoil;
import supersymmetry.common.blocks.BlockSinteringBrick;
import supersymmetry.common.blocks.SuSyBlocks;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Supplier;

/**
 * Class containing global predicates
 */
public class SuSyPredicates {

    private static final Supplier<TraceabilityPredicate> COOLING_COILS = () -> new TraceabilityPredicate(blockWorldState -> {
        IBlockState state = blockWorldState.getBlockState();
        if (state.getBlock() instanceof BlockCoolingCoil) {
            BlockCoolingCoil.CoolingCoilType type = SuSyBlocks.COOLING_COIL.getState(state);
            Object currentCoil = blockWorldState.getMatchContext().getOrPut("CoolingCoilType", type);
            if (!currentCoil.equals(type)) {
                blockWorldState.setError(new PatternStringError("gregtech.multiblock.pattern.error.coils"));
                return false;
            }
            blockWorldState.getMatchContext().getOrPut("VABlock", new LinkedList<>()).add(blockWorldState.getPos());
            return true;
        }
        return false;
    }, () -> Arrays.stream(BlockCoolingCoil.CoolingCoilType.values())
            .map(type -> new BlockInfo(SuSyBlocks.COOLING_COIL.getState(type)))
            .toArray(BlockInfo[]::new)
    );

    private static final Supplier<TraceabilityPredicate> SINTERING_BRICKS = () -> new TraceabilityPredicate(blockWorldState -> {
        IBlockState state = blockWorldState.getBlockState();
        if (state.getBlock() instanceof BlockSinteringBrick) {
            BlockSinteringBrick.SinteringBrickType type = SuSyBlocks.SINTERING_BRICK.getState(state);
            Object currentBrick = blockWorldState.getMatchContext().getOrPut("SinteringBrickType", type);
            if (!currentBrick.equals(type)) {
                blockWorldState.setError(new PatternStringError("gregtech.multiblock.pattern.error.sintering_bricks"));
                return false;
            }
            blockWorldState.getMatchContext().getOrPut("VABlock", new LinkedList<>()).add(blockWorldState.getPos());
            return true;
        }
        return false;
    }, () -> Arrays.stream(BlockSinteringBrick.SinteringBrickType.values())
            .map(type -> new BlockInfo(SuSyBlocks.SINTERING_BRICK.getState(type)))
            .toArray(BlockInfo[]::new)
    );

    private static final Supplier<TraceabilityPredicate> RAILS = () -> new TraceabilityPredicate(blockWorldState -> {
        if(!Loader.isModLoaded(SuSyValues.MODID_IMMERSIVERAILROADING)) return true;

        IBlockState state = blockWorldState.getBlockState();

        Block block = state.getBlock();

        return block == IRBlocks.BLOCK_RAIL.internal || block == IRBlocks.BLOCK_RAIL_GAG.internal;
    });

    /**
     * A predicate for allowing using only the same type of metal sheet blocks in a structure
     * This includes predicate for both small & large metal sheets
     * but only supplies {@link BlockInfo[]} for small ones
     *
     * @see #LARGE_METAL_SHEETS
     */
    private static final Supplier<TraceabilityPredicate> METAL_SHEETS = () -> new TraceabilityPredicate(blockWorldState -> {
        IBlockState state = blockWorldState.getBlockState();
        if (state.getBlock() instanceof BlockColored colored) {
            IBlockState defaultState = colored.getDefaultState();
            int colorValue = colored.getState(state).getMetadata();
            int typeValue = defaultState == MetaBlocks.METAL_SHEET.getDefaultState() ? 0 :
                    defaultState == MetaBlocks.LARGE_METAL_SHEET.getDefaultState() ? 1 : -1;
            if (typeValue >= 0) {
                byte value = (byte) (typeValue << 4 | colorValue);
                Object currentCoil = blockWorldState.getMatchContext().getOrPut("MetalSheet", value);
                if (!currentCoil.equals(value)) {
                    blockWorldState.setError(new PatternStringError("gregtech.multiblock.pattern.error.metal_sheets"));
                    return false;
                }
                return true;
            }
        }
        return false;
    }, () -> Arrays.stream(EnumDyeColor.values())
            .map(type -> new BlockInfo(MetaBlocks.METAL_SHEET.getState(type)))
            .toArray(BlockInfo[]::new)
    );

    /**
     * This only supplies {@link BlockInfo[]} for large metal sheet blocks.
     *
     * @see #METAL_SHEETS
     */
    private static final Supplier<TraceabilityPredicate> LARGE_METAL_SHEETS = () -> new TraceabilityPredicate(blockWorldState -> false, () -> Arrays.stream(EnumDyeColor.values())
            .map(type -> new BlockInfo(MetaBlocks.LARGE_METAL_SHEET.getState(type)))
            .toArray(BlockInfo[]::new)
    );

    @NotNull
    public static TraceabilityPredicate coolingCoils() {
        return COOLING_COILS.get();
    }

    @NotNull
    public static TraceabilityPredicate sinteringBricks() {
        return SINTERING_BRICKS.get();
    }

    @NotNull
    public static TraceabilityPredicate rails() {
        return RAILS.get();
    }

    @NotNull
    public static TraceabilityPredicate metalSheets() {
        return METAL_SHEETS.get().or(LARGE_METAL_SHEETS.get());
    }
}

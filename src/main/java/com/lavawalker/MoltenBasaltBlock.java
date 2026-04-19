package com.lavawalker;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class MoltenBasaltBlock extends Block {
    public static final IntProperty AGE = IntProperty.of("age", 0, 2);

    public MoltenBasaltBlock(Settings settings) {
        super(settings);
        // Establecemos el estado inicial
        this.setDefaultState(this.stateManager.getDefaultState().with(AGE, 0));
    }

    // --- ESTO ES LO QUE TE FALTABA PARA QUE NO CRASHEE ---
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    public void reprogramarTick(World world, BlockPos pos) {
        boolean esNether = world.getDimension().ultrawarm();
        // Nether: ~1-2 seg | Overworld: ~4-6 seg
        int tiempo = esNether ? 20 + world.random.nextBetween(0, 20) : 80 + world.random.nextBetween(0, 40);
        world.scheduleBlockTick(pos, this, tiempo);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!world.isClient) {
            this.reprogramarTick(world, pos);
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random){
        int edadActual = state.get(AGE);

        if (edadActual < 2) {
            // Aumentar edad
            world.setBlockState(pos, state.with(AGE, edadActual + 1), 3);
            this.reprogramarTick(world, pos);
        } else {
            // --- ESTO ES LO QUE HACE QUE VUELVA LA LAVA ---
            world.setBlockState(pos, Blocks.LAVA.getDefaultState());
        }
    }
}
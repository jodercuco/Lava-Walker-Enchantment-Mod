package com.lavawalker;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class LavaWalkerEnchantmentMod implements ModInitializer {
	public static final String MOD_ID = "lava-walker-enchantment-mod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Creo el encantamiento
	public static final Enchantment LAVA_WALKER = new LavaWalkerEnchantment();

	//Bloque
	public static final Block MOLTEN_BASALT = new MoltenBasaltBlock(
			FabricBlockSettings.copyOf(Blocks.BASALT)
					.ticksRandomly()
					.luminance(state -> 8)
	);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		Registry.register(Registries.ENCHANTMENT, new Identifier(MOD_ID, "lava_walker"), LAVA_WALKER);

		Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "molten_basalt"), MOLTEN_BASALT);
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "molten_basalt"), new BlockItem(MOLTEN_BASALT, new FabricItemSettings()));

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(content -> {
			content.add(MOLTEN_BASALT);
		});

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
			// Libro Nivel 1
			ItemStack libro1 = new ItemStack(Items.ENCHANTED_BOOK);
			EnchantedBookItem.addEnchantment(libro1, new EnchantmentLevelEntry(LAVA_WALKER, 1));
			content.add(libro1);

			// Libro Nivel 2
			ItemStack libro2 = new ItemStack(Items.ENCHANTED_BOOK);
			EnchantedBookItem.addEnchantment(libro2, new EnchantmentLevelEntry(LAVA_WALKER, 2));
			content.add(libro2);
		});


		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

				// Comprobamos: 1. No es espectador, 2. Está en el suelo
				if (!player.isSpectator() && player.isOnGround()) {

					int level = EnchantmentHelper.getEquipmentLevel(LAVA_WALKER, player);

					if (level > 0) {
						World world = player.getWorld();
						BlockPos feetPos = player.getBlockPos();
						int radius = 1 + level;

						// --- CAMBIO PARA EL RADIO CIRCULAR ---
						double radiusSq = radius * radius;
						// -------------------------------------

						// Buscamos en un área alrededor de los pies, específicamente un bloque abajo
						for (BlockPos pos : BlockPos.iterate(feetPos.add(-radius, -1, -radius), feetPos.add(radius, -1, radius))) {

							// --- CAMBIO PARA EL RADIO CIRCULAR ---
							// Solo si el bloque está dentro de la distancia circular del jugador
							if (pos.getSquaredDistance(player.getX(), pos.getY(), player.getZ()) <= radiusSq) {
								// -------------------------------------

								BlockState state = world.getBlockState(pos);

								// CONDICIÓN 1: Que sea Lava
								if (state.isOf(Blocks.LAVA)) {

									// CONDICIÓN 2: Que sea lava ESTÁTICA
									boolean esFuente = state.getFluidState().isStill();

									// CONDICIÓN 3: Que haya aire arriba
									if (esFuente && world.getBlockState(pos.up()).isAir()) {
										world.setBlockState(pos, MOLTEN_BASALT.getDefaultState());
									}
								}
							}
						}
					}
				}
			}
		});
	}
}
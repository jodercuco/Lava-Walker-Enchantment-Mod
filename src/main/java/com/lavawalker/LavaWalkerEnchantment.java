package com.lavawalker;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;

public class LavaWalkerEnchantment extends Enchantment {
    public LavaWalkerEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
    }
    @Override
    public int getMinPower(int level) {
        return 15 + (level - 1) * 12;
    }

    @Override
    public int getMaxPower(int level) {
        return super.getMinPower(level) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public boolean canAccept(Enchantment other) {
        // Si el otro encantamiento es Paso Helado (Frost Walker), devuelve false
        return super.canAccept(other) && other != Enchantments.FROST_WALKER;
    }

}

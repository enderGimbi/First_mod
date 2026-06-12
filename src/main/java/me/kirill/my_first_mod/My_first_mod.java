package me.kirill.my_first_mod;

import net.fabricmc.api.ModInitializer;

public class My_first_mod implements ModInitializer {

    @Override
    public void onInitialize() {
        ModItems.registerModItems();
        ModPotions.registerModPotions();
        ModPotions.registerPotionRecipes();
    }
}

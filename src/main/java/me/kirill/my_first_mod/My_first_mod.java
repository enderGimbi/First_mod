package me.kirill.my_first_mod;

import net.fabricmc.api.ModInitializer;

public class My_first_mod implements ModInitializer {

    @Override
    public void onInitialize() {
        ModItems.registerModItems();
        ModPotions.registerModPotions();
        ModPotions.registerPotionRecipes();
        ModBlocks.registerModBlocks();
        ModItemGroups.registerItemGroups();
        ModItems.registerAttackEvents();
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                ModEntities.GRANADE_TYPE,
                GrenadeEntityRenderer::new
        );
    }
}

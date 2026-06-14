package me.kirill.my_first_mod;

import me.kirill.my_first_mod.entity.AntiSandEntity;
import me.kirill.my_first_mod.entity.GrenadeEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {

    public static final EntityType<AntiSandEntity> ANTI_SAND_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier("my_first_mod", "anti_sand_entity"),
            FabricEntityTypeBuilder.<AntiSandEntity>create(SpawnGroup.MISC, AntiSandEntity::new)
                    .dimensions(EntityDimensions.fixed(0.98F, 0.98F)) // Размеры как у ванильного песка
                    .build() // Обязательный метод build() для Fabric!
    );

    public static final EntityType<GrenadeEntity> GRANADE_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier("my_first_mod", "granade_entity"),
            EntityType.Builder.<GrenadeEntity>create(GrenadeEntity::new, SpawnGroup.MISC)
                    .setDimensions(0.98f, 0.98f)
                    .build("")
    );

    public static void registerModEntities() {
        // Метод вызовем в главном инициализаторе мода
    }
}
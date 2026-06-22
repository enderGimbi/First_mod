package me.kirill.my_first_mod.client.model;

import me.kirill.my_first_mod.entity.GrenadeEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class GrenadeModel extends GeoModel<GrenadeEntity> {

    @Override
    public Identifier getModelResource(GrenadeEntity animatable) {
        // Указывает на файл геометрии: assets/my_first_mod/geo/grenade.geo.json
        return new Identifier("my_first_mod", "geo/entity/grenade_entity.geo.json");
    }

    @Override
    public Identifier getTextureResource(GrenadeEntity animatable) {
        // Указывает на текстуру: assets/my_first_mod/textures/entity/grenade.png
        return new Identifier("my_first_mod", "textures/entity/grenade_entity.png");
    }

    @Override
    public Identifier getAnimationResource(GrenadeEntity animatable) {
        // Указывает на файл анимаций: assets/my_first_mod/animations/grenade.animation.json
        // Помнишь Object ID "grenade" из Blockbench? Вот он здесь в названии файла!
        return new Identifier("my_first_mod", "animations/entity/grenade_entity.animation.json");
    }
}
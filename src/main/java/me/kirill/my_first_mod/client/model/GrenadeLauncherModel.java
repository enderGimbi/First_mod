package me.kirill.my_first_mod.client.model;

import me.kirill.my_first_mod.item.Glauncher_v2;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class GrenadeLauncherModel extends GeoModel<Glauncher_v2> {

    @Override
    public Identifier getModelResource(Glauncher_v2 animatable) {
        // Путь к assets/my_first_mod/geo/item/grenade_launcher.geo.json
        return new Identifier("my_first_mod", "geo/item/glauncher_v2.geo.json");
    }

    @Override
    public Identifier getTextureResource(Glauncher_v2 animatable) {
        // Путь к assets/my_first_mod/textures/item/grenade_launcher.png
        return new Identifier("my_first_mod", "textures/item/glauncher_v2.png");
    }

    @Override
    public Identifier getAnimationResource(Glauncher_v2 animatable) {
        // Путь к assets/my_first_mod/animations/item/grenade_launcher.animation.json
        return new Identifier("my_first_mod", "animations/item/glauncher_v2.animation.json");
    }
}
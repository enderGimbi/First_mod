package me.kirill.my_first_mod.client.model;

import me.kirill.my_first_mod.item.Glauncher_v2;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class GrenadeLauncherModel extends GeoModel<Glauncher_v2> {
    // Переносим матрицу и флаг сюда!
    public static final Matrix4f muzzleMatrix = new Matrix4f();
    public static boolean isMatrixLocalValid = false;

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

    /**
     * Этот метод вызывается GeckoLib каждую миллисекунду для просчета кадров анимации!
     */
    @Override
    public void setCustomAnimations(Glauncher_v2 animatable, long instanceId, AnimationState<Glauncher_v2> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        // Используем CoreGeoBone вместо GeoBone, чтобы избежать ошибки типов
        CoreGeoBone bone = this.getAnimationProcessor().getBone("for_grenade");

        if (bone != null) {
            muzzleMatrix.identity();

            // Извлекаем локальные координаты смещения и анимации кости
            muzzleMatrix.translate(bone.getPosX() / 16.0f, bone.getPosY() / 16.0f, bone.getPosZ() / 16.0f);

            // Учитываем вращение и масштаб
            muzzleMatrix.rotateXYZ(bone.getRotX(), bone.getRotY(), bone.getRotZ());
            muzzleMatrix.scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());

            isMatrixLocalValid = true;
        }
    }
}
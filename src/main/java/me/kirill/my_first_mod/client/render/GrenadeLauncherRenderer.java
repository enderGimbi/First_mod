package me.kirill.my_first_mod.client.render;

import me.kirill.my_first_mod.client.model.GrenadeLauncherModel;
import me.kirill.my_first_mod.item.Glauncher_v2;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone; // Импортируем правильный класс кости
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.Optional;

public class GrenadeLauncherRenderer extends GeoItemRenderer<Glauncher_v2> {

    public static double jsonXOffset = 0.0;
    public static double jsonYOffset = 0.0;
    public static double jsonZOffset = 0.0;
    public static boolean isLocatorFound = false;

    public GrenadeLauncherRenderer() {
        super(new GrenadeLauncherModel());
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode transformType, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight, int packedOverlay) {
        this.animatable = (Glauncher_v2) stack.getItem();
        this.currentItemStack = stack;

        // Вызываем супер-метод, чтобы GeckoLib запек и подготовил модель к этому кадру
        super.render(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);

        // Сразу после отрисовки вытаскиваем координаты локатора из текущей скомпилированной геометрии
        if (!isLocatorFound) { // Сделаем проверку один раз, чтобы не нагружать процессор делением каждый кадр
            BakedGeoModel model = this.getGeoModel().getBakedModel(this.getGeoModel().getModelResource(this.animatable));
            if (model != null) {
                // Извлекаем Optional<GeoBone> согласно твоей версии библиотеки
                Optional<GeoBone> boneOptional = model.getBone("for_grenade");

                if (boneOptional.isPresent()) {
                    GeoBone bone = boneOptional.get();

                    // Переводим пиксели Blockbench в метры (блоки) Майнкрафта
                    jsonXOffset = bone.getPosX() / 16.0;
                    jsonYOffset = bone.getPosY() / 16.0;
                    // Инвертируем Z под координатную сетку игрового мира
                    jsonZOffset = -bone.getPosZ() / 16.0;

                    isLocatorFound = true;
                }
            }
        }
    }
}
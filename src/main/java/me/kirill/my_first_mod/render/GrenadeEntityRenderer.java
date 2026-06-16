package me.kirill.my_first_mod.render;

import me.kirill.my_first_mod.entity.GrenadeEntity;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class GrenadeEntityRenderer extends EntityRenderer<GrenadeEntity> {
    private final BlockRenderManager blockRenderManager;

    public GrenadeEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        // Получаем доступ к системному отрисовщику блоков Майнкрафта
        this.blockRenderManager = ctx.getBlockRenderManager();
    }

    @Override
    public void render(GrenadeEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        // 1. Приподнимаем блок чуть-чуть вверх (по оси Y), чтобы его центр совпадал с центром хитбокса
        matrices.translate(0.0, 0.5, 0.0);

        // 2. ВРАЩАЕМ блок. Так как мы ещё не сдвигали оси X и Z, вращение произойдет ровно по центру блока!
        float age = entity.age + tickDelta;
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(age * 5.0f));

        // 3. А вот ТЕПЕРЬ сдвигаем саму модельку блока на половину её размера во все стороны,
        // чтобы "художник" (BlockRenderManager) нарисовал её ровно вокруг нашей крутящейся оси.
        matrices.translate(-0.5, -0.5, -0.5);

        // Рендерим блок ванильного ТНТ
        this.blockRenderManager.renderBlockAsEntity(
                Blocks.TNT.getDefaultState(),
                matrices,
                vertexConsumers,
                light,
                OverlayTexture.DEFAULT_UV
        );

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(GrenadeEntity entity) {
        // Для блоков этот метод почти не используется, но мы обязаны его переопределить
        return new Identifier("minecraft", "textures/block/tnt_side.png");
    }
}
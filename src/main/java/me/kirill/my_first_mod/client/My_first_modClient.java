package me.kirill.my_first_mod.client;

import me.kirill.my_first_mod.client.render.GrenadeLauncherRenderer;
import me.kirill.my_first_mod.client.render.GrenadeRenderer;
import me.kirill.my_first_mod.item.Glauncher_v2;
import me.kirill.my_first_mod.render.AntiSandRenderer;
import me.kirill.my_first_mod.ModEntities;
import me.kirill.my_first_mod.ModItems;
import me.kirill.my_first_mod.util.IPlayerBazookaSettings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class My_first_modClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Для отображения антипеска
        EntityRendererRegistry.register(ModEntities.ANTI_SAND_TYPE, AntiSandRenderer::new);
        EntityRendererRegistry.register(
                ModEntities.GRENADE_TYPE,
                GrenadeRenderer::new
        );

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            PlayerEntity player = client.player;

            // Если игрока нет или открыто какое-то меню (инвентарь, пауза) — ничего не рисуем
            if (player == null || client.currentScreen != null) return;

            // Проверяем, держит ли игрок НАШУ базуку в главной руке
            if (player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof Glauncher_v2) {
                renderBazookaHUD(drawContext, client, player);
            }
        });

        ModelPredicateProviderRegistry.register(
                ModItems.SUPER_BREAD,
                new Identifier("my_first_mod", "eating_stage"),
                (stack, world, entity, seed) -> {
                    if (entity == null) {
                        return 0.0f;
                    }

                    float[] stages = {0.0f,0.125f,0.25f,0.375f,0.5f,0.625f,0.75f}; // Стадии поедания
                    int damage = stack.getDamage();


                    if (entity.isUsingItem() && entity.getActiveItem() == stack) {
                        int timeLeft = entity.getItemUseTimeLeft();

                        switch (damage){
                            case 0:
                                if (timeLeft <= 2)
                                    return stages[6];
                                if (timeLeft <= 8)
                                    return stages[5];
                                if (timeLeft <= 12)
                                    return stages[4];
                                if (timeLeft <= 18)
                                    return stages[3];
                                if (timeLeft <= 22)
                                    return stages[2];
                                if (timeLeft <= 28)
                                    return stages[1];
                                return stages[0];
                            case 1:
                                if(timeLeft<=8)
                                    return stages[6];
                                if(timeLeft<=14)
                                    return stages[5];
                                if(timeLeft<=20)
                                    return stages[4];
                                if(timeLeft<=26)
                                    return stages[3];
                                return stages[2];
                            case 2:
                                if(timeLeft<=12)
                                    return stages[6];
                                if(timeLeft<=22)
                                    return stages[5];
                                return stages[4];
                            default:
                                break;
                        }


                    }

                    if (damage == 1) {
                        return stages[2];
                    } else if (damage >= 2) {
                        return stages[4];
                    }
                    return stages[0];
                }

        );
    }

    private void renderBazookaHUD(DrawContext drawContext, MinecraftClient client, PlayerEntity player) {
        TextRenderer textRenderer = client.textRenderer;

        // Получаем динамический размер окна (благодаря этому текст масштабируется вместе с GUI Майнкрафта)
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        if (player instanceof IPlayerBazookaSettings settings) {
            float power = settings.getExplosionPower();
            int delay = settings.getFuseDelay();
            float velocity = settings.getShootVelocity();

            // Высчитываем координаты относительно ХОТБАРА:
            // centerX - 91 — это левая граница хотбара. Сдвигаем левее, например до -160.
            int x = (screenWidth / 2) - 185;

            // screenHeight - 22 — это примерно уровень иконок хотбара по высоте.
            // Поднимаем каждую строчку повыше, чтобы они шли снизу вверх аккуратным столбиком.
            int yBase = screenHeight - 10;

            // Сдержанный белый цвет текста
            int textColor = 0xFFFFFF;

            // Отрисовка мелкого аккуратного текста (true включает стандартную ванильную тень блока текста)
            drawContext.drawText(textRenderer, "Сила: " + power, x, yBase - 24, textColor, true);
            drawContext.drawText(textRenderer, "Задержка: " + delay + "т", x, yBase - 12, textColor, true);
            drawContext.drawText(textRenderer, "Нач. скорость: " + velocity, x, yBase, textColor, true);
        }
    }
}

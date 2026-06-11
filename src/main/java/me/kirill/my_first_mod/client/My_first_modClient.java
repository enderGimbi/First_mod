package me.kirill.my_first_mod.client;

import me.kirill.my_first_mod.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

public class My_first_modClient implements ClientModInitializer {

    // Выносим переменную наружу (как поле класса или статическую переменную),
    // чтобы игра помнила, какая стадия была в предыдущем кадре.
    // Добавьте эту строчку ПЕРЕД методом @Override onInitializeClient() или внутри класса:
    private static float lastStage = -1.0f;

    @Override
    public void onInitializeClient() {
        ModelPredicateProviderRegistry.register(
                ModItems.SUPER_BREAD,
                new Identifier("my_first_mod", "eating_stage"),
                (stack, world, entity, seed) -> {
                    if (entity == null) {
                        return 0.0f;
                    }

                    float currentStage = 0.0f;

                    if (entity.isUsingItem() && entity.getActiveItem() == stack) {
                        int timeLeft = entity.getItemUseTimeLeft();

                        // Стадии поедания хлеба
                        if (timeLeft <= 14) {
                            currentStage = 0.66f; // Горбушка
                        } else if (timeLeft <= 26) {
                            currentStage = 0.33f; // Половинка
                        } else {
                            currentStage = 0.0f; // Целый
                        }
                    }

                    return currentStage;
                }
        );
    }
}

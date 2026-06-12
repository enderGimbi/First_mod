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
}

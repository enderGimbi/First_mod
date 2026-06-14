package me.kirill.my_first_mod;

import me.kirill.my_first_mod.item.GLauncher;
import me.kirill.my_first_mod.item.Glauncher_v2;
import me.kirill.my_first_mod.item.SuperBreadItem;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModItems {

    // Cупер-хлеб на 3 укуса
    public static final Item SUPER_BREAD = new SuperBreadItem(new Item.Settings()
            .maxDamage(3) //
            .food(new FoodComponent.Builder()
                    .hunger(5)
                    .saturationModifier(0.6f)
                    .statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1), 0.1f)
                    .build()
            )
    );

    // Сгоревшая версия на 1 использование
    public static final Item BURNED_SUPER_BREAD = new Item(new Item.Settings()
            .maxDamage(131)
            .food(new FoodComponent.Builder()
                    .hunger(-5)
                    .saturationModifier(0)
                    .statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 200, 1), 1.0f)
                    .statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA,100,1),1.0f)
                    .build()
            )
    );

    // Через AttackEntityCallback.EVENT.register от фабрика проверяем каджый раз когда кто-то кого-то бьет на факт использования нашего черного друга
    public static void registerAttackEvents() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient() && hand == Hand.MAIN_HAND) {
                ItemStack stack = player.getStackInHand(hand);

                // Проверяем на хлеб
                if (stack.isOf(BURNED_SUPER_BREAD)) {
                    float breadDamage = 3.0f;
                    boolean isCritical = player.fallDistance > 0.0f
                            && !player.isClimbing()
                            && !player.isSwimming()
                            && !player.isTouchingWater();

                    if(isCritical){
                        breadDamage *= 1.5f;

                        if(world instanceof net.minecraft.server.world.ServerWorld serverWorld){
                            serverWorld.spawnParticles(
                                    ParticleTypes.CRIT,
                                    entity.getX(),
                                    entity.getBodyY(0.5), // Для нахождения середины тела (0 - ноги, 1 - голова)
                                    entity.getZ(),
                                    15,
                                    0.0,0.0,0.0, // Настройка разлета
                                    1.0
                            );

                        }
                    }


                    // Наносим урон каменного меча
                    entity.damage(world.getDamageSources().playerAttack(player), breadDamage);

                    // Тратим 1 единицу прочности хлеба при ударе
                    stack.damage(1, player, (p) -> p.sendToolBreakStatus(hand));

                    // Произойдет наше событие доп урона
                    return ActionResult.PASS;
                    // существует также FAIL для отмены любого действия
                    // для завершения цикла проверок колбеков от модов используется SUCCESS
                    // потому нужно правильно выбрать между SUCCESS и PASS, так как
                    // при PASS майн добавит свои базовые 1.0F урона
                }
            }
            // Произойдет ванильное событие (без вмешательства кода выше)
            return ActionResult.PASS;
        });
    }

    public static final Item GLAUNCHER = new GLauncher(new Item.Settings()
            .maxDamage(200)
            .rarity(Rarity.UNCOMMON)
    );

    public static final Item GLAUNCHER_V2 = new Glauncher_v2(new Item.Settings()
            .maxDamage(200)
            .rarity(Rarity.RARE)
    );

    public static void registerModItems() {
        // Регистрация предметов в игре
        Registry.register(Registries.ITEM, new Identifier("my_first_mod", "super_bread"), SUPER_BREAD);
        Registry.register(Registries.ITEM, new Identifier("my_first_mod", "burned_super_bread"), BURNED_SUPER_BREAD);
        Registry.register(Registries.ITEM,new Identifier("my_first_mod","glauncher"),GLAUNCHER);
        Registry.register(Registries.ITEM,new Identifier("my_first_mod","glauncher_v2"),GLAUNCHER_V2);

        // Добавление во вкладку еды
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(content -> {
            content.add(SUPER_BREAD);
            content.add(BURNED_SUPER_BREAD);
        });
    }
}
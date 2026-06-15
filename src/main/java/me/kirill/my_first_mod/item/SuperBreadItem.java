package me.kirill.my_first_mod.item; // Твой пакет

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SuperBreadItem extends Item {

    public SuperBreadItem(Settings settings) {
        super(settings);
    }

    /*
     * Для реализации правильной логики многоразового хлеба для него, в обход стандартным функциям, написана
     * своя логика поедания (при желании, Вова, можешь прикрутить тут свои звуки или же сменить координаты
     * воспроизводимого звука)
     */
    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        // Проверяем, является ли предмет едой (наш хлеб является)
        if (this.isFood()) {

            /*
             * Вместо использования {@code "user.eatFood(world,stack)"}
             * сами пишем что будет происходить с предметом, а также с игроком
             *
             * Функция, что избегаем выше после съедения хлебушка его удаляет игнорируя прочность (damage)
             */
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    user.getEatSound(stack),
                    net.minecraft.sound.SoundCategory.NEUTRAL, 1.0F, 1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.4F);

            // Начисление сытости, если это игрок
            if (user instanceof PlayerEntity player) {
                player.getHungerManager().add(this.getFoodComponent().getHunger(), this.getFoodComponent().getSaturationModifier());

                // Проигрываем звук сытости
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sound.SoundEvents.ENTITY_PLAYER_BURP,
                        net.minecraft.sound.SoundCategory.PLAYERS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
            }

            // Накатываем эффекты
            if (!world.isClient && this.getFoodComponent() != null) {
                this.getFoodComponent().getStatusEffects().forEach(pair -> {
                    if (world.random.nextFloat() < pair.getSecond()) {
                        user.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(pair.getFirst()));
                    }
                });
            }
        }

        // Если игрок в креативе — возвращаем целый предмет
        if (user instanceof PlayerEntity player && player.getAbilities().creativeMode) {
            return stack;
        }

        // Для выживания: вместо ванильного уменьшения стака (замена stack.decrement из user.eatFood)
        if (!world.isClient && user instanceof PlayerEntity player) {
            stack.damage(1, player, (p) -> {
                // Пустышка, чтобы ничего не происходило при ломании хлеба (можно поиграться и добавить ловушек :)) )
            });
        }

        return stack;
    }

    // Видимость полоски прочности
    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        // Полоска видна, если текущий урон (укусы) больше нуля
        return stack.getDamage() > 0;
    }

    // В целом из названия понятно
    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0xFFAA00; // Золотой цвет/оранжевый
    }

    // Отображение при наведении сколько осталось использований (можно будет заменить на визуальное отображение)
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        int maxUses = stack.getMaxDamage();
        int currentDamage = stack.getDamage();
        int remainingUses = maxUses - currentDamage;

        tooltip.add(Text.literal("Осталось порций: ")
                .append(Text.literal(String.valueOf(remainingUses)).formatted(Formatting.GREEN))
                .append(Text.literal(" / " + maxUses))
                .formatted(Formatting.GRAY));

        super.appendTooltip(stack, world, tooltip, context);
    }
}
package my_first_mod.item; // Укажите ваш пакет

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
    private final int maxBites;

    public SuperBreadItem(Settings settings,int maxBites) {
        super(settings);
        this.maxBites = maxBites;
    }

    public int getRemainingBites(ItemStack stack){
        if(!stack.hasNbt()){
            return this.maxBites;
        }
        int currentBites = stack.getNbt().getInt("My_first_mod_Bites");
        return Math.max(0,this.maxBites-currentBites);
    }

    public int getMaxBites(){
        return this.maxBites;
    }

    // МЕХАНИКА МНОГОРАЗОВОСТИ: Вызывается, когда игрок закончил есть
    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        // Вызываем стандартное насыщение
        ItemStack resultStack = super.finishUsing(stack, world, user);

        if (user instanceof PlayerEntity player && !world.isClient) {
            // Создаем или получаем кастомный NBT-тег предмета
            var nbt = stack.getOrCreateNbt();

            // Читаем, сколько укусов уже сделано (по умолчанию 0)
            int bites = nbt.getInt("My_first_mod_Bites");
            bites++;

            if (bites >= 3) {
                // Если укусили 3 раза — уменьшаем стак хлеба на 1 штуку
                stack.decrement(1);
            } else {
                // Иначе — просто записываем новую стадию укуса в этот конкретный хлеб!
                nbt.putInt("My_first_mod_Bites", bites);
            }
        }
        return resultStack;
    }

    // НАСТРОЙКА ЦВЕТА ПОЛОСКИ: Сделаем её, например, золотой или оранжевой
    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0xFFAA00; // Шестнадцатеричный код цвета (Gold/Orange)
    }

    // ДИНАМИЧЕСКОЕ ОПИСАНИЕ (TOOLTIP)
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        // Вычисляем, сколько порций осталось
        // Тка как maxDamage = 3, а damage растет от 0 до 3:
        int remainingUses = getRemainingBites(stack);

        // Добавляем красивую строчку в описание
        tooltip.add(Text.literal("Осталось порций: ")
                .append(Text.literal(String.valueOf(remainingUses)).formatted(Formatting.GREEN))
                .append(Text.literal(" / " + this.maxBites))
                .formatted(Formatting.GRAY));

        // Дополнительная пасхалка, если остался последний укус
        if (remainingUses == 1) {
            tooltip.add(Text.literal("Осторожно, осталась только горбушка!").formatted(Formatting.RED, Formatting.ITALIC));
        }

        super.appendTooltip(stack, world, tooltip, context);
    }
}
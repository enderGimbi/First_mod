package me.kirill.my_first_mod.item;

import me.kirill.my_first_mod.ModEntities;
import me.kirill.my_first_mod.client.model.GrenadeLauncherModel;
import me.kirill.my_first_mod.client.render.GrenadeLauncherRenderer;
import me.kirill.my_first_mod.entity.GrenadeEntity;
import me.kirill.my_first_mod.networking.ModPackets;
import me.kirill.my_first_mod.util.IPlayerBazookaSettings;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.DataTicket;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Glauncher_v2 extends GLauncher implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    // Важно: имя анимации должно быть точь-в-точь как в Blockbench!
    private static final RawAnimation SHOOT_ANIM = RawAnimation.begin().thenPlay("fire");

    float divergence = 0.1f;
    // int shootCooldownTicks = 20;

    public Glauncher_v2(Settings settings) {
        super(settings);
    }

//    @Override
//    public boolean isUsedOnRelease(ItemStack stack) {
//        return false;
//    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (world.isClient()) {
            // Пинаем анимацию GeckoLib — она запустится плавно и без сбросов
            this.triggerAnim(user, 0, "shoot_controller", "shoot");
        }

        if(!world.isClient()){
            user.getItemCooldownManager().set(this,20);
        }

        this.shot(world, user, hand);

        return TypedActionResult.fail(itemStack);
    }

    public void shot(World world, PlayerEntity player, Hand hand) {
        if (world.isClient()) {
            // 1. Указываем путь к нашему JSON файлу модели
            Identifier geoPath = new Identifier("my_first_mod", "geo/item/glauncher_v2.geo.json"); // подставь свой точный путь и ID мода

            // 2. Читаем координаты "for_fire" напрямую через наш парсер
            Vec3d localLocator = me.kirill.my_first_mod.util.ModMathUtils.getLocatorPath(geoPath, "for_fire");

            // Если парсер вернул нули (файл не прочитался), подставим безопасный дефолт, чтобы не спавнить в голове
            double lx = localLocator.x == 0 ? 0.35 : localLocator.x;
            double ly = localLocator.y == 0 ? -0.15 : localLocator.y;
            double lz = localLocator.z == 0 ? 0.65 : localLocator.z;

            // 3. Пробрасываем эти точные, стабильные координаты в нашу тригонометрическую утилиту вращения
            Vec3d finalWorldPos = me.kirill.my_first_mod.util.ModMathUtils.getDynamicWorldPosition(
                    player, hand, lx, ly, lz
            );

            // 4. Отправляем пакет на сервер с точными мировыми координатами
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeDouble(finalWorldPos.x);
            buf.writeDouble(finalWorldPos.y);
            buf.writeDouble(finalWorldPos.z);
            ClientPlayNetworking.send(ModPackets.GLAUNCHER_V2_GRENADE_SPAWN, buf);
        }
    }

    // =========================================================================
    // НАДЁЖНЫЙ КОНТРОЛЛЕР ДЛЯ GECKOLIB 4 ДЛЯ ПРЕДМЕТОВ
    // =========================================================================
    // =========================================================================
    // БЕЗОПАСНЫЙ КОНТРОЛЛЕР ДЛЯ GECKOLIB 4 (БЕЗ ОШИБОК КОМПИЛЯЦИИ)
    // =========================================================================
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Чистый и стабильный контроллер без внутренних обработчиков GeckoLib
        controllers.add(new AnimationController<>(this, "shoot_controller", 0,
                state -> {
                    ModelTransformationMode transformMode = state.getData(DataTickets.ITEM_RENDER_PERSPECTIVE);

                    if(transformMode==null)
                        return PlayState.STOP;

                    if(transformMode == ModelTransformationMode.GUI ||
                    transformMode == ModelTransformationMode.GROUND ||
                    transformMode == ModelTransformationMode.FIXED ||
                    transformMode == ModelTransformationMode.HEAD){
                        state.getController().stop();
                        return PlayState.STOP;
                    }

                    return PlayState.CONTINUE;
                })
                .triggerableAnim("shoot", SHOOT_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private GrenadeLauncherRenderer renderer;

            @Override
            public BuiltinModelItemRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GrenadeLauncherRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }

    @Override
    public net.minecraft.util.UseAction getUseAction(ItemStack stack) {
        // Говорим игре, что анимация использования — это NONE (ничего).
        // Это заблокирует ванильное подергивание руки при кликах,
        // но никак не помешает твоему миксину и анимации GeckoLib!
        return net.minecraft.util.UseAction.NONE;
    }

}
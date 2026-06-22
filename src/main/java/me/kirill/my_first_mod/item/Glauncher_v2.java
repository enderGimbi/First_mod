package me.kirill.my_first_mod.item;

import com.terraformersmc.modmenu.util.mod.Mod;
import me.kirill.my_first_mod.ModEntities;
import me.kirill.my_first_mod.client.model.GrenadeLauncherModel;
import me.kirill.my_first_mod.client.render.GrenadeLauncherRenderer;
import me.kirill.my_first_mod.entity.GrenadeEntity;
import me.kirill.my_first_mod.networking.ModPackets;
import me.kirill.my_first_mod.util.IPlayerBazookaSettings;
import me.kirill.my_first_mod.util.ModMathUtils;
import me.kirill.my_first_mod.util.RotatableWeapon;
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
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Glauncher_v2 extends GLauncher implements GeoItem, RotatableWeapon {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    // Важно: имя анимации должно быть точь-в-точь как в Blockbench!
    private static final RawAnimation SHOOT_ANIM = RawAnimation.begin().thenPlay("fire");

    float divergence = 0.1f;
    int shootCooldownTicks = 20;

    private static final float THIRD_PERSON_SCALE = 0.5f;

    @Override
    public float getThirdPersonScale() {
        return THIRD_PERSON_SCALE;
    }

    public Glauncher_v2(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (world.isClient()) {
            this.triggerAnim(user, 0, "shoot_controller", "shoot");
        }

        if(!world.isClient()){
            user.getItemCooldownManager().set(this,shootCooldownTicks);
        }

        this.shot(world, user, hand);

        return TypedActionResult.fail(itemStack);
    }

    public void shot(World world, PlayerEntity player, Hand hand) {
        if (world.isClient()) {
            // Идентификатор для парсера (прописывать путь)
            Identifier geoPath = new Identifier("my_first_mod", "geo/item/glauncher_v2.geo.json");

            // Чтение координат через кастомный парсер
            Vec3d for_grenade = ModMathUtils.getLocatorPath(geoPath, "for_grenade");
            Vec3d for_fire = ModMathUtils.getLocatorPath(geoPath,"for_fire");


            // Если парсер вернул нули (файл не прочитался), подставим безопасный дефолт, чтобы не спавнить в голове
//            double gx = for_grenade.x == 0 ? 0.35 : for_grenade.x;
//            double gy = for_grenade.y == 0 ? -0.15 : for_grenade.y;
//            double gz = for_grenade.z == 0 ? 0.65 : for_grenade.z;
//
//            double fx = for_fire.x == 0 ? 0.35 : for_fire.x;
//            double fy = for_fire.y == 0 ? -0.15 : for_fire.y;
//            double fz = for_fire.z == 0 ? -0.65 : for_fire.z;


            // Обработка координат через утилиту
            Vec3d finalWorldPos = ModMathUtils.getDynamicWorldPosition(
                    player, hand, for_grenade
            );

            Vec3d particleWorldPos = ModMathUtils.getDynamicWorldPosition(
                    player, hand, for_fire
            );

            // Отправка пакета
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeDouble(finalWorldPos.x);
            buf.writeDouble(finalWorldPos.y);
            buf.writeDouble(finalWorldPos.z);
            buf.writeDouble(particleWorldPos.x);
            buf.writeDouble(particleWorldPos.y);
            buf.writeDouble(particleWorldPos.z);
            buf.writeInt(10);
            ClientPlayNetworking.send(ModPackets.GLAUNCHER_V2_GRENADE_SPAWN, buf);
        }
    }

    // Контроллер регистрации анимаций
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
        return net.minecraft.util.UseAction.NONE;
    }

}
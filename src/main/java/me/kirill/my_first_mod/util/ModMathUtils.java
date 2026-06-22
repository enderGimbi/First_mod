package me.kirill.my_first_mod.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ModMathUtils {

    /**
     * Рассчитывает мировые координаты точки оружия, используя в качестве
     * опорного элемента (Pivot) ось поворота плеча конкретной руки игрока.
     *
     * @param player   Игрок
     * @param hand     Какая рука делает выстрел (MAIN_HAND или OFF_HAND)
     * @param localPos Локальный вектор точки из JSON-парсера (в блоках, например, деленный на 16)
     */
    public static Vec3d getDynamicWorldPosition(PlayerEntity player, Hand hand, Vec3d localPos) {
        if (localPos == null) return player.getEyePos();

        // Определение руки из которой произведен выстрел
        boolean isRightArm = true;
        if (hand == Hand.MAIN_HAND) {
            isRightArm = player.getMainArm() == Arm.RIGHT;
        } else {
            isRightArm = player.getMainArm() == Arm.LEFT;
        }

        // Задаем смещение плечевого сустава относительно центра глаз (EyePos)
        float armOffsetSign = isRightArm ? -1.0f : 1.0f;
        Vector3f shoulderPivot = new Vector3f(
                0.35f * armOffsetSign, // Смещение вбок к суставу
                -0.5f,                 // Смещение вниз от линии глаз
                0.0f                   // На оси тела
        );

        ItemStack stack = player.getStackInHand(hand);
        float scale = 1.0f;

        if(stack.getItem() instanceof RotatableWeapon weapon){
            scale = weapon.getThirdPersonScale();
        }

        // Берем локальные координаты ствола/партикла из парсера
        float x = (float) localPos.x * scale;
        float y = (float) localPos.y * scale;
        float z = (float) localPos.z * scale;

        // Если оружие в левой руке, зеркалим локальную координату X самого ствола
        if (!isRightArm) {
            x = -x;
        }

        // Вектор от плеча до дула
        Vector3f localOffset = new Vector3f(x, y, z);

        // Поворот системы координат
        // Получаем углы поворота тела и головы игрока
        float yawRad = (float) Math.toRadians(-player.getYaw());
        float pitchRad = (float) Math.toRadians(player.getPitch());

        // Кватернион общего вращения игрока
        Quaternionf playerRotation = new Quaternionf()
                .rotationY(yawRad)
                .rotateX(pitchRad);

        // Поворачиваем точку сустава плеча (чтобы плечо двигалось вместе с поворотом тела)
        shoulderPivot.rotate(playerRotation);

        // Поворачиваем локальный вектор ствола вокруг этого плеча
        localOffset.rotate(playerRotation);

        // Итоговая позиция в мире:
        // Позиция Глаз + Позиция повернутого Плеча + Позиция повернутого Ствола
        Vec3d eyePos = player.getEyePos();

        return new Vec3d(
                eyePos.x + shoulderPivot.x + localOffset.x,
                eyePos.y + shoulderPivot.y + localOffset.y,
                eyePos.z + shoulderPivot.z + localOffset.z
        );
    }


    // Кэш для хранения координат, чтобы не читать JSON при каждом выстреле
    // Ключ: "имя_модели:имя_локатора", Значение: вектор локальных координат в БЛОКАХ
    private static final Map<String, Vec3d> LOCATOR_CACHE = new HashMap<>();

    /**
     * Получить локальные координаты локатора из Geo JSON файла (в блоках, деленные на 16)
     *
     * @param modelPath Путь к геометрии, например: new Identifier("my_first_mod", "geo/glauncher_v2.geo.json")
     * @param locatorName Имя локатора, например: "for_fire"
     * @return Vec3d локальных координат (X, Y, Z) или Vec3d.ZERO, если не найдено
     */
    public static Vec3d getLocatorPath(Identifier modelPath, String locatorName) {
        String cacheKey = modelPath.toString() + ":" + locatorName;

        // Если мы уже парсили этот локатор, отдаем из памяти мгновенно
        if (LOCATOR_CACHE.containsKey(cacheKey)) {
            return LOCATOR_CACHE.get(cacheKey);
        }

        try {
            // Читаем файл из ресурсов Майнкрафта
            var resource = MinecraftClient.getInstance().getResourceManager().getResource(modelPath);
            if (resource.isEmpty()) {
                return Vec3d.ZERO;
            }

            try (var reader = new InputStreamReader(resource.get().getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

                // В GeckoLib структура корня обычно: "minecraft:geometry" -> Массив объектов
                if (root.has("minecraft:geometry")) {
                    JsonArray geometries = root.getAsJsonArray("minecraft:geometry");

                    for (JsonElement geoElement : geometries) {
                        JsonObject geo = geoElement.getAsJsonObject();
                        if (!geo.has("bones")) continue;

                        JsonArray bones = geo.getAsJsonArray("bones");
                        for (JsonElement boneElement : bones) {
                            JsonObject bone = boneElement.getAsJsonObject();

                            if (bone.has("name") && bone.get("name").getAsString().equals("locators")) {
                                if (bone.has("locators")) {
                                    JsonObject locatorsObj = bone.getAsJsonObject("locators");

                                    if (locatorsObj.has(locatorName)) {
                                        JsonArray coords = locatorsObj.getAsJsonArray(locatorName);

                                        // Blockbench координаты: [X, Y, Z] в пикселях
                                        double rawX = coords.get(0).getAsDouble();
                                        double rawY = coords.get(1).getAsDouble();
                                        double rawZ = coords.get(2).getAsDouble();

                                        // Переводим пиксели модели в блоки Майнкрафта (16 пикселей = 1 блок)
                                        Vec3d localPos = new Vec3d(rawX / 16.0, rawY / 16.0, rawZ / 16.0);

                                        // Сохраняем в кэш и логируем успех
                                        LOCATOR_CACHE.put(cacheKey, localPos);
                                        return localPos;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Если что-то пошло не так, возвращаем нулевой вектор, чтобы не крашить игру
        return Vec3d.ZERO;
    }
}
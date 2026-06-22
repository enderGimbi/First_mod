package me.kirill.my_first_mod.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ModMathUtils {

    // =========================================================================
    // ДЕФОЛТНЫЕ ЗНАЧЕНИЯ (ЕСЛИ ПАРАМЕТРЫ НЕ ПЕРЕДАНЫ)
    // =========================================================================
    private static final double DEFAULT_PITCH_UP = -0.5;
    private static final double DEFAULT_PITCH_DOWN = -0.4;
    private static final double DEFAULT_Y_CORRECTION = 0.8;

    /**
     * 1. КОРОТКИЙ ВАРИАНТ (Для обычного выстрела без кастомных настроек).
     * Автоматически подставит дефолтные значения -0.5, -0.4 и 0.8
     */
    public static Vec3d getDynamicWorldPosition(PlayerEntity player, Hand hand, double localX, double localY, double localZ) {
        return getDynamicWorldPosition(player, hand, localX, localY, localZ, null, null, null);
    }

    /**
     * 2. ПОЛНЫЙ ВАРИАНТ (Основной метод, который выполняет всю математику).
     * Принимает Double (объектный класс), чтобы параметры могли быть null.
     */
    public static Vec3d getDynamicWorldPosition(
            PlayerEntity player,
            Hand hand,
            double localX,
            double localY,
            double localZ,
            @Nullable Double upCorrection,
            @Nullable Double downCorrection,
            @Nullable Double yCorrection
    ) {
        // Если параметры переданы как null, берем стандартные константы
        double pitchUpConfig = (upCorrection != null) ? upCorrection : DEFAULT_PITCH_UP;
        double pitchDownConfig = (downCorrection != null) ? downCorrection : DEFAULT_PITCH_DOWN;
        double yCorrectionConfig = (yCorrection != null) ? yCorrection : DEFAULT_Y_CORRECTION;

        // Базовая точка — глаза игрока
        double baseX = player.getX();
        double baseY = player.getEyeY();
        double baseZ = player.getZ();

        // 1. ЛОГИКА РУК (Зеркалирование X)
        boolean isLeftArm = player.getMainArm() == Arm.LEFT;
        if (hand == Hand.OFF_HAND) {
            isLeftArm = !isLeftArm;
        }

        if (!isLeftArm) {
            localX = -localX;
        }

        // Переводим углы в радианы
        float yaw = player.getYaw() * MathHelper.RADIANS_PER_DEGREE;
        float pitch = player.getPitch() * MathHelper.RADIANS_PER_DEGREE;

        float cosYaw = MathHelper.cos(yaw);
        float sinYaw = MathHelper.sin(yaw);
        float cosPitch = MathHelper.cos(pitch);
        float sinPitch = MathHelper.sin(pitch);

        // ИДЕАЛЬНАЯ МАТРИЦА СФЕРИЧЕСКОГО ВРАЩЕНИЯ
        // (Применяем выбранную высоту yCorrectionConfig)
        double worldX = localX * cosYaw - localY * sinYaw * sinPitch - localZ * sinYaw * cosPitch;
        double worldY = localY * cosPitch - localZ * sinPitch - yCorrectionConfig;
        double worldZ = localX * sinYaw + localY * cosYaw * sinPitch + localZ * cosYaw * cosPitch;

        // 2. РАЗДЕЛЬНАЯ ГОРИЗОНТАЛЬНАЯ КОРРЕКЦИЯ (ВВЕРХ / ВНИЗ)
        double currentCorrectionConfig = 0.0;

        // В Майнкрафте pitch < 0 означает, что игрок смотрит ВВЕРХ
        if (player.getPitch() < 0) {
            currentCorrectionConfig = pitchUpConfig;
        } else {
            // Игрок смотрит прямо или ВНИЗ
            currentCorrectionConfig = pitchDownConfig;
        }

        // Вычисляем итоговую силу сдвига на основе выбранного конфига
        double correctionMagnitude = sinPitch * currentCorrectionConfig;

        // Применяем сдвиг вдоль горизонтального направления взгляда
        worldX -= sinYaw * correctionMagnitude;
        worldZ += cosYaw * correctionMagnitude;

        return new Vec3d(baseX + worldX, baseY + worldY, baseZ + worldZ);
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
                System.out.println("[ModParser] Файл модели не найден по пути: " + modelPath);
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

                            // Ищем нашу кость "locators"
                            if (bone.has("name") && bone.get("name").getAsString().equals("locators")) {
                                if (bone.has("locators")) {
                                    JsonObject locatorsObj = bone.getAsJsonObject("locators");

                                    // Ищем наш заветный локатор "for_fire"
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
                                        System.out.println("[ModParser] Успешно загружен локатор " + locatorName + ": " + localPos);
                                        return localPos;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[ModParser] Ошибка при парсинге локатора " + locatorName);
            e.printStackTrace();
        }

        // Если что-то пошло не так, возвращаем нулевой вектор, чтобы не крашить игру
        return Vec3d.ZERO;
    }
}
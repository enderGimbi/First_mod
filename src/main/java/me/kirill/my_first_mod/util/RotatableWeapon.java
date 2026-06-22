package me.kirill.my_first_mod.util;

public interface RotatableWeapon {

    /**
     * Возвращает масштаб модели оружия для 3-го лица.
     * По умолчанию возвращает 1.0f (без изменений).
     * Каждое оружие сможет переопределить это значение под себя.
     */
    default float getThirdPersonScale() {
        return 1.0f;
    }
}
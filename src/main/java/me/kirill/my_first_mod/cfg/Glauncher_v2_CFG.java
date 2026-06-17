package me.kirill.my_first_mod.cfg;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;


@Config(name = "my_first_mod")
public class Glauncher_v2_CFG implements ConfigData {
    public float explosionPower = 5.0f;
    public int fuseDelayTicks = 0;
    public float shootVelocity = 2.0f;
    public float soundVolume = 1.0f;
}

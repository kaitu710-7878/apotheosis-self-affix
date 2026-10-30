package com.yutaka.apotheosis_self_affix.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue AUTO_EQUIP_ON_JOIN;
    public static final ForgeConfigSpec.BooleanValue KEEP_ON_DEATH;
    public static final ForgeConfigSpec.DoubleValue DURABLE_XP_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue DEBUG_LOG;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Apotheosis Self Affix Configuration");

        AUTO_EQUIP_ON_JOIN = builder
                .comment("Whether players should automatically receive and equip a Soul in the Soul slot on first join if missing.")
                .define("autoEquipOnJoin", true);

        KEEP_ON_DEATH = builder
                .comment("Whether the Soul (including its NBT and affixes) is preserved across player death.")
                .define("keepOnDeath", true);

        DURABLE_XP_MULTIPLIER = builder
                .comment("Multiplier applied when converting Durable affix bonus into bonus experience gained. Default 1.0 means 100% bonus conversion.")
                .defineInRange("durableXpMultiplier", 1.0, 0.0, 100.0);

        DEBUG_LOG = builder
                .comment("Enable debug logging for affix triggers.")
                .define("debugLog", false);

        SPEC = builder.build();
    }
}

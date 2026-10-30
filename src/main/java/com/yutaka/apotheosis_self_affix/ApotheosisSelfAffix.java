package com.yutaka.apotheosis_self_affix;

import com.yutaka.apotheosis_self_affix.config.ModConfig;
import com.yutaka.apotheosis_self_affix.item.SoulItem;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ApotheosisSelfAffix.MODID)
public class ApotheosisSelfAffix {
    public static final String MODID = "apotheosis_self_affix";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<SoulItem> SOUL = ITEMS.register("soul", SoulItem::new);

    public static LootCategory SOUL_CATEGORY;

    public ApotheosisSelfAffix() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modBus);
        modBus.addListener(this::setup);

        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.SPEC);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Register custom LootCategory for Soul
            SOUL_CATEGORY = LootCategory.register(null, "soul", stack -> stack.is(SOUL.get()), new EquipmentSlot[0]);
            LOGGER.info("Registered LootCategory 'soul' for Apotheosis Self Affix.");
        });
    }
}

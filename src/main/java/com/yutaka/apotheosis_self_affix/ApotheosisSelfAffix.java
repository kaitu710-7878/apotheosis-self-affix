package com.yutaka.apotheosis_self_affix;

import com.yutaka.apotheosis_self_affix.config.ModConfig;
import com.yutaka.apotheosis_self_affix.item.SoulItem;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;

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
        modBus.addListener(this::enqueueIMC);

        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.SPEC);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Register custom LootCategory for Soul with all equipment slots to ensure attribute modifiers can match
            EquipmentSlot[] allSlots = EquipmentSlot.values();
            SOUL_CATEGORY = LootCategory.register(null, "soul", stack -> stack.is(SOUL.get()), allSlots);
            LOGGER.info("Registered LootCategory 'soul' with all equipment slots for Apotheosis Self Affix.");
        });
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE, () ->
                new SlotTypeMessage.Builder("soul")
                        .icon(new ResourceLocation(MODID, "slot/empty_soul_slot"))
                        .size(1)
                        .priority(150)
                        .build()
        );
        LOGGER.info("Sent Curios IMC to register 'soul' slot.");
    }
}

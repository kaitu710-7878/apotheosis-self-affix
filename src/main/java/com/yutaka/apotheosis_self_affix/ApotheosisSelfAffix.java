package com.yutaka.apotheosis_self_affix;

import com.yutaka.apotheosis_self_affix.command.SoulCommand;
import com.yutaka.apotheosis_self_affix.config.ModConfig;
import com.yutaka.apotheosis_self_affix.item.SoulItem;
import com.yutaka.apotheosis_self_affix.menu.SoulMenu;
import com.yutaka.apotheosis_self_affix.network.ModPacketHandler;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
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

@Mod(ApotheosisSelfAffix.MODID)
public class ApotheosisSelfAffix {
    public static final String MODID = "apotheosis_self_affix";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);

    public static final RegistryObject<SoulItem> SOUL = ITEMS.register("soul", SoulItem::new);
    public static final RegistryObject<MenuType<SoulMenu>> SOUL_MENU = MENUS.register("soul_menu",
            () -> IForgeMenuType.create((windowId, inv, data) -> new SoulMenu(windowId, inv)));

    public static LootCategory SOUL_CATEGORY;

    public ApotheosisSelfAffix() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modBus);
        MENUS.register(modBus);

        modBus.addListener(this::setup);
        modBus.addListener(this::enqueueIMC);

        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);

        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.SPEC);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModPacketHandler.register();

            // Register custom LootCategory for Soul with NO vanilla equipment slots.
            // Soul is strictly an independent accessory, so vanilla slots must be empty
            // to prevent duplicate "When on Head/Chest/Legs/Feet" tooltips.
            EquipmentSlot[] noSlots = new EquipmentSlot[0];
            SOUL_CATEGORY = LootCategory.register(null, "soul", stack -> stack.is(SOUL.get()), noSlots);
            LOGGER.info("Registered LootCategory 'soul' with no vanilla equipment slots for Apotheosis Self Affix.");
        });
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        if (ModList.get().isLoaded("curios")) {
            try {
                InterModComms.sendTo("curios", "register_type", () ->
                        new top.theillusivec4.curios.api.SlotTypeMessage.Builder("soul")
                                .icon(new ResourceLocation(MODID, "slot/empty_soul_slot"))
                                .size(1)
                                .priority(150)
                                .build()
                );
                LOGGER.info("Optional Curios registration sent for backward compatibility.");
            } catch (Throwable ignored) {
            }
        }
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        SoulCommand.register(event.getDispatcher());
    }
}

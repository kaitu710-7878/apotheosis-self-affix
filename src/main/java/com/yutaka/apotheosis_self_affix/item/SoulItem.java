package com.yutaka.apotheosis_self_affix.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.yutaka.apotheosis_self_affix.capability.ISoulHandler;
import com.yutaka.apotheosis_self_affix.capability.SoulCapabilityProvider;
import com.yutaka.apotheosis_self_affix.config.ModConfig;
import com.yutaka.apotheosis_self_affix.manager.SoulAttributeManager;
import com.yutaka.apotheosis_self_affix.util.SoulAffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class SoulItem extends Item {

    private static final DecimalFormat ATTRIBUTE_FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private static final UUID DUMMY_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    public SoulItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .fireResistant()
                .rarity(Rarity.EPIC));
    }

    /**
     * Right-clicking with a Soul in hand directly equips it into the player's dedicated Soul Slot.
     * If the slot already has a soul, it swaps them.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        stripGems(held);

        if (!level.isClientSide) {
            player.getCapability(SoulCapabilityProvider.SOUL_CAPABILITY).ifPresent(handler -> {
                ItemStack currentEquipped = handler.getSoul();
                if (currentEquipped.isEmpty()) {
                    handler.setSoul(held.copy());
                    held.shrink(1);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.2F);
                    player.displayClientMessage(Component.translatable("message.apotheosis_self_affix.equipped").withStyle(ChatFormatting.AQUA), true);
                } else {
                    // Swap
                    ItemStack toEquip = held.copy();
                    toEquip.setCount(1);
                    ItemStack toReturn = currentEquipped.copy();

                    handler.setSoul(toEquip);
                    player.setItemInHand(hand, toReturn);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.PLAYERS, 0.8F, 1.0F);
                    player.displayClientMessage(Component.translatable("message.apotheosis_self_affix.swapped").withStyle(ChatFormatting.AQUA), true);
                }
                SoulAttributeManager.updateSoulAttributes(player);
            });
        }

        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        // Suppress all vanilla slot modifiers to eliminate duplicate vanilla "When on Head/Chest/..." headers
        return ImmutableMultimap.of();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        stripGems(stack);
    }

    public static void stripGems(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return;
        net.minecraft.nbt.CompoundTag tag = stack.getTagElement(AffixHelper.AFFIX_DATA);
        if (tag != null) {
            tag.remove(dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper.GEMS);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag flag) {
        stripGems(stack);
        tooltips.add(Component.translatable("tooltip.apotheosis_self_affix.soul_desc").withStyle(ChatFormatting.DARK_AQUA));
        if (ModConfig.KEEP_ON_DEATH.get()) {
            tooltips.add(Component.translatable("tooltip.apotheosis_self_affix.keep_on_death").withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        int sockets = dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper.getSockets(stack);
        if (sockets > 0) {
            tooltips.add(Component.translatable("tooltip.apotheosis_self_affix.socket_buff").withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        // Render dedicated Soul Slot attribute modifiers directly
        if (AffixHelper.hasAffixes(stack) || sockets > 0) {
            Multimap<Attribute, AttributeModifier> modifiers = SoulAffixHelper.getSoulAttributeModifiers(stack, DUMMY_UUID);
            if (!modifiers.isEmpty()) {
                tooltips.add(Component.empty());
                tooltips.add(Component.translatable("tooltip.apotheosis_self_affix.soul_slot_header").withStyle(ChatFormatting.GOLD));
                for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
                    Attribute attr = entry.getKey();
                    AttributeModifier mod = entry.getValue();
                    double amount = mod.getAmount();
                    if (Math.abs(amount) < 0.0001) continue;

                    double displayAmount = amount;
                    String formattedNum;
                    if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                        formattedNum = (displayAmount > 0 ? "+" : "") + ATTRIBUTE_FORMAT.format(displayAmount);
                    } else {
                        displayAmount = amount * 100.0;
                        formattedNum = (displayAmount > 0 ? "+" : "") + ATTRIBUTE_FORMAT.format(displayAmount) + "%";
                    }

                    MutableComponent attrComp = Component.translatable(attr.getDescriptionId());
                    MutableComponent line = Component.literal("  " + formattedNum + " ").append(attrComp);
                    if (amount > 0) {
                        line.withStyle(ChatFormatting.BLUE);
                    } else {
                        line.withStyle(ChatFormatting.RED);
                    }
                    tooltips.add(line);
                }
            }
        }

        super.appendHoverText(stack, level, tooltips, flag);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || AffixHelper.hasAffixes(stack);
    }
}

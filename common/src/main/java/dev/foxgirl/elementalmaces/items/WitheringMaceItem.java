package dev.foxgirl.elementalmaces.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

public class WitheringMaceItem extends AbstractElementalMaceItem {

    public WitheringMaceItem() {
        super(
            properties -> properties
                .component(DataComponents.ITEM_NAME, Component.translatable("item.elementalmaces.withering_mace").withStyle(ChatFormatting.DARK_GRAY))
                .component(DataComponents.LORE, ItemLore.EMPTY
                    .withLineAdded(Component.translatable("item.elementalmaces.withering_mace.lore.line1"))
                    .withLineAdded(Component.translatable("item.elementalmaces.withering_mace.lore.line2"))
                ),
            attributes -> attributes
                .withModifierAdded(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 8.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .withModifierAdded(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.4F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
        );
    }

    @Override
    protected void onTick(ItemStack stack, ServerPlayer player, int slot, boolean isSelected, boolean isInHands) {
        if (isSelected || isInHands) {
            player.removeEffect(MobEffects.WITHER);
        }
    }

    @Override
    protected boolean onSmashAttack(ItemStack stack, ServerPlayer player, LivingEntity target) {
        getNearbyTargets(player, target, 4.0).forEach(entity -> {
            entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 8 * 20, 1));
        });

        var effectCloud = new AreaEffectCloud(player.level(), player.getX(), target.getY(), player.getZ());
        effectCloud.setOwner(player);
        effectCloud.setRadius(4.0F);
        effectCloud.setRadiusOnUse(-0.5F);
        effectCloud.setWaitTime(0);
        effectCloud.setDuration(5 * 20);
        effectCloud.setRadiusPerTick(0.0F);
        // effectCloud.setRadiusPerTick((-effectCloud.getRadius() / (float) effectCloud.getDuration()) * 0.1F);
        effectCloud.addEffect(new MobEffectInstance(MobEffects.WITHER, 8 * 20, 1));
        player.level().addFreshEntity(effectCloud);

        player.level().playSound(null, player.getX(), player.getY() + player.getEyeHeight(), player.getZ(), SoundEvents.WITHER_AMBIENT, SoundSource.PLAYERS, 3.0F, 1.0F);

        return true;
    }

}

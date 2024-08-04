package dev.foxgirl.elementalmaces.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

public class ResonatingMaceItem extends AbstractElementalMaceItem {

    public ResonatingMaceItem() {
        super(
            properties -> properties
                .component(DataComponents.ITEM_NAME, Component.translatable("item.elementalmaces.resonating_mace").withStyle(ChatFormatting.DARK_AQUA))
                .component(DataComponents.LORE, ItemLore.EMPTY
                    .withLineAdded(Component.translatable("item.elementalmaces.resonating_mace.lore.line1"))
                    .withLineAdded(Component.translatable("item.elementalmaces.resonating_mace.lore.line2"))
                ),
            attributes -> attributes
                .withModifierAdded(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 11.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .withModifierAdded(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.4F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
        );
    }

    @Override
    protected void onTick(ItemStack stack, ServerPlayer player, int slot, boolean isSelected, boolean isInHands) {
    }

    @Override
    protected boolean onSmashAttack(ItemStack stack, ServerPlayer player, LivingEntity target) {
        getNearbyTargets(player, target, 4.0).forEach(entity -> {
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 5 * 20));
        });

        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20));

        for (int i = 0; i < 20; i++) {
            player.serverLevel().sendParticles(
                ParticleTypes.SONIC_BOOM,
                player.getRandomX(5.0),
                Math.min(player.getY(), target.getY()) + 0.2 + 4.0 * player.getRandom().nextDouble(),
                player.getRandomZ(5.0),
                1, 0.0, 0.0, 0.0, 0.0
            );
        }

        player.level().playSound(null, player.getX(), player.getY() + player.getEyeHeight(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 3.0F, 1.0F);

        return true;
    }

}

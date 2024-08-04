package dev.foxgirl.elementalmaces.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EnderMaceItem extends AbstractElementalMaceItem {

    public EnderMaceItem() {
        super(
            properties -> properties
                .component(DataComponents.ITEM_NAME, Component.translatable("item.elementalmaces.ender_mace").withStyle(ChatFormatting.LIGHT_PURPLE))
                .component(DataComponents.LORE, ItemLore.EMPTY
                    .withLineAdded(Component.translatable("item.elementalmaces.ender_mace.lore.line1"))
                    .withLineAdded(Component.translatable("item.elementalmaces.ender_mace.lore.line2"))
                ),
            attributes -> attributes
                .withModifierAdded(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 5.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .withModifierAdded(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.4F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
        );
    }

    @Override
    protected void onTick(ItemStack stack, ServerPlayer player, int slot, boolean isSelected, boolean isInHands) {
    }

    @Override
    protected boolean onSmashAttack(ItemStack stack, ServerPlayer player, LivingEntity target) {
        getNearbyTargets(player, target, 4.0).forEach(entity -> {
            player.serverLevel().explode(
                player,
                player.damageSources().explosion(player, player),
                new ExplosionDamageCalculator() {
                    @Override
                    public boolean shouldBlockExplode(Explosion explosion, BlockGetter reader, BlockPos pos, BlockState state, float power) {
                        return false;
                    }
                    @Override
                    public boolean shouldDamageEntity(Explosion explosion, Entity target) {
                        return target == entity;
                    }
                    @Override
                    public float getKnockbackMultiplier(Entity target) {
                        return 1.0F;
                    }
                    @Override
                    public float getEntityDamageAmount(Explosion explosion, Entity target) {
                        return 0.1F;
                    }
                },
                entity.getX(),
                entity.getY() + 0.1,
                entity.getZ(),
                0.1F,
                false,
                Level.ExplosionInteraction.TRIGGER,
                ParticleTypes.GUST_EMITTER_SMALL,
                ParticleTypes.GUST_EMITTER_LARGE,
                SoundEvents.BREEZE_WIND_CHARGE_BURST
            );
            entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 5 * 20));
        });

        var effectCloud = new AreaEffectCloud(player.level(), player.getX(), target.getY(), player.getZ());
        effectCloud.setOwner(player);
        effectCloud.setRadius(3.5F);
        effectCloud.setRadiusOnUse(-0.5F);
        effectCloud.setWaitTime(0);
        effectCloud.setDuration(3 * 20);
        effectCloud.setRadiusPerTick(-effectCloud.getRadius() / (float) effectCloud.getDuration());
        effectCloud.setParticle(ParticleTypes.DRAGON_BREATH);
        player.level().addFreshEntity(effectCloud);

        player.level().playSound(null, player.getX(), player.getY() + player.getEyeHeight(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 3.0F, 1.0F);

        return true;
    }

}

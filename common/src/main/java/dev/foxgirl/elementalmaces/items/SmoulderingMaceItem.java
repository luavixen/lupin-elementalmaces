package dev.foxgirl.elementalmaces.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.level.block.Blocks;

public class SmoulderingMaceItem extends AbstractElementalMaceItem {

    public SmoulderingMaceItem() {
        super(
            properties -> properties
                .component(DataComponents.ITEM_NAME, Component.translatable("item.elementalmaces.smouldering_mace").withStyle(ChatFormatting.RED))
                .component(DataComponents.LORE, ItemLore.EMPTY
                    .withLineAdded(Component.translatable("item.elementalmaces.smouldering_mace.lore.line1"))
                    .withLineAdded(Component.translatable("item.elementalmaces.smouldering_mace.lore.line2"))
                ),
            attributes -> attributes
                .withModifierAdded(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 2.5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .withModifierAdded(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.4F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
        );
    }

    @Override
    protected void onTick(ItemStack stack, ServerPlayer player, int slot, boolean isSelected, boolean isInHands) {
        if (isSelected || isInHands) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 70));
        }
    }

    @Override
    protected boolean onSmashAttack(ItemStack stack, ServerPlayer player, LivingEntity target) {
        getNearbyTargets(player, target, 4.0).forEach(entity -> {
            entity.igniteForSeconds(5.0F);
        });

        var playerPos = player.blockPosition();
        for (int x = playerPos.getX() - 5; x <= playerPos.getX() + 5; x++) {
            for (int z = playerPos.getZ() - 5; z <= playerPos.getZ() + 5; z++) {
                if (player.distanceToSqr(x + 0.5, player.getY(), z + 0.5) > 3.5 * 3.5) {
                    continue;
                }
                for (int y = playerPos.getY() + 5; y >= playerPos.getY() - 10; y--) {
                    var blockPos = new BlockPos(x, y, z);
                    if (
                        player.level().getBlockState(blockPos).isAir() &&
                        player.level().getBlockState(blockPos.below()).isSolid()
                    ) {
                        player.level().setBlockAndUpdate(blockPos, Blocks.FIRE.defaultBlockState());
                        break;
                    }
                }
            }
        }

        player.level().playSound(null, player.getX(), player.getY() + player.getEyeHeight(), player.getZ(), SoundEvents.BLAZE_AMBIENT, SoundSource.PLAYERS, 3.0F, 1.0F);

        return true;
    }

}

package dev.foxgirl.elementalmaces.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.Function;

public abstract class AbstractElementalMaceItem extends MaceItem {

    public AbstractElementalMaceItem(
        Function<Item.Properties, Item.Properties> propertiesFunction,
        Function<ItemAttributeModifiers, ItemAttributeModifiers> attributesFunction
    ) {
        super(propertiesFunction.apply(
            new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .durability(500)
                .component(DataComponents.TOOL, MaceItem.createToolProperties())
                .attributes(attributesFunction.apply(MaceItem.createAttributes()))
        ));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (entity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) entity;
            onTick(stack, player, slotId, isSelected, player.getMainHandItem() == stack || player.getOffhandItem() == stack);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (super.hurtEnemy(stack, target, attacker)) {
            if (attacker instanceof ServerPlayer && canSmashAttack(attacker)) {
                return onSmashAttack(stack, (ServerPlayer) attacker, target);
            } else {
                return true;
            }
        }
        return false;
    }

    protected static List<LivingEntity> getNearbyTargets(ServerPlayer player, LivingEntity target, double range) {
        return player.level().getEntitiesOfClass(
            LivingEntity.class,
            AABB.ofSize(target.position(), 0.0, 0.0, 0.0).inflate(range),
            entity -> entity != player && entity.isAlive()
        );
    }

    protected abstract void onTick(ItemStack stack, ServerPlayer player, int slot, boolean isSelected, boolean isInHands);
    protected abstract boolean onSmashAttack(ItemStack stack, ServerPlayer player, LivingEntity target);

}

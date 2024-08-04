package dev.foxgirl.elementalmaces;

import dev.foxgirl.elementalmaces.items.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public final class ElementalMaces {

    public static final Logger LOGGER = LogManager.getLogger();

    public interface ItemRegisterAdapter {
        ResourceLocation getID(Item item);
        Item getItem(ResourceLocation id);

        <T extends Item> Supplier<T> add(String name, Supplier<T> supplier);
        void register();
    }

    public interface LoaderAdapter {
        ItemRegisterAdapter createItemRegister(String modID);
        boolean isModLoaded(String modID);
        boolean isDedicatedServer();
    }

    public final LoaderAdapter loader;
    public final ItemRegisterAdapter items;

    public final Supplier<AbstractElementalMaceItem> resonatingMace, witheringMace, enderMace, smoulderingMace;
    public final Supplier<Item> resonatingRod, witheringRod;

    public ElementalMaces(LoaderAdapter loader) {
        LOGGER.info("Setting up Elemental Maces...");

        this.loader = loader;
        this.items = loader.createItemRegister("elementalmaces");

        this.resonatingMace = items.add("resonating_mace", ResonatingMaceItem::new);
        this.witheringMace = items.add("withering_mace", WitheringMaceItem::new);
        this.enderMace = items.add("ender_mace", EnderMaceItem::new);
        this.smoulderingMace = items.add("smouldering_mace", SmoulderingMaceItem::new);
        this.resonatingRod = items.add("resonating_rod", () -> new Item(new Item.Properties()));
        this.witheringRod = items.add("withering_rod", () -> new Item(new Item.Properties()));
    }

    public List<AbstractElementalMaceItem> getMaceItems() {
        return Arrays.asList(
            resonatingMace.get(),
            witheringMace.get(),
            enderMace.get(),
            smoulderingMace.get()
        );
    }
    public List<Item> getRodItems() {
        return Arrays.asList(
            resonatingRod.get(),
            witheringRod.get()
        );
    }

    public void registerItems() {
        LOGGER.info("Registering items...");
        items.register();
    }

    public void registerMace3DModels() {
        if (!loader.isDedicatedServer() && loader.isModLoaded("mace3d")) {
            LOGGER.info("Registering external 3D mace models");
            try {
                MethodHandle registerModelMethod = MethodHandles.lookup().findStatic(
                    Class.forName("dev.foxgirl.mace3d.Mace3D"), "registerExternalMaceModel",
                    MethodType.methodType(void.class, Item.class, ResourceLocation.class, ResourceLocation.class)
                );
                for (var item : getMaceItems()) {
                    var id = items.getID(item);
                    registerModelMethod.invoke(item, id, id.withSuffix("_in_hand"));
                }
            } catch (Throwable cause) {
                LOGGER.error("Failed to register external 3D mace models", cause);
            }
        } else {
            LOGGER.info("Skipping external 3D mace models, 'mace3d' is not loaded");
        }
    }

    public void registerAll() {
        registerItems();
        registerMace3DModels();
    }

    public boolean shouldIgnoreDamage(LivingEntity entity, DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_FIRE) && entity.isHolding(smoulderingMace.get())) {
            return true;
        }
        if (source.is(DamageTypes.WITHER) && entity.isHolding(witheringMace.get())) {
            return true;
        }
        return false;
    }

}

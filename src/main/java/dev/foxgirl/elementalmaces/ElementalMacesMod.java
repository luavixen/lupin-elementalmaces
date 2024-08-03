package dev.foxgirl.elementalmaces;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.utils.Env;
import dev.foxgirl.elementalmaces.items.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class ElementalMacesMod implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create("elementalmaces", Registries.ITEM);

    public static final RegistrySupplier<AbstractElementalMaceItem> WITHERING_MACE = ITEMS.register("withering_mace", WitheringMaceItem::new);
    public static final RegistrySupplier<AbstractElementalMaceItem> RESONATING_MACE = ITEMS.register("resonating_mace", ResonatingMaceItem::new);
    public static final RegistrySupplier<AbstractElementalMaceItem> ENDER_MACE = ITEMS.register("ender_mace", EnderMaceItem::new);
    public static final RegistrySupplier<AbstractElementalMaceItem> SMOULDERING_MACE = ITEMS.register("smouldering_mace", SmoulderingMaceItem::new);

    public static final RegistrySupplier<Item> WITHERING_ROD = ITEMS.register("withering_rod", () -> new Item(new Item.Properties()));
    public static final RegistrySupplier<Item> RESONATING_ROD = ITEMS.register("resonating_rod", () -> new Item(new Item.Properties()));

    public static void forEachMaceItem(Consumer<AbstractElementalMaceItem> consumer) {
        consumer.accept(WITHERING_MACE.get());
        consumer.accept(RESONATING_MACE.get());
        consumer.accept(ENDER_MACE.get());
        consumer.accept(SMOULDERING_MACE.get());
    }
    public static void forEachRodItem(Consumer<Item> consumer) {
        consumer.accept(WITHERING_ROD.get());
        consumer.accept(RESONATING_ROD.get());
    }

    private static void tryRegisterMace3DModels() {
        if (
            Platform.getEnvironment() == Env.CLIENT &&
            Platform.isModLoaded("mace3d")
        ) {
            LOGGER.info("(Mace3D support) Registering external 3D mace models");
            try {
                Class<?> mace3D = Class.forName("dev.foxgirl.mace3d.Mace3D");
                Method registerMethod = mace3D.getMethod("registerExternalMaceModel", Item.class, ResourceLocation.class, ResourceLocation.class);
                registerMethod.invoke(null, WITHERING_MACE.get(),
                    ResourceLocation.fromNamespaceAndPath("elementalmaces", "withering_mace"),
                    ResourceLocation.fromNamespaceAndPath("elementalmaces", "withering_mace_in_hand")
                );
                registerMethod.invoke(null, RESONATING_MACE.get(),
                    ResourceLocation.fromNamespaceAndPath("elementalmaces", "resonating_mace"),
                    ResourceLocation.fromNamespaceAndPath("elementalmaces", "resonating_mace_in_hand")
                );
                registerMethod.invoke(null, ENDER_MACE.get(),
                    ResourceLocation.fromNamespaceAndPath("elementalmaces", "ender_mace"),
                    ResourceLocation.fromNamespaceAndPath("elementalmaces", "ender_mace_in_hand")
                );
                registerMethod.invoke(null, SMOULDERING_MACE.get(),
                    ResourceLocation.fromNamespaceAndPath("elementalmaces", "smouldering_mace"),
                    ResourceLocation.fromNamespaceAndPath("elementalmaces", "smouldering_mace_in_hand")
                );
            } catch (ReflectiveOperationException cause) {
                LOGGER.error("(Mace3D support) Failed to register external 3D mace models", cause);
            }
        } else {
            LOGGER.info("(Mace3D support) Mace3D is not loaded, skipping external 3D mace models");
        }
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Hello from Elemental Maces :3c");

        ITEMS.register();

        tryRegisterMace3DModels();

        ItemGroupEvents
            .modifyEntriesEvent(CreativeModeTabs.COMBAT)
            .register(content -> forEachMaceItem(item -> content.addAfter(Items.MACE, item)));
        ItemGroupEvents
            .modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
            .register(content -> forEachRodItem(item -> content.addAfter(Items.BREEZE_ROD, item)));

        EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            if (source.is(DamageTypes.IN_FIRE) && entity.isHolding(SMOULDERING_MACE.get())) {
                return EventResult.interruptFalse();
            }
            if (source.is(DamageTypes.WITHER) && entity.isHolding(WITHERING_MACE.get())) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });
    }

}

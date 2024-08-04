package dev.foxgirl.elementalmaces;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.DeferredRegister;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Supplier;

public final class ElementalMacesMod implements ModInitializer {

    private final ElementalMaces elementalMacesImpl;

    private ElementalMaces.ItemRegisterAdapter createItemRegisterAdapter(String modID) {
        return new ElementalMaces.ItemRegisterAdapter() {
            private final DeferredRegister<Item> register = DeferredRegister.create(modID, Registries.ITEM);

            @Override
            public ResourceLocation getID(Item item) {
                return register.getRegistrar().getId(item);
            }
            @Override
            public Item getItem(ResourceLocation id) {
                return register.getRegistrar().get(id);
            }

            @Override
            public <T extends Item> Supplier<T> add(String name, Supplier<T> supplier) {
                return register.register(name, supplier);
            }
            @Override
            public void register() {
                register.register();
            }
        };
    }

    private ElementalMaces.LoaderAdapter createLoaderAdapter() {
        return new ElementalMaces.LoaderAdapter() {
            @Override
            public ElementalMaces.ItemRegisterAdapter createItemRegister(String modID) {
                return createItemRegisterAdapter(modID);
            }

            @Override
            public boolean isModLoaded(String modID) {
                return Platform.isModLoaded(modID);
            }
            @Override
            public boolean isDedicatedServer() {
                return Platform.getEnv() == EnvType.SERVER;
            }
        };
    }

    public ElementalMacesMod() {
        elementalMacesImpl = new ElementalMaces(createLoaderAdapter());
    }

    @Override
    public void onInitialize() {
        elementalMacesImpl.registerAll();

        ItemGroupEvents
            .modifyEntriesEvent(CreativeModeTabs.COMBAT)
            .register(content ->
                elementalMacesImpl.getMaceItems().forEach(item -> content.addAfter(Items.MACE, item))
            );
        ItemGroupEvents
            .modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
            .register(content ->
                elementalMacesImpl.getRodItems().forEach(item -> content.addAfter(Items.BREEZE_ROD, item))
            );

        EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            return elementalMacesImpl.shouldIgnoreDamage(entity, source, amount)
                ? EventResult.interruptFalse()
                : EventResult.pass();
        });
    }

}

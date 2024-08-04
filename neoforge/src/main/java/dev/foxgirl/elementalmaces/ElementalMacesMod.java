package dev.foxgirl.elementalmaces;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@Mod("elementalmaces")
public final class ElementalMacesMod {

    private final IEventBus modEventBus, forgeEventBus;

    private final ElementalMaces elementalMacesImpl;

    private ElementalMaces.ItemRegisterAdapter createItemRegisterAdapter(String modID) {
        return new ElementalMaces.ItemRegisterAdapter() {
            private final DeferredRegister<Item> register = DeferredRegister.create(Registries.ITEM, modID);

            @Override
            public ResourceLocation getID(Item item) {
                return register.getRegistry().get().getKey(item);
            }
            @Override
            public Item getItem(ResourceLocation id) {
                return register.getRegistry().get().get(id);
            }

            @Override
            public <T extends Item> Supplier<T> add(String name, Supplier<T> supplier) {
                return register.register(name, supplier);
            }
            @Override
            public void register() {
                register.register(modEventBus);
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
                return ModList.get().isLoaded(modID);
            }
            @Override
            public boolean isDedicatedServer() {
                return FMLLoader.getDist().isDedicatedServer();
            }
        };
    }

    public ElementalMacesMod(IEventBus modEventBusImpl) {
        modEventBus = modEventBusImpl;
        forgeEventBus = NeoForge.EVENT_BUS;

        elementalMacesImpl = new ElementalMaces(createLoaderAdapter());
        elementalMacesImpl.registerItems();

        modEventBus.addListener(this::onFMLCommonSetupEvent);
        modEventBus.addListener(this::onBuildCreativeModeTabContentsEvent);
        forgeEventBus.addListener(this::onLivingIncomingDamageEvent);
    }

    private void onFMLCommonSetupEvent(FMLCommonSetupEvent event) {
        elementalMacesImpl.registerMace3DModels();
    }

    private void onBuildCreativeModeTabContentsEvent(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            elementalMacesImpl.getMaceItems().forEach(item -> {
                event.insertAfter(new ItemStack(Items.MACE), new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            });
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            elementalMacesImpl.getRodItems().forEach(item -> {
                event.insertAfter(new ItemStack(Items.BREEZE_ROD), new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            });
        }
    }

    private void onLivingIncomingDamageEvent(LivingIncomingDamageEvent event) {
        if (elementalMacesImpl.shouldIgnoreDamage(event.getEntity(), event.getSource(), event.getAmount())) {
            event.setCanceled(true);
        }
    }

}

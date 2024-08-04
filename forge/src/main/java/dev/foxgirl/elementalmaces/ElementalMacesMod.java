package dev.foxgirl.elementalmaces;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

@Mod("elementalmaces")
public final class ElementalMacesMod {

    private final IEventBus modEventBus, forgeEventBus;

    private final ElementalMaces elementalMacesImpl;

    private ElementalMaces.ItemRegisterAdapter createItemRegisterAdapter(String modID) {
        return new ElementalMaces.ItemRegisterAdapter() {
            private final DeferredRegister<Item> register = DeferredRegister.create(ForgeRegistries.ITEMS, modID);

            @Override
            public ResourceLocation getID(Item item) {
                return ForgeRegistries.ITEMS.getKey(item);
            }
            @Override
            public Item getItem(ResourceLocation id) {
                return ForgeRegistries.ITEMS.getValue(id);
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

    public ElementalMacesMod() {
        modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        forgeEventBus = MinecraftForge.EVENT_BUS;

        elementalMacesImpl = new ElementalMaces(createLoaderAdapter());
        elementalMacesImpl.registerItems();

        modEventBus.addListener(this::onFMLCommonSetupEvent);
        modEventBus.addListener(this::onBuildCreativeModeTabContentsEvent);
        forgeEventBus.addListener(this::onLivingAttackEvent);
    }

    private void onFMLCommonSetupEvent(FMLCommonSetupEvent event) {
        elementalMacesImpl.registerMace3DModels();
    }

    private void onBuildCreativeModeTabContentsEvent(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            elementalMacesImpl.getMaceItems().forEach(event::accept);
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            elementalMacesImpl.getRodItems().forEach(event::accept);
        }
    }

    private void onLivingAttackEvent(LivingAttackEvent event) {
        if (elementalMacesImpl.shouldIgnoreDamage(event.getEntity(), event.getSource(), event.getAmount())) {
            event.setCanceled(true);
        }
    }

}

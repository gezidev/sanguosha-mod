package cn.solo.sanguosha;

import cn.solo.sanguosha.chessboard.ChessboardCommands;
import cn.solo.sanguosha.chessboard.ModChessboards;
import cn.solo.sanguosha.chessboard.block.ChessboardBlock;
import cn.solo.sanguosha.chessboard.client.ChessboardClient;
import cn.solo.sanguosha.chessboard.client.renderer.ChessboardRenderer;
import cn.solo.sanguosha.client.CardItemBakedModel;
import cn.solo.sanguosha.client.ClientGeneralCatalog;
import cn.solo.sanguosha.client.GameTable2Screen;
import cn.solo.sanguosha.client.GroundCardRenderer;
import cn.solo.sanguosha.client.HandContainerScreen;
import cn.solo.sanguosha.client.TableBlockEntityRenderer;
import cn.solo.sanguosha.command.SgsCommands;
import cn.solo.sanguosha.config.GeneralAssetManager;
import cn.solo.sanguosha.config.GeneralManager;
import cn.solo.sanguosha.deck.PlayerDeckDrawConfig;
import cn.solo.sanguosha.game.GameRoomManager;
import cn.solo.sanguosha.game.HandPouchSessionManager;
import cn.solo.sanguosha.item.GeneralCardItem;
import cn.solo.sanguosha.item.HealthCardItem;
import cn.solo.sanguosha.item.PlaceableCardItem;
import cn.solo.sanguosha.network.ModNetwork;
import cn.solo.sanguosha.registry.ModBlockEntities;
import cn.solo.sanguosha.registry.ModBlocks;
import cn.solo.sanguosha.registry.ModEntities;
import cn.solo.sanguosha.registry.ModItems;
import cn.solo.sanguosha.registry.ModMenus;
import cn.solo.sanguosha.server.HandContainerCountTracker;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(value="sanguosha")
public final class SanguoshaMod {
    public static final String MOD_ID = "sanguosha";

    public SanguoshaMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModItems.TABS.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModChessboards.BLOCKS.register(modBus);
        ModChessboards.ITEMS.register(modBus);
        ModChessboards.BLOCK_ENTITIES.register(modBus);
        ModNetwork.register();
        GeneralManager.reload();
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
        MinecraftForge.EVENT_BUS.addListener(this::playerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(this::playerLoggedOut);
        MinecraftForge.EVENT_BUS.addListener(this::playerTick);
        MinecraftForge.EVENT_BUS.addListener(this::playerClone);
        MinecraftForge.EVENT_BUS.addListener(this::playerChangedDimension);
        MinecraftForge.EVENT_BUS.addListener(this::playerRespawn);
        MinecraftForge.EVENT_BUS.addListener(this::livingDeath);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopped);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        SgsCommands.register((CommandDispatcher<CommandSourceStack>)event.getDispatcher());
        ChessboardCommands.register(event.getDispatcher());
    }

    private void serverStarting(ServerStartingEvent event) {
        GeneralManager.reload();
        GameRoomManager.clear();
        HandPouchSessionManager.clear();
    }

    private void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            GameRoomManager.login(player2);
            ModNetwork.sendSyncCustomGenerals(player2);
        }
    }

    private void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            GameRoomManager.disconnect(player2);
            HandPouchSessionManager.disconnect(player2);
            HandContainerCountTracker.clear(player2);
        }
    }

    private void playerTick(TickEvent.PlayerTickEvent event) {
        Player player;
        if (event.phase == TickEvent.Phase.END && (player = event.player) instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            GameRoomManager.tick(player2);
            HandContainerCountTracker.tick(player2);
        }
    }

    private void playerClone(PlayerEvent.Clone event) {
        PlayerDeckDrawConfig.copyOnClone(event.getOriginal(), event.getEntity());
        Player player = event.getOriginal();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            HandContainerCountTracker.clear(player2);
        }
    }

    private void playerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            HandContainerCountTracker.clear(player2);
        }
    }

    private void playerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            HandContainerCountTracker.clear(player2);
        }
    }

    private void livingDeath(LivingDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)livingEntity;
            HandContainerCountTracker.clear(player);
        }
    }

    private void serverStopped(ServerStoppedEvent event) {
        GameRoomManager.clear();
        HandPouchSessionManager.clear();
        HandContainerCountTracker.clearAll();
    }

    @Mod.EventBusSubscriber(modid="sanguosha", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
    public static final class ClientEvents {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                GeneralManager.reload();
                ClientGeneralCatalog.reload();
                ChessboardBlock.openScreenAction = pos -> ChessboardClient.openScreen(pos);
                ChessboardBlock.openMenuKeyDown = () -> {
                    Minecraft mc = Minecraft.m_91087_();
                    return ChessboardClient.OPEN_MENU.m_90859_() || (mc.f_91074_ != null && mc.f_91074_.m_6047_());
                };
                MenuScreens.m_96206_((MenuType)((MenuType)ModMenus.HAND_CONTAINER.get()), HandContainerScreen::new);
                MenuScreens.m_96206_((MenuType)((MenuType)ModMenus.GAME_TABLE_2.get()), GameTable2Screen::new);
                ItemProperties.register((Item)((Item)ModItems.GENERAL.get()), (ResourceLocation)new ResourceLocation(SanguoshaMod.MOD_ID, "general_index"), (stack, level, entity, seed) -> {
                    String id = GeneralCardItem.id(stack);
                    if (id.startsWith("custom_")) {
                        return 0.0f;
                    }
                    for (int i = 0; i < GeneralAssetManager.assets().size(); ++i) {
                        if (!GeneralAssetManager.assets().get(i).id().equals(id)) continue;
                        return i + 1;
                    }
                    return 0.0f;
                });
                ItemProperties.register((Item)((Item)ModItems.HEALTH.get()), (ResourceLocation)new ResourceLocation(SanguoshaMod.MOD_ID, "health_index"), (stack, level, entity, seed) -> HealthCardItem.getHealth(stack));
            });
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer((EntityType)ModEntities.GROUND_CARD.get(), GroundCardRenderer::new);
            event.registerBlockEntityRenderer((BlockEntityType)ModBlockEntities.GAME_TABLE_2.get(), TableBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(ModChessboards.CHESSBOARD_BE.get(), ChessboardRenderer::new);
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            ChessboardClient.registerKeys(event);
        }

        @SubscribeEvent
        public static void wrapCardItemModels(ModelEvent.ModifyBakingResult event) {
            ModItems.ITEMS.getEntries().stream().filter(entry -> entry.get() instanceof PlaceableCardItem).forEach(entry -> {
                ModelResourceLocation model = new ModelResourceLocation(entry.getId(), "inventory");
                event.getModels().computeIfPresent(model, (location, original) -> entry.get() instanceof GeneralCardItem ? CardItemBakedModel.generalCard(original) : new CardItemBakedModel((BakedModel)original));
            });
        }
    }
}


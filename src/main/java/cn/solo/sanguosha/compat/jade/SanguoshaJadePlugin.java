package cn.solo.sanguosha.compat.jade;

import cn.solo.sanguosha.block.GameTable2BlockEntity;
import cn.solo.sanguosha.block.TableBlockEntity;
import cn.solo.sanguosha.entity.GroundCardEntity;
import cn.solo.sanguosha.item.GenericCardItem;
import cn.solo.sanguosha.item.StandardCardItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import snownee.jade.api.Accessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ViewGroup;

@WailaPlugin(value="sanguosha")
public final class SanguoshaJadePlugin
implements IWailaPlugin {
    private static final ResourceLocation TABLE_STORAGE = new ResourceLocation("sanguosha", "game_table2_item_storage");

    public void register(IWailaCommonRegistration registration) {
        if (!ModList.get().isLoaded("jade")) {
            return;
        }
        registration.registerItemStorage((IServerExtensionProvider)TableItemStorageProvider.INSTANCE, GameTable2BlockEntity.class);
    }

    public void registerClient(IWailaClientRegistration registration) {
        if (!ModList.get().isLoaded("jade")) {
            return;
        }
        registration.registerItemStorageClient((IClientExtensionProvider)TableItemStorageClientProvider.INSTANCE);
        registration.addTooltipCollectedCallback((tooltip, accessor) -> {
            CompoundTag tag;
            EntityAccessor entityAccessor;
            Entity patt2233$temp;
            if (!(accessor instanceof EntityAccessor) || !((patt2233$temp = (entityAccessor = (EntityAccessor)accessor).getEntity()) instanceof GroundCardEntity)) {
                return;
            }
            GroundCardEntity cardEntity = (GroundCardEntity)patt2233$temp;
            ItemStack stack = cardEntity.getCard();
            if (cardEntity.isFaceDown()) {
                tooltip.clear();
                return;
            }
            Item patt2481$temp = stack.m_41720_();
            if (patt2481$temp instanceof StandardCardItem) {
                StandardCardItem standard = (StandardCardItem)patt2481$temp;
                SanguoshaJadePlugin.replaceWithVisibleCard(tooltip, stack, standard.suit(), standard.rank());
            } else if (stack.m_41720_() instanceof GenericCardItem && (tag = stack.m_41783_()) != null) {
                SanguoshaJadePlugin.replaceWithVisibleCard(tooltip, stack, tag.m_128461_("SanguoshaSuit"), tag.m_128461_("SanguoshaRank"));
            }
        });
    }

    public static List<ItemStack> visibleStorage(TableBlockEntity table) {
        ArrayList<ItemStack> visible = new ArrayList<ItemStack>();
        for (int slot = 0; slot < 7; ++slot) {
            ItemStack original;
            if (slot == 0 || (original = table.items().getStackInSlot(slot)).m_41619_()) continue;
            visible.add(original.m_41777_());
        }
        return visible;
    }

    private static void replaceWithVisibleCard(ITooltip tooltip, ItemStack stack, String suit, String rank) {
        tooltip.clear();
        tooltip.add((Component)stack.m_41786_().m_6881_());
        boolean red = "h".equals(suit) || "d".equals(suit);
        String symbol = switch (suit) {
            case "h" -> "\u2665";
            case "d" -> "\u2666";
            case "s" -> "\u2660";
            case "c" -> "\u2663";
            default -> "?";
        };
        tooltip.add((Component)Component.m_237113_((String)(symbol + (rank == null ? "" : rank))).m_130940_(red ? ChatFormatting.RED : ChatFormatting.DARK_GRAY));
    }

    private static enum TableItemStorageProvider implements IServerExtensionProvider<GameTable2BlockEntity, ItemStack>
    {
        INSTANCE;


        public List<ViewGroup<ItemStack>> getGroups(ServerPlayer player, ServerLevel level, GameTable2BlockEntity table, boolean showDetails) {
            List<ItemStack> visible = SanguoshaJadePlugin.visibleStorage(table);
            return visible.isEmpty() ? List.of() : List.of(new ViewGroup(visible));
        }

        public ResourceLocation getUid() {
            return TABLE_STORAGE;
        }

        public int getDefaultPriority() {
            return 0;
        }
    }

    private static enum TableItemStorageClientProvider implements IClientExtensionProvider<ItemStack, ItemView>
    {
        INSTANCE;


        public List<ClientViewGroup<ItemView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<ItemStack>> groups) {
            if (groups == null || groups.isEmpty()) {
                return List.of();
            }
            return ClientViewGroup.map(groups, ItemView::new, null);
        }

        public ResourceLocation getUid() {
            return TABLE_STORAGE;
        }
    }
}


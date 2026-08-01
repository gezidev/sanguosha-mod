package cn.solo.sanguosha.entity;

import cn.solo.sanguosha.config.GeneralAssetManager;
import cn.solo.sanguosha.deck.CardMatchAdapter;
import cn.solo.sanguosha.deck.DeckDrawConfig;
import cn.solo.sanguosha.deck.PlayerDeckDrawConfig;
import cn.solo.sanguosha.item.CardDeckItem;
import cn.solo.sanguosha.item.GeneralCardItem;
import cn.solo.sanguosha.item.IdentityCardItem;
import cn.solo.sanguosha.item.PlaceableCardItem;
import cn.solo.sanguosha.item.StandardCardItem;
import cn.solo.sanguosha.registry.ModEntities;
import cn.solo.sanguosha.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public final class GroundCardEntity
extends Entity {
    private static final String DECK_TYPE_TAG = "DeckType";
    private static final EntityDataAccessor<ItemStack> CARD = SynchedEntityData.m_135353_(GroundCardEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135033_);
    private static final EntityDataAccessor<Boolean> FACE_DOWN = SynchedEntityData.m_135353_(GroundCardEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135035_);
    private static final EntityDataAccessor<Float> ROTATION = SynchedEntityData.m_135353_(GroundCardEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135029_);
    private static final EntityDataAccessor<Integer> CARD_COUNT = SynchedEntityData.m_135353_(GroundCardEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Integer> DECK_TYPE = SynchedEntityData.m_135353_(GroundCardEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135028_);
    private static final EntityDataAccessor<Integer> HEALTH_VALUE = SynchedEntityData.m_135353_(GroundCardEntity.class, (EntityDataSerializer)EntityDataSerializers.f_135028_);
    private final List<ItemStack> cards = new ArrayList<ItemStack>();

    public GroundCardEntity(EntityType<? extends GroundCardEntity> type, Level level) {
        super(type, level);
        this.m_20242_(true);
    }

    public GroundCardEntity(Level level, double x, double y, double z, ItemStack stack, float rotation) {
        this((EntityType<? extends GroundCardEntity>)((EntityType)ModEntities.GROUND_CARD.get()), level);
        this.m_6034_(x, y, z);
        this.cards.add(stack.m_255036_(1));
        this.syncTopCard();
        this.f_19804_.m_135381_(ROTATION, Float.valueOf(rotation));
    }

    public static GroundCardEntity emptyDeck(Level level, double x, double y, double z, CardDeckItem.DeckType type, float rotation) {
        GroundCardEntity deck = new GroundCardEntity((EntityType<? extends GroundCardEntity>)((EntityType)ModEntities.GROUND_CARD.get()), level);
        deck.m_6034_(x, y, z);
        deck.f_19804_.m_135381_(DECK_TYPE, type == CardDeckItem.DeckType.IDENTITY ? 1 : (type == CardDeckItem.DeckType.GENERAL ? 3 : 2));
        deck.f_19804_.m_135381_(ROTATION, Float.valueOf(rotation));
        if (type == CardDeckItem.DeckType.IDENTITY) {
            deck.cards.add(new ItemStack((ItemLike)ModItems.LORD.get()));
            deck.cards.add(new ItemStack((ItemLike)ModItems.LOYALIST.get()));
            deck.cards.add(new ItemStack((ItemLike)ModItems.REBEL.get()));
            deck.cards.add(new ItemStack((ItemLike)ModItems.RENEGADE.get()));
        } else if (type == CardDeckItem.DeckType.GENERAL) {
            for (GeneralAssetManager.GeneralAsset asset : GeneralAssetManager.assets()) {
                ItemStack card = GeneralCardItem.create(asset.id(), 1);
                card.m_41784_().m_128379_("SanguoshaFaceDown", true);
                deck.cards.add(card);
            }
            deck.shuffleCards();
        } else {
            for (ModItems.CardDefinition definition : ModItems.CARD_DEFINITIONS) {
                ItemStack card = ModItems.createStandardCard(definition.id());
                card.m_41784_().m_128379_("SanguoshaFaceDown", true);
                deck.cards.add(card);
            }
        }
        deck.syncTopCard();
        return deck;
    }

    public static GroundCardEntity deckFromCards(Level level, double x, double y, double z, List<ItemStack> cards, int deckType, float rotation) {
        GroundCardEntity deck = new GroundCardEntity((EntityType<? extends GroundCardEntity>)((EntityType)ModEntities.GROUND_CARD.get()), level);
        deck.m_6034_(x, y, z);
        deck.f_19804_.m_135381_(DECK_TYPE, deckType);
        deck.f_19804_.m_135381_(ROTATION, Float.valueOf(rotation));
        for (ItemStack card : cards) {
            ItemStack copy = card.m_255036_(1);
            if (deckType == 2 || deckType == 3) {
                copy.m_41784_().m_128379_("SanguoshaFaceDown", true);
            }
            deck.cards.add(copy);
        }
        deck.syncTopCard();
        return deck;
    }

    public static GroundCardEntity deckFromCards(Level level, double x, double y, double z, List<ItemStack> cards, CardDeckItem.DeckType type, float rotation) {
        int deckType = type == CardDeckItem.DeckType.IDENTITY ? 1 : (type == CardDeckItem.DeckType.GENERAL ? 3 : 2);
        return GroundCardEntity.deckFromCards(level, x, y, z, cards, deckType, rotation);
    }

    protected void m_8097_() {
        this.f_19804_.m_135372_(CARD, ItemStack.f_41583_);
        this.f_19804_.m_135372_(FACE_DOWN, true);
        this.f_19804_.m_135372_(ROTATION, Float.valueOf(0.0f));
        this.f_19804_.m_135372_(CARD_COUNT, 0);
        this.f_19804_.m_135372_(DECK_TYPE, 0);
        this.f_19804_.m_135372_(HEALTH_VALUE, 1);
    }

    private void syncTopCard() {
        ItemStack top = this.cards.isEmpty() ? ItemStack.f_41583_ : this.cards.get(this.cards.size() - 1);
        this.f_19804_.m_135381_(CARD, top.m_255036_(1));
        this.f_19804_.m_135381_(FACE_DOWN, Boolean.valueOf(!top.m_41619_() && PlaceableCardItem.isFaceDown(top)));
        this.f_19804_.m_135381_(CARD_COUNT, this.cards.size());
    }

    public ItemStack getCard() {
        return (ItemStack)this.f_19804_.m_135370_(CARD);
    }

    public boolean isFaceDown() {
        return (Boolean)this.f_19804_.m_135370_(FACE_DOWN);
    }

    public float getCardRotation() {
        return ((Float)this.f_19804_.m_135370_(ROTATION)).floatValue();
    }

    public int getCardCount() {
        return (Integer)this.f_19804_.m_135370_(CARD_COUNT);
    }

    public boolean isDeckTypeRestricted() {
        return (Integer)this.f_19804_.m_135370_(DECK_TYPE) != 0;
    }

    public int getHealthValue() {
        return (Integer)this.f_19804_.m_135370_(HEALTH_VALUE);
    }

    public void setHealthValue(int hp) {
        this.f_19804_.m_135381_(HEALTH_VALUE, Math.max(1, Math.min(5, hp)));
    }

    public boolean acceptsDiscard() {
        return !this.m_9236_().f_46443_ && (Integer)this.f_19804_.m_135370_(DECK_TYPE) == 0;
    }

    public boolean addDiscard(ItemStack stack) {
        if (!this.acceptsDiscard() || !this.accepts(stack) || stack.m_41613_() != 1) {
            return false;
        }
        this.cards.add(this.prepareForDeck(stack));
        this.syncTopCard();
        return true;
    }

    public static boolean isCollectibleCard(ItemStack stack) {
        return !stack.m_41619_() && stack.m_41613_() == 1 && stack.m_41720_() instanceof PlaceableCardItem;
    }

    public List<ItemStack> copyCollectibleCards() {
        if (this.m_9236_().f_46443_ || this.cards.isEmpty()) {
            return List.of();
        }
        ArrayList<ItemStack> copy = new ArrayList<ItemStack>(this.cards.size());
        for (ItemStack card : this.cards) {
            if (!GroundCardEntity.isCollectibleCard(card)) {
                return List.of();
            }
            copy.add(card.m_255036_(1));
        }
        return List.copyOf(copy);
    }

    public boolean appendCollectedCards(List<ItemStack> incoming) {
        if (!this.acceptsDiscard() || incoming.isEmpty()) {
            return false;
        }
        for (ItemStack card : incoming) {
            if (GroundCardEntity.isCollectibleCard(card)) continue;
            return false;
        }
        for (ItemStack card : incoming) {
            this.cards.add(card.m_255036_(1));
        }
        this.syncTopCard();
        return true;
    }

    public boolean isCollectiblePile() {
        return !this.m_9236_().f_46443_ && (Integer)this.f_19804_.m_135370_(DECK_TYPE) == 0 && !this.copyCollectibleCards().isEmpty();
    }

    private FilteredDrawResult drawConfiguredToInventory(Player player) {
        if (this.m_9236_().f_46443_ || this.cards.isEmpty()) {
            return FilteredDrawResult.REJECTED;
        }
        int deckType = (Integer)this.f_19804_.m_135370_(DECK_TYPE);
        int index;
        if (deckType == 3) {
            index = this.m_9236_().f_46441_.m_188503_(this.cards.size());
        } else {
            DeckDrawConfig config = PlayerDeckDrawConfig.get(player);
            index = GroundCardEntity.findMatchingIndex(this.cards.size(), config.fromBottom(), i -> CardMatchAdapter.matchCard(this.cards.get(i), config));
        }
        if (index < 0) {
            return FilteredDrawResult.NOT_FOUND;
        }
        ItemStack card = this.cards.remove(index);
        if (deckType == 2 || deckType == 3) {
            card.m_41784_().m_128379_("SanguoshaFaceDown", false);
        }
        if (!player.m_150109_().m_36054_(card)) {
            player.m_36176_(card, false);
        }
        if (this.cards.isEmpty() && (deckType == 0 || deckType == 3)) {
            this.m_146870_();
        } else {
            this.syncTopCard();
        }
        return FilteredDrawResult.SUCCESS;
    }

    static int findMatchingIndex(int size, boolean fromBottom, IntPredicate matches) {
        if (fromBottom) {
            for (int i = 0; i < size; ++i) {
                if (!matches.test(i)) continue;
                return i;
            }
        } else {
            for (int i = size - 1; i >= 0; --i) {
                if (!matches.test(i)) continue;
                return i;
            }
        }
        return -1;
    }

    private boolean accepts(ItemStack stack) {
        int type = (Integer)this.f_19804_.m_135370_(DECK_TYPE);
        if (type == 0) {
            return stack.m_41720_() instanceof PlaceableCardItem;
        }
        if (type == 1) {
            return stack.m_41720_() instanceof IdentityCardItem;
        }
        if (type == 3) {
            return stack.m_41720_() instanceof GeneralCardItem;
        }
        return stack.m_41720_() instanceof PlaceableCardItem && !(stack.m_41720_() instanceof IdentityCardItem) && !(stack.m_41720_() instanceof GeneralCardItem);
    }

    private ItemStack prepareForDeck(ItemStack stack) {
        ItemStack card = stack.m_255036_(1);
        if ((Integer)this.f_19804_.m_135370_(DECK_TYPE) == 2 || (Integer)this.f_19804_.m_135370_(DECK_TYPE) == 3) {
            card.m_41784_().m_128379_("SanguoshaFaceDown", true);
        }
        return card;
    }

    private boolean flipPlacedCardServer(Player player) {
        if (this.m_9236_().f_46443_ || this.cards.isEmpty()) {
            return false;
        }
        int topIndex = this.cards.size() - 1;
        ItemStack changed = this.cards.get(topIndex).m_255036_(1);
        if (!PlaceableCardItem.canFlip(changed)) {
            return false;
        }
        PlaceableCardItem.toggleFaceAndNotify(changed, player, false);
        this.cards.set(topIndex, changed);
        this.f_19804_.m_135381_(CARD, changed.m_41777_());
        this.f_19804_.m_135381_(FACE_DOWN, PlaceableCardItem.isFaceDown(changed));
        return true;
    }

    private void shuffleCards() {
        if (this.cards.size() < 2) {
            return;
        }
        for (int i = this.cards.size() - 1; i > 0; --i) {
            int j = this.m_9236_().f_46441_.m_188503_(i + 1);
            ItemStack card = this.cards.get(i);
            this.cards.set(i, this.cards.get(j));
            this.cards.set(j, card);
        }
        this.syncTopCard();
    }

    private void shuffle(Player player) {
        if (this.m_9236_().f_46443_) {
            return;
        }
        this.shuffleCards();
        player.m_5661_((Component)Component.m_237113_((String)"\u724c\u5806\u5df2\u6d17\u724c"), true);
    }

    private boolean pickupDeck(Player player) {
        if (this.m_9236_().f_46443_) {
            return false;
        }
        int deckType = (Integer)this.f_19804_.m_135370_(DECK_TYPE);
        if (deckType < 1 || deckType > 3) {
            return false;
        }
        if (this.cards.isEmpty()) {
            this.m_146870_();
            player.m_5661_((Component)Component.m_237115_((String)"message.sanguosha.deck_pickup_empty"), true);
            return true;
        }
        Item item = deckType == 1 ? ModItems.IDENTITY_DECK.get() : (deckType == 3 ? ModItems.GENERAL_DECK.get() : ModItems.NON_IDENTITY_DECK.get());
        ItemStack deckItem = new ItemStack((ItemLike)item, 1);
        ListTag list = new ListTag();
        for (ItemStack card : this.cards) {
            list.add(card.m_255036_(1).m_41739_(new CompoundTag()));
        }
        deckItem.m_41784_().m_128365_("DeckCards", list);
        if (!player.m_150109_().m_36054_(deckItem)) {
            player.m_36176_(deckItem, false);
        }
        this.m_146870_();
        player.m_5661_((Component)Component.m_237110_((String)"message.sanguosha.deck_pickup", (Object[])new Object[]{this.cards.size()}), true);
        return true;
    }

    public InteractionResult m_6096_(Player player, InteractionHand hand) {
        FilteredDrawResult result;
        ItemStack held = player.m_21120_(hand);
        if (player.m_6047_()) {
            if (held.m_41619_() && this.isDeckTypeRestricted()) {
                if (!this.m_9236_().f_46443_) {
                    this.pickupDeck(player);
                }
                return InteractionResult.m_19078_((boolean)this.m_9236_().f_46443_);
            }
            if (this.m_9236_().f_46443_) {
                return InteractionResult.SUCCESS;
            }
            return this.flipPlacedCardServer(player) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        if (!held.m_41619_() && this.accepts(held)) {
            if (!this.m_9236_().f_46443_) {
                this.cards.add(this.prepareForDeck(held));
                this.syncTopCard();
                if (!player.m_150110_().f_35937_) {
                    held.m_41774_(1);
                }
            }
            return InteractionResult.m_19078_((boolean)this.m_9236_().f_46443_);
        }
        if (!held.m_41619_()) {
            return InteractionResult.PASS;
        }
        if (!this.m_9236_().f_46443_ && !this.cards.isEmpty() && (result = this.drawConfiguredToInventory(player)) == FilteredDrawResult.NOT_FOUND) {
            player.m_5661_((Component)Component.m_237115_((String)"message.sanguosha.deck_draw.not_found"), true);
        }
        return InteractionResult.m_19078_((boolean)this.m_9236_().f_46443_);
    }

    public boolean m_6469_(DamageSource source, float amount) {
        Entity entity = source.m_7639_();
        if (!(entity instanceof Player)) {
            return false;
        }
        Player player = (Player)entity;
        if (player.m_6047_()) {
            this.shuffle(player);
        } else {
            this.flipPlacedCardServer(player);
        }
        return true;
    }

    public void m_8119_() {
        super.m_8119_();
        this.m_20334_(0.0, 0.0, 0.0);
    }

    protected void m_7378_(CompoundTag tag) {
        ItemStack top;
        CompoundTag cardTag;
        this.cards.clear();
        if (tag.m_128425_("Cards", 9)) {
            ListTag list = tag.m_128437_("Cards", 10);
            for (int i = 0; i < list.size(); ++i) {
                this.cards.add(ItemStack.m_41712_((CompoundTag)list.m_128728_(i)));
            }
        } else if (tag.m_128425_("Card", 10)) {
            this.cards.add(ItemStack.m_41712_((CompoundTag)tag.m_128469_("Card")));
        }
        if (!(this.cards.isEmpty() || !tag.m_128425_("FaceDown", 1) || (cardTag = (top = this.cards.get(this.cards.size() - 1)).m_41783_()) != null && cardTag.m_128425_("SanguoshaFaceDown", 1))) {
            top.m_41784_().m_128379_("SanguoshaFaceDown", tag.m_128471_("FaceDown"));
        }
        this.f_19804_.m_135381_(ROTATION, Float.valueOf(tag.m_128457_("Rotation")));
        this.f_19804_.m_135381_(DECK_TYPE, tag.m_128451_(DECK_TYPE_TAG));
        this.f_19804_.m_135381_(HEALTH_VALUE, tag.m_128451_("HealthValue"));
        this.syncTopCard();
    }

    protected void m_7380_(CompoundTag tag) {
        ListTag list = new ListTag();
        for (ItemStack card : this.cards) {
            list.add(card.m_41739_(new CompoundTag()));
        }
        tag.m_128365_("Cards", (Tag)list);
        tag.m_128350_("Rotation", this.getCardRotation());
        tag.m_128405_(DECK_TYPE_TAG, ((Integer)this.f_19804_.m_135370_(DECK_TYPE)).intValue());
        tag.m_128405_("HealthValue", this.getHealthValue());
    }

    public boolean m_6087_() {
        return true;
    }

    public float m_6143_() {
        return 0.0f;
    }

    public boolean m_6097_() {
        return true;
    }

    public Packet<ClientGamePacketListener> m_5654_() {
        return NetworkHooks.getEntitySpawningPacket((Entity)this);
    }

    public static enum FilteredDrawResult {
        SUCCESS,
        NOT_FOUND,
        REJECTED;

    }
}


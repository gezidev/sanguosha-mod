package cn.solo.sanguosha.client;

import cn.solo.sanguosha.client.CustomGeneralImageManager;
import cn.solo.sanguosha.config.GeneralAssetManager;
import cn.solo.sanguosha.config.GeneralDefinition;
import cn.solo.sanguosha.config.GeneralManager;
import cn.solo.sanguosha.image.ImageDataValidator;
import cn.solo.sanguosha.item.GeneralCardItem;
import cn.solo.sanguosha.network.ModNetwork;
import com.mojang.blaze3d.platform.NativeImage;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class ClientGeneralCatalog {
    private static final Map<String, GeneralDefinition> CATALOG = new ConcurrentHashMap<String, GeneralDefinition>();
    private static final Map<String, String> CATEGORY_MAP = new ConcurrentHashMap<String, String>();
    private static final Map<String, AnimatedTexture> TEXTURES = new ConcurrentHashMap<String, AnimatedTexture>();
    private static final Set<String> LOADING = ConcurrentHashMap.newKeySet();
    private static final Set<String> REQUESTED = ConcurrentHashMap.newKeySet();
    private static final AtomicLong GENERATION = new AtomicLong();
    private static final ResourceLocation FALLBACK = new ResourceLocation("sanguosha", "textures/item/general_card.png");

    private ClientGeneralCatalog() {
    }

    public static void reload() {
        CATALOG.clear();
        CATEGORY_MAP.clear();
        for (GeneralAssetManager.GeneralAsset asset : GeneralAssetManager.assets()) {
            GeneralDefinition def = new GeneralDefinition(asset.id(), asset.chineseName(), asset.kingdom(), 0, "", "", List.of());
            CATALOG.put(def.id(), def);
            CATEGORY_MAP.put(def.id(), asset.category());
        }
        for (GeneralDefinition def : GeneralManager.all()) {
            ClientGeneralCatalog.registerCustomGeneral(def, null);
        }
    }

    public static void registerCustomGeneral(GeneralDefinition def, byte[] imageData) {
        if (def == null || def.id() == null) {
            return;
        }
        CATALOG.put(def.id(), def);
        CATEGORY_MAP.put(def.id(), def.category());
        if (imageData != null && imageData.length > 0 && def.hasImage()) {
            ClientGeneralCatalog.acceptDownloadedImage(def.imageId(), def.imageFormat(), imageData);
        }
    }

    public static Collection<GeneralDefinition> all() {
        return Collections.unmodifiableCollection(CATALOG.values());
    }

    public static List<GeneralDefinition> byCategory(String category) {
        return ClientGeneralCatalog.all().stream().filter(d -> ClientGeneralCatalog.getCategory(d.id()).equals(category)).toList();
    }

    public static Optional<GeneralDefinition> get(String id) {
        return Optional.ofNullable(CATALOG.get(id));
    }

    public static boolean contains(String id) {
        return CATALOG.containsKey(id);
    }

    public static String getCategory(String id) {
        return CATEGORY_MAP.getOrDefault(id, "\u6807");
    }

    public static ResourceLocation texture(ItemStack stack) {
        return ClientGeneralCatalog.texture(GeneralCardItem.id(stack));
    }

    public static ResourceLocation texture(String id) {
        if (id == null || id.isBlank()) {
            return FALLBACK;
        }
        Optional<GeneralAssetManager.GeneralAsset> builtIn = GeneralAssetManager.get(id);
        if (builtIn.isPresent()) {
            return builtIn.get().texture();
        }
        GeneralDefinition def = CATALOG.get(id);
        if (def == null || !def.hasImage()) {
            return FALLBACK;
        }
        AnimatedTexture texture = TEXTURES.get(def.imageId());
        if (texture != null) {
            return texture.current();
        }
        ClientGeneralCatalog.ensureImage(def.imageId(), def.imageFormat());
        return FALLBACK;
    }

    static String cacheStatus(String generalId) {
        if (generalId == null || generalId.isBlank()) {
            return "fallback";
        }
        if (GeneralAssetManager.get(generalId).isPresent()) {
            return "builtin";
        }
        GeneralDefinition def = CATALOG.get(generalId);
        if (def == null || !def.hasImage()) {
            return "fallback";
        }
        if (TEXTURES.containsKey(def.imageId())) {
            return "ready";
        }
        if (LOADING.contains(def.imageId())) {
            return "loading";
        }
        if (REQUESTED.contains(def.imageId())) {
            return "requested";
        }
        return "missing";
    }

    private static void ensureImage(String id, String format) {
        if (!LOADING.add(id)) {
            return;
        }
        long generation = GENERATION.get();
        CustomGeneralImageManager.readCacheAsync(id, format).whenComplete((cached, error) -> {
            if (generation != GENERATION.get()) {
                LOADING.remove(id);
                return;
            }
            if (error == null && cached != null && ((byte[])cached).length > 0) {
                ClientGeneralCatalog.decodeAndRegister(id, format, cached, generation);
            } else {
                LOADING.remove(id);
                if (REQUESTED.add(id)) {
                    ModNetwork.requestGeneralImage(id, format);
                }
            }
        });
    }

    public static void acceptDownloadedImage(String id, String format, byte[] data) {
        if (!ImageDataValidator.validContentId(id) || data == null || data.length == 0) {
            return;
        }
        REQUESTED.remove(id);
        long generation = GENERATION.get();
        CustomGeneralImageManager.writeCacheAsync(id, format, data).whenComplete((saved, error) -> {
            if (Boolean.TRUE.equals(saved) && generation == GENERATION.get()) {
                ClientGeneralCatalog.decodeAndRegister(id, format, data, generation);
            } else {
                LOADING.remove(id);
            }
        });
    }

    private static void decodeAndRegister(String id, String format, byte[] data, long generation) {
        CustomGeneralImageManager.decodeAsync(data).whenComplete((decoded, error) -> Minecraft.m_91087_().execute(() -> {
            LOADING.remove(id);
            if (generation != GENERATION.get() || error != null || decoded == null || !decoded.contentId().equals(id) || !decoded.format().equals(format)) {
                return;
            }
            ClientGeneralCatalog.release(id);
            ResourceLocation[] locations = new ResourceLocation[decoded.frames().size()];
            int[] delays = new int[decoded.frames().size()];
            for (int i = 0; i < decoded.frames().size(); ++i) {
                ImageDataValidator.Frame frame = decoded.frames().get(i);
                NativeImage nativeImage = ClientGeneralCatalog.toNative(frame.image());
                locations[i] = Minecraft.m_91087_().m_91097_().m_118490_("sanguosha_general/" + id + "_" + i, new DynamicTexture(nativeImage));
                delays[i] = frame.delayMillis();
            }
            TEXTURES.put(id, new AnimatedTexture(locations, delays));
        }));
    }

    private static NativeImage toNative(BufferedImage image) {
        NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), true);
        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                int argb = image.getRGB(x, y);
                int abgr = argb & 0xFF00FF00 | (argb & 0xFF0000) >> 16 | (argb & 0xFF) << 16;
                nativeImage.m_84988_(x, y, abgr);
            }
        }
        return nativeImage;
    }

    public static void clearTextures() {
        for (String id : new HashSet<String>(TEXTURES.keySet())) {
            ClientGeneralCatalog.release(id);
        }
        LOADING.clear();
        REQUESTED.clear();
        GENERATION.incrementAndGet();
    }

    private static void release(String id) {
        AnimatedTexture removed = TEXTURES.remove(id);
        if (removed != null) {
            for (ResourceLocation location : removed.frames) {
                Minecraft.m_91087_().m_91097_().m_118513_(location);
            }
        }
    }

    private static final class AnimatedTexture {
        final ResourceLocation[] frames;
        final int[] delays;
        final long cycle;

        AnimatedTexture(ResourceLocation[] frames, int[] delays) {
            this.frames = frames;
            this.delays = delays;
            long total = 0L;
            for (int delay : delays) {
                total += (long)Math.max(10, delay);
            }
            this.cycle = Math.max(1L, total);
        }

        ResourceLocation current() {
            if (this.frames.length == 1) {
                return this.frames[0];
            }
            long elapsed = System.currentTimeMillis() % this.cycle;
            for (int i = 0; i < this.frames.length; ++i) {
                if ((elapsed -= (long)Math.max(10, this.delays[i])) >= 0L) continue;
                return this.frames[i];
            }
            return this.frames[0];
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonParseException
 *  net.minecraftforge.fml.loading.FMLPaths
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package cn.solo.sanguosha.config;

import cn.solo.sanguosha.config.GeneralDefinition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class GeneralManager {
    private static final Logger LOGGER = LogManager.getLogger((String)"SanguoshaGenerals");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path DIR = FMLPaths.CONFIGDIR.get().resolve("sanguosha/generals");
    private static final AtomicReference<Map<String, GeneralDefinition>> SNAPSHOT = new AtomicReference(Map.of());

    private GeneralManager() {
    }

    public static int reload() {
        LinkedHashMap loaded = new LinkedHashMap();
        try {
            Files.createDirectories(DIR, new FileAttribute[0]);
            try (Stream<Path> files = Files.list(DIR);){
                files.filter(p -> p.getFileName().toString().endsWith(".json")).sorted().forEach(path -> GeneralManager.read(path, loaded));
            }
        }
        catch (IOException e) {
            LOGGER.error("\u65e0\u6cd5\u8bfb\u53d6\u6b66\u5c06\u914d\u7f6e\u76ee\u5f55 {}", (Object)DIR, (Object)e);
        }
        SNAPSHOT.set(Collections.unmodifiableMap(loaded));
        LOGGER.info("\u5df2\u8f7d\u5165 {} \u540d\u6b66\u5c06\uff08\u542b\u81ea\u5b9a\u4e49\uff09", (Object)loaded.size());
        return loaded.size();
    }

    private static void read(Path path, Map<String, GeneralDefinition> target) {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);){
            GeneralDefinition def = (GeneralDefinition)GSON.fromJson((Reader)reader, GeneralDefinition.class);
            if (def == null || def.id() == null || def.name() == null) {
                throw new JsonParseException("id/name \u7f3a\u5931");
            }
            if (!def.id().matches("[a-z0-9_\\-]+") && !def.id().matches("custom_[a-f0-9]+")) {
                throw new JsonParseException("id \u975e\u6cd5: " + def.id());
            }
            target.put(def.id(), def);
        }
        catch (Exception e) {
            LOGGER.error("\u8df3\u8fc7\u65e0\u6548\u914d\u7f6e {}: {}", (Object)path.getFileName(), (Object)e.getMessage());
        }
    }

    public static Optional<GeneralDefinition> get(String id) {
        return Optional.ofNullable(SNAPSHOT.get().get(id));
    }

    public static Collection<GeneralDefinition> all() {
        return SNAPSHOT.get().values();
    }

    public static Set<String> ids() {
        return SNAPSHOT.get().keySet();
    }

    public static List<GeneralDefinition> byCategory(String category) {
        return GeneralManager.all().stream().filter(d -> d.category().equals(category)).toList();
    }

    public static boolean contains(String id) {
        return SNAPSHOT.get().containsKey(id);
    }

    public static synchronized void saveCustom(GeneralDefinition def) {
        if (def == null || def.id() == null || !def.id().startsWith("custom_")) {
            return;
        }
        GeneralManager.saveDefinitionOnly(def);
        GeneralManager.reload();
    }

    public static synchronized void saveDefinitionOnly(GeneralDefinition def) {
        if (def == null || def.id() == null) {
            return;
        }
        try {
            Files.createDirectories(DIR, new FileAttribute[0]);
            Path jsonPath = DIR.resolve(def.id() + ".json");
            try (BufferedWriter writer = Files.newBufferedWriter(jsonPath, StandardCharsets.UTF_8, new OpenOption[0]);){
                GSON.toJson((Object)def, (Appendable)writer);
            }
        }
        catch (IOException e) {
            LOGGER.error("\u4fdd\u5b58\u6b66\u5c06\u5b9a\u4e49\u5931\u8d25 {}", (Object)def.id(), (Object)e);
        }
    }

    public static synchronized void saveImage(String id, byte[] pngData) {
        if (pngData == null || pngData.length == 0) {
            return;
        }
        try {
            Files.createDirectories(DIR, new FileAttribute[0]);
            Path image = DIR.resolve(id + ".png");
            Files.write(image, pngData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (IOException e) {
            LOGGER.error("\u4fdd\u5b58\u81ea\u5b9a\u4e49\u6b66\u5c06\u56fe\u7247\u5931\u8d25 {}", (Object)id, (Object)e);
        }
    }
}


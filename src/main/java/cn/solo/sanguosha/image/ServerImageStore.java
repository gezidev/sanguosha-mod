/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.level.storage.LevelResource
 */
package cn.solo.sanguosha.image;

import cn.solo.sanguosha.image.ImageDataValidator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

public final class ServerImageStore {
    public static final int CHUNK_BYTES = 24576;
    public static final int MAX_CHUNKS = 683;
    public static final long UPLOAD_TIMEOUT_MS = 30000L;
    private static final Map<UUID, Upload> UPLOADS = new ConcurrentHashMap<UUID, Upload>();

    private ServerImageStore() {
    }

    public static synchronized UploadResult accept(ServerPlayer player, String id, String format, int totalBytes, int totalChunks, int index, byte[] chunk) {
        ServerImageStore.expire();
        if (!ImageDataValidator.validContentId(id) || !"png".equals(format) && !"gif".equals(format)) {
            return ServerImageStore.fail(player, "\u56fe\u7247\u6807\u8bc6\u6216\u683c\u5f0f\u975e\u6cd5");
        }
        if (totalBytes <= 0 || totalBytes > 0x1000000 || totalChunks <= 0 || totalChunks > 683) {
            return ServerImageStore.fail(player, "\u56fe\u7247\u5927\u5c0f\u6216\u5206\u7247\u603b\u6570\u8d8a\u754c");
        }
        int expectedChunks = (totalBytes + 24576 - 1) / 24576;
        if (totalChunks != expectedChunks || index < 0 || index >= totalChunks || chunk == null || chunk.length <= 0 || chunk.length > 24576) {
            return ServerImageStore.fail(player, "\u56fe\u7247\u5206\u7247\u8fb9\u754c\u975e\u6cd5");
        }
        if (index < totalChunks - 1 && chunk.length != 24576) {
            return ServerImageStore.fail(player, "\u975e\u672b\u5c3e\u5206\u7247\u957f\u5ea6\u975e\u6cd5");
        }
        int expectedLast = totalBytes - 24576 * (totalChunks - 1);
        if (index == totalChunks - 1 && chunk.length != expectedLast) {
            return ServerImageStore.fail(player, "\u672b\u5c3e\u5206\u7247\u957f\u5ea6\u975e\u6cd5");
        }
        UUID owner = player.m_20148_();
        Upload upload = UPLOADS.get(owner);
        if (index == 0) {
            upload = new Upload(owner, id, format, totalBytes, totalChunks);
            UPLOADS.put(owner, upload);
        }
        if (!(upload != null && upload.owner.equals(owner) && upload.id.equals(id) && upload.next == index && upload.totalBytes == totalBytes && upload.totalChunks == totalChunks && upload.format.equals(format))) {
            return ServerImageStore.fail(player, "\u56fe\u7247\u5206\u7247\u987a\u5e8f\u6216\u6240\u6709\u8005\u975e\u6cd5");
        }
        upload.bytes.write(chunk, 0, chunk.length);
        ++upload.next;
        upload.touched = System.currentTimeMillis();
        if (upload.next < upload.totalChunks) {
            return new UploadResult(false, true, "", id, format);
        }
        UPLOADS.remove(owner);
        byte[] data = upload.bytes.toByteArray();
        try {
            ImageDataValidator.Decoded decoded = ImageDataValidator.validate(data, false);
            if (!decoded.contentId().equals(id) || !decoded.format().equals(format)) {
                return ServerImageStore.fail(player, "\u56fe\u7247\u54c8\u5e0c\u6216\u683c\u5f0f\u6821\u9a8c\u5931\u8d25");
            }
            ServerImageStore.persist(player.m_20194_(), id, format, data);
            return new UploadResult(true, true, "\u56fe\u7247\u4e0a\u4f20\u6210\u529f", id, format);
        }
        catch (IOException exception) {
            return ServerImageStore.fail(player, "\u56fe\u7247\u89e3\u7801\u5931\u8d25\uff1a" + exception.getMessage());
        }
    }

    public static byte[] read(MinecraftServer server, String id, String format) {
        if (!ImageDataValidator.validContentId(id) || !"png".equals(format) && !"gif".equals(format)) {
            return new byte[0];
        }
        Path file = ServerImageStore.imageDir(server).resolve(id + "." + format).normalize();
        if (!file.getParent().equals(ServerImageStore.imageDir(server)) || !Files.isRegularFile(file, new LinkOption[0])) {
            return new byte[0];
        }
        try {
            long size = Files.size(file);
            if (size <= 0L || size > 0x1000000L) {
                return new byte[0];
            }
            byte[] data = Files.readAllBytes(file);
            return ImageDataValidator.sha256(data).equals(id) ? data : new byte[]{};
        }
        catch (IOException ignored) {
            return new byte[0];
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void persist(MinecraftServer server, String id, String format, byte[] data) throws IOException {
        Path dir = ServerImageStore.imageDir(server);
        Files.createDirectories(dir, new FileAttribute[0]);
        Path target = dir.resolve(id + "." + format).normalize();
        if (!target.getParent().equals(dir)) {
            throw new IOException("\u975e\u6cd5\u5b58\u50a8\u8def\u5f84");
        }
        if (Files.isRegularFile(target, new LinkOption[0])) {
            return;
        }
        Path temp = Files.createTempFile(dir, id + ".", ".tmp", new FileAttribute[0]);
        try {
            Files.write(temp, data, StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
            }
            catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temp, target, new CopyOption[0]);
            }
        }
        finally {
            Files.deleteIfExists(temp);
        }
    }

    private static Path imageDir(MinecraftServer server) {
        return server.m_129843_(LevelResource.f_78182_).resolve("sanguosha/images").toAbsolutePath().normalize();
    }

    private static UploadResult fail(ServerPlayer player, String message) {
        UPLOADS.remove(player.m_20148_());
        return new UploadResult(true, false, message, "", "");
    }

    private static void expire() {
        long cutoff = System.currentTimeMillis() - 30000L;
        UPLOADS.entrySet().removeIf(entry -> ((Upload)entry.getValue()).touched < cutoff);
    }

    public record UploadResult(boolean complete, boolean success, String message, String contentId, String format) {
    }

    private static final class Upload {
        final UUID owner;
        final String id;
        final String format;
        final int totalBytes;
        final int totalChunks;
        final ByteArrayOutputStream bytes;
        int next;
        long touched;

        Upload(UUID owner, String id, String format, int totalBytes, int totalChunks) {
            this.owner = owner;
            this.id = id;
            this.format = format;
            this.totalBytes = totalBytes;
            this.totalChunks = totalChunks;
            this.bytes = new ByteArrayOutputStream(totalBytes);
            this.touched = System.currentTimeMillis();
        }
    }
}


package cn.solo.sanguosha.client;

import cn.solo.sanguosha.client.ClientGeneralCatalog;
import cn.solo.sanguosha.image.ImageDataValidator;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientImageTransferManager {
    private static final Map<String, Download> DOWNLOADS = new ConcurrentHashMap<String, Download>();

    private ClientImageTransferManager() {
    }

    public static synchronized void accept(String id, String format, int totalBytes, int totalChunks, int index, byte[] chunk) {
        int expected;
        long cutoff = System.currentTimeMillis() - 30000L;
        DOWNLOADS.entrySet().removeIf(e -> ((Download)e.getValue()).touched < cutoff);
        if (!ImageDataValidator.validContentId(id) || !"png".equals(format) && !"gif".equals(format) || totalBytes <= 0 || totalBytes > 0x1000000 || totalChunks <= 0 || totalChunks > 683 || index < 0 || index >= totalChunks || totalChunks != (totalBytes + 24576 - 1) / 24576 || chunk == null || chunk.length <= 0 || chunk.length > 24576) {
            DOWNLOADS.remove(id);
            return;
        }
        int n = expected = index == totalChunks - 1 ? totalBytes - 24576 * (totalChunks - 1) : 24576;
        if (chunk.length != expected) {
            DOWNLOADS.remove(id);
            return;
        }
        Download download = DOWNLOADS.get(id);
        if (index == 0) {
            download = new Download(format, totalBytes, totalChunks);
            DOWNLOADS.put(id, download);
        }
        if (download == null || download.next != index || download.totalBytes != totalBytes || download.totalChunks != totalChunks || !download.format.equals(format)) {
            DOWNLOADS.remove(id);
            return;
        }
        download.bytes.write(chunk, 0, chunk.length);
        ++download.next;
        download.touched = System.currentTimeMillis();
        if (download.next == totalChunks) {
            DOWNLOADS.remove(id);
            byte[] data = download.bytes.toByteArray();
            if (data.length == totalBytes && ImageDataValidator.sha256(data).equals(id)) {
                ClientGeneralCatalog.acceptDownloadedImage(id, format, data);
            }
        }
    }

    private static final class Download {
        final String format;
        final int totalBytes;
        final int totalChunks;
        final ByteArrayOutputStream bytes;
        int next;
        long touched;

        Download(String format, int totalBytes, int totalChunks) {
            this.format = format;
            this.totalBytes = totalBytes;
            this.totalChunks = totalChunks;
            this.bytes = new ByteArrayOutputStream(totalBytes);
            this.touched = System.currentTimeMillis();
        }
    }
}


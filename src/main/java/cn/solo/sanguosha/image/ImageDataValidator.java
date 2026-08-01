/*
 * Decompiled with CFR 0.152.
 */
package cn.solo.sanguosha.image;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class ImageDataValidator {
    public static final int MAX_BYTES = 0x1000000;
    public static final int MAX_SIDE = 4096;
    public static final int MAX_FRAMES = 256;
    public static final long MAX_TOTAL_PIXELS = 0x4000000L;
    private static final byte[] PNG_MAGIC = new byte[]{-119, 80, 78, 71, 13, 10, 26, 10};
    private static final byte[] GIF87 = new byte[]{71, 73, 70, 56, 55, 97};
    private static final byte[] GIF89 = new byte[]{71, 73, 70, 56, 57, 97};

    private ImageDataValidator() {
    }

    public static Decoded validate(byte[] data, boolean keepFrames) throws IOException {
        if (data == null || data.length == 0 || data.length > 0x1000000) {
            throw new IOException("\u56fe\u7247\u5927\u5c0f\u5fc5\u987b\u5728 1 \u5b57\u8282\u5230 16 MiB \u4e4b\u95f4");
        }
        String format = ImageDataValidator.detectFormat(data);
        if (format == null) {
            throw new IOException("\u4ec5\u652f\u6301\u771f\u5b9e PNG \u6216 GIF \u6587\u4ef6");
        }
        Decoded decoded = "png".equals(format) ? ImageDataValidator.decodePng(data, keepFrames) : ImageDataValidator.decodeGif(data, keepFrames);
        return new Decoded(ImageDataValidator.sha256(data), decoded.format(), decoded.width(), decoded.height(), decoded.frames());
    }

    public static String detectFormat(byte[] data) {
        if (ImageDataValidator.startsWith(data, PNG_MAGIC)) {
            return "png";
        }
        if (ImageDataValidator.startsWith(data, GIF87) || ImageDataValidator.startsWith(data, GIF89)) {
            return "gif";
        }
        return null;
    }

    public static String sha256(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        }
        catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    public static boolean validContentId(String value) {
        return value != null && value.matches("[a-f0-9]{64}");
    }

    private static Decoded decodePng(byte[] data, boolean keepFrames) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
        if (image == null) {
            throw new IOException("PNG \u65e0\u6cd5\u89e3\u7801");
        }
        ImageDataValidator.validateCanvas(image.getWidth(), image.getHeight(), 1);
        List<Frame> frames = keepFrames ? List.of(new Frame(ImageDataValidator.toArgb(image), 0)) : List.of();
        return new Decoded("", "png", image.getWidth(), image.getHeight(), frames);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Decoded decodeGif(byte[] data, boolean keepFrames) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(data));){
            int height;
            int width;
            BufferedImage first;
            int count;
            ImageReader reader;
            block15: {
                Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
                if (!readers.hasNext()) {
                    throw new IOException("\u5f53\u524d Java \u8fd0\u884c\u65f6\u6ca1\u6709 GIF reader");
                }
                reader = readers.next();
                reader.setInput(input, false, false);
                count = reader.getNumImages(true);
                if (count <= 0 || count > 256) {
                    throw new IOException("GIF \u5e27\u6570\u65e0\u6548\u6216\u8d85\u8fc7 256 \u5e27");
                }
                int[] logical = ImageDataValidator.logicalSize(reader.getStreamMetadata());
                first = reader.read(0);
                width = logical[0] > 0 ? logical[0] : first.getWidth();
                height = logical[1] > 0 ? logical[1] : first.getHeight();
                ImageDataValidator.validateCanvas(width, height, count);
                if (keepFrames) break block15;
                Decoded decoded = new Decoded("", "gif", width, height, List.of());
                reader.dispose();
                return decoded;
            }
            try {
                BufferedImage canvas = new BufferedImage(width, height, 2);
                ArrayList<Frame> frames = new ArrayList<Frame>(count);
                for (int i = 0; i < count; ++i) {
                    BufferedImage raw = i == 0 ? first : reader.read(i);
                    FrameMeta meta = ImageDataValidator.frameMeta(reader.getImageMetadata(i));
                    BufferedImage before = "restoreToPrevious".equals(meta.disposal) ? ImageDataValidator.copy(canvas) : null;
                    Graphics2D graphics = canvas.createGraphics();
                    graphics.setComposite(AlphaComposite.SrcOver);
                    graphics.drawImage((Image)raw, meta.left, meta.top, null);
                    graphics.dispose();
                    frames.add(new Frame(ImageDataValidator.copy(canvas), Math.max(10, meta.delayHundredths * 10)));
                    if ("restoreToBackgroundColor".equals(meta.disposal)) {
                        Graphics2D clear = canvas.createGraphics();
                        clear.setComposite(AlphaComposite.Clear);
                        clear.fillRect(meta.left, meta.top, raw.getWidth(), raw.getHeight());
                        clear.dispose();
                        continue;
                    }
                    if (before == null) continue;
                    canvas = before;
                }
                Decoded decoded = new Decoded("", "gif", width, height, List.copyOf(frames));
                reader.dispose();
                return decoded;
            }
            catch (Throwable throwable) {
                reader.dispose();
                throw throwable;
            }
        }
    }

    private static void validateCanvas(int width, int height, int frames) throws IOException {
        if (width <= 0 || height <= 0 || width > 4096 || height > 4096) {
            throw new IOException("\u56fe\u7247\u5c3a\u5bf8\u65e0\u6548\u6216\u5355\u8fb9\u8d85\u8fc7 4096 \u50cf\u7d20");
        }
        if ((long)width * (long)height * (long)frames > 0x4000000L) {
            throw new IOException("\u56fe\u7247\u89e3\u7801\u603b\u50cf\u7d20\u8d85\u8fc7\u9650\u5236");
        }
    }

    private static int[] logicalSize(IIOMetadata metadata) {
        int[] result = new int[]{0, 0};
        if (metadata == null) {
            return result;
        }
        Node root = metadata.getAsTree(metadata.getNativeMetadataFormatName());
        Node descriptor = ImageDataValidator.find(root, "LogicalScreenDescriptor");
        if (descriptor != null) {
            result[0] = ImageDataValidator.intAttr(descriptor, "logicalScreenWidth", 0);
            result[1] = ImageDataValidator.intAttr(descriptor, "logicalScreenHeight", 0);
        }
        return result;
    }

    private static FrameMeta frameMeta(IIOMetadata metadata) {
        Node root = metadata.getAsTree(metadata.getNativeMetadataFormatName());
        Node descriptor = ImageDataValidator.find(root, "ImageDescriptor");
        Node control = ImageDataValidator.find(root, "GraphicControlExtension");
        return new FrameMeta(ImageDataValidator.intAttr(descriptor, "imageLeftPosition", 0), ImageDataValidator.intAttr(descriptor, "imageTopPosition", 0), ImageDataValidator.intAttr(control, "delayTime", 10), ImageDataValidator.stringAttr(control, "disposalMethod", "none").toLowerCase(Locale.ROOT));
    }

    private static Node find(Node node, String name) {
        if (node == null) {
            return null;
        }
        if (name.equals(node.getNodeName())) {
            return node;
        }
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            Node found = ImageDataValidator.find(child, name);
            if (found == null) continue;
            return found;
        }
        return null;
    }

    private static int intAttr(Node node, String name, int fallback) {
        try {
            return Integer.parseInt(ImageDataValidator.stringAttr(node, name, Integer.toString(fallback)));
        }
        catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static String stringAttr(Node node, String name, String fallback) {
        if (node == null) {
            return fallback;
        }
        NamedNodeMap attrs = node.getAttributes();
        Node attr = attrs == null ? null : attrs.getNamedItem(name);
        return attr == null ? fallback : attr.getNodeValue();
    }

    private static BufferedImage copy(BufferedImage source) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), 2);
        Graphics2D graphics = copy.createGraphics();
        graphics.setComposite(AlphaComposite.Src);
        graphics.drawImage((Image)source, 0, 0, null);
        graphics.dispose();
        return copy;
    }

    private static BufferedImage toArgb(BufferedImage source) {
        return ImageDataValidator.copy(source);
    }

    private static boolean startsWith(byte[] data, byte[] magic) {
        if (data == null || data.length < magic.length) {
            return false;
        }
        for (int i = 0; i < magic.length; ++i) {
            if (data[i] == magic[i]) continue;
            return false;
        }
        return true;
    }

    public record Decoded(String contentId, String format, int width, int height, List<Frame> frames) {
    }

    public record Frame(BufferedImage image, int delayMillis) {
    }

    private record FrameMeta(int left, int top, int delayHundredths, String disposal) {
    }
}


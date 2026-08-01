package cn.solo.sanguosha.client;

import cn.solo.sanguosha.image.ImageDataValidator;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public final class CustomGeneralImageManager {
    private static final ExecutorService IMAGE_IO = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "Sanguosha-Image-IO");
        thread.setDaemon(true);
        return thread;
    });
    private static final long POWERSHELL_TIMEOUT_SECONDS = 120L;
    private static final String POWERSHELL_DIALOG_SCRIPT = "$ErrorActionPreference='Stop';Add-Type -AssemblyName System.Windows.Forms;$d=New-Object System.Windows.Forms.OpenFileDialog;$d.Title='\u9009\u62e9\u6b66\u5c06\u56fe\u7247';$d.Filter='PNG \u6216 GIF \u56fe\u7247 (*.png;*.gif)|*.png;*.gif';$d.Multiselect=$false;$d.CheckFileExists=$true;if($d.ShowDialog() -eq [System.Windows.Forms.DialogResult]::OK){$b=[Text.Encoding]::UTF8.GetBytes($d.FileName);[Console]::Out.Write('OK:'+[Convert]::ToBase64String($b))}else{[Console]::Out.Write('CANCEL')}";

    private CustomGeneralImageManager() {
    }

    public static CompletableFuture<SelectedImage> chooseAsync() {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return CompletableFuture.completedFuture(CustomGeneralImageManager.failure("\u6587\u4ef6\u9009\u62e9\u5668\u53ea\u80fd\u5728 Minecraft \u5ba2\u6237\u7aef\u4f7f\u7528"));
        }
        return CompletableFuture.supplyAsync(() -> {
            Selection selection;
            Selection selection2 = selection = CustomGeneralImageManager.isWindows() ? CustomGeneralImageManager.chooseWindows() : CustomGeneralImageManager.chooseAwt();
            if (selection.path() == null) {
                return CustomGeneralImageManager.failure(selection.message());
            }
            return CustomGeneralImageManager.loadSelected(selection.path());
        }, IMAGE_IO);
    }

    private static Selection chooseWindows() {
        Selection selection;
        block8: {
            MemoryStack stack = MemoryStack.stackPush();
            try {
                PointerBuffer filters = stack.mallocPointer(2);
                filters.put(stack.UTF8((CharSequence)"*.png"));
                filters.put(stack.UTF8((CharSequence)"*.gif"));
                filters.flip();
                String selected = TinyFileDialogs.tinyfd_openFileDialog((CharSequence)"\u9009\u62e9\u6b66\u5c06\u56fe\u7247", (CharSequence)"", (PointerBuffer)filters, (CharSequence)"PNG \u6216 GIF \u56fe\u7247", (boolean)false);
                Selection selection2 = selection = selected == null ? Selection.cancelled() : Selection.selected(Path.of(selected, new String[0]));
                if (stack == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (stack != null) {
                        try {
                            stack.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Throwable tinyfdFailure) {
                    return CustomGeneralImageManager.chooseWindowsPowerShell();
                }
            }
            stack.close();
        }
        return selection;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Selection chooseWindowsPowerShell() {
        String encoded = Base64.getEncoder().encodeToString(POWERSHELL_DIALOG_SCRIPT.getBytes(StandardCharsets.UTF_16LE));
        Process process = null;
        try {
            process = new ProcessBuilder("powershell.exe", "-NoLogo", "-NoProfile", "-NonInteractive", "-WindowStyle", "Hidden", "-EncodedCommand", encoded).redirectErrorStream(true).start();
            if (!process.waitFor(120L, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                Selection selection = Selection.failed("\u7cfb\u7edf\u6587\u4ef6\u9009\u62e9\u5668\u7b49\u5f85\u8d85\u65f6");
                return selection;
            }
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            Selection selection = CustomGeneralImageManager.parsePowerShellResult(output, process.exitValue());
            return selection;
        }
        catch (IOException exception) {
            Selection selection = Selection.failed("\u65e0\u6cd5\u542f\u52a8 Windows \u7cfb\u7edf\u6587\u4ef6\u9009\u62e9\u5668");
            return selection;
        }
        catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            Selection selection = Selection.failed("\u6587\u4ef6\u9009\u62e9\u5df2\u4e2d\u65ad");
            return selection;
        }
        finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    static Selection parsePowerShellResult(String output, int exitCode) {
        if (exitCode != 0) {
            return Selection.failed("Windows \u7cfb\u7edf\u6587\u4ef6\u9009\u62e9\u5668\u6267\u884c\u5931\u8d25");
        }
        if ("CANCEL".equals(output)) {
            return Selection.cancelled();
        }
        if (output == null || !output.startsWith("OK:")) {
            return Selection.failed("Windows \u7cfb\u7edf\u6587\u4ef6\u9009\u62e9\u5668\u8fd4\u56de\u4e86\u65e0\u6548\u7ed3\u679c");
        }
        try {
            String path = new String(Base64.getDecoder().decode(output.substring(3)), StandardCharsets.UTF_8);
            return path.isBlank() ? Selection.failed("Windows \u7cfb\u7edf\u6587\u4ef6\u9009\u62e9\u5668\u8fd4\u56de\u4e86\u7a7a\u8def\u5f84") : Selection.selected(Path.of(path, new String[0]));
        }
        catch (RuntimeException exception) {
            return Selection.failed("Windows \u7cfb\u7edf\u6587\u4ef6\u9009\u62e9\u5668\u8fd4\u56de\u4e86\u65e0\u6548\u8def\u5f84");
        }
    }

    private static Selection chooseAwt() {
        if (GraphicsEnvironment.isHeadless()) {
            return Selection.failed("\u5f53\u524d\u975e Windows \u5e73\u53f0\u6ca1\u6709\u53ef\u7528\u684c\u9762\u73af\u5883\uff0c\u65e0\u6cd5\u6253\u5f00\u6587\u4ef6\u9009\u62e9\u5668");
        }
        Window dialog = null;
        try {
            dialog = new FileDialog((Frame)null, "\u9009\u62e9\u6b66\u5c06\u56fe\u7247", 0);
            ((FileDialog)dialog).setFilenameFilter((dir, name) -> CustomGeneralImageManager.hasAllowedExtension(Path.of(name, new String[0])));
            ((Dialog)dialog).setVisible(true);
            if (((FileDialog)dialog).getFile() == null || ((FileDialog)dialog).getDirectory() == null) {
                Selection selection = Selection.cancelled();
                return selection;
            }
            Selection selection = Selection.selected(Path.of(((FileDialog)dialog).getDirectory(), ((FileDialog)dialog).getFile()));
            return selection;
        }
        catch (RuntimeException exception) {
            Selection selection = Selection.failed("\u65e0\u6cd5\u6253\u5f00\u7cfb\u7edf\u6587\u4ef6\u9009\u62e9\u5668\uff1a" + CustomGeneralImageManager.safeMessage(exception));
            return selection;
        }
        finally {
            if (dialog != null) {
                dialog.dispose();
            }
        }
    }

    private static SelectedImage loadSelected(Path selected) {
        try {
            String displayName = selected.getFileName().toString();
            if (!CustomGeneralImageManager.hasAllowedExtension(selected)) {
                return CustomGeneralImageManager.failure("\u4ec5\u652f\u6301 PNG \u6216 GIF \u56fe\u7247");
            }
            if (!Files.isRegularFile(selected, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(selected)) {
                return CustomGeneralImageManager.failure("\u6240\u9009\u6587\u4ef6\u4e0d\u662f\u666e\u901a\u6587\u4ef6");
            }
            long size = Files.size(selected);
            if (size <= 0L || size > 0x1000000L) {
                return CustomGeneralImageManager.failure("\u56fe\u7247\u5fc5\u987b\u5c0f\u4e8e\u6216\u7b49\u4e8e 16 MiB");
            }
            byte[] data = Files.readAllBytes(selected);
            ImageDataValidator.Decoded decoded = ImageDataValidator.validate(data, false);
            return new SelectedImage(true, "\u56fe\u7247\u5df2\u9a8c\u8bc1\uff1a" + displayName, displayName, decoded.contentId(), decoded.format(), data);
        }
        catch (IOException | RuntimeException exception) {
            return CustomGeneralImageManager.failure("\u56fe\u7247\u65e0\u6548\u6216\u65e0\u6cd5\u89e3\u7801\uff1a" + CustomGeneralImageManager.safeMessage(exception));
        }
    }

    static boolean hasAllowedExtension(Path path) {
        if (path == null || path.getFileName() == null) {
            return false;
        }
        String lower = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return lower.endsWith(".png") || lower.endsWith(".gif");
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    private static String safeMessage(Throwable throwable) {
        String message = throwable.getMessage();
        return message == null || message.isBlank() ? throwable.getClass().getSimpleName() : message;
    }

    public static CompletableFuture<ImageDataValidator.Decoded> decodeAsync(byte[] data) {
        byte[] safeCopy = data == null ? new byte[]{} : Arrays.copyOf(data, data.length);
        return CompletableFuture.supplyAsync(() -> {
            try {
                return ImageDataValidator.validate(safeCopy, true);
            }
            catch (IOException ignored) {
                return null;
            }
        }, IMAGE_IO);
    }

    public static CompletableFuture<Boolean> writeCacheAsync(String contentId, String format, byte[] data) {
        byte[] safeCopy = Arrays.copyOf(data, data.length);
        return CompletableFuture.supplyAsync(() -> {
            try {
                ImageDataValidator.Decoded decoded = ImageDataValidator.validate(safeCopy, false);
                if (!decoded.contentId().equals(contentId) || !decoded.format().equals(format)) {
                    return false;
                }
                Path dir = Minecraft.m_91087_().f_91069_.toPath().resolve("sanguosha-cache/images").toAbsolutePath().normalize();
                Files.createDirectories(dir, new FileAttribute[0]);
                Path target = dir.resolve(contentId + "." + format).normalize();
                if (!target.getParent().equals(dir)) {
                    return false;
                }
                Path temp = Files.createTempFile(dir, contentId + ".", ".tmp", new FileAttribute[0]);
                try {
                    Files.write(temp, safeCopy, new OpenOption[0]);
                    try {
                        Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                    }
                    catch (AtomicMoveNotSupportedException ignored) {
                        Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                finally {
                    Files.deleteIfExists(temp);
                }
                return true;
            }
            catch (IOException | RuntimeException ignored) {
                return false;
            }
        }, IMAGE_IO);
    }

    public static CompletableFuture<byte[]> readCacheAsync(String contentId, String format) {
        if (!ImageDataValidator.validContentId(contentId) || !"png".equals(format) && !"gif".equals(format)) {
            return CompletableFuture.completedFuture(new byte[0]);
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path dir = Minecraft.m_91087_().f_91069_.toPath().resolve("sanguosha-cache/images").toAbsolutePath().normalize();
                Path file = dir.resolve(contentId + "." + format).normalize();
                if (!file.getParent().equals(dir) || !Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(file)) {
                    return new byte[0];
                }
                long size = Files.size(file);
                if (size <= 0L || size > 0x1000000L) {
                    return new byte[0];
                }
                byte[] data = Files.readAllBytes(file);
                return ImageDataValidator.sha256(data).equals(contentId) ? data : new byte[]{};
            }
            catch (IOException ignored) {
                return new byte[0];
            }
        }, IMAGE_IO);
    }

    private static SelectedImage failure(String message) {
        return new SelectedImage(false, message, "", "", "", new byte[0]);
    }

    public record SelectedImage(boolean success, String message, String fileName, String contentId, String format, byte[] data) {
    }

    record Selection(Path path, String message) {
        static Selection selected(Path path) {
            return new Selection(path, "");
        }

        static Selection cancelled() {
            return new Selection(null, "\u5df2\u53d6\u6d88\u9009\u62e9");
        }

        static Selection failed(String message) {
            return new Selection(null, message);
        }
    }
}


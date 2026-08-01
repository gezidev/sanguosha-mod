import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import net.minecraftforge.accesstransformer.AccessTransformerEngine;

public class ApplyAt {
    public static void main(String[] args) throws Exception {
        Path inJar = Paths.get(args[0]);
        Path atJar = Paths.get(args[1]);
        Path outJar = Paths.get(args[2]);

        AccessTransformerEngine.INSTANCE.addResource(atJar, "accesstransformer.cfg");

        int total = 0;
        try (JarFile in = new JarFile(inJar.toFile());
             JarOutputStream out = new JarOutputStream(Files.newOutputStream(outJar))) {
            var entries = in.stream().toList();
            for (JarEntry entry : entries) {
                String name = entry.getName();
                try {
                    if (name.endsWith(".class")) {
                        byte[] bytes = in.getInputStream(entry).readAllBytes();
                        ClassReader cr = new ClassReader(bytes);
                        ClassNode cn = new ClassNode();
                        cr.accept(cn, ClassReader.EXPAND_FRAMES);
                        if (AccessTransformerEngine.INSTANCE.transform(cn, Type.getObjectType(cn.name))) {
                            ClassWriter cw = new ClassWriter(0);
                            cn.accept(cw);
                            bytes = cw.toByteArray();
                            total++;
                        }
                        out.putNextEntry(new JarEntry(name));
                        out.write(bytes);
                    } else {
                        out.putNextEntry(new JarEntry(name));
                        in.getInputStream(entry).transferTo(out);
                    }
                    out.closeEntry();
                } catch (Exception e) {
                    throw new RuntimeException("Failed on " + name, e);
                }
            }
        }
        System.out.println("AT applied to " + outJar + " (" + total + " classes modified)");
    }
}

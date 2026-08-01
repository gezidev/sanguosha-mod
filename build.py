#!/usr/bin/env python3
"""
Build script for the SanguoshaForge mod (Forge 1.20.1).

The mod's source references SRG-named (m_/f_) Minecraft, so this build
compiles the sources against the runtime jars, with Forge's Access
Transformer applied to the vanilla client jar (same as a Forge dev env).

Steps:
  1. Build the compile classpath from the installed Forge libraries.
  2. Apply Forge's access transformer (AT) to the SRG-named client jar.
  3. Compile src/main/java with javac (JDK 17).
  4. Package classes + src/main/resources into a mod jar.

Requirements:
  - JDK 17 (any JDK works; javac must be on PATH)
  - A Forge 1.20.1 (47.3.x) instance whose version JSON + libraries are on disk
  - Python 3 (only for this build driver)
"""
import json
import os
import subprocess
import sys
import zipfile

ROOT = os.path.dirname(os.path.abspath(__file__))
JAVA_SRC = os.path.join(ROOT, "src", "main", "java")
RES_SRC = os.path.join(ROOT, "src", "main", "resources")
LIBS_DIR = os.path.join(ROOT, "libs")
BUILD = os.path.join(ROOT, "build")

# ---- config ---------------------------------------------------------------
# Point at your local Forge 1.20.1 instance via environment variables:
#   SANGUOSHA_GAME_DIR  = path to the instance folder (contains the version JSON)
#   SANGUOSHA_MC_LIBS   = path to your Minecraft libraries folder
GAME_DIR = os.environ.get("SANGUOSHA_GAME_DIR", "").strip()
MC_LIBS = os.environ.get("SANGUOSHA_MC_LIBS", "").strip()
MC_VERSION = "1.20.1"
FORGE_VERSION = "1.20.1-47.3.33"
CLIENT_TIMESTAMP = "20230612.114412"
OUT_JAR = os.path.join(BUILD, "libs", "SanguoshaForge-rebuilt.jar")


def run(cmd, **kw):
    print(">>", " ".join(cmd) if isinstance(cmd, list) else cmd)
    r = subprocess.run(cmd, **kw)
    if r.returncode != 0:
        sys.exit(r.returncode)
    return r


def build_classpath():
    """Classpath of all installed libraries + the AT-applied client jar."""
    ver_json = os.path.join(GAME_DIR, os.path.basename(GAME_DIR) + ".json")
    d = json.load(open(ver_json, encoding="utf-8"))
    cp = []
    for lib in d.get("libraries", []):
        p = lib.get("downloads", {}).get("artifact", {}).get("path")
        if p and os.path.exists(os.path.join(MC_LIBS, p)):
            cp.append(os.path.join(MC_LIBS, p))
    # Forge API / @Mod annotation jars (not in the version JSON library list).
    # IMPORTANT: these must come BEFORE the client jar so Forge-patched classes
    # (BlockEntity.getCapability, Slot.getSlotIndex, ItemProperties.register, ...)
    # win over the vanilla classes in the raw client jar.
    for rel in (
        r"net\minecraftforge\forge\1.20.1-47.3.33\forge-1.20.1-47.3.33-universal.jar",
        r"net\minecraftforge\forge\1.20.1-47.3.33\forge-1.20.1-47.3.33-client.jar",
        r"net\minecraftforge\fmlcore\1.20.1-47.3.33\fmlcore-1.20.1-47.3.33.jar",
        r"net\minecraftforge\javafmllanguage\1.20.1-47.3.33\javafmllanguage-1.20.1-47.3.33.jar",
        r"net\minecraftforge\fmlloader\1.20.1-47.3.33\fmlloader-1.20.1-47.3.33.jar",
        r"net\minecraftforge\fmlearlydisplay\1.20.1-47.3.33\fmlearlydisplay-1.20.1-47.3.33.jar",
    ):
        p = os.path.join(MC_LIBS, rel)
        if os.path.exists(p):
            cp.append(p)
    # The AT-applied vanilla client jar goes last.
    cp.append(os.path.join(BUILD, "client-at.jar"))
    for f in sorted(os.listdir(LIBS_DIR)):
        if f.endswith(".jar"):
            cp.append(os.path.join(LIBS_DIR, f))
    return ";".join(dict.fromkeys(cp))


def apply_at():
    """Apply Forge's access transformer to the SRG-named client jar."""
    at_client = os.path.join(BUILD, "client-at.jar")
    if os.path.exists(at_client):
        return at_client
    client = os.path.join(
        MC_LIBS, "net", "minecraft", "client", CLIENT_TIMESTAMP,
        f"client-{CLIENT_TIMESTAMP}-srg.jar",
    )
    forge = os.path.join(MC_LIBS, "net", "minecraftforge", "forge", FORGE_VERSION,
                         f"forge-{FORGE_VERSION}-universal.jar")
    at_tool = os.path.join(ROOT, "tools", "ApplyAt.java")
    os.makedirs(os.path.join(BUILD, "tools-classes"), exist_ok=True)
    cp = os.path.join(MC_LIBS, "cpw/mods/securejarhandler/2.1.10/securejarhandler-2.1.10.jar") + ";" + \
        os.path.join(MC_LIBS, "org/ow2/asm/asm/9.7.1/asm-9.7.1.jar") + ";" + \
        os.path.join(MC_LIBS, "org/ow2/asm/asm-tree/9.7.1/asm-tree-9.7.1.jar") + ";" + \
        os.path.join(MC_LIBS, "org/ow2/asm/asm-commons/9.7.1/asm-commons-9.7.1.jar") + ";" + \
        os.path.join(MC_LIBS, "com/google/code/gson/gson/2.10/gson-2.10.jar") + ";" + \
        os.path.join(MC_LIBS, "org/apache/logging/log4j/log4j-api/2.19.0/log4j-api-2.19.0.jar") + ";" + \
        os.path.join(MC_LIBS, "org/antlr/antlr4-runtime/4.9.1/antlr4-runtime-4.9.1.jar")
    run(["javac", "-encoding", "UTF-8", "-cp", cp,
         "-d", os.path.join(BUILD, "tools-classes"), at_tool])
    # strip trailing '# name' comments (parser 8.0.4 does not accept them)
    at_cfg = os.path.join(BUILD, "at.cfg")
    with zipfile.ZipFile(forge) as z:
        lines = z.read("META-INF/accesstransformer.cfg").decode("utf-8").splitlines()
    clean = []
    for ln in lines:
        s = ln.strip()
        if not s or s.startswith("#"):
            continue
        s = s.split(" #", 1)[0].strip()
        if s:
            clean.append(s)
    with open(at_cfg, "w", encoding="utf-8") as f:
        f.write("\n".join(clean))
    run(["java", "-cp", os.path.join(BUILD, "tools-classes") + ";" + cp,
         "ApplyAt", client, at_cfg, at_client])
    return at_client


def main():
    if not GAME_DIR or not MC_LIBS:
        print("Set the SANGUOSHA_GAME_DIR and SANGUOSHA_MC_LIBS environment variables first (see README).")
        sys.exit(2)
    os.makedirs(os.path.join(BUILD, "classes"), exist_ok=True)
    apply_at()
    cp = build_classpath()
    with open(os.path.join(ROOT, "classpath.txt"), "w") as f:
        f.write(cp)
    sources = []
    for root, _dirs, files in os.walk(JAVA_SRC):
        for f in files:
            if f.endswith(".java"):
                sources.append(os.path.join(root, f))
    run(["javac", "-encoding", "UTF-8", "-nowarn",
         "-d", os.path.join(BUILD, "classes"), "-cp", cp] + sources)
    # package jar
    os.makedirs(os.path.join(BUILD, "jar-staging"), exist_ok=True)
    staging = os.path.join(BUILD, "jar-staging")
    for d in (os.path.join(BUILD, "classes"), RES_SRC):
        for root, dirs, files in os.walk(d):
            for f in files:
                src = os.path.join(root, f)
                rel = os.path.relpath(src, d)
                dst = os.path.join(staging, rel)
                os.makedirs(os.path.dirname(dst), exist_ok=True)
                import shutil
                shutil.copy2(src, dst)
    os.makedirs(os.path.dirname(OUT_JAR), exist_ok=True)
    run(["jar", "cf", OUT_JAR, "-C", staging, "."])
    size = os.path.getsize(OUT_JAR) // (1024 * 1024)
    print(f"\nBuilt {OUT_JAR} ({size} MB). Drop it into the instance mods/ folder.")


if __name__ == "__main__":
    main()

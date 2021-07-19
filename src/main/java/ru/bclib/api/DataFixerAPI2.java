package ru.bclib.api;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import ru.bclib.BCLib;
import ru.bclib.config.Configs;
import ru.bclib.config.PathConfig;
import ru.bclib.config.SessionConfig;
import ru.bclib.util.Logger;

public class DataFixerAPI2 {
    private static final Logger LOGGER = new Logger("DataFixerAPI");

    public static void fixData(SessionConfig config) {
        if (true || !Configs.MAIN_CONFIG.getBoolean(Configs.MAIN_PATCH_CATEGORY, "applyPatches", true)){
            LOGGER.info("World Patches are disabled");
            return;
        }

        final File dir = config.levelFolder;
        Patch.MigrationData data =  Patch.createMigrationData(config);
        if (!data.hasAnyFixes()) {
            LOGGER.info("Everything up to date");
            return;
        }

        List<File> regions = getAllRegions(dir, null);
        regions.parallelStream().forEach((file) -> {
            try {
                LOGGER.info("Inspecting " + file);
                boolean[] changed = new boolean[1];
                RegionFile region = new RegionFile(file, file.getParentFile(), true);
                for (int x = 0; x < 32; x++) {
                    for (int z = 0; z < 32; z++) {
                        ChunkPos pos = new ChunkPos(x, z);
                        changed[0] = false;
                        if (region.hasChunk(pos)) {
                            DataInputStream input = region.getChunkDataInputStream(pos);
                            CompoundTag root = NbtIo.read(input);
                            // if ((root.toString().contains("betternether") || root.toString().contains("bclib")) && root.toString().contains("chest")) {
                            //    NbtIo.write(root, new File(file.toString() + "-" + x + "-" + z + ".nbt"));
                            // }
                            input.close();

                            ListTag tileEntities = root.getCompound("Level").getList("TileEntities", 10);
                            fixItemArrayWithID(tileEntities, changed, data, true);

                            ListTag sections = root.getCompound("Level").getList("Sections", 10);
                            sections.forEach((tag) -> {
                                ListTag palette = ((CompoundTag) tag).getList("Palette", 10);
                                palette.forEach((blockTag) -> {
                                    CompoundTag blockTagCompound = ((CompoundTag) blockTag);
                                    changed[0] = data.replaceStringFromIDs(blockTagCompound, "Name");
                                });
                            });
                            if (changed[0]) {
                                LOGGER.warning("Writing '{}': {}/{}",  file, x, z);
                                DataOutputStream output = region.getChunkDataOutputStream(pos);
                                NbtIo.write(root, output);
                                output.close();
                            }
                        }
                    }
                }
                region.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });

        data.markApplied();
    }

    private static boolean fixItemArrayWithID(ListTag items, boolean[] changed, Patch.MigrationData data, boolean recursive){
        items.forEach(inTag -> {
            final CompoundTag tag = (CompoundTag) inTag;

            changed[0] |= data.replaceStringFromIDs(tag, "id");

            if (recursive && tag.contains("Items")){
                changed[0] |= fixItemArrayWithID(tag.getList("Items", 10), changed, data, true);
            }
        });

        return changed[0];
    }

    private static List<File> getAllRegions(File dir, List<File> list) {
        if (list == null) {
            list = new ArrayList<>();
        }
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                getAllRegions(file, list);
            }
            else if (file.isFile() && file.getName().endsWith(".mca")) {
                list.add(file);
            }
        }
        return list;
    }

    public abstract static class Patch {
        private static List<Patch> ALL = new ArrayList<>(10);
        private final int level;
        @NotNull
        private final String modID;

        static List<Patch> getALL() {
            return ALL;
        }

        /**
         * register a new Patch
         * @param patch A #Supplier that will instantiate the new Patch Object
         */
        public static void registerPatch(Supplier<Patch> patch){
            ALL.add(patch.get());
        }

        /**
         * Returns the highest patch-level that is available for the given mod. If no patches were
         * registerd for the mod, this will return 0
         * @param modID The ID of the mod you want to query
         * @return The highest Patch-Level that was found
         */
        public static int maxPatchLevel(@NotNull String modID){
            return ALL.stream()
                    .filter(p-> p.getModID().equals(modID))
                    .mapToInt(p->p.level)
                    .max()
                    .orElse(0);
        }

        /**
         * Called by inheriting classes.
         * 
         * Performs some sanity checks on the values and might throw a #RuntimeException if any 
         * inconsistencies are found.
         * 
         * @param modID The ID of the Mod you want to register a patch for. This should be your 
         *              ModID only. The ModID can not be {@code null} or an empty String.
         * @param level The level of the Patch. This needs to be a non-zero positive value. 
         *              Developers are responsible for registering their patches in the correct 
         *              order (with increasing levels). You are not allowed to register a new
         *              Patch with a Patch-level lower or equal than
         *              {@link Patch#maxPatchLevel(String)}
         */
        protected Patch(@NotNull String modID, int level) {
            //Patchlevels need to be unique and registered in ascending order
            if (modID==null || "".equals(modID)){
                throw new RuntimeException("[INTERNAL ERROR] Patches need a valid modID!");
            }
            if (!ALL.stream().filter(p-> p.getModID().equals(modID)).noneMatch(p -> p.getLevel() >= level)  || level <= 0){
                throw new RuntimeException("[INTERNAL ERROR] Patch-levels need to be created in ascending order beginning with 1.");
            }


            BCLib.LOGGER.info("Creating Patchlevel {} ({}, {})", level, ALL, ALL.stream().noneMatch(p -> p.getLevel() >= level));
            this.level = level;
            this.modID = modID;
        }

        @Override
        public String toString() {
            return "Patch{" +
                    "level=" + getModID() + ":" + getLevel() +
                    '}';
        }

        final public int getLevel() {
            return level;
        }

        /**
         * The Mod-ID that registered this Patch
         * @return The ID
         */
        final public String getModID() {
            return modID;
        }


        /**
         * Return block data fixes. Fixes will be applied on world load if current patch-level for
         * the linked mod is lower than the {@link #level}.
         *
         * The default implementation of this method returns an empty map.
         *
         * @return The returned Map should contain the replacements. All occurences of the
         * {@code KeySet} are replaced with the associated value.
         */
        public Map<String, String> getIDReplacements(){
            return new HashMap<String, String>();
        }

        /**
         * Generates ready to use data for all currently registered patches. The list of
         * patches is selected by the current patch-level of the world.
         *
         * A {@link #Patch} with a given {@link #level} is only included if the patch-level of the
         * world is less
         * @return a new {@link MigrationData} Object.
         */
        static MigrationData createMigrationData(PathConfig config){
            return new MigrationData(config);
        }

        static class MigrationData{
            final Set<String> mods;
            final Map<String, String> idReplacements;
            private final PathConfig config;

            private MigrationData(PathConfig config){
                this.config = config;

                this.mods = Collections.unmodifiableSet(Patch.getALL().stream().map(p -> p.modID).collect(Collectors.toSet()));

                HashMap<String, String> replacements = new HashMap<String, String>();
                for(String modID : mods){
                    Patch.getALL().stream().filter(p -> p.modID.equals(modID)).forEach(patch -> {
                        if (currentPatchLevel(modID) < patch.level) {
                            replacements.putAll(patch.getIDReplacements());
                            LOGGER.info("Applying " + patch);
                        } else {
                            LOGGER.info("Ignoring " + patch);
                        }
                    });
                }

                this.idReplacements = Collections.unmodifiableMap(replacements);
            }

            final public void markApplied(){
                for(String modID : mods){
                    LOGGER.info("Updating Patch-Level for '{}' from {} to {} -> {}",
                            modID,
                            currentPatchLevel(modID),
                            Patch.maxPatchLevel(modID),
                            config.setInt(Configs.MAIN_PATCH_CATEGORY, modID, Patch.maxPatchLevel(modID))
                    );
                }

                config.saveChanges();
            }

            public int currentPatchLevel(@NotNull String modID){
                return config.getInt(Configs.MAIN_PATCH_CATEGORY, modID, 0);
            }

            public boolean hasAnyFixes(){
                return idReplacements.size()>0;
            }

            public boolean replaceStringFromIDs(@NotNull CompoundTag tag, @NotNull String key){
                if (!tag.contains(key)) return false;

                final String val = tag.getString(key);
                final String replace = idReplacements.get(val);

                if (replace != null){
                    LOGGER.warning("Replacing ID '{}' with '{}'.", val, replace);
                    tag.putString(key, replace);
                    return true;
                }

                return false;
            }
        }
    }
}

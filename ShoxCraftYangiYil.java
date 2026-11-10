package uz.shoxcraft.yangiyil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShoxCraftYangiYil extends JavaPlugin implements CommandExecutor {

    private static final String DUNYO = "shoxcraft_yangiyil";
    private static final String TAG_HOLO = "shoxcraft_holo";
    private static final String TAG_SANOQ = "shoxcraft_sanoq";
    private static final String PREFIX = "§6§l[ShoxCraft] §r";

    private final Random rnd = new Random();

    private static final Material[] RANGLAR = {
            Material.RED_CONCRETE, Material.BLUE_CONCRETE, Material.GREEN_CONCRETE,
            Material.YELLOW_CONCRETE, Material.PURPLE_CONCRETE, Material.ORANGE_CONCRETE
    };
    private static final Material[] BEZAK = {
            Material.GLOWSTONE, Material.SEA_LANTERN, Material.RED_WOOL,
            Material.YELLOW_CONCRETE, Material.LIGHT_BLUE_CONCRETE
    };

    // ------------------------------------------------------------------ yoqish

    @Override
    public void onEnable() {
        if (getCommand("yangiyil") != null) {
            getCommand("yangiyil").setExecutor(this);
        }

        // Server qayta yoqilganda dunyo bor bo'lsa, uni yuklab olamiz
        Bukkit.getScheduler().runTask(this, () -> {
            File papka = new File(Bukkit.getWorldContainer(), DUNYO);
            if (papka.exists() && Bukkit.getWorld(DUNYO) == null) {
                yaratDunyo();
            }
        });

        // Yangi yilgacha qolgan vaqt hologrammasini har soniyada yangilaymiz
        Bukkit.getScheduler().runTaskTimer(this, this::sanoqniYangila, 40L, 20L);
        getLogger().info("ShoxCraft Yangi Yil plagini yoqildi!");
    }

    // ----------------------------------------------------------------- komanda

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("shoxcraft.yangiyil")) {
            sender.sendMessage(PREFIX + "§cSizda bu komanda uchun ruxsat yo'q!");
            return true;
        }

        sender.sendMessage(PREFIX + "§eYangi Yil dunyosi qurilmoqda, biroz kuting...");

        World eski = Bukkit.getWorld(DUNYO);
        if (eski != null) {
            World asosiy = Bukkit.getWorlds().get(0);
            for (Player p : new ArrayList<>(eski.getPlayers())) {
                p.teleport(asosiy.getSpawnLocation());
            }
            File papka = eski.getWorldFolder();
            if (!Bukkit.unloadWorld(eski, false)) {
                sender.sendMessage(PREFIX + "§cEski dunyoni o'chirib bo'lmadi. Qayta urinib ko'ring.");
                return true;
            }
            ochir(papka);
        } else {
            File papka = new File(Bukkit.getWorldContainer(), DUNYO);
            if (papka.exists()) {
                ochir(papka);
            }
        }

        World dunyo = yaratDunyo();
        if (dunyo == null) {
            sender.sendMessage(PREFIX + "§cDunyoni yaratib bo'lmadi! Konsolni tekshiring.");
            return true;
        }

        qur(dunyo);

        Location nuqta = new Location(dunyo, 0.5, 64, 8.5, 180f, 0f);
        if (sender instanceof Player) {
            Player p = (Player) sender;
            p.teleport(nuqta);
            p.sendTitle("§6§lShoxCraft", "§bYangi Yil bilan!", 10, 70, 20);
        }

        Bukkit.broadcastMessage(PREFIX + "§aYangi Yil dunyosi tayyor! §7(/yangiyil)");
        return true;
    }

    // ------------------------------------------------------------------- dunyo

    private World yaratDunyo() {
        try {
            WorldCreator wc = new WorldCreator(DUNYO);
            wc.environment(World.Environment.NORMAL);
            wc.generator(new QorGenerator());
            wc.biomeProvider(new QorBiom());
            wc.generateStructures(false);
            return wc.createWorld();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Tekis qorli dunyo: pastda bedrock, keyin tuproq va qor bloklari. */
    private static class QorGenerator extends ChunkGenerator {
        @Override
        public void generateNoise(WorldInfo info, Random random, int chunkX, int chunkZ, ChunkData data) {
            int min = info.getMinHeight();
            data.setRegion(0, min, 0, 16, min + 1, 16, Material.BEDROCK);
            data.setRegion(0, min + 1, 0, 16, 62, 16, Material.DIRT);
            data.setRegion(0, 62, 0, 16, 64, 16, Material.SNOW_BLOCK);
        }

        @Override
        public Location getFixedSpawnLocation(World world, Random random) {
            return new Location(world, 0.5, 64, 8.5);
        }
    }

    /** Hamma joy qorli tekislik biomi (qor yog'adi). */
    private static class QorBiom extends BiomeProvider {
        @Override
        public Biome getBiome(WorldInfo info, int x, int y, int z) {
            return Biome.SNOWY_PLAINS;
        }

        @Override
        public List<Biome> getBiomes(WorldInfo info) {
            return List.of(Biome.SNOWY_PLAINS);
        }
    }

    // ------------------------------------------------------------------- qurish

    private void qur(World w) {
        w.setStorm(true);
        w.setThundering(false);
        w.setTime(18000);
        w.setSpawnFlags(false, false);
        w.setGameRuleValue("doDaylightCycle", "false");
        w.setGameRuleValue("doWeatherCycle", "false");
        w.getWorldBorder().setCenter(0, 0);
        w.getWorldBorder().setSize(110);
        w.setSpawnLocation(0, 64, 8);

        platforma(w);

        // Platformadagi 4 ta yolka
        int[][] burchak = {{5, 5}, {-5, 5}, {5, -5}, {-5, -5}};
        for (int[] b : burchak) {
            yolka(w, b[0], b[1]);
            gologramma(w, b[0] + 0.5, 74.6, b[1] + 0.5, "§a§lYolka", null);
        }

        // Platforma atrofidagi chiroq ustunlari
        for (int i = 0; i < 8; i++) {
            double burchakR = Math.toRadians(i * 45);
            int x = (int) Math.round(9 * Math.cos(burchakR));
            int z = (int) Math.round(9 * Math.sin(burchakR));
            chiroq(w, x, z);
        }

        // Podium atrofida sovg'alar
        for (int i = 0; i < 12; i++) {
            double burchakR = Math.toRadians(i * 30 + 15);
            int x = (int) Math.round(3 * Math.cos(burchakR));
            int z = (int) Math.round(3 * Math.sin(burchakR));
            sovga(w, x, z);
        }

        // Atrofda tasodifiy yolkalar, qor odamlar va sovg'alar
        for (int i = 0; i < 45; i++) {
            int[] n = tasodifiyNuqta(14, 45);
            yolka(w, n[0], n[1]);
        }
        for (int i = 0; i < 10; i++) {
            int[] n = tasodifiyNuqta(12, 42);
            qorOdam(w, n[0], n[1]);
            gologramma(w, n[0] + 0.5, 68.1, n[1] + 0.5, "§f§lQor odam", null);
        }
        for (int i = 0; i < 25; i++) {
            int[] n = tasodifiyNuqta(10, 45);
            sovga(w, n[0], n[1]);
        }

        asosiyGologrammalar(w);
    }

    private int[] tasodifiyNuqta(int minMasofa, int maxMasofa) {
        double burchak = rnd.nextDouble() * Math.PI * 2;
        int masofa = minMasofa + rnd.nextInt(maxMasofa - minMasofa + 1);
        return new int[]{(int) Math.round(masofa * Math.cos(burchak)),
                (int) Math.round(masofa * Math.sin(burchak))};
    }

    private void set(World w, int x, int y, int z, Material m) {
        w.getBlockAt(x, y, z).setType(m, false);
    }

    private void platforma(World w) {
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 > 64) continue;
                Material m;
                if (d2 >= 56) m = Material.SEA_LANTERN;
                else if (d2 >= 49) m = Material.RED_CONCRETE;
                else m = Material.QUARTZ_BLOCK;
                set(w, dx, 63, dz, m);
            }
        }
        // Markaziy podium
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                set(w, dx, 64, dz, Material.GOLD_BLOCK);
            }
        }
    }

    private void yolka(World w, int cx, int cz) {
        // Tanasi
        for (int y = 64; y <= 72; y++) {
            set(w, cx, y, cz, Material.SPRUCE_LOG);
        }
        // Barglari
        for (int i = 0; i <= 6; i++) {
            int y = 66 + i;
            int r = 3 - i / 2;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    if (r > 1 && Math.abs(dx) == r && Math.abs(dz) == r) continue;
                    Block b = w.getBlockAt(cx + dx, y, cz + dz);
                    Leaves l = (Leaves) Material.SPRUCE_LEAVES.createBlockData();
                    l.setPersistent(true);
                    b.setBlockData(l, false);
                    boolean tashqi = Math.abs(dx) == r || Math.abs(dz) == r;
                    if (tashqi && rnd.nextInt(7) == 0) {
                        b.setType(BEZAK[rnd.nextInt(BEZAK.length)], false);
                    }
                }
            }
        }
        // Cho'qqidagi yulduz
        set(w, cx, 73, cz, Material.GLOWSTONE);
    }

    private void qorOdam(World w, int x, int z) {
        set(w, x, 64, z, Material.SNOW_BLOCK);
        set(w, x, 65, z, Material.SNOW_BLOCK);
        set(w, x, 66, z, Material.SNOW_BLOCK);
        set(w, x, 67, z, Material.CARVED_PUMPKIN);
    }

    private void sovga(World w, int x, int z) {
        set(w, x, 64, z, RANGLAR[rnd.nextInt(RANGLAR.length)]);
        set(w, x, 65, z, Material.WHITE_CARPET);
    }

    private void chiroq(World w, int x, int z) {
        for (int y = 64; y <= 66; y++) {
            set(w, x, y, z, Material.SPRUCE_FENCE);
        }
        set(w, x, 67, z, Material.LANTERN);
    }

    // -------------------------------------------------------------- gologramma

    private void asosiyGologrammalar(World w) {
        double x = 0.5, z = 0.5;
        gologramma(w, x, 70.2, z, "§6§l✦ ShoxCraft ✦", null);
        gologramma(w, x, 69.7, z, "§b§lYANGI YIL BILAN!", null);
        gologramma(w, x, 69.2, z, "§aOmad, baxt va sog'liq tilaymiz!", null);
        gologramma(w, x, 68.7, z, "§eYangi yilgacha: ...", TAG_SANOQ);
        gologramma(w, x, 68.2, z, "§7Qor ustida o'ynang va rohatlaning ❄", null);
    }

    private void gologramma(World w, double x, double y, double z, String matn, String qoshimchaTag) {
        Location l = new Location(w, x, y, z);
        w.spawn(l, ArmorStand.class, a -> {
            a.setInvisible(true);
            a.setMarker(true);
            a.setGravity(false);
            a.setSmall(true);
            a.setInvulnerable(true);
            a.setPersistent(true);
            a.setCustomName(matn);
            a.setCustomNameVisible(true);
            a.addScoreboardTag(TAG_HOLO);
            if (qoshimchaTag != null) {
                a.addScoreboardTag(qoshimchaTag);
            }
        });
    }

    private void sanoqniYangila() {
        World w = Bukkit.getWorld(DUNYO);
        if (w == null || w.getPlayers().isEmpty()) return;

        ZonedDateTime hozir = ZonedDateTime.now(ZoneId.of("Asia/Tashkent"));
        String matn;
        if (hozir.getMonthValue() == 1 && hozir.getDayOfMonth() == 1) {
            matn = "§d§lYangi yil muborak!";
        } else {
            ZonedDateTime yangiYil = ZonedDateTime.of(hozir.getYear() + 1, 1, 1, 0, 0, 0, 0, hozir.getZone());
            Duration d = Duration.between(hozir, yangiYil);
            long kun = d.toDays();
            long soat = d.toHours() % 24;
            long daqiqa = d.toMinutes() % 60;
            long soniya = d.getSeconds() % 60;
            matn = String.format("§eYangi yilgacha: §f%d kun %02d:%02d:%02d", kun, soat, daqiqa, soniya);
        }

        for (ArmorStand a : w.getEntitiesByClass(ArmorStand.class)) {
            if (a.getScoreboardTags().contains(TAG_SANOQ)) {
                a.setCustomName(matn);
            }
        }
    }

    // ------------------------------------------------------------------ yordamchi

    private void ochir(File f) {
        File[] bolalar = f.listFiles();
        if (bolalar != null) {
            for (File c : bolalar) {
                ochir(c);
            }
        }
        f.delete();
    }
}

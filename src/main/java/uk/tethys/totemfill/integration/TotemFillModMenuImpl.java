package uk.tethys.totemfill.integration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.TranslatableText;
import uk.tethys.totemfill.TotemFill;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@Environment(EnvType.CLIENT)
public class TotemFillModMenuImpl implements ModMenuApi {

    public static TotemFillConfig getConfig() {
        return config;
    }

    private static TotemFillConfig config;
    private final Gson gson;

    public TotemFillModMenuImpl() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        config = loadConfig();
    }

    @Override
    public String getModId() {
        return TotemFill.MOD_ID;
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(new TranslatableText("title.totemfill.config"));

            builder.setSavingRunnable(() -> {
                File file = new File(FabricLoader.getInstance().getConfigDirectory() + File.separator + "totemfill.json");
                try (FileWriter writer = new FileWriter(file)) {
                    gson.toJson(config, writer);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to save config file", e);
                }
            });

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            ConfigCategory general = builder.getOrCreateCategory(new TranslatableText("category.totemfill.general"));

            general.addEntry(entryBuilder.startIntSlider(new TranslatableText("option.totemfill.minhealth"), 4, 0, 20)
                    .setTooltip(new TranslatableText("tooltip.totemfill.minhealth"))
                    .setDefaultValue(4)
                    .setSaveConsumer(config::setMinhealth)
                    .build());

            ConfigCategory messages = builder.getOrCreateCategory(new TranslatableText("category.totemfill.messages"));

            messages.addEntry(entryBuilder.startStrField(new TranslatableText("option.totemfill.nomoretotems"), config.getNomoretotems())
                    .setDefaultValue("You have run out of totems!")
                    .setSaveConsumer(config::setNomoretotems)
                    .build());

            messages.addEntry(entryBuilder.startStrField(new TranslatableText("option.totemfill.totemused"), config.getTotemused())
                    .setDefaultValue("You used a totem!")
                    .setSaveConsumer(config::setTotemused)
                    .build());

            messages.addEntry(entryBuilder.startStrField(new TranslatableText("option.totemfill.totemcount"), config.getTotemcount())
                    .setDefaultValue("You have % totems remaining.")
                    .setTooltip(new TranslatableText("tooltip.totemfill.totemcount"))
                    .setSaveConsumer(config::setTotemcount)
                    .build());

            messages.addEntry(entryBuilder.startStrField(new TranslatableText("option.totemfill.totemarmed"), config.getTotemarmed())
                    .setDefaultValue("Low health; totem armed.")
                    .setSaveConsumer(config::setTotemarmed)
                    .build());

            return builder.build();
        };
    }

    private TotemFillConfig loadConfig() {
        File file = new File(FabricLoader.getInstance().getConfigDirectory() + File.separator + "totemfill.json");
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                return gson.fromJson(new JsonReader(reader), TotemFillConfig.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load config file", e);
            }
        } else {
            return new TotemFillConfig(
                    // todo make translatable and merge with usage above
                    "You have run out of totems!",
                    "You used a totem!",
                    "You have % totems remaining.",
                    "Low health; totem armed.", 4);
        }
    }

    public static class TotemFillConfig {

        public String getNomoretotems() {
            return nomoretotems;
        }

        public void setNomoretotems(String nomoretotems) {
            this.nomoretotems = nomoretotems;
        }

        public String getTotemused() {
            return totemused;
        }

        public void setTotemused(String totemused) {
            this.totemused = totemused;
        }

        public String getTotemcount() {
            return totemcount;
        }

        public void setTotemcount(String totemcount) {
            this.totemcount = totemcount;
        }

        public int getMinhealth() {
            return minhealth;
        }

        public void setMinhealth(int minhealth) {
            this.minhealth = minhealth;
        }

        public String getTotemarmed() {
            return totemarmed;
        }

        public void setTotemarmed(String totemarmed) {
            this.totemarmed = totemarmed;
        }

        private String nomoretotems;
        private String totemused;
        private String totemcount;
        private String totemarmed;
        private int minhealth;

        public TotemFillConfig(String nomoretotems, String totemused, String totemcount, String totemarmed, int minhealth) {
            this.nomoretotems = nomoretotems;
            this.totemused = totemused;
            this.totemcount = totemcount;
            this.totemarmed = totemarmed;
            this.minhealth = minhealth;
        }

    }

}

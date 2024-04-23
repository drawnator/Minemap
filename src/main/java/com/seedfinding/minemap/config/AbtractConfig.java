package com.seedfinding.minemap.config;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seedfinding.minemap.MineMap;
import com.seedfinding.minemap.init.Logger;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

public abstract class AbtractConfig {

    public static class LocaleAgnosticFieldNamingStrategy implements FieldNamingStrategy {
        @Override
        public String translateName(Field field) {
            return separateCamelCase(field.getName(), "_").toLowerCase(Locale.ROOT);
        }

        /**
         * Converts the field name that uses camel-case define word separation into
         * separate words that are separated by the provided {@code separatorString}.
         */
        private static String separateCamelCase(String name, String separator) {
            StringBuilder translation = new StringBuilder();
            for (int i = 0; i < name.length(); i++) {
                char character = name.charAt(i);
                if (Character.isUpperCase(character) && translation.length() != 0) {
                    translation.append(separator);
                }
                translation.append(character);
            }
            return translation.toString();
        }
    }

    private static final Gson GSON = new GsonBuilder()
        .setFieldNamingStrategy(new LocaleAgnosticFieldNamingStrategy())
        .excludeFieldsWithoutExposeAnnotation()
        .excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT)
        .setPrettyPrinting()
        .enableComplexMapKeySerialization()
        .create();
    protected String root = MineMap.SETTINGS_DIR;
    protected String extension = ".json";

    public void generateConfig() {
        this.resetConfig();

        try {
            this.writeConfig();
        } catch (IOException e) {
            Logger.LOGGER.severe(e.toString());
            e.printStackTrace();
        }
    }

    private File getConfigFile() {
        return new File(this.root + File.separator + this.getName() + this.extension);
    }

    public abstract String getName();

    public AbtractConfig readConfig() {
        try {
                AbtractConfig config = GSON.fromJson(Files.newBufferedReader(Paths.get(this.getConfigFile().getPath())), this.getClass());
            if (config == null) throw new Exception("WTF");
            return config;
        } catch (Exception e) {
            Logger.LOGGER.warning(e.getMessage());
            this.generateConfig();
        }

        return this;
    }

    public AbtractConfig forceGenerateConfig() {
        this.generateConfig();
        return this;
    }

    public void updateConfig() {
        this.maintainConfig();

        try {
            this.writeConfig();
        } catch (IOException e) {
            Logger.LOGGER.severe(e.toString());
            e.printStackTrace();
        }
    }

    public abstract void maintainConfig();

    protected abstract void resetConfig();

    public void writeConfig() throws IOException {
        File dir = new File(this.root);
        if (!dir.exists() && !dir.mkdirs()) return;
        if (!this.getConfigFile().exists() && !this.getConfigFile().createNewFile()) return;
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(this.getConfigFile().getPath()),StandardCharsets.UTF_8);
        GSON.toJson(this, writer);
        writer.flush();
        writer.close();
    }

}

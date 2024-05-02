package com.meteor.wechatbc.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

public class YamlConfiguration extends BaseConfigurationSection {

    public static YamlConfiguration loadConfiguration(File file) {
        YamlConfiguration config = new YamlConfiguration();
        Yaml yaml = new Yaml();
        try (FileReader reader = new FileReader(file)) {
            config.data = yaml.load(reader);
            if (config.data == null) {
                config.data = new HashMap<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    public void save(File file) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        try (FileWriter writer = new FileWriter(file)) {
            yaml.dump(data, writer);
        }
    }


    @Override
    public ConfigurationSection getConfigurationSection(String path) {
        String[] keys = path.split("\\.");
        Map<String, Object> current = data;

        for (String key : keys) {
            Object value = current.get(key);
            if (value instanceof Map) {
                current = (Map<String, Object>) value;
            } else {
                return null; // 如果路径中的任何部分不是 Map，则返回 null
            }
        }

        SimpleConfigurationSection section = new SimpleConfigurationSection();
        section.data = current;
        return section;
    }
}

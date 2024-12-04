package br.com.devlovers.util;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;

@Component
public class WppLoader {

    private Map<String, String> sectors;

    @PostConstruct
    public void loadWppChats() {
        LoaderOptions loaderOptions = new LoaderOptions();
        Yaml yaml = new Yaml(new Constructor(WppConfig.class, loaderOptions));
        loaderOptions.setMaxAliasesForCollections(50);

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("wpp.api/chats.yml")) {
            if (inputStream != null) {
                WppConfig config = yaml.load(inputStream);
                this.sectors = config.getSectors();
            } else {
                throw new IllegalArgumentException("chats.yml file not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading chats.yml file", e);
        }
    }

    public Map<String, String> getSectors() {
        return sectors;
    }

    public String getChatIdBySector(String sector) {
        return sectors.get(sector);
    }

    public static class WppConfig {
        private Map<String, String> sectors;

        public Map<String, String> getSectors() {
            return sectors;
        }

        public void setSectors(Map<String, String> sectors) {
            this.sectors = sectors;
        }
    }
}

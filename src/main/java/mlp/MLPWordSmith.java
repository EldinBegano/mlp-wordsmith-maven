package mlp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MLPWordSmith {
    private static final Logger LOGGER = Logger.getLogger(MLPWordSmith.class.getName());
    private static final String DEFAULT_LANGUAGE = "English";
    private final Map<String, Map<String, String>> translations = new HashMap<>();
    private final Set<String> availableLanguages = new HashSet<>();
    private String currentLanguage = DEFAULT_LANGUAGE;
    private String path = "Example/src/main/resources/translation.csv"; // Default path

    private MLPWordSmith() {
        loadTranslations();
    }

    private static final class InstanceHolder {
        private static final MLPWordSmith instance = new MLPWordSmith();
    }

    public static MLPWordSmith getInstance() {
        return InstanceHolder.instance;
    }

    public void setFilePath(String filePath) {
        this.path = filePath;
        reloadTranslations();
    }

    private void reloadTranslations() {
        translations.clear();
        availableLanguages.clear();
        loadTranslations();
    }

    private void loadTranslations() {
        try (BufferedReader reader = new BufferedReader(new FileReader(path, StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            if (line != null) {
                String[] languages = line.split(",");
                for (String s : languages) {
                    String language = s.trim();
                    availableLanguages.add(language);
                    translations.put(language, new HashMap<>());
                }

                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= languages.length) {
                        String key = parts[0].trim();
                        for (int i = 0; i < languages.length; i++) {
                            String language = languages[i].trim();
                            String translation = parts[i].trim();
                            translations.get(language).put(key, translation);
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load translations from: " + path, e);
        }
    }

    public String translate(String text) {
        if (DEFAULT_LANGUAGE.equals(currentLanguage)) {
            return text;
        }

        Map<String, String> langTranslations = translations.get(currentLanguage);
        if (langTranslations == null) {
            LOGGER.log(Level.WARNING, "No translations found for language: " + currentLanguage);
            return text;
        }

        String translation = langTranslations.get(text);
        if (translation == null || translation.isEmpty()) {
            LOGGER.log(Level.INFO, "Missing translation for key: " + text);
            return text;
        }
        return translation;
    }

    public Set<String> getAvailableLanguages() {
        return Collections.unmodifiableSet(availableLanguages);
    }

    public void setCurrentLanguage(String language) {
        if (availableLanguages.contains(language)) {
            this.currentLanguage = language;
        } else {
            LOGGER.log(Level.WARNING, "Language not available: " + language);
        }
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }
}
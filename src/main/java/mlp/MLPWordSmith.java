package mlp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A thread-safe translation service that loads translations from CSV files.
 * Supports multiple languages and provides efficient text translation capabilities.
 */
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

    /**
     * Returns the singleton instance of MLPWordSmith.
     * @return the singleton instance
     */
    public static MLPWordSmith getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * Sets the path to the CSV file containing translations and reloads them.
     * @param filePath the path to the CSV file
     * @throws IllegalArgumentException if the file path is null or empty
     */
    public void setFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        Path path = Paths.get(filePath.trim());
        if (!Files.exists(path)) {
            LOGGER.log(Level.WARNING, "Translation file does not exist: " + filePath);
        }
        this.path = filePath;
        reloadTranslations();
    }

    /**
     * Reloads translations from the current CSV file path.
     */
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

    /**
     * Translates the given text to the current language.
     * @param text the text to translate
     * @return the translated text, or the original text if no translation is found
     * @throws IllegalArgumentException if text is null
     */
    public String translate(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text to translate cannot be null");
        }
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

    /**
     * Returns an unmodifiable set of available languages.
     * @return set of available languages
     */
    public Set<String> getAvailableLanguages() {
        return Collections.unmodifiableSet(availableLanguages);
    }

    /**
     * Sets the current language for translations.
     * @param language the language to set as current
     * @throws IllegalArgumentException if language is null or not available
     */
    public void setCurrentLanguage(String language) {
        if (language == null) {
            throw new IllegalArgumentException("Language cannot be null");
        }
        String trimmedLanguage = language.trim();
        if (!availableLanguages.contains(trimmedLanguage)) {
            throw new IllegalArgumentException("Language not available: " + trimmedLanguage +
                    ". Available languages: " + availableLanguages);
        }
        this.currentLanguage = trimmedLanguage;
        LOGGER.log(Level.INFO, "Language changed to: " + trimmedLanguage);
    }

    /**
     * Checks if a translation exists for the given text in the current language.
     * @param text the text to check
     * @return true if translation exists, false otherwise
     */
    public boolean hasTranslation(String text) {
        if (text == null || DEFAULT_LANGUAGE.equals(currentLanguage)) {
            return true;
        }

        Map<String, String> langTranslations = translations.get(currentLanguage);
        if (langTranslations == null) {
            return false;
        }

        String translation = langTranslations.get(text);
        return translation != null && !translation.isEmpty();
    }

    /**
     * Returns the current language.
     * @return the current language
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }

    /**
     * Returns the current CSV file path.
     * @return the current CSV file path
     */
    public String getFilePath() {
        return path;
    }
}
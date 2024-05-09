package org.sing_group.uniprot_id_mapping.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class PersistentIdCache implements IdCache {
    private Map<String, List<String>> cache = new HashMap<>();
    private File cacheFile;

    public PersistentIdCache(String cacheFilePath) {
        this.cacheFile = new File(cacheFilePath);
        loadCache();
    }

    @Override
    public List<String> getFromCache(String id) {
        return cache.get(id);
    }

    @Override
    public void addToCache(String id, List<String> mappedIds) {
        if(!cache.containsKey(id) || !cache.get(id).equals(mappedIds)){
            cache.put(id, mappedIds);
            appendToCacheFile(id, mappedIds);
        }
    }

    private void loadCache() {
        Properties properties = new Properties();
        if (cacheFile.exists()) {
            try (FileInputStream fis = new FileInputStream(cacheFile)) {
                properties.load(fis);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load cache file.", e);
            }
            properties.forEach((key, value) -> {
                List<String> ids = Arrays.stream(((String) value).split(",")).collect(Collectors.toList());
                cache.put((String) key, ids);
            });
        }
    }

    private void appendToCacheFile(String id, List<String> mappedIds) {
        try (FileWriter fw = new FileWriter(cacheFile, true);  // Set true for append mode
             PrintWriter pw = new PrintWriter(fw)) {
            // Prepare the entry in the format "key=value\n"
            String entry = id + "=" + String.join(",", mappedIds) + "\n";
            // Write directly to the file without loading existing content
            pw.print(entry);
        } catch (IOException e) {
            throw new RuntimeException("Failed to append to cache file.", e);
        }
    }
}

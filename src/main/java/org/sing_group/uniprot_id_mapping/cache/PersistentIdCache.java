/*
 * #%L
 * UniProt ID Mapping
 * %%
 * Copyright (C) 2024 Hugo López-Fernández
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
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

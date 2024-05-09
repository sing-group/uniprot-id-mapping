package org.sing_group.uniprot_id_mapping.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VolatileIdCache implements IdCache {
    private Map<String, List<String>> cache = new HashMap<>();

    @Override
    public List<String> getFromCache(String id) {
        return cache.get(id);
    }

    @Override
    public void addToCache(String id, List<String> mappedIds) {
        cache.put(id, mappedIds);
    }
}

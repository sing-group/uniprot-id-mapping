package org.sing_group.uniprot_id_mapping.cache;

import java.util.Arrays;
import java.util.List;

public interface IdCache {
    List<String> getFromCache(String id);

    void addToCache(String id, List<String> mappedIds);

    default void addToCache(String id, String... mappedIds) {
        addToCache(id, Arrays.asList(mappedIds));
    }
}

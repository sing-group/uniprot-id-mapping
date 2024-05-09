package org.sing_group.uniprot_id_mapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sing_group.uniprot_id_mapping.cache.IdCache;
import org.sing_group.uniprot_id_mapping.cache.PersistentIdCache;
import org.sing_group.uniprot_id_mapping.cache.VolatileIdCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UniProtBatchProcessor implements UniProtIdMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(UniProtBatchProcessor.class);
    private static final int DEFAULT_BATCH_SIZE = 100;

    private UniProtClient client;
    private IdCache cache;
    private int batchSize;

    public UniProtBatchProcessor() {
        this(DEFAULT_BATCH_SIZE, new UniProtClient(), new VolatileIdCache());
    }

    public UniProtBatchProcessor(int batchSize) {
        this(batchSize, new UniProtClient(), new VolatileIdCache());
    }

    public UniProtBatchProcessor(IdCache cache) {
        this(DEFAULT_BATCH_SIZE, new UniProtClient(), cache);
    }

    public UniProtBatchProcessor(int batchSize, UniProtClient client, IdCache cache) {
        this.batchSize = batchSize;
        this.client = client;
        this.cache = cache;
    }

    public Map<String, List<String>> mapIds(UniProtDbFrom from, UniProtDbTo to, List<String> ids) {
        int batchCounter = 1;
        List<String> batch = new ArrayList<>();
        Map<String, List<String>> allResults = new HashMap<>();
        for (String id : ids) {
            if (cache.getFromCache(id) == null) {
                batch.add(id);
                if (batch.size() == this.batchSize) {
                    LOGGER.debug("Processing batch {} ...", batchCounter++);
                    try {
                        allResults.putAll(processBatch(from, to, batch));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    batch.clear();
                }
            } else {
                allResults.put(id, cache.getFromCache(id));
            }
        }

        if (!batch.isEmpty()) {
            LOGGER.debug("Processing batch {} ...", batchCounter++);
            try {
                allResults.putAll(processBatch(from, to, batch));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return allResults;
    }

    private Map<String, List<String>> processBatch(UniProtDbFrom from, UniProtDbTo to, List<String> batch) throws IOException {
        UniProtJob job = client.mapIds(from, to, batch);
        Map<String, List<String>> results;
        if (job.getStatus().equals(JobStatus.FINISHED)) {
            results = job.getResults();
            results.forEach((id, mappedIds) -> cache.addToCache(id, mappedIds));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            results = new HashMap<>();
        }
        return results;
    }

    public static void main(String[] args) {
        IdCache cache = new PersistentIdCache("/tmp/cache.txt");
        cache.addToCache("P92177", "12345", "67890");

        UniProtBatchProcessor client = new UniProtBatchProcessor(2, new UniProtClient(), cache);

        Map<String, List<String>> results = client.mapIds(
            UniProtDbFrom.UNIPROTKB_AC_ID, 
            UniProtDbTo.GENEID,
            "O77134", "P92177", "Q7KN62"
        );

        results.forEach((k, v) -> {
            System.out.println(k + " -> " + v + " (" + v.size() + ")");
        });
    }
}
package org.sing_group.uniprot_id_mapping;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UniProtIdMapper {
    public Map<String, List<String>> mapIds(UniProtDbFrom from, UniProtDbTo to, List<String> ids);

    default Map<String, List<String>> mapIds(UniProtDbFrom from, UniProtDbTo to, Set<String> ids) {
        return mapIds(from, to, ids.toArray(new String[ids.size()]));
    }

    default public Map<String, List<String>> mapIds(UniProtDbFrom from, UniProtDbTo to, String... ids) {
        return mapIds(from, to, Arrays.asList(ids));
    }
}

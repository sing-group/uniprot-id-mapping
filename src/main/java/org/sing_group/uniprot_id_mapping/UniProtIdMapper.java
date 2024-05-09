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

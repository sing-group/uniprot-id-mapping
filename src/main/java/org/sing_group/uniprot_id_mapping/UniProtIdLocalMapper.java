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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UniProtIdLocalMapper implements UniProtIdMapper {
  private static final Logger LOGGER = LoggerFactory.getLogger(UniProtIdLocalMapper.class);

  private InputStream uniprotMappingDatStream;
  private Map<UniProtDbFrom, Map<String, Map<UniProtDbTo, List<String>>>> dbMapping;

  public UniProtIdLocalMapper(File uniprotMappingDatFile) throws IOException {
    this(new FileInputStream(uniprotMappingDatFile));
  }

  public UniProtIdLocalMapper(InputStream uniprotMappingDatStream) throws IOException {
    this.uniprotMappingDatStream = uniprotMappingDatStream;
    this.createMaps();
  }

  private void createMaps() throws IOException {
      this.dbMapping = new ConcurrentHashMap<>();

      try (Stream<String> lines = readFile()) {
          ForkJoinPool customThreadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
          customThreadPool.submit(() -> lines.parallel().forEach(this::processLine)).join();
      }
  }

  private void processLine(String line) {
      String[] fields = line.split("\t");
      if (fields.length != 3) {
          LOGGER.warn("Ignoring line: " + line);
          return;
      }

      Optional<UniProtDbTo> optionalDbTo = UniProtDbTo.get(fields[1]);
      if (!optionalDbTo.isPresent()) {
          LOGGER.debug("Unknown to database: " + fields[1]);
          return;
      }

      String uniprotKbID = fields[0];
      String dbID = fields[2];

      dbMapping.computeIfAbsent(UniProtDbFrom.UNIPROTKB_AC_ID, k -> new ConcurrentHashMap<>())
              .computeIfAbsent(uniprotKbID, k -> new ConcurrentHashMap<>())
              .computeIfAbsent(optionalDbTo.get(), k -> Collections.synchronizedList(new LinkedList<>()))
              .add(dbID);

      Optional<UniProtDbFrom> optionalDbFrom = UniProtDbFrom.get(fields[1]);
      if (!optionalDbFrom.isPresent()) {
          LOGGER.debug("Unknown from database: " + fields[1]);
          return;
      }

      dbMapping.computeIfAbsent(optionalDbFrom.get(), k -> new ConcurrentHashMap<>())
              .computeIfAbsent(dbID, k -> new ConcurrentHashMap<>())
              .computeIfAbsent(UniProtDbTo.UNIPROTKB, k -> Collections.synchronizedList(new LinkedList<>()))
              .add(uniprotKbID);
  }

  private Stream<String> readFile() throws IOException {
      BufferedReader br = new BufferedReader(new InputStreamReader(uniprotMappingDatStream));
      return br.lines();
  }

  public Map<String, List<String>> mapIds(UniProtDbFrom from, UniProtDbTo to, List<String> ids) {
    Map<String, List<String>> toret = new HashMap<>();

    for (String id : ids) {
      Map<String, Map<UniProtDbTo, List<String>>> fromDbMapping = this.dbMapping.get(from);
      if (fromDbMapping != null) {
        Map<UniProtDbTo, List<String>> fromDbIdMappings = fromDbMapping.get(id);
        if (fromDbIdMappings != null) {
          List<String> result = fromDbIdMappings.get(to);
          if (result != null && !result.isEmpty()) {
            toret.put(id, result);
          }
        }
      }
    }

    return toret;
  }
  
  public static void main(String[] args) throws IOException {
    UniProtIdLocalMapper localMapper = new UniProtIdLocalMapper(
        new File("/home/hlfernandez/Descargas/DROME_7227_idmapping.dat"));

    Map<String, List<String>> results = localMapper.mapIds(UniProtDbFrom.UNIPROTKB_AC_ID, UniProtDbTo.GENEID,
        Arrays.asList(new String[] { "O77134", "P92177", "Q7KN62" }));
    results.forEach((k, v) -> {
      System.out.println(k + " -> " + v + " (" + v.size() + ")");
    });
  }
}

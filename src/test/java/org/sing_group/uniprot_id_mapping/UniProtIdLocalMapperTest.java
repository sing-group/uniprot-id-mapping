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

import static java.util.Arrays.asList;
import static org.sing_group.uniprot_id_mapping.UniProtDbFrom.ENSEMBL;
import static org.sing_group.uniprot_id_mapping.UniProtDbFrom.FLYBASE;
import static org.sing_group.uniprot_id_mapping.UniProtDbFrom.UNIPROTKB_AC_ID;
import static org.sing_group.uniprot_id_mapping.UniProtDbTo.GENEID;
import static org.sing_group.uniprot_id_mapping.UniProtDbTo.UNIPROTKB;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class UniProtIdLocalMapperTest {
  private UniProtIdLocalMapper drosophilaMapper;
  
  public UniProtIdLocalMapperTest() throws IOException {
    this.drosophilaMapper = new UniProtIdLocalMapper(new File("src/test/resources/DROME_7227_idmapping_subset.dat"));
  }

  @Test
  public void testMapFlyBaseToUniProtKb() {
    Map<String, List<String>> result = drosophilaMapper.mapIds(FLYBASE, UNIPROTKB, "FBgn0010339", "FBgn0010340");

    Assert.assertEquals(asList("P32234"), result.get("FBgn0010339"));
    Assert.assertEquals(asList("P81928", "A0A0B4KFZ0"), result.get("FBgn0010340"));
  }

  @Test
  public void testMapUniProtKbToGeneId() throws IOException {
    Map<String, List<String>> result = drosophilaMapper.mapIds(UNIPROTKB_AC_ID, GENEID, "P32234", "P81928");

    Assert.assertEquals(asList("36288"), result.get("P32234"));
    Assert.assertEquals(asList("41720"), result.get("P81928"));
  }

  @Test
  public void testMapEnsemblToUniProtKbWithoutDeversioning() throws IOException {
    UniProtIdLocalMapper musMusculusMapper = new UniProtIdLocalMapper(new File("src/test/resources/MOUSE_10090_idmapping_subset.dat"));
    Map<String, List<String>> result = musMusculusMapper.mapIds(ENSEMBL, UNIPROTKB, "ENSMUSG00000017843");

    Assert.assertTrue(result.isEmpty());
  }

  @Test
  public void testMapEnsemblToUniProtKbWithDeversioning() throws IOException {
    UniProtIdLocalMapper musMusculusMapper = new UniProtIdLocalMapper(new File("src/test/resources/MOUSE_10090_idmapping_subset.dat"), true);
    Map<String, List<String>> result = musMusculusMapper.mapIds(ENSEMBL, UNIPROTKB, "ENSMUSG00000017843", "ENSMUSG00000017843.15");

    Assert.assertFalse(result.isEmpty());
    Assert.assertEquals(asList("Q60996", "A0A1Y7VIR0", "A0A1Y7VJC8"), result.get("ENSMUSG00000017843"));
    Assert.assertEquals(asList("Q60996", "A0A1Y7VIR0", "A0A1Y7VJC8"), result.get("ENSMUSG00000017843.15"));
  }
}

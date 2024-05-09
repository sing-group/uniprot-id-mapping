package org.sing_group.uniprot_id_mapping;

import static java.util.Arrays.asList;
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
  private UniProtIdLocalMapper mapper;
  
  public UniProtIdLocalMapperTest() throws IOException {
    this.mapper = new UniProtIdLocalMapper(new File("src/test/resources/DROME_7227_idmapping_subset.dat"));
  }

  @Test
  public void testMapFlyBaseToUniProtKb() {
    Map<String, List<String>> result = mapper.mapIds(FLYBASE, UNIPROTKB, "FBgn0010339", "FBgn0010340");

    Assert.assertEquals(asList("P32234"), result.get("FBgn0010339"));
    Assert.assertEquals(asList("P81928", "A0A0B4KFZ0"), result.get("FBgn0010340"));
  }

  @Test
  public void testMapUniProtKbToGeneId() throws IOException {
    Map<String, List<String>> result = mapper.mapIds(UNIPROTKB_AC_ID, GENEID, "P32234", "P81928");

    Assert.assertEquals(asList("36288"), result.get("P32234"));
    Assert.assertEquals(asList("41720"), result.get("P81928"));
  }
}

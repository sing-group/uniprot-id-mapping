package org.sing_group.uniprot_id_mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public enum UniProtDbFrom {
  UNIPROTKB_AC_ID("UniProtKB_AC-ID"),
  UNIPROTKB_SWISS_PROT("UniProtKB-Swiss-Prot"),
  UNIPARC("UniParc"),
  UNIREF50("UniRef50"),
  UNIREF90("UniRef90"),
  UNIREF100("UniRef100"),
  GENE_NAME("Gene_Name"),
  CRC64("CRC64"),
  CCDS("CCDS"),
  EMBL_GENBANK_DDBJ("EMBL-GenBank-DDBJ"),
  EMBL_GENBANK_DDBJ_CDS("EMBL-GenBank-DDBJ_CDS"),
  GI_NUMBER("GI_number"),
  PIR("PIR"),
  REFSEQ_NUCLEOTIDE("RefSeq_Nucleotide"),
  REFSEQ_PROTEIN("RefSeq_Protein"),
  PDB("PDB"),
  BIOGRID("BioGRID"),
  COMPLEXPORTAL("ComplexPortal"),
  DIP("DIP"),
  STRING("STRING"),
  CHEMBL("ChEMBL"),
  DRUGBANK("DrugBank"),
  GUIDETOPHARMACOLOGY("GuidetoPHARMACOLOGY"),
  SWISSLIPIDS("SwissLipids"),
  ALLERGOME("Allergome"),
  CLAE("CLAE"),
  ESTHER("ESTHER"),
  MEROPS("MEROPS"),
  PEROXIBASE("PeroxiBase"),
  REBASE("REBASE"),
  TCDB("TCDB"),
  GLYCONNECT("GlyConnect"),
  BIOMUTA("BioMuta"),
  DMDM("DMDM"),
  WORLD_2DPAGE("World-2DPAGE"),
  CPTAC("CPTAC"),
  PROTEOMICSDB("ProteomicsDB"),
  DNASU("DNASU"),
  ENSEMBL("Ensembl"),
  ENSEMBL_GENOMES("Ensembl_Genomes"),
  ENSEMBL_GENOMES_PROTEIN("Ensembl_Genomes_Protein"),
  ENSEMBL_GENOMES_TRANSCRIPT("Ensembl_Genomes_Transcript"),
  ENSEMBL_PROTEIN("Ensembl_Protein"),
  ENSEMBL_TRANSCRIPT("Ensembl_Transcript"),
  GENEID("GeneID"),
  KEGG("KEGG"),
  PATRIC("PATRIC"),
  UCSC("UCSC"),
  WBPARASITE("WBParaSite"),
  WBPARASITE_TRANSCRIPT_PROTEIN("WBParaSite_Transcript-Protein"),
  ARACHNOSERVER("ArachnoServer"),
  ARAPORT("Araport"),
  CGD("CGD"),
  CONOSERVER("ConoServer"),
  DICTYBASE("dictyBase"),
  ECHOBASE("EchoBASE"),
  EUHCVDB("euHCVdb"),
  FLYBASE("FlyBase"),
  GENECARDS("GeneCards"),
  GENEREVIEWS("GeneReviews"),
  HGNC("HGNC"),
  LEGIOLIST("LegioList"),
  LEPROMA("Leproma"),
  MAIZEGDB("MaizeGDB"),
  MGI("MGI"),
  MIM("MIM"),
  NEXTPROT("neXtProt"),
  ORPHANET("Orphanet"),
  PHARMGKB("PharmGKB"),
  POMBASE("PomBase"),
  PSEUDOCAP("PseudoCAP"),
  RGD("RGD"),
  SGD("SGD"),
  TUBERCULIST("TubercuList"),
  VEUPATHDB("VEuPathDB"),
  VGNC("VGNC"),
  WORMBASE("WormBase"),
  WORMBASE_PROTEIN("WormBase_Protein"),
  WORMBASE_TRANSCRIPT("WormBase_Transcript"),
  XENBASE("Xenbase"),
  ZFIN("ZFIN"),
  EGGNOG("eggNOG"),
  GENETREE("GeneTree"),
  HOGENOM("HOGENOM"),
  OMA("OMA"),
  OPENTARGETS("OpenTargets"),
  ORTHODB("OrthoDB"),
  TREEFAM("TreeFam"),
  BIOCYC("BioCyc"),
  PLANTREACTOME("PlantReactome"),
  REACTOME("Reactome"),
  UNIPATHWAY("UniPathway"),
  CHITARS("ChiTaRS"),
  GENEWIKI("GeneWiki"),
  GENOMERNAI("GenomeRNAi"),
  PHI_BASE("PHI-base"),
  COLLECTF("CollecTF"),
  DISPROT("DisProt"),
  IDEAL("IDEAL");

  private final String databaseName;

  UniProtDbFrom(String databaseName) {
      this.databaseName = databaseName;
  }

  public String getDatabaseName() {
      return databaseName;
  }
  
  @Override
  public String toString() {
    return this.getDatabaseName();
  }

  private static final Map<String, UniProtDbFrom> MAPPINGS = new HashMap<>();

  static { 
    MAPPINGS.put("EMBL-CDS", UniProtDbFrom.EMBL_GENBANK_DDBJ_CDS);
    MAPPINGS.put("EMBL", UniProtDbFrom.EMBL_GENBANK_DDBJ);
    MAPPINGS.put("EnsemblGenome_PRO", UniProtDbFrom.ENSEMBL_GENOMES_PROTEIN);
    MAPPINGS.put("EnsemblGenome_TRS", UniProtDbFrom.ENSEMBL_GENOMES_TRANSCRIPT);
    MAPPINGS.put("EnsemblGenome", UniProtDbFrom.ENSEMBL_GENOMES);
    MAPPINGS.put("Ensembl_PRO", UniProtDbFrom.ENSEMBL_PROTEIN);
    MAPPINGS.put("Ensembl_TRS", UniProtDbFrom.ENSEMBL_TRANSCRIPT);
    MAPPINGS.put("Ensembl", UniProtDbFrom.ENSEMBL);
    MAPPINGS.put("GI", UniProtDbFrom.GI_NUMBER);
    MAPPINGS.put("RefSeq", UniProtDbFrom.REFSEQ_PROTEIN);
    MAPPINGS.put("RefSeq_NT", UniProtDbFrom.REFSEQ_NUCLEOTIDE);
    MAPPINGS.put("UniProtKB_AC-ID", UniProtDbFrom.UNIPROTKB_AC_ID);
  }

  public static Optional<UniProtDbFrom> get(String string) {
    if (MAPPINGS.containsKey(string)) {
      return Optional.of(MAPPINGS.get(string));
    }

    return Stream.of(UniProtDbFrom.values())
      .filter(
        value -> value.getDatabaseName().equalsIgnoreCase(string)
          || value.getDatabaseName().replace(" ", "_").equalsIgnoreCase(string)
          || value.getDatabaseName().replace(" ", "-").equalsIgnoreCase(string)
      ).findAny();
  }
}

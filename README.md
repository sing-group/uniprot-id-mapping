# Java API client for the UniProt ID Mapping service

This projet provides `uniprot-id-mapping`, a Java API client for the [UniProt ID Mapping service](https://www.uniprot.org/id-mapping/).

# Maven dependency

To include `uniprot-id-mapping` in your project, add our repository to the `pom.xml` first:

```xml
<repositories>
    <repository>
        <id>sing-maven-releases</id>
        <name>SING Maven Releases</name>
        <url>https://maven.sing-group.org/repository/maven-releases/</url>
    </repository>
</repositories>
```

And then add the corresponding dependency:

```xml
<dependency>
    <groupId>org.sing_group</groupId>
	<artifactId>uniprot-id-mapping</artifactId>
	<version>1.0.0</version>
</dependency>
```

# Use cases

## 1. Basic UniProt remote client

The core client is implemented by the `UniProtClient` class. It can be used as follows:

```java
UniProtClient client = new UniProtClient();

UniProtJob job = client.mapIds(
        UniProtDbFrom.UNIPROTKB_AC_ID,
        UniProtDbTo.GENEID,
        "O77134", "P92177", "Q7KN62");

System.out.println("Job ID: " + job.getJobId());
System.out.println("Job status: " + job.getStatus());
if (job.getStatus().equals(JobStatus.FINISHED)) {
    job.getResults().forEach((k, v) -> {
        System.out.println(k + " -> " + v);
    });
}
```

The `mapIds` method returns a `UniProtJob` instance that can be queried to retrieve job ID, job status and, when available, the mapping results. The source and target conversion databases are specified by enums `UniProtDbFrom` and `UniProtDbTo` respectively.

## 2. Advanced remote client

The advanced client is implemented by the `UniProtBatchProcessor` class, which uses the `UniProtClient` internally to provide batch processing and allowing the use of a cache to avoid repeating queries. It can be used as follows:


```java
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

```

The `UniProtBatchProcessor` constructor takes three arguments:
- `batchSize`: the maximum number of IDs in every single query. Larger lists will be divided in several queries.
- `client`: an `UniProtClient` instance.
- `cache`: an object that implements the `IdCache`. There are two implementations: `VolatileIdCache` and `PersistentIdCache`.

## 3. Local mapper

As UniProt provides the underlying data files uing by the web service, the `UniProtIdLocalMapper` allows using them for mapping identifiers locally. It can be used as follows:

```java
UniProtIdLocalMapper localMapper = new UniProtIdLocalMapper(
    new File("src/test/resources/DROME_7227_idmapping_subset.dat"));

Map<String, List<String>> results = localMapper.mapIds(
    UniProtDbFrom.UNIPROTKB_AC_ID,
    UniProtDbTo.GENEID,
    "P32234", "P92177");

results.forEach((k, v) -> {
    System.out.println(k + " -> " + v + " (" + v.size() + ")");
});
```

The `UniProtIdLocalMapper` constructor only takes a `.dat` file with the UniProt mapping data.

The database names in such `.dat` files are automatically mapped to `UniProtDbFrom` or `UniProtDbTo` when they have the same names than in the REST API. However, some names are different and are mapped directly, according to the following table:

| .dat file              | enum constant                     |
|------------------------|-----------------------------------|
| EMBL-CDS               | EMBL_GENBANK_DDBJ_CDS             |
| EMBL                   | EMBL_GENBANK_DDBJ                 |
| EnsemblGenome_PRO      | ENSEMBL_GENOMES_PROTEIN           |
| EnsemblGenome_TRS      | ENSEMBL_GENOMES_TRANSCRIPT        |
| EnsemblGenome          | ENSEMBL_GENOMES                   |
| Ensembl_PRO            | ENSEMBL_PROTEIN                   |
| Ensembl_TRS            | ENSEMBL_TRANSCRIPT                |
| Ensembl                | ENSEMBL                           |
| GI                     | GI_NUMBER                         |
| RefSeq                 | REFSEQ_PROTEIN                    |
| RefSeq_NT              | REFSEQ_NUCLEOTIDE                 |
| Gene_ORFName           | GENE_NAME                         |
| UniProtKB_AC-ID        | UniProtDbFrom.UNIPROTKB_AC_ID     |
| UniProtKB-ID           | UniProtDbTo.UNIPROTKB             |

Finally, other DB names that appear in the file but do not have a correspondence to a known REST API database are ommitted. These are: EMDB, Gene_Synonym, MINT, and NCBI_TaxID.

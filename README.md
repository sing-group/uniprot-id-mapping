# Java API client for the UniProt ID Mapping service

This projet provides `uniprot-id-mapping`, a Java API client for the [UniProt ID Mapping service](https://www.uniprot.org/id-mapping/).

## 1. Basic client

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

## 2. Advanced client

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

package org.sing_group.uniprot_id_mapping;

import java.util.List;
import java.util.Map;

public class UniProtJob {

    private String jobId;
    private JobStatus status;
    private Map<String, List<String>> results;

    public UniProtJob(String jobId, JobStatus status, Map<String, List<String>> results) {
        this.jobId = jobId;
        this.status = status;
        this.results = results;
    }

    public String getJobId() {
        return jobId;
    }

    public JobStatus getStatus() {
        return status;
    }

    public Map<String, List<String>> getResults() {
        return results;
    }
}
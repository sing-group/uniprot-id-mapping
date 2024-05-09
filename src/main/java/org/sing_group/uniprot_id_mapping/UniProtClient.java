package org.sing_group.uniprot_id_mapping;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UniProtClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(UniProtClient.class);
    private static final String API_URL = "https://rest.uniprot.org";

    private int pollingWaitTime;

    public UniProtClient() {
        this(5000);
    }

    public UniProtClient(int pollingWaitTime) {
        this.pollingWaitTime = pollingWaitTime;
    }

    public UniProtJob mapIds(UniProtDbFrom from, UniProtDbTo to, String...ids) throws IOException {
        return mapIds(from, to, asList(ids));
    }

    public UniProtJob mapIds(UniProtDbFrom from, UniProtDbTo to, List<String> ids) throws IOException {
        String jobId = submitIdMappingRequest(from, to, ids);
        LOGGER.debug("Submitted job ID: {}", jobId);
        JobStatus status = checkJobStatus(jobId);
        Map<String, List<String>> results = new HashMap<>();
        if (status.equals(JobStatus.FINISHED)) {
            results = getResults(jobId);
        }

        return new UniProtJob(jobId, status, results);
    }

    private String submitIdMappingRequest(UniProtDbFrom from, UniProtDbTo to, List<String> ids) throws IOException {
        Set<String> setIds = new HashSet<>(ids);

        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(API_URL + "/idmapping/run");
        String body = String.format("from=%s&to=%s&ids=%s",
                                    URLEncoder.encode(from.toString(), "UTF-8"),
                                    URLEncoder.encode(to.toString(), "UTF-8"),
                                    URLEncoder.encode(String.join(",", setIds), "UTF-8"));
        post.setEntity(new StringEntity(body));
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");

        HttpResponse response = client.execute(post);
        String responseString = EntityUtils.toString(response.getEntity());
        if (response.getStatusLine().getStatusCode() == 200) {
            try {
                JSONObject jsonResponse = new JSONObject(responseString);
                return jsonResponse.getString("jobId");
            } catch (JSONException e) {
                throw new RuntimeException("Failed to parse JSON response: " + responseString, e);
            }
        } else {
            throw new RuntimeException("Failed to submit ID mapping: " + responseString);
        }
    }

    public JobStatus checkJobStatus(String jobId) throws IOException {
        HttpClient client = HttpClientBuilder.create()
            // Disable automatic redirection to the results page when finished to be able to check its status
            .disableRedirectHandling()
            .build();
        HttpGet get = new HttpGet(API_URL + "/idmapping/status/" + jobId);

        while (true) {
            HttpResponse response = client.execute(get);
            String responseString = EntityUtils.toString(response.getEntity());
            if (response.getStatusLine().getStatusCode() == 303 || response.getStatusLine().getStatusCode() == 200) {
                String status = new JSONObject(responseString).getString("jobStatus");
                if ("FINISHED".equals(status)) {
                    LOGGER.debug("Job finished!");
                    return JobStatus.FINISHED;
                } else if ("RUNNING".equals(status)) {
                    LOGGER.debug("Job is still running; retrying in {} milliseconds...", this.pollingWaitTime);
                    try {
                        Thread.sleep(this.pollingWaitTime);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    return JobStatus.valueOf(status);
                }
            } else {
                throw new RuntimeException("Failed to check job status: " + responseString);
            }
        }
    }

    public static Map<String, List<String>> buildIdMapping(String jsonString) {
      Map<String, List<String>> fromToMap = new HashMap<>();
      JSONObject jsonObject = new JSONObject(jsonString);
      JSONArray results = jsonObject.getJSONArray("results");
      
      for (int i = 0; i < results.length(); i++) {
          JSONObject current = results.getJSONObject(i);
          
          String from = current.getString("from");
          String to = current.getString("to");
          
          if (!fromToMap.containsKey(from)) {
              fromToMap.put(from, new ArrayList<>());
          }
          
          fromToMap.get(from).add(to);
      }
      
      LOGGER.debug("Results processed, mapped {} identifiers", fromToMap.size());

      return fromToMap;
  }
    
    public Map<String, List<String>> getResults(String jobId) throws IOException {
        HttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(API_URL + "/idmapping/stream/" + jobId);

        HttpResponse response = client.execute(get);
        String responseString = EntityUtils.toString(response.getEntity());
        if (response.getStatusLine().getStatusCode() == 200) {
            return buildIdMapping(responseString);
        } else {
            throw new RuntimeException("Failed to retrieve results: " + responseString);
        }
    }

    public static void main(String[] args) throws IOException {
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
    }
}

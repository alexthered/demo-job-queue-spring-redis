package me.hiepdoan.jrqueue.service;

import com.fasterxml.uuid.Generators;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import me.hiepdoan.jrqueue.model.Job;
import me.hiepdoan.jrqueue.model.JobMetaData;
import me.hiepdoan.jrqueue.model.JobQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JobQueueService {

  @Autowired
  private RedisTemplate<String, Object> redisTemplate;
  private Map<String, JobQueue> jobQueueMap;

  private static final String JOB_KEY_TEMPLATE = "jobs#%s";
  private static final Long JOB_KEY_EXPIRATION_PERIOD = 6L;

  @PostConstruct
  public void init() {
    jobQueueMap = new HashMap<>();
  }

  public boolean hasJobQueue(String queueName) {
    return jobQueueMap.containsKey(queueName);
  }

  public void registerJobQueue(String queueName) {
    if (jobQueueMap.containsKey(queueName)) {
      throw new IllegalArgumentException("Queue is already registered");
    }

    JobQueue queue = new JobQueue(queueName);

    jobQueueMap.put(queueName, queue);

    log.info(String.format("Registered queue: %s", queueName));
  }

  public Job insertToWaitingQueue(String queueName, Job job) {
    if (!jobQueueMap.containsKey(queueName)) {
      log.warn(String.format("Queue: %s does not exist", queueName));
      return null;
    }

    JobQueue jobQueue = jobQueueMap.get(queueName);

    // get unique Id for this job
    UUID uuid = Generators.randomBasedGenerator().generate();
    job.setId(uuid);

    // construct timestamp data for this job and store it on Redis
    JobMetaData metaData = new JobMetaData(LocalDateTime.now());
    setMetaDataForJob(job, metaData);

    redisTemplate.opsForList().leftPush(jobQueue.getWaitingQueueName(), job);

    return job;
  }

  public Job getJobToProcess(String queueName) {
    JobQueue jobQueue = jobQueueMap.get(queueName);

    Job job =  (Job) redisTemplate.opsForList().rightPopAndLeftPush(jobQueue.getWaitingQueueName(),
        jobQueue.getProcessingQueueName());

    if (job != null) {
      JobMetaData metaData = getMetaDataForJob(job);
      if (metaData == null) {
        log.warn(String.format("No meta data found for job: %s, queueName: %s", job.getId().toString(), queueName));
      } else {
        metaData.setExecutedAt(LocalDateTime.now());
        metaData.setExecutionCount(metaData.getExecutionCount() + 1);
        setMetaDataForJob(job, metaData);
      }
    }

    return job;
  }

  public void clearProcessedJob(String queueName, Job job) {
    JobQueue jobQueue = jobQueueMap.get(queueName);

    JobMetaData metaData = getMetaDataForJob(job);
    if (metaData == null) {
      log.warn(String.format("No meta data found for job: %s, queueName: %s", job.getId().toString(), queueName));
    } else {
      metaData.setFinishedAt(LocalDateTime.now());
      setMetaDataForJob(job, metaData);
    }

    redisTemplate.opsForList().remove(jobQueue.getProcessingQueueName(), 1, job);
  }

  /**
   * cron job to periodically check for jobs that has been
   * in the processing queue for more than max time out
   * for now, it just move items to the dead letter queue,
   * but in the future we should re-queue it until maxRetries number first
   * TODO: implement re-queue function
   */

  @Scheduled(fixedRate = 1800000)  //run every 30 mins
  public void checkExpiredJobs() {

    //loop over each registered queues
    for (JobQueue jobQueue : jobQueueMap.values()) {
      Integer maxTimeOut = jobQueue.getMaxTimeOut();

      List<Object> objectList = redisTemplate.opsForList().range(jobQueue.getProcessingQueueName(), 0, -1);
      if (objectList != null) {
        for (Object object : objectList) {
          Job job = (Job) object;

          // get metadata for this job
          JobMetaData metaData = getMetaDataForJob(job);
          if (metaData != null
              && Duration.between(metaData.getExecutedAt(), LocalDateTime.now()).toMinutes() > maxTimeOut) {
            log.warn(String.format("Found expired job to move to deal letter queue for queue name: %s",
                jobQueue.getQueueName()));
            // move the job to dead letter queue
            redisTemplate.opsForList().leftPush(jobQueue.getDeadLetterQueueName(), job);
            // remove it from processing queue
            redisTemplate.opsForList().remove(jobQueue.getProcessingQueueName(), 1, job);
          }
        }
      }

    }
  }

  protected String getJobKey(UUID jobId) {
    return String.format(JOB_KEY_TEMPLATE, jobId.toString());
  }

  private void setMetaDataForJob(Job job, JobMetaData metaData) {
    redisTemplate.opsForValue().set(getJobKey(job.getId()), metaData,
        JOB_KEY_EXPIRATION_PERIOD, TimeUnit.HOURS);
  }

  private JobMetaData getMetaDataForJob(Job job) {
    return (JobMetaData) redisTemplate.opsForValue().get(getJobKey(job.getId()));
  }

}

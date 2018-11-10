package me.hiepdoan.jrqueue;

import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import me.hiepdoan.jrqueue.model.LoggingJob;
import me.hiepdoan.jrqueue.service.JobQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class DemoJobQueueSpringRedisApplication implements CommandLineRunner {

  @Autowired
  private JobQueueService jobQueueService;

  public static void main(String[] args) {
    SpringApplication.run(DemoJobQueueSpringRedisApplication.class, args).close();
  }

  @Override
  public void run(String... args) throws Exception {

    // register the queue
    final String queueName = "logging_jobs";
    jobQueueService.registerJobQueue(queueName);

    // add job to the queue
    Random random = new Random();
    for (int i = 0; i < 10; i++) {
      LoggingJob job = new LoggingJob(random.nextInt(10));
      jobQueueService.insertToWaitingQueue(queueName, job);
    }

    // processing job queue - mimic the behaviour of worker machines to take job
    // form the queue and execute
    while (true) {
      LoggingJob jobToExecute = (LoggingJob) jobQueueService.getJobToProcess(queueName);

      if (jobToExecute == null) {
        break;
      }

      for (int i = 0; i < jobToExecute.getNumLog(); i++) {
        log.info(String.format("I am just printing the log for job: %s", jobToExecute.getId().toString()));
      }
    }


  }
}

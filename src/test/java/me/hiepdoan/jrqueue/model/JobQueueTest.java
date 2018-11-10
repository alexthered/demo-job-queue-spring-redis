package me.hiepdoan.jrqueue.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
public class JobQueueTest {

  @Test
  public void whenCreateQueueWithName_thenGetRedisListNameCorrectly() {
    JobQueue jobQueue = new JobQueue("test");

    assertEquals("test:waiting_queue", jobQueue.getWaitingQueueName());
    assertEquals("test:processing_queue", jobQueue.getProcessingQueueName());
    assertEquals("test:dead_letter_queue", jobQueue.getDeadLetterQueueName());
  }

  @Test
  public void whenCreateQueueWithName_thenMaxRetriesAndTimeoutHasDefaultValue() {
    JobQueue jobQueue = new JobQueue("test");

    assertEquals(1, jobQueue.getMaxRetries().intValue());
    assertEquals(30, jobQueue.getMaxTimeOut().intValue());
  }

  @Test
  public void whenCreateQueueWithMaxRetriesAndTimeOut_thenReturnCorrectly() {
    Integer maxRetries = 2;
    Integer maxTimeout = 30;
    JobQueue jobQueue = new JobQueue("test", maxRetries, maxTimeout);

    assertEquals(maxRetries, jobQueue.getMaxRetries());
    assertEquals(maxTimeout, jobQueue.getMaxTimeOut());
  }

}
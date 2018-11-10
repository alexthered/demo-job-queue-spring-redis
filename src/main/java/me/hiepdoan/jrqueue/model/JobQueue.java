package me.hiepdoan.jrqueue.model;

import lombok.Getter;

public class JobQueue {

  private static final String WAITING_QUEUE_NAME_TEMPLATE = "%s:waiting_queue";
  private static final String PROCESSING_QUEUE_NAME_TEMPLATE = "%s:processing_queue";
  private static final String DEAD_LETTER_QUEUE_NAME_TEMPLATE = "%s:dead_letter_queue";

  @Getter
  private String queueName;
  @Getter
  private Integer maxRetries = 1;
  @Getter
  private Integer maxTimeOut = 30; //in minutes

  public JobQueue(String queueName) {
    if (queueName == null) {
      throw new IllegalArgumentException("Queue name must be set");
    }
    this.queueName = queueName;
  }

  public JobQueue(String queueName, Integer maxRetries,
                  Integer maxTimeOut) {
    this(queueName);
    this.maxRetries = maxRetries;
    this.maxTimeOut = maxTimeOut;
  }


  public Integer getMaxRetries() {
    return this.maxRetries;
  }

  public String getWaitingQueueName() {
    return String.format(WAITING_QUEUE_NAME_TEMPLATE, this.queueName);
  }

  public String getProcessingQueueName() {
    return String.format(PROCESSING_QUEUE_NAME_TEMPLATE, this.queueName);
  }

  public String getDeadLetterQueueName() {
    return String.format(DEAD_LETTER_QUEUE_NAME_TEMPLATE, this.queueName);
  }

}

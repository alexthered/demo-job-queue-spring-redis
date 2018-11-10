package me.hiepdoan.jrqueue.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * dummy job to execute just by printing
 * a number of log
 */
@Data
@NoArgsConstructor
public class LoggingJob extends Job {

  private Integer numLog = 0;

  public LoggingJob(Integer numLog) {
    this.numLog = numLog;
  }

}

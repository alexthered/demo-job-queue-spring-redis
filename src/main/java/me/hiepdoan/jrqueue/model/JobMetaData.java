package me.hiepdoan.jrqueue.model;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // for Jackson
public class JobMetaData {
  private LocalDateTime insertedAt;
  private LocalDateTime executedAt;
  private LocalDateTime finishedAt;

  private Integer executionCount = 0;

  public JobMetaData(LocalDateTime insertedAt) {
    this.insertedAt = insertedAt;
  }
}

package me.hiepdoan.jrqueue.model;

import java.io.Serializable;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class Job implements Serializable {

  private UUID id;
}
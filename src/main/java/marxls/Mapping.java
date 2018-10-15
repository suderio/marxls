package marxls;

import java.util.List;

import lombok.Data;

@Data
public class Mapping {
  private String name;
  private String sheet;
  private String className;
  private List<Member> members;
}

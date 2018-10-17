package marxls;

import java.util.List;

import lombok.Data;

@Data
public class Mapping {
  private String name;
  private String sheet;
  private String className;
  private List<Member> members;

  public Member getMember(String titleOrColumn) {
    return members.stream()
        .filter(member -> member.isTitleBased() ? titleOrColumn.equals(member.getTitle())
            : titleOrColumn.equals(member.getColumn()))
        .findFirst().orElse(null);
  }
}

package marxls;

import java.util.List;

import lombok.Data;

@Data
public class PlaceTags {
  private List<String> tags;
  private List<Place> places;
}

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString(includeFieldNames = true)
@Getter
@Setter
@NoArgsConstructor
public class User {
    private int age;
    private String firstName;
    private String lastName;
    private String password;
    private String email;
}

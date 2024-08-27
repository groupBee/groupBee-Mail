package groupbee.email.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {

    private String username;
    private String password;
    private List<String> to;
    private String subject;
    private String body;
    private List<String> cc;}


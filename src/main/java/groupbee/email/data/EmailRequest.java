package groupbee.email.data;

import lombok.*;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {

    private List<String> to;
    private String subject;
    private String body;
    private List<String> cc;
}


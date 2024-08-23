package groupbee.email.controller;

import groupbee.email.data.EmailRequest;
import groupbee.email.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public Map<String, String> sendEmail(@RequestBody EmailRequest request) {
        emailService.sendEmail(
                request.getUsername(),
                request.getPassword(),
                request.getTo(),
                request.getSubject(),
                request.getBody()
        );
        Map<String, String> response = new HashMap<>();
        response.put("message", "Email sent successfully!");
        return response;
    }

    @PostMapping("/check")
    public ResponseEntity<List<Map<String, String>>> checkEmail(@RequestBody EmailRequest request) {
        System.out.println(request.getUsername()+request.getPassword());
        try {
            List<Map<String, String>> emails = emailService.checkEmail(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(Collections.singletonMap("error", e.getMessage())));
        }
    }
}

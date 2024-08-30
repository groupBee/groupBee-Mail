package groupbee.email.controller;

import groupbee.email.data.EmailRequest;
import groupbee.email.service.EmailService;
import jakarta.mail.SendFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public Map<String, String> sendEmail(@RequestBody EmailRequest request) {
        try {
            emailService.sendEmail(
                    request.getTo(),
                    request.getCc(),
                    request.getSubject(),
                    request.getBody()
            );
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email sent successfully!");
            return response;
        } catch (SendFailedException e) {
            // SendFailedException 예외를 특정하게 처리
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email address: " + e.getMessage());
        } catch (RuntimeException e) {
            // 나머지 모든 런타임 예외를 처리
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            // 나머지 모든 예외를 처리
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while sending email.");
        }
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getReason());
        return new ResponseEntity<>(response, ex.getStatusCode());
    }


    @GetMapping("/check")
    public ResponseEntity<List<Map<String, String>>> checkEmail() {
        try {
            List<Map<String, String>> emails = emailService.checkEmail();
            return ResponseEntity.ok(emails);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(Collections.singletonMap("error", e.getMessage())));
        }
    }
}

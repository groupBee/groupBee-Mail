package groupbee.email.service;

import groupbee.email.service.feign.EmployeeFeignClient;
import jakarta.mail.*;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmployeeFeignClient employeeFeignClient;

    @Value("${mail.host}")
    private String mailHost;

    @Value("${mail.port}")
    private int mailPort;

    @Value("${mail.protocol}")
    private String mailProtocol;

    @Value("${mail.smtp.starttls.enable}")
    private boolean starttlsEnable;

    @Value("${mail.imap.host}")
    private String imapHost;

    @Value("${mail.imap.port}")
    private int imapPort;

    @Value("${mail.imap.protocol}")
    private String imapProtocol;

    @Value("${mail.imap.starttls.enable}")
    private boolean imapStarttlsEnable;

    public void sendEmail(List<String> to, List<String> cc, String subject, String body) throws Exception {
        Map<String, Object> userInfo = employeeFeignClient.getUserInfo();
        String username = userInfo.get("email").toString();
        String password = userInfo.get("password").toString();

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", mailProtocol);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // TLS 사용
        props.put("mail.smtp.ssl.trust", "*"); // SSL 검증 비활성화
        props.put("mail.smtp.ssl.checkserveridentity", "false"); // SSL 검증 비활성화
//        props.put("mail.debug", "true");

        MimeMessage message = mailSender.createMimeMessage();
        message.setFrom(username);

        InternetAddress[] recipientAddresses = to.stream()
                .map(email -> {
                    try {
                        return new InternetAddress(email);
                    } catch (AddressException e) {
                        throw new RuntimeException("Invalid email address: " + email);
                    }
                })
                .toArray(InternetAddress[]::new);

        message.setRecipients(Message.RecipientType.TO, recipientAddresses);

        if (cc != null && !cc.isEmpty()) {
            InternetAddress[] ccAddresses = cc.stream()
                    .map(email -> {
                        try {
                            return new InternetAddress(email);
                        } catch (AddressException e) {
                            throw new RuntimeException("Invalid email address: " + email);
                        }
                    })
                    .toArray(InternetAddress[]::new);
            message.addRecipients(Message.RecipientType.CC, ccAddresses);
        }

        message.setSubject(subject, "UTF-8");  // 제목 인코딩
        message.setText(body, "UTF-8");  // 본문 인코딩

        message.setSentDate(new Date());


        try {
            mailSender.send(message);
            System.out.println("Email sent successfully!");
            saveSentEmailToIMAP(username, password, message);
        } catch (MailException e) {
            System.out.println("Error while sending email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        } catch (Exception ex) {
            System.out.println("IMAP save failed: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Failed to save email to IMAP: " + ex.getMessage(), ex);
        }

    }

//여러 내용들
    private void saveSentEmailToIMAP(String username, String password, MimeMessage message) throws Exception {

        Properties props = new Properties();
        props.put("mail.store.protocol", imapProtocol);
        props.put("mail.imap.host", imapHost);
        props.put("mail.imap.port", String.valueOf(imapPort));
        props.put("mail.imap.starttls.enable", String.valueOf(imapStarttlsEnable));
        props.put("mail.imap.ssl.enable", "false");
        props.put("mail.debug", "true");
        props.put("mail.imap.ssl.trust", "*");
        props.put("mail.imap.ssl.checkserveridentity", "false");

        Session session = Session.getInstance(props);
        Store store = session.getStore(imapProtocol);
        store.connect(imapHost, username, password);

        Folder sentFolder = store.getFolder("Sent"); // Adjust if different folder name
        if (!sentFolder.exists()) {
            sentFolder.create(Folder.HOLDS_MESSAGES);
        }

        sentFolder.open(Folder.READ_WRITE);
///여기에 파싱한거 하기

        System.out.println("1111111111111"+message.getSubject());

        MimeMessage copiedMessage = new MimeMessage(session);

// 기존 메일의 각 필드를 복사
        copiedMessage.setSubject(message.getSubject());
        copiedMessage.setText(message.getContent().toString());
        copiedMessage.setSentDate(message.getSentDate());
        copiedMessage.setRecipients(Message.RecipientType.TO, message.getRecipients(Message.RecipientType.TO));
        copiedMessage.setRecipients(Message.RecipientType.CC, message.getRecipients(Message.RecipientType.CC));
        Address[] fromAddresses = message.getFrom();
        copiedMessage.setFrom(fromAddresses[0]);


        sentFolder.appendMessages(new MimeMessage[]{copiedMessage});
        sentFolder.close(false);

        store.close();
    }


    private boolean isValidEmailAddress(String email) {
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
            return true;
        } catch (AddressException ex) {
            return false;
        }
    }

    private String getEmailAddressOnly(Address[] addresses) {
        if (addresses == null || addresses.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (Address address : addresses) {
            if (address instanceof InternetAddress) {
                InternetAddress internetAddress = (InternetAddress) address;
                result.append(internetAddress.getAddress()).append(", ");
            }
        }
        if (result.length() > 0) {
            result.setLength(result.length() - 2); // 마지막 콤마와 공백 제거
        }
        return result.toString();
    }

    public List<Map<String, String>> checkEmail() throws Exception {
        List<Map<String, String>> emailList = new ArrayList<>();
        Map<String, Object> userInfo = employeeFeignClient.getUserInfo();
        String username = userInfo.get("email").toString();
        String password = userInfo.get("password").toString();

        Properties props = new Properties();
        props.put("mail.store.protocol", imapProtocol);
        props.put("mail.imap.host", imapHost);
        props.put("mail.imap.port", String.valueOf(imapPort));
        props.put("mail.imap.starttls.enable", String.valueOf(imapStarttlsEnable));
        props.put("mail.imap.ssl.enable", "false");
        props.put("mail.smtp.sendpartial", "false");
//        props.put("mail.debug", "true");
        props.put("mail.imap.ssl.trust", "*");
        props.put("mail.imap.ssl.checkserveridentity", "false");


        Session session = Session.getInstance(props);
        Store store = session.getStore(imapProtocol);
        store.connect(imapHost, username, password);

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        Message[] messages = inbox.getMessages();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.M.d HH:mm:ss");

        for (Message message : messages) {
            Map<String, String> emailData = new HashMap<>();
            emailData.put("subject", message.getSubject());
            emailData.put("from", getEmailAddressOnly(message.getFrom()));
            emailData.put("receivedDate", dateFormat.format(message.getReceivedDate()));

            String content = "";
            if (message.isMimeType("text/plain")) {
                content = message.getContent().toString();
            } else if (message.isMimeType("multipart/*")) {
                Multipart multipart = (Multipart) message.getContent();
                content = getTextFromMultipart(multipart);
            } else if (message.isMimeType("text/html")) {
                content = message.getContent().toString();
            }
            emailData.put("content", content);
            emailList.add(emailData);
        }

        inbox.close(false);
        store.close();

        return emailList;
    }

    public List<Map<String, String>> sentEmail() throws Exception {

        List<Map<String, String>> emailList = new ArrayList<>();
        Map<String, Object> userInfo = employeeFeignClient.getUserInfo();
        String username = userInfo.get("email").toString();
        String password = userInfo.get("password").toString();
        Properties props = new Properties();


        props.put("mail.store.protocol", imapProtocol);
        props.put("mail.imap.host", imapHost);
        props.put("mail.imap.port", String.valueOf(imapPort));
        props.put("mail.imap.starttls.enable", String.valueOf(imapStarttlsEnable));
        props.put("mail.imap.ssl.enable", "false");
        props.put("mail.smtp.sendpartial", "false");
//        props.put("mail.debug", "true");
        props.put("mail.imap.ssl.trust", "*");
        props.put("mail.imap.ssl.checkserveridentity", "false");


        Session session = Session.getInstance(props);
        Store store = session.getStore(imapProtocol);
        store.connect(imapHost, username, password);

        Folder sentFolder = store.getFolder("Sent");

        sentFolder.open(Folder.READ_WRITE);
        Message[] messages = sentFolder.getMessages();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        for (Message message : messages) {
            Map<String, String> emailData = new HashMap<>();

            // 제목 null 체크
            String subject = message.getSubject();
            emailData.put("subject", subject);

            // 받는 사람 null 체크
            String to = getEmailAddressOnly(message.getRecipients(Message.RecipientType.TO));
            emailData.put("to", to);

            // 날짜 null 체크
            Date sentDate = message.getSentDate();
            emailData.put("sentDate", String.valueOf(sentDate));

            emailData.put("cc", getEmailAddressOnly(message.getRecipients(Message.RecipientType.CC)));
            // 본문 null 체크
            String content = "";
            if (message.isMimeType("text/plain")) {
                content = message.getContent() != null ? message.getContent().toString() : "No Content";
            } else if (message.isMimeType("multipart/*")) {
                Multipart multipart = (Multipart) message.getContent();
                content = getTextFromMultipart(multipart);
            } else if (message.isMimeType("text/html")) {
                content = message.getContent() != null ? message.getContent().toString() : "No Content";
            }
            emailData.put("content", content);

            emailList.add(emailData);
        }

        sentFolder.close(false);
        store.close();

        return emailList;
    }


    private String getTextFromMultipart(Multipart multipart) throws Exception {
        StringBuilder result = new StringBuilder();
        int count = multipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent());
            } else if (bodyPart.isMimeType("text/html")) {
                result.append(bodyPart.getContent());
            } else if (bodyPart.getContent() instanceof Multipart) {
                result.append(getTextFromMultipart((Multipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }
}
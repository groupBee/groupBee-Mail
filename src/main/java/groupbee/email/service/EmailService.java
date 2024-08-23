package groupbee.email.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class EmailService {

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

    public void sendEmail(String username, String password, String to, String subject, String body) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);

        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", mailProtocol);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(starttlsEnable));  // STARTTLS 활성화
        props.put("mail.debug", "true");

        // SSL 검증 비활성화
        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.ssl.checkserveridentity", "false");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(username);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    public List<Map<String, String>> checkEmail(String username, String password) throws Exception {
        List<Map<String, String>> emailList = new ArrayList<>();

        Properties props = new Properties();
        props.put("mail.store.protocol", imapProtocol);
        props.put("mail.imap.host", imapHost);
        props.put("mail.imap.port", String.valueOf(imapPort));
        props.put("mail.imap.starttls.enable", String.valueOf(imapStarttlsEnable));  // STARTTLS 활성화
        props.put("mail.imap.ssl.enable", "false");
        props.put("mail.debug", "true");

        // SSL 검증 비활성화
        props.put("mail.imap.ssl.trust", "*");
        props.put("mail.imap.ssl.checkserveridentity", "false");

        Session session = Session.getInstance(props);
        Store store = session.getStore(imapProtocol);
        System.out.println(username);
        store.connect(imapHost, username, password);

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        Message[] messages = inbox.getMessages();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.M.d HH:mm:ss");

        for (Message message : messages) {
            Map<String, String> emailData = new HashMap<>();
            emailData.put("subject", message.getSubject());
            emailData.put("from", getEmailAddressOnly(message.getFrom()));
            emailData.put("receivedDate", dateFormat.format(message.getReceivedDate()));  // 날짜 형식 변경

            // 이메일 본문 내용 가져오기
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

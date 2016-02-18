package test;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class TestJavaMail {
    private String fromEmail = "tommy.yeh1112@gmail.com.tw";

    private String[] toEmails = new String[] {
	    "passeord42@gmail.com", 
	    "chobits0202@gmail.com"
    };

    // 自己的 email 帳號
    private final String username = "tommy.yeh1112";

    // email 密碼
    private final String password = "7kcr4iv32ilidloi";

    private void start() {
	String subject = "從 Java Mail 來的啦";
	String mailContent = "幹~~可以附檔耶~~酷";
	String attachFilePath = "D:/Dropbox/Tommy & Vincent/2013-05-13 13.02.53.jpg";

	sendMailWithAttachFile(subject, mailContent, attachFilePath);

	System.out.println("Send mail done");
    }

    private void sendMailWithAttachFile(String subject, String mailContentMsg, String attachFilePath) {
	Properties props = new Properties();
	props.put("mail.smtp.host", "smtp.gmail.com");
	props.put("mail.smtp.socketFactory.port", "465");
	props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	props.put("mail.smtp.auth", "true");
	props.put("mail.smtp.port", "465");

	Session session = Session.getInstance(props, new Authenticator() {
	    protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(username, password);
	    }
	});

	try {
	    // create a message
	    Message mail = new MimeMessage(session);
	    mail.setFrom(new InternetAddress(fromEmail));

	    InternetAddress[] toAddress = new InternetAddress[toEmails.length];
	    for (int index = 0; index < toEmails.length; index++) {
		toAddress[index] = new InternetAddress(toEmails[index]);
	    }

	    mail.setRecipients(Message.RecipientType.TO, toAddress);

	    mail.setSubject(subject);

	    // create and fill the first message
	    MimeBodyPart bodyMailContent = new MimeBodyPart();
	    bodyMailContent.setText(mailContentMsg);

	    // create the second message part
	    MimeBodyPart bodyAttachFile = new MimeBodyPart();
	    FileDataSource fds = new FileDataSource(new File(attachFilePath));
	    bodyAttachFile.setDataHandler(new DataHandler(fds));
	    bodyAttachFile.setFileName(fds.getName());

	    // create the Multipart and add its parts to it
	    Multipart multipart = new MimeMultipart();
	    multipart.addBodyPart(bodyMailContent);
	    multipart.addBodyPart(bodyAttachFile);

	    // add the Multipart to the message
	    mail.setContent(multipart);

	    // set the Date: header
	    mail.setSentDate(new Date());

	    Transport.send(mail);
	} catch (MessagingException e) {
	    e.printStackTrace();
	}
    }

    public static void main(String[] args) {
	TestJavaMail test = new TestJavaMail();
	test.start();
    }
}
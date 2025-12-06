package com.lexivo.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.util.Properties;

public abstract class Email {
	private final static String SENDER_EMAIL = System.getenv("SENDER_EMAIL");
	private final static String EMAIL_PASSWORD = System.getenv("EMAIL_PASSWORD");
	public static boolean sendTo(String recipientEmail, String subject, String htmlBody) {
//		TODO: Edit when sender email changed
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
		Session session = Session.getInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(SENDER_EMAIL, EMAIL_PASSWORD);
			}
		});

		try {
			Multipart mp = new MimeMultipart();
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(htmlBody, "text/html");
			mp.addBodyPart(htmlPart);

			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(SENDER_EMAIL, "Lexivo noreply"));
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
			msg.setSubject(subject);
			msg.setContent(mp);

			Transport.send(msg);
			return true;
		} catch (Exception e) {
//			TODO: Proper logging
			System.err.println(e.getMessage());
			return false;
		}
	}

	public static void sendConfirmationCode(String recipientEmail, String confirmationCode) {
		String htmlBody = """
		<div style="padding: 50px;">
			<p style="text-align: center; font-size: 20px; font-weight: bold;">Confirmation code</p>
			<div style="padding: 10px; background-color: #f9f9f9;">
				<p style="text-align: center; font-size: 20px; font-weight: bold; letter-spacing: 3px;">%</p>
			</div>
		</div>
		""";

		htmlBody = htmlBody.replace("%", confirmationCode);

		sendTo(recipientEmail, "Email confirmation", htmlBody);
	}
}

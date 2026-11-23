/**
 * Email management system
 * 
 */

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;

class Email {
	
	static String server     = "";
	static String port       = "587";
	static String senderName = "Administrator";
	static String sender     = "";
	static String password   = "";
	static String protocol   = "TLSv1.2";
	static String auth       = "true";
	static String tls        = "true";

	static void
	sendEmail(
		String recipient, 
		String subject,
		String text, 
		String html) {

		Settings.refresh();
		
		// For small project, send email directly
		/*
		EmailSender e = new EmailSender(recipient, subject, text, html);
		e.start();
		*/
		
		// For large project, add to database, and run the program to send it
		Storage.queueEmail(recipient, subject, text, html);
	}
	
	static String
	readEmail(String fileName, String extension, String language) {
		String result = null;
		
		String path = fileName + "-" + language + extension;
		File file = new File(path);
		if (file.exists() == true) {
			try {
				FileReader reader = new FileReader(file);
				result = reader.readAllAsString();
				reader.close();
			} catch (Exception e) { }
			
		}
		
		// Fallback: Use the default language
		if (result == null) {
			path = fileName + "-" + Translation.defaultLanguage + extension;
			
			try {
				FileReader reader = new FileReader(path);
				result = reader.readAllAsString();
				reader.close();
			} catch (Exception e) { }
			
		}
		
		if (result == null) result = "";
		return result;
	}
	
	// TODO: Add sendChangePasswordConfirm()
	
	static void 
	sendActivationCode(String email, String activationCode, String language) {
		if (language == null) {
			language = Translation.defaultLanguage;
		}
		String text = readEmail("email/activation-code", ".txt",  language);
		String html = readEmail("email/activation-code", ".html", language);
		
		text = text.replaceAll("--ACTIVATION-CODE--", activationCode);
		html = html.replaceAll("--ACTIVATION-CODE--", activationCode);
		
		var translate = Translation.getTranslation(language);
		String subject = translate.get("activation-code");
		
		Email.sendEmail(
				email,
				subject,
				text,
				html);
	}
	
	static void
	sendResetCode(String email, String resetCode, String language) {
		if (language == null) {
			language = Translation.defaultLanguage;
		}
		String text = readEmail("email/reset-code", ".txt",  language);
		String html = readEmail("email/reset-code", ".html", language);
		
		text = text.replaceAll("--RESET-CODE--", resetCode);
		html = html.replaceAll("--RESET-CODE--", resetCode);
		
		var translate = Translation.getTranslation(language);
		String subject = translate.get("reset-code");
		
		Email.sendEmail(
				email,
				subject,
				text,
				html);
	}
	
	static void
	sendResetSuccess(String email, String language) {
		if (language == null) {
			language = Translation.defaultLanguage;
		}
		String text = readEmail("email/reset-success", ".txt",  language);
		String html = readEmail("email/reset-success", ".html", language);
		
		var translate = Translation.getTranslation(language);
		String subject = translate.get("reset-success");
		
		Email.sendEmail(
				email,
				subject,
				text,
				html);
	}
	
	static void
	sendRemoveAccount(String email, String language) {
		if (language == null) {
			language = Translation.defaultLanguage;
		}
		String text = readEmail("email/remove-account", ".txt",  language);
		String html = readEmail("email/remove-account", ".html", language);
		
		var translate = Translation.getTranslation(language);
		String subject = translate.get("remove-account");
		
		Email.sendEmail(
				email,
				subject,
				text,
				html);
	}
	
	static void
	sendWelcome(String email, String language) {
		if (language == null) {
			language = Translation.defaultLanguage;
		}
		String text = readEmail("email/welcome", ".txt",  language);
		String html = readEmail("email/welcome", ".html", language);
		
		var translate = Translation.getTranslation(language);
		String subject = translate.get("account-created");
		
		Email.sendEmail(
				email,
				subject,
				text,
				html);
	}

}

class EmailSender extends Thread {
	
	EmailSender(String recipient, String subject,
		String text, String html) {
		this.recipient = recipient;
		this.subject = subject;
		this.text = text;
		this.html = html;
	}

	String recipient;
	String subject;
	String text;
	String html;
	
	@Override public void 
	run() {
		try {
			Properties detail = new Properties();
			detail.put("mail.smtp.host",            Email.server);
			detail.put("mail.smtp.port",            Email.port);
			detail.put("mail.smtp.ssl.protocols",   Email.protocol);
			detail.put("mail.smtp.auth",            Email.auth);
			detail.put("mail.smtp.starttls.enable", Email.tls);
			var credential = new EmailCredential(
										Email.sender, 
										Email.password);
			Session session = Session.getInstance(detail, credential);

			Message message = new MimeMessage(session);
			String sender = Email.senderName + "<" + Email.sender + ">";
			
			message.setFrom(new InternetAddress(sender));
			message.setRecipients(
				Message.RecipientType.TO,
				InternetAddress.parse(recipient)
			);
			message.setSubject(subject);

			MimeBodyPart body = new MimeBodyPart();
			body.setContent(text, "text/plain");
			body.setContent(html, "text/html; charset=utf-8");
			Multipart part = new MimeMultipart();
			part.addBodyPart(body);

			message.setContent(part);
			Transport.send(message);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
		
class EmailCredential extends Authenticator {
	EmailCredential(String address, String password) {
		this.address = address;
		this.password = password;
	}
	
	String address;
	String password;

	@Override protected PasswordAuthentication 
	getPasswordAuthentication() {
		return new PasswordAuthentication(address, password);
	}
	
}

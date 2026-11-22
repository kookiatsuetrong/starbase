
// Windows
/*

set EMAIL=..\runtime
set EMAIL=%EMAIL%;..\runtime\json.jar
set EMAIL=%EMAIL%;..\runtime\mysql.jar
set EMAIL=%EMAIL%;..\runtime\jakarta-mail.jar
set EMAIL=%EMAIL%;..\runtime\jakarta-activation.jar

java --class-path %EMAIL% EmailThread.java

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
import java.io.FileReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Properties;
import org.json.JSONObject;

class EmailManager {
	
	String source;
	String defaultLanguage;
	
	public static void main(String[] data) {
		EmailManager manager = new EmailManager();
		manager.readSettings();
		manager.startServer();
	}
	
	void startServer() {
		try {
			ServerSocket server = new ServerSocket(9315);
			while (true) {
				System.out.println("Socket Waiting ...");
				Socket client = server.accept();
				client.close();
				this.checkEmail();
			}
		} catch (Exception e) { }
	}
	
	void checkEmail() {

		System.out.print("Checking Email ...");

		try {
			String s =  " select * from emails     " +
						" where status = 'created' ";

			var cn = DriverManager.getConnection(source);
			var ps = cn.prepareStatement(s);
			var rs = ps.executeQuery();
			if (rs.next()) {
				long number    = rs.getLong("number");
				String address = rs.getString("address");
				String subject = rs.getString("subject");
				String text    = rs.getString("text");
				String html    = rs.getString("html");

				System.out.println("Sending email to " + 
									address + " " + text);

				this.update(number, "preparing");

				EmailSender e = new EmailSender(address, 
										subject, text, html);
				e.send();
				
				this.update(number, "sent");
			}
			rs.close();
			ps.close();
			cn.close();
		} catch (Exception e) { }

		System.out.println(" finished");
	}
	
	void update(long number, String status) {
		try {
			String s =  " update emails set status = ? " +
						" where number = ?             ";

			var cn = DriverManager.getConnection(source);
			var ps = cn.prepareStatement(s);
			ps.setString(1, status);
			ps.setLong  (2, number);
			ps.execute();
			ps.close();
			cn.close();
		} catch (Exception e) { }

	}
	
	void readSettings() {
		HashMap<String, String> map = new HashMap<>();
		try {
			FileReader reader = new FileReader("settings.json");
			String buffer = reader.readAllAsString();
			reader.close();
			
			JSONObject root = new JSONObject(buffer);
			source = root.getString("connectionString");
			
		} catch (Exception e) { }
		
		System.out.println("The Connection String is " + source);
		
		map.clear();
		
		try {
			var sql = " select * from settings ";
			var cn = DriverManager.getConnection(source);
			var ps = cn.prepareStatement(sql);
			var rs = ps.executeQuery();
			while (rs.next()) {
				String name  = rs.getString("name");
				String value = rs.getString("value");
				map.put(name, value);
			}
			rs.close(); ps.close(); cn.close();
					
		} catch (Exception e) {
			System.out.println("fatal-error");
			System.out.println(e);
		}
		
		this.defaultLanguage = map.get("default-language");
		
		EmailSettings.fullWebSite  = map.get("full-web-site");
		EmailSettings.supportEmail = map.get("support-email");
		
		EmailSettings.server     = map.get("email-server");
		EmailSettings.port       = map.get("email-port");
		EmailSettings.senderName = map.get("email-sender-name");
		EmailSettings.sender     = map.get("email-sender");
		EmailSettings.password   = map.get("email-password");
		EmailSettings.protocol   = map.get("email-protocol");
		EmailSettings.auth       = map.get("email-auth");
		EmailSettings.tls        = map.get("email-tls");

		System.out.println("Update Settings");
		System.out.println("Storage.source       " + this.source);
		System.out.println("EmailSettings.sender " + EmailSettings.sender);
		System.out.println("defaultLanguage      " + defaultLanguage);
		
	}
	
}

class EmailSettings {
	
	static String server     = "";
	static String port       = "587";
	static String senderName = "Administrator";
	static String sender     = "";
	static String password   = "";
	static String protocol   = "TLSv1.2";
	static String auth       = "true";
	static String tls        = "true";
	
	static String fullWebSite  = "";
	static String supportEmail = "";
	
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
	
	void send() {
		try {
			Properties detail = new Properties();
			detail.put("mail.smtp.host",            EmailSettings.server);
			detail.put("mail.smtp.port",            EmailSettings.port);
			detail.put("mail.smtp.ssl.protocols",   EmailSettings.protocol);
			detail.put("mail.smtp.auth",            EmailSettings.auth);
			detail.put("mail.smtp.starttls.enable", EmailSettings.tls);
			var credential = new EmailCredential(
										EmailSettings.sender, 
										EmailSettings.password);
			Session session = Session.getInstance(detail, credential);

			Message message = new MimeMessage(session);
			String sender = EmailSettings.senderName + "<" + 
							EmailSettings.sender + ">";
			
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

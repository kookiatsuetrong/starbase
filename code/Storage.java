/**
 * The data access layer
 * 
 */

import java.io.File;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

class Storage {
	
	static String source = "";
	
	static String fullWebSite  = "";
	static String supportEmail = "";
	
	static String 
	getFullWebSite() {
		return fullWebSite;
	}
	
	static String 
	getSupportEmail() {
		return supportEmail;
	}
	
	static ArrayList<Language> languages = new ArrayList<>();
	
	public static ArrayList<Language>
	listLanguages() {
		return languages;
	}
	
	static HashSet<String> languageAlias = new HashSet<>();
	
	public static HashSet<String>
	listLanguageAlias() {
		return languageAlias;
	}
		
	public static HashMap<String, HashMap<String, String>>
	refreshTranslation() {
		HashMap<String, HashMap<String, String>> map = new HashMap<>();
		
		try {
			var sql = " select * from translations ";
			var cn = DriverManager.getConnection(source);
			var ps = cn.prepareStatement(sql);
			var rs = ps.executeQuery();
			while (rs.next()) {
				String language = rs.getString("language");
				String code     = rs.getString("code");
				String text     = rs.getString("text");
				
				// System.out.println(language + " " + code + " " + text);
				
				HashMap<String,String> m = map.get(language);
				if (m == null) {
					m = new HashMap<>();
					map.put(language, m);
				}
				
				m.put(code, text);
				
			}
			rs.close(); ps.close(); cn.close();
		} catch (Exception e) { }
		
		return map;
	}
	
	static User 
	getUserByEmail(String email) {
		if (email == null) return null;
		User user = null;
		try {
			var sql = " select * from users where email = ? ";
			var cn = DriverManager.getConnection(source);
			var ps = cn.prepareStatement(sql);
			ps.setString(1, email);
			var rs = ps.executeQuery();
			if (rs.next()) {
				user = new User();
				user.number     = rs.getLong  ("number");
				user.email      = rs.getString("email");
				user.password   = rs.getString("password");
				user.firstName  = rs.getString("first_name");
				user.lastName   = rs.getString("last_name");
				user.status     = rs.getString("status");
				user.language   = rs.getString("language");
				user.signature  = rs.getString("signature");
				user.theme      = rs.getString("theme");
				user.identifier = rs.getLong  ("identifier");
				
				String buffer;
				
				buffer             = rs.getString("administrator");
				buffer             = buffer == null ? "F" : buffer;
				user.administrator = buffer.charAt(0) == 'T';
				
				buffer             = rs.getString("staff");
				buffer             = buffer == null ? "F" : buffer;
				user.staff         = buffer.charAt(0) == 'T';
				
			}
			rs.close(); ps.close(); cn.close();
		} catch (Exception e) { }
		return user;
	}
	
	static User 
	getUserByAlias(String alias) {
		if (alias == null) return null;
		User user = null;
		try {
			var sql = " select * from users where signature = ? ";
			var cn = DriverManager.getConnection(source);
			var ps = cn.prepareStatement(sql);
			ps.setString(1, alias);
			var rs = ps.executeQuery();
			if (rs.next()) {
				user = new User();
				user.number     = rs.getLong  ("number");
				user.email      = rs.getString("email");
				user.password   = rs.getString("password");
				user.firstName  = rs.getString("first_name");
				user.lastName   = rs.getString("last_name");
				user.status     = rs.getString("status");
				user.language   = rs.getString("language");
				user.signature  = rs.getString("signature");
				user.theme      = rs.getString("theme");
				user.identifier = rs.getLong  ("identifier");
				
				String buffer;
				
				buffer             = rs.getString("administrator");
				buffer             = buffer == null ? "F" : buffer;
				user.administrator = buffer.charAt(0) == 'T';
				
				buffer             = rs.getString("staff");
				buffer             = buffer == null ? "F" : buffer;
				user.staff         = buffer.charAt(0) == 'T';
				
			}
			rs.close(); ps.close(); cn.close();
		} catch (Exception e) { }
		return user;
	}
	
	static boolean
	resetPassword(User user, String password) {
		if (user == null) return false;
		if (password == null) return false;
		try {
			var sql =   " update users set password = sha2(?, 512) " +
						" where email = ?                          ";
			
			var cn = DriverManager.getConnection(source);
			var ps = cn.prepareStatement(sql);
			ps.setString(1, password);
			ps.setString(2, user.email);
			ps.executeUpdate();
			ps.close(); cn.close();
			return true;
		} catch (Exception e) { }
		return false;
	}
	
	static User 
	getUserByEmailPassword(String email, String password) {
		User user = null;
		try {
			var sql =   " select * from users where email = ? and " +
						" password = sha2(?, 512)                 ";
			var cn = DriverManager.getConnection(source);
			var ps = cn.prepareStatement(sql);
			ps.setString(1, email);
			ps.setString(2, password);
			var rs = ps.executeQuery();
			if (rs.next()) {
				user = new User();
				user.number     = rs.getLong  ("number");
				user.email      = rs.getString("email");
				user.password   = rs.getString("password");
				user.firstName  = rs.getString("first_name");
				user.lastName   = rs.getString("last_name");
				user.status     = rs.getString("status");
				user.language   = rs.getString("language");
				user.signature  = rs.getString("signature");
				user.theme      = rs.getString("theme");
				user.identifier = rs.getLong  ("identifier");
				
				String buffer;
				
				buffer             = rs.getString("administrator");
				buffer             = buffer == null ? "F" : buffer;
				user.administrator = buffer.charAt(0) == 'T';
				
				buffer             = rs.getString("staff");
				buffer             = buffer == null ? "F" : buffer;
				user.staff         = buffer.charAt(0) == 'T';
			}
			rs.close(); ps.close(); cn.close();
		} catch (Exception e) { }
		return user;
	}
	
	static boolean 
	createAccount(User user) {
		if (user == null) return false;
		boolean b1 = false;
		boolean b2 = false;
		boolean b3 = false;
		
		try {
			var sql =   " insert into users(email, password, " +
						"   first_name, last_name, status,   " +
						"   administrator, staff,            " +
						"   signature, identifier)           " +
						" values(?, sha2(?, 512), ?, ?, ?,   " +
						"        ?, ?, ?, ? )                ";
			
			var cn = DriverManager.getConnection(source);
			var ps = cn.prepareStatement(sql);
			ps.setString(1, user.email);
			ps.setString(2, user.password);
			ps.setString(3, user.firstName);
			ps.setString(4, user.lastName);
			ps.setString(5, user.status);
			ps.setString(6, user.administrator ? "T" : "F");
			ps.setString(7, user.staff         ? "T" : "F");
			
			String signature = Tool.randomCode(15);
			long identifier  = Long.parseLong(signature);
			ps.setString(8, signature);
			ps.setLong  (9, identifier);
			
			ps.execute();
			ps.close(); cn.close();
			b1 = true;
		} catch (Exception e) {
			System.out.println(e);
		}
		
		long userNumber = 0;
		
		try {
			var sql =   " select * from users  " +
						" where email = ?      ";
			
			var cn = DriverManager.getConnection(source);
			var ps = cn.prepareStatement(sql);
			ps.setString(1, user.email);
			var rs = ps.executeQuery();
			if (rs.next()) {
				userNumber = rs.getLong("number");
			}
			rs.close(); ps.close(); cn.close();
			b2 = true;
		} catch (Exception e) {
			System.out.println(e);
		}
		
		try {
			user.identifier = Tool.createIdentifier(userNumber);
			user.signature  = "" + user.identifier;
			
			var sql =   " update users set     " +
						"    signature  = ?,   " +
						"    identifier = ?    " +
						" where   email = ?    ";
			
			var cn = DriverManager.getConnection(source);
			var ps = cn.prepareStatement(sql);
			
			ps.setString(1, user.signature);
			ps.setLong  (2, user.identifier);
			ps.setString(3, user.email);
			
			ps.execute();
			ps.close(); cn.close();
			b3 = true;
		} catch (Exception e) {
			System.out.println(e);
		}
		
		return b1 && b2 && b3;
	}
	
	static boolean 
	changePassword(User user, String current, String password) {
		if (user == null) return false;
		if (current == null) return false;
		if (password == null) return false;
		try {
			var sql =   " update users set password = sha2(?, 512) " +
						" where password = sha2(?, 512)            " +
						" and email =  ?                           ";
			var cn = DriverManager.getConnection(source);
			var ps = cn.prepareStatement(sql);
			ps.setString(1, password);
			ps.setString(2, current);
			ps.setString(3, user.email);
			int r = ps.executeUpdate();
			ps.close(); cn.close();
			return r == 1;
		} catch (Exception e) { }
		return false;
	}

	static boolean 
	removeAccount(User user, String password) {
		if (user == null) return false;
		if (password == null) return false;
		
		// TODO: Remove all addresses of this user
		// TODO: Remove all manages of this user
		
		// remove profile photo
		String[] files = {
						"./web/uploaded/user-" + user.identifier + ".png",
						"./web/uploaded/card-" + user.identifier + ".png"
						};
		for (String f : files) {			
			File file = new File(f);
			boolean result = file.delete();
		}
		
		boolean found = false;
		
		try {
			var sql =   " select * from users        " +
						" where email = ? and        " +
						"    password = sha2(?, 512) ";	
			
			var cn = DriverManager.getConnection(source);
			var ps = cn.prepareStatement(sql);
			ps.setString(1, user.email);
			ps.setString(2, password);
			var rs = ps.executeQuery();
			if (rs.next()) {
				found = true;
			}
			rs.close(); ps.close(); cn.close();
		} catch (Exception e) { }
		
		if (found == false) return false;
		
		try {
			var sql =   " update users set             " +
						"   email      = identifier,   " +
						"   first_name = 'removed',    " +
						"   last_name  = 'removed',    " +
						"   status     = 'removed',    " +
						"   signature  = identifier    " +
						" where email  = ? and         " +
						"     password = sha2(?, 512)  ";
			
			var cn = DriverManager.getConnection(source);
			var ps = cn.prepareStatement(sql);
			ps.setString(1, user.email);
			ps.setString(2, password);
			ps.execute();
			ps.close(); cn.close();
		} catch (Exception e) { }
		
		return found;
	}
	
	static boolean 
	updateUserDetail(User user, 
		String firstName, String lastName, String language) {
		if (user == null) return false;
		if (firstName == null) return false;
		if (lastName == null) return false;
		if (language == null) language = Translation.defaultLanguage;
		
		try {
			var sql =   " update users          " +
						" set first_name = ?,   " +
						"     last_name = ?,    " +
						"     language   = ?    " +
						" where email = ?       ";
			var cn = DriverManager.getConnection(source);
			var ps = cn.prepareStatement(sql);
			ps.setString(1, firstName);
			ps.setString(2, lastName);
			ps.setString(3, language);
			ps.setString(4, user.email);
			ps.execute();
			ps.close(); cn.close();
			return true;
		} catch (Exception e) { }
		
		return false;
	}

	static boolean 
	changeSignature(User user, String signature, String theme) {
		if (user == null) return false;
		if (signature == null) return false;
		if (theme == null) return false;
		
		try {
			var sql =   " update users        " +
						" set signature = ?,  " +
						"         theme = ?   " +
						" where   email = ?   ";
			var cn = DriverManager.getConnection(source);
			var ps = cn.prepareStatement(sql);
			ps.setString(1, signature);
			ps.setString(2, theme);
			ps.setString(3, user.email);
			ps.execute();
			ps.close(); cn.close();
			return true;
		} catch (Exception e) { }
		
		return false;
	}

	static void
	insertPasswordTrial(String email) {
		if (email == null) return;
		try {
			String s =  " insert into password_trials(user)  " +
						" select users.number from users     " +
						" where  users.email = ?             ";
			
			var cn = DriverManager.getConnection(source);
			var ps = cn.prepareStatement(s);
			ps.setString(1, email);
			ps.execute();
			ps.close();
			cn.close();
		} catch (Exception e) { }
	}
	
	static int
	getDailyPasswordTrial(String email) {
		if (email == null) return 0;
		int total = 0;
		try {
			String s =  " select count(*) as total               " +
						" from password_trials                   " +
						" where user =                           " +
						"              (select number from users " +
						"                where users.email = ? ) " +
						" and happened >= utc_timestamp() -      " +
						"                 interval 1 hour        ";
			
			var cn = DriverManager.getConnection(source);
			var ps = cn.prepareStatement(s);
			ps.setString(1, email);
			var rs = ps.executeQuery();
			if (rs.next()) {
				total = rs.getInt("total");
			}
			rs.close();
			ps.close();
			cn.close();
		} catch (Exception e) { }
		
		return total;
	}
	
	/*
	select * from password_trials 
	where happened >= utc_timestamp() - interval 60 minute;
	*/
	
	static boolean
	updateStatus(User user, String status) {
		if (user == null) return false;
		if (status == null) return false;
		
		try {
			var sql =   " update users     " +
						" set status = ?   " +
						" where email = ?  ";
			var cn = DriverManager.getConnection(source);
			var ps = cn.prepareStatement(sql);
			ps.setString(1, status);
			ps.setString(2, user.email);
			ps.execute();
			ps.close(); cn.close();
			return true;
		} catch (Exception e) { }

		return false;
	}
	
	static String
	getUserStatus(String signature) {
		if (signature == null) return "unknown";

		String status = "unknown";
		try {
			String s =  " select status from users " +
						" where signature = ?      ";
			
			var cn = DriverManager.getConnection(source);
			var ps = cn.prepareStatement(s);
			ps.setString(1, signature);
			var rs = ps.executeQuery();
			if (rs.next()) {
				status = rs.getString("status");
			}
			rs.close();
			ps.close();
			cn.close();
		} catch (Exception e) { }
	
		return status;
	}
}

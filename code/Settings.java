/**
 * Settings.java
 * 
 */

/*
Sample configuration file is as follow:

File: start.json

{
	"connectionString": 
			"jdbc:mysql://127.0.0.1/simplex?user=me&password=password"

}

Don't forget to call the following subprograms manually:
Settings.refresh();
Translation.refresh();
*/

import java.io.FileReader;
import java.util.HashMap;
import java.sql.DriverManager;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

class Settings {
	
	static HashMap<String, String> map = new HashMap<>();
	
	static void refresh() {
		String buffer = "";
		try {
			FileReader reader = new FileReader("start.json");
			buffer = reader.readAllAsString();
			reader.close();
		} catch (Exception e) {
			System.out.println("fatal-error");
			System.out.println(e);
		}
		
		JSONObject detail = new JSONObject(buffer);
		Storage.source    = (String)detail.getString("connectionString");
		
		map.clear();
		
		try {
			var sql = " select * from settings ";
			var cn = DriverManager.getConnection(Storage.source);
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
		
		Storage.fullWebSite  = map.get("full-web-site");
		Storage.supportEmail = map.get("support-email");
		
		Translation.defaultLanguage = map.get("default-language");
		
		Email.server     = map.get("email-server");
		Email.port       = map.get("email-port");
		Email.senderName = map.get("email-sender-name");
		Email.sender     = map.get("email-sender");
		Email.password   = map.get("email-password");
		Email.protocol   = map.get("email-protocol");
		Email.auth       = map.get("email-auth");
		Email.tls        = map.get("email-tls");

		System.out.println("Update Settings");
		System.out.println("Storage.source              " + Storage.source);
		System.out.println("Email.sender                " + Email.sender);
		System.out.println("Translation.defaultLanguage " + 
									Translation.defaultLanguage);
		
		Storage.languages.clear();
		try {
			var sql = " select * from languages ";
			var cn = DriverManager.getConnection(Storage.source);
			var ps = cn.prepareStatement(sql);
			var rs = ps.executeQuery();
			while (rs.next()) {
				Language language = new Language();
				language.number   = rs.getLong("number");
				language.name     = rs.getString("name");
				language.alias    = rs.getString("alias");
				Storage.languages.add(language);
			}
			rs.close(); ps.close(); cn.close();
		} catch (Exception e) { }
		
		Storage.languageAlias.clear();
		for (Language lang : Storage.languages) {
			Storage.languageAlias.add(lang.alias);
		}
		
		modules.clear();
		
		JSONArray data = (JSONArray)detail.getJSONArray("modules");
		int n = data.length();
		for (int i = 0; i < n; i++) {
			String name = data.getString(i);
			System.out.println("Module " + name);
			modules.add(name);
		}
	}
	
	static ArrayList<String> modules = new ArrayList<>();
	
	static String get(String name) {
		String value = map.get(name);
		return value;
	}
	
}

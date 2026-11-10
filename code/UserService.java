import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collection;
import jakarta.servlet.http.Part;
import jakarta.servlet.http.HttpSession;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.util.Set;

import web.framework.Context;
import web.framework.Redirect;

class UserService {
	
	public static final int MAXIMUM_PASSWORD_TRIAL = 3;
	
	/**
	 * Creates a new photo code and do the following tasks:
	 * 1. send photo to the client
	 * 2. keep value in the session
	 * 
	 * GET /service-create-photo-code
	 */
	static Object 
	createPhotoCode(Context context) {
		String value = Tool.randomPhotoCode();
		String photo = Tool.createPhotoCode(value);
		// System.out.println("New Photo Code is " + value);

		if (context.testing == true) {
			context.setSession("photo-code", value);
		}

		if (context.testing == false) {
			HttpSession session = context.getSession(true);
			session.setAttribute("photo-code", value);
		}

		TreeMap<String, Object> map = new TreeMap<>();
		map.put("result", "OK");
		map.put("photo", photo);
		return map;
	}

	/**
	 * Checks the existence of the given email
	 * 
	 * context.bodyBuffer is a string of JSON such as:
	 * "{ email: 'user@email.com', photoCode: '1234' }"
	 * 
	 */
	static Object 
	checkEmail(Context context) {
		Map input = context.read();
		String email = (String)input.get("email");
		
		if (email == null) {
			return Tool.createError("invalid-email");
		}
		
		HttpSession session = context.getSession(true);
		
		// Case 1: Correct code   and Existing email ---> Ask password
		// Case 2: Correct code   and New Email      ---> Create account
		// Case 3: Incorrect code and Existing email ---> Restart
		// Case 4: Incorrect code and New Email      ---> Restart
		
		String photoCode = "";
		if (context.testing == true) {
			photoCode = (String)context.getSession("photo-code");
		}

		if (context.testing == false) {
			photoCode = (String)session.getAttribute("photo-code");
			session.removeAttribute("photo-code");
		}
	
		boolean correctPhotoCode = false;
		String code = (String)input.get("photoCode");
		if (photoCode == null) {
			photoCode = "";
		}
		if (photoCode.equals(code)) {
			correctPhotoCode = true;
		}
		
		TreeMap<String, Object> output = new TreeMap<>();
		
		if (correctPhotoCode == false) {
			output.put("result", "ERROR");
			output.put("reason", "incorrect-photo-code");
			output.put("correctPhotoCode", correctPhotoCode);
			return output;
		}
		
		// from this point, correctPhotoCode is true
		
		User user = Storage.getUserByEmail(email);
		
		if (valid(user) == true) {
			System.out.println("Email was found");
			System.out.println("The email is " + email);
			output.put("result", "OK");
			output.put("reason", "email-was-found");
			output.put("correctPhotoCode", correctPhotoCode);
			session.setAttribute("email", email);
		}
		
		if (valid(user) == false) {
			System.out.println("Email was not found");
			System.out.println("The email is " + email);
			output.put("result", "ERROR");
			output.put("reason", "email-not-found");
			output.put("correctPhotoCode", correctPhotoCode);
			session.setAttribute("email", email);
			
			String activationCode = Tool.randomActivationCode();
			session.setAttribute("activation-code", activationCode);
			System.out.println("Activation code is " + activationCode);
			
			String language = (String)context.request.
									getAttribute("language");
			Email.sendActivationCode(email, activationCode, language);
		}
		
		System.out.println(output);
		
		return output;
	}
	
	/**
	 * Checks the email and password 
	 * 
	 * POST /service-check-password
	 * {email:"", password:""}
	 */
	static TreeMap<String, Object>
	checkPassword(Context context) {
		TreeMap<String, Object> input = (TreeMap<String,Object>)context.read();
		String password = (String)input.get("password");
		
		HttpSession session = context.getSession(true);
		String email = (String)session.getAttribute("email");
		session.removeAttribute("email");
		
		if (email == null) return Tool.createError("invalid-email");
		if (password == null) return Tool.createError("invalid-password");
		
		User user = Storage.getUserByEmailPassword(email, password);
		if (user == null) {
			Storage.insertPasswordTrial(email);
			
			int trial = Storage.getDailyPasswordTrial(email);
			System.out.println("Daily password fails: " + trial);
			if (trial >= MAXIMUM_PASSWORD_TRIAL) {
				return Tool.createError("account-has-been-suspended");
			}

			return Tool.createError("incorrect-password");
		}
		
		// This user.status must be: 
		//   'typical', 'busy', 'active', 'on-call', 'ready'
		
		// This user.status must not be: 
		//   'banned', 'removed', 'suspended'
		
		if ("banned".equals(user.status) == true) {
			return Tool.createError("invalid-user-status");
		}
		
		if ("removed".equals(user.status) == true) {
			return Tool.createError("invalid-user-status");
		}
		
		context.setSession("user", user);
		
		// From this point, user is valid
		TreeMap<String, Object> result = new TreeMap<>();
		result.put("result", "OK");
		result.put("language", user.language);
		return result;
	}
	// invalid-email
	// invalid-password
	// incorrect-password
	// invalid-user-status
	// account-has-been-suspended
	
	/**
	 * Creates a new account
	 * 
	 * POST /service-create-account
	 * { password:"", firstName: "", lastName: "", activationCode: "" }
	 */
	static TreeMap<String, Object> 
	createAccount(Context context) {
		TreeMap<String, Object> input = (TreeMap<String,Object>)context.read();
		String password       = (String)input.get("password");
		String firstName      = (String)input.get("firstName");
		String lastName       = (String)input.get("lastName");
		String activationCode = (String)input.get("activationCode");
		
		String email = (String)context.getSession("email");
		
		if (firstName == null) firstName = "";
		if (lastName  == null) lastName  = "";
		if (firstName.length() <  2) 
			return Tool.createError("first-name-too-short");
		if (firstName.length() > 40) 
			return Tool.createError("first-name-too-long");
		if (lastName.length() <   2) 
			return Tool.createError("last-name-too-short");
		if (lastName.length() >  40) 
			return Tool.createError("last-name-too-long");
		
		TreeMap<String, Object> result = new TreeMap<>();

		if (email == null) return Tool.createError("invalid-email");
		if (password == null) return Tool.createError("invalid-password");
		
		// From this point, email and password are not null
		
		String secretCode = (String)context.getSession("activation-code");
		
		if (secretCode == null) {
			secretCode = "";
		}

		boolean correctActivationCode = false;
		
		if (secretCode.equals(activationCode)) {
			correctActivationCode = true;
		}
		
		System.out.println("Activation Code: " + correctActivationCode);
		
		if (correctActivationCode == false) {
			return Tool.createError("incorrect-activation-code");
		}
		
		// From this point, correctActivationCode is true
		int detail = PasswordDetail.check(password);
		if (detail > PasswordDetail.OK) {
			ArrayList<String> errors = PasswordDetail.listError(detail);
			result.put("result", "ERROR");
			result.put("reason", "invalid-password");
			result.put("errors", errors);
			return result;
		}

		// From this point, password is OK

		// Add this account to the database 
		User user      = new User();
		user.email     = email;
		user.password  = password;
		user.firstName = firstName;
		user.lastName  = lastName;
		user.status    = "typical";
		
		boolean success = Storage.createAccount(user);
		
		// Get user.number from the last method call
		User newly = Storage.getUserByEmail(user.email);
		
		// TODO: There are two cases of failure:
		//       1. Duplicated email
		//       2. Duplicated signature
		
		if (success == false) {
			return Tool.createError("duplicated-email");
		}
		
		// From this point, account created successfully
		context.removeSession("email");
		context.removeSession("activationCode");
		
		// Log in automatically
		context.setSession("user", newly);
		result.put("result", "OK");
		result.put("language", user.language);
		
		return result;
	}
	
	/**
	 * Queries user information.
	 * 
	 * GET /service-me 
	 */
	static TreeMap<String, Object>
	getUserDetail(Context context) {
		TreeMap<String, Object> result = new TreeMap<>();
		User user = (User)context.getSession("user");
		
		if (user == null) return Tool.createError("invalid-user");
		
		result.put("email",         user.email);
		result.put("firstName",     user.firstName);
		result.put("lastName",      user.lastName);
		result.put("status",        user.status);
		result.put("language",      user.language);
		result.put("signature",     "" + user.signature);
		result.put("identifier",    "" + user.identifier);
		result.put("administrator", user.administrator);
		result.put("staff",         user.staff);
		result.put("result",        "OK");
		return result;
	}
	
	/**
	 * Logs user out from the web application.
	 * 
	 * GET /service-logout
	 */
	static Object
	logOut(Context context) {
		context.removeSession("user");
		context.removeSession("shop");
		
		return Tool.createSuccess("logout-successfully");
	}
	
	static Object 
	sendResetCode(Context context) {
		Map input = context.read();
		System.out.println(input);
		String email = (String)input.get("email");
		
		TreeMap<String, Object> output = new TreeMap<>();
		if (email == null) return Tool.createError("invalid-email");
		
		String photoCode = "";
		if (context.testing == true) {
			photoCode = (String)context.getSession("photo-code");
		}

		if (context.testing == false) {
			photoCode = (String)context.getSession("photo-code");
			HttpSession session = context.getSession(true);
			session.removeAttribute("photo-code");
		}
		
		// From this point, photo-code in the session is invalid
		// use reset-code instead
	
		boolean correctPhotoCode = false;
		String code = (String)input.get("photoCode");
		if (photoCode == null) {
			photoCode = "";
		}
		if (photoCode.equals(code)) {
			correctPhotoCode = true;
		}
		output.put("correctPhotoCode", correctPhotoCode);
		
		if (correctPhotoCode == false) {
			output.put("reason", "incorrect-photo-code");
			return output;
		}
		
		// From this point, the photo code is correct
		
		User user = Storage.getUserByEmail(email);
		
		if (valid(user) == true  && correctPhotoCode) {
			output.put("result", "OK");
			output.put("reason", "email-was-found");
			
			HttpSession session = context.getSession(true);
			session.setAttribute("email", email);
			
			String resetCode = Tool.randomActivationCode();
			session.setAttribute("reset-code", resetCode);
			System.out.println("Reset code is " + resetCode);
			
			String language = (String)context.request.
									getAttribute("language");
			Email.sendResetCode(email, resetCode, language);
		}
		
		if (valid(user) == false && correctPhotoCode) {
			output.put("result", "ERROR");
			output.put("reason", "email-not-found");
		}
		
		return output;
	}
	
	static Object 
	checkResetCode(Context context) {
		Map input = context.read();
		String code = (String)input.get("code");
		
		TreeMap<String, Object> output = new TreeMap<>();
		
		String resetCode = (String)context.getSession("reset-code");
		if (resetCode == null) {
			resetCode = "";
		}
		
		boolean correct = resetCode.equals(code);
		
		// From this point, the reset code is correct
		
		HttpSession session = context.getSession(true);
		session.removeAttribute("reset-code");
		session.setAttribute("reset-code", "OK");    // it becomes "OK"
		// email is still in the session (cookies)
	
		if (correct) {
			output.put("result", "OK");
			output.put("reason", "correct-reset-code");
		}
		
		if (correct == false) {
			output.put("result", "ERROR");
			output.put("reason", "incorrect-reset-code");
			
			session.removeAttribute("email");
		}
		
		return output;
	}
	
	static Object 
	createNewPassword(Context context) {
		Map input = context.read();
		String password = (String)input.get("password");
		String confirm  = (String)input.get("confirm");
		if (password == null) {
			password = "";
		}
		
		TreeMap<String, Object> result = new TreeMap<>();
		
		String email = (String)context.getSession("email");
		User user = Storage.getUserByEmail(email);
		
		if (email == null || user == null) {
			result.put("result", "ERROR");
			result.put("reason", "invalid-email");
			return result;
		}
		
		// From this point, email is in the database
		
		String resetCode = (String)context.getSession("reset-code");
		boolean resetCodePassed = "OK".equals(resetCode);
		
		if (resetCodePassed == false) {
			return Tool.createError("incorrect-reset-code");
		}
		
		// From this point, the reset code is correct
		
		int detail = PasswordDetail.check(password);
		if (detail > PasswordDetail.OK) {
			ArrayList<String> errors = PasswordDetail.listError(detail);
			result.put("result", "ERROR");
			result.put("reason", "invalid-password");
			result.put("errors", errors);
			return result;
		}
		
		// From this point, password is in the correct format
		
		boolean matched = password.equals(confirm);
		if (matched == false) {
			return Tool.createError("password-not-identical");
		}
		
		// From this point, password and confirm is matched
		
		// Clear session
		context.removeSession("email");
		context.removeSession("reset-code");
		
		// From this point, password is in the correct format
		Storage.resetPassword(user, password);
		
		// Log in automatically
		User newly = Storage.getUserByEmail(email);
		context.setSession("user", newly);
		
		return Tool.createSuccess("new-password-created");
	}
	
	/**
	 * Changes the current user's password
	 * 
	 * POST /service-change-password
	 */
	static Object
	changePassword(Context context) {
		User user = (User)context.getSession("user");
		if (user == null) return Tool.createError("invalid-user");
		
		TreeMap<String,Object> input = (TreeMap<String,Object>)context.read();
		
		String current  = (String)input.get("current");
		String password = (String)input.get("password");
		String confirm  = (String)input.get("confirm");
		
		if (password == null) {
			password = "";
		}
		
		boolean matched = password.equals(confirm);
		
		if (matched == false) {
			return Tool.createError("password-not-identical");
		}
		
		TreeMap<String, Object> result = new TreeMap<>();
		
		int detail = PasswordDetail.check(password);
		if (detail > 0) {
			ArrayList<String> errors = PasswordDetail.listError(detail);
			result.put("result", "ERROR");
			result.put("reason", "incorrect-password-format");
			result.put("errors", errors);
			return result;
		}
		
		boolean passed = Storage.changePassword(user, current, password);
		
		if (passed == false) {
			return Tool.createError("incorrect-current-password");
		}
		
		return Tool.createSuccess("change-password-success");
	}
	
	/**
	 * Removes user account
	 * 
	 * POST /service-remove-account
	 */
	static Object
	removeAccount(Context context) {
		User user = (User)context.getSession("user");
		if (user == null) return Tool.createError("invalid-user");
		
		TreeMap<String,Object> input = (TreeMap<String,Object>)context.read();
		
		String password = (String)input.get("password");
		boolean passed = Storage.removeAccount(user, password);
		
		if (passed == false) {
			return Tool.createError("incorrect-password");
		}
		
		context.removeSession("user");
		context.removeSession("shop");
		
		return Tool.createSuccess("account-has-been-removed");
	}
	
	/**
	 * Updates user information (first name, last name)
	 * 
	 * POST /service-update-user-detail
	 */
	static Object
	updateDetail(Context context) {
		User user = (User)context.getSession("user");
		if (user == null) return Tool.createError("invalid-user");
		
		TreeMap<String,Object> input = (TreeMap<String,Object>)context.read();
		
		String firstName    = (String)input.get("firstName");
		String lastName     = (String)input.get("lastName");
		String languageName = (String)input.get("languageName");
		
		if (firstName == null) firstName = "";
		if (lastName  == null) lastName  = "";
		if (languageName == null) languageName = "";
		
		if (firstName.length() <  2) 
			return Tool.createError("first-name-too-short");
		if (firstName.length() > 40) 
			return Tool.createError("first-name-too-long");
		if (lastName.length() <   2) 
			return Tool.createError("last-name-too-short");
		if (lastName.length() >  40) 
			return Tool.createError("last-name-too-long");
		
		ArrayList<Language> languages = Storage.listLanguages();
		Language language = null;
		for (Language lang : languages) {
			if (languageName.equals(lang.name)) {
				language = lang;
			}
		}
		
		boolean passed = Storage.updateUserDetail
							(user, firstName, lastName, language.alias);
		
		if (passed == false) {
			return Tool.createError("unable-to-update-user-information");
		}
		
		// Replace session information
		user.firstName = firstName;
		user.lastName  = lastName;
		user.language  = language.alias;
		context.setSession("user", user);
		
		var detail = Tool.createSuccess("user-information-has-been-updated");
		detail.put("language", user.language);
		return detail;
	}
	
	// savePhoto() using JSON 
	static Object
	savePhoto(Context context) {
		User user = (User)context.getSession("user");
		
		String language = (String)context.request.getAttribute("language");
		if (user == null) return new Redirect("/" + language + "/photo");
		
		String path = context.request.getServletContext().getRealPath("");

		Map<String, Object> raw = context.read();
		ArrayList data  = (ArrayList)raw.get("data");
		int total = data.size();
		
		for (int i = 0; i < total; i++) {
			Object o = data.get(i);
			if (o instanceof Map map) {
				
				Set key = map.keySet();
				
				String name        = "";
				String size        = "";
				String contentSize = "";
				String type        = "";
				String content     = ",";
				
				for (Object k : key) {
					if (k instanceof String s && 
						map.get(s) instanceof String value) {
						switch (s) {
							case "name"        -> name        = value;
							case "size"        -> size        = value;
							case "contentSize" -> contentSize = value;
							case "type"        -> type        = value;
							case "content"     -> content     = value;
						}
					}
				}
				
				System.out.println("Name "        + name);
				System.out.println("Size "        + size);
				System.out.println("BASE64 Size " + contentSize);
				System.out.println("Type "        + type);
				System.out.println("Content "     + content.substring(0, 30));

				// The content is starting with:
				// PNG  --> "data:image/png;base64,iVBORw0K"
				// JPG  --> "data:image/jpeg;base64,/9j/4AA"
				// WEBP --> "data:image/webp;base64,UklGRm7"
					
				String[] items = content.split(",");
				if (items.length == 2) {
					byte[] array = java.util.Base64.getDecoder()
											.decode(items[1]);
					
					String extension = ".txt";
					/*
					if ("image/jpg" .equals(type)) extension = ".jpg";
					if ("image/jpeg".equals(type)) extension = ".jpg";
					if ("image/webp".equals(type)) extension = ".webp";
					if ("image/png" .equals(type)) extension = ".png";
					
					String fileName = path + "uploaded/user-temporary-" +
												i + extension;
					*/
					
					// this version force to .png
					extension = ".png";
					
					String fileName = path + "uploaded/user-" + 
									user.identifier + ".png";
				
					System.out.println("Saving " + fileName);
					try {
						Files.write(Paths.get(fileName), array);
					} catch (Exception e) { 
						System.out.println(e);
					}
				}
			}
		}
		
		
		return Tool.createSuccess("upload-photo-successfully");
	}

	static Object
	refreshCache(Context context) {
		User user = (User)context.getSession("user");
		if (user == null) return Tool.createError("invalid-user");
		
		if (user.isAdministrator() == false) {
			return Tool.createError("permission-denied");
		}
		
		Settings.refresh();
		WebService.refresh();
		Translation.refresh();
		
		return Tool.createSuccess("cache-refreshing-success");
	}
	
	static Object
	changeSignature(Context context) {
		User user = (User)context.getSession("user");
		if (user == null) return Tool.createError("invalid-user");
		
		Map<String, Object> map = context.read();
		String signature = (String)map.get("signature");
		String color     = (String)map.get("color");
		String defaultColor = "#778899";
		if (color == null) color = defaultColor;
		color = color.toLowerCase();
		if (color.charAt(0) == '#') { }
		if (color.charAt(0) != '#') { color = defaultColor; }
		char[] buffer = color.toCharArray();
		for (int i = 1; i < buffer.length; i++) {
			boolean passed = false;
			if ('0' <= buffer[i] && buffer[i] <= '9') passed = true;
			if ('a' <= buffer[i] && buffer[i] <= 'f') passed = true;
			if (passed == true) { }
			if (passed == false) { color = defaultColor; }
		}
		if (buffer.length == 7) { }
		if (buffer.length != 7) { color = defaultColor; }
		
		String pattern = "[a-z][a-z0-9\\-\\.]*[a-z0-9]";
		
		if (signature == null) {
			return Tool.createError("signature-is-empty");
		}
		
		if (signature.length() < 10) {
			return Tool.createError("signature-is-too-short");
		}
		if (signature.length() > 20) {
			return Tool.createError("signature-is-too-long");
		}
		
		if (signature.matches(pattern) == false) {
			return Tool.createError("signature-is-invalid");
		}
		
		boolean result = Storage.changeSignature(user, signature, color);
		if (result == false) {
			return Tool.createError("duplicated-signature");
		}
		
		// update user signature in the session
		user.signature = signature;
		user.theme     = color;
		context.setSession("user", user);
		
		return Tool.createSuccess("change-signature-successfully");
	}
	
	static Object
	saveCard(Context context) {
		User user = (User)context.getSession("user");
		if (user == null) return Tool.createError("invalid-user");
		
		Map<String, Object> map = context.read();
		String detail = (String)map.get("detail");
		
		if (detail == null) detail = "";
		// System.out.println(detail);
		
		int index = detail.indexOf(",");
		if (index < 0) { }
		if (index >= 0) {
			String buffer = detail.substring(index + 1);
			System.out.println(buffer);
			
			byte[] data = Base64.getDecoder().decode(buffer);
			String fileName = "./web/uploaded/card-" + 
								user.identifier + ".png";
            Path target = Paths.get(fileName);
			try {
	            Files.write(target, data);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		
		return Tool.createSuccess("save-card-successfully");
	}
	
	static Object
	updateStatus(Context context) {
		User user = (User)context.getSession("user");
		if (user == null) return Tool.createError("invalid-user");
		
		String status = context.getParameter("status");
		boolean valid = false;
		
		if ("typical".equals(status)) { valid = true; }
		if ("calling".equals(status)) { valid = true; }
		if ("busy"   .equals(status)) { valid = true; }

		if (valid == true) {
			boolean result = Storage.updateStatus(user, status);
			if (result == true) {
				return Tool.createSuccess("update-status-successfully");		
			}
		}
		
		return Tool.createError("unable-to-update-status");
	}
	
	
	/**
	 * Checks whether the give instance is valid
	 */
	static boolean
	valid(Object o) {
		return o != null;
	}

}

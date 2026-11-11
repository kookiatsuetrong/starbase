/**
 * The main of the program
 * 
 */
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.time.ZoneOffset;
import java.time.LocalDateTime;

import web.framework.Context;
import web.framework.Freemarker;
import static web.framework.Rockstar.*;

class Start {
	
	void
	main() {
		
		Settings.refresh();
		WebService.refresh();
		Translation.refresh();
		
		/*
		LocalDateTime t = LocalDateTime.now(ZoneOffset.UTC);
		String s = t.toString();
		System.out.println(t);
		*/
		
		// Add modules
		for (String name : Settings.modules) {
			try {
				Class<?> clz = Class.forName(name);
				Constructor<?> cns = clz.getDeclaredConstructor();
				cns.setAccessible(true);
				Object app = cns.newInstance();
				Method m = clz.getDeclaredMethod("start");
				m.setAccessible(true);
				m.invoke(app);
			} catch (Exception e) {
				System.err.println(e);
			}
		}
		
		addFilter(Start::languageFilter);
		
		handle( "GET /",               Start::showStart);
		handle( "GET /notify",         Start::notify);
		
		handle( "GET /ask-email",      UserHandler::askEmail);
		handle( "GET /ask-password",   UserHandler::askPassword);
		handle( "GET /create-account", UserHandler::createAccount);
		handle( "GET /reset-password", UserHandler::resetPassword);
		
		handle( "GET /profile",        UserHandler::showProfile);
		handle( "GET /password",       UserHandler::showPassword);
		handle( "GET /user-photo",     UserHandler::showPhotoManagement);
		handle("POST /user-photo",     UserService::savePhoto);
		handle( "GET /remove-account", UserHandler::showRemoveAccount);
		handle( "GET /settings",       UserHandler::showSettings);
		handle( "GET /user-signature", UserHandler::showSignature);
		handle( "GET /user-status",    UserHandler::showStatus);
		
		handle( "GET /service-list-languages", 
										WebService::listLanguages);
		
		// Language Management
		handle( "GET /manage-language",           LanguageHandler::show);
		handle("POST /service-add-language",      LanguageService::add);
		
		handle( "GET /service-create-photo-code", UserService::createPhotoCode);
		handle("POST /service-reset-password",    UserService::sendResetCode);
		handle("POST /service-check-reset-code",  UserService::checkResetCode);
		handle("POST /service-create-new-password",
											UserService::createNewPassword);

		handle("POST /service-check-email",     UserService::checkEmail);
		handle("POST /service-check-password",  UserService::checkPassword);
		handle("POST /service-create-account",  UserService::createAccount);
		handle( "GET /service-me",              UserService::getUserDetail);
		handle("POST /service-change-password", UserService::changePassword);
		handle("POST /service-remove-account",  UserService::removeAccount);
		handle("POST /service-save-card",       UserService::saveCard);
		handle( "GET /service-logout",          UserService::logOut);
		
		handle("POST /service-update-user-detail", UserService::updateDetail);
		handle( "GET /service-update-status",      UserService::updateStatus);
		
		handle( "GET /service-get-user-status",   WebService::getUserStatus);
		// handle( "GET /service-check-signaure", UserService::checkSignature);
		handle("POST /service-change-signature",  UserService::changeSignature);
		
		handle( "GET /privacy-policy",        Start::showPrivacyPolicy);
		handle( "GET /term-of-service",       Start::showTermOfService);
		handle( "GET /remove-account-detail", Start::showAccountRemovalDetail);
		
		handle( "GET /service-refresh-cache",     UserService::refreshCache);
		
		handle("POST /service-test-upload-photo", TestService::handleUpload);
		
		// System-Wide User Management
		// handle( "GET /manage-user",  SystemHandler.manageUser);
		
		// In-Shop User Management
		// handle( "GET /manage-staff", StaffHandler.manage);
		
		handle( "GET /empty", Start::showEmpty);
		
		addFallback(Start::showUser);
		addFallback(Start::showServiceError);
		addFallback(Start::showError);
	}
	
	static Object
	showEmpty(Context context) {
		var model = createModel(context);
		return Freemarker.render("/web/empty.html", model);
	}
	
	static Object
	languageFilter(Context context) {	
		HashSet<String> languages = Storage.listLanguageAlias();
	
		String verb = context.request.getMethod();
		String uri  = context.request.getRequestURI();

		String pattern = verb + " " + uri;
		String userLanguage = Translation.defaultLanguage;
		
		User user = (User)context.getSession("user");
		if (valid(user) && valid(user.language)) {
			userLanguage = user.language;
		}
		
		uri = uri.toLowerCase();
		String[] tokens = uri.split("/");
		
		if (tokens.length <= 1) {
			context.request.setAttribute("language", userLanguage);
			context.request.setAttribute("pattern",  pattern);
			return null;
		}
		
		if (tokens.length > 1) {
			String alias = tokens[1];
			
			if (languages.contains(alias)) {
				System.out.println("Language Alias:   " + alias);
				userLanguage = alias;
				// remove from "/en-us/login" to "/login"
				String buffer = "";
				for (int i = 2; i < tokens.length; i++) {
					buffer += "/";
					buffer += tokens[i];
				}
				if (buffer.length() == 0) {
					buffer = "/";
				}
				pattern = verb + " " + buffer;
			}

			context.request.setAttribute("language", userLanguage);
			context.request.setAttribute("pattern",  pattern);

			System.out.println("Original Pattern: " + verb + " " + uri);
			System.out.println("Filtered Pattern: " + pattern);
			System.out.println("User Language:    " + userLanguage);
		}
		
		return null; // Not important
	}
	
	static HashMap<String, Object>
	createModel(Context context) {
		String language = (String)context.request.getAttribute("language");
		var translate = Translation.getTranslation(language);
		
		User user = (User)context.getSession("user");
		
		HashMap<String, Object> model = new HashMap<>();
		model.put("user",      user);
		model.put("translate", translate);
		model.put("language",  language);
		return model;
	}
	
	// GET /
	static Object showStart(Context context) {
		var model = createModel(context);
		return Freemarker.render("/web/show-start.html", model);
	}
	
	// GET /main
	static Object showMain(Context context) {
		var model = createModel(context);
		return Freemarker.render("/web/show-main.html", model);
	}
	
	// GET /privacy-policy
	static Object showPrivacyPolicy(Context context) {
		var model = createModel(context);
		model.put("supportEmail", Storage.getSupportEmail());
		return Freemarker.render("/web/privacy-policy.html", model);
	}
	
	// GET /term-of-service
	static Object showTermOfService(Context context) {
		var model = createModel(context);
		return Freemarker.render("/web/term-of-service.html", model);
	}
	
	// GET /remove-account-detail
	static Object showAccountRemovalDetail(Context context) {
		var model = createModel(context);
		model.put("fullWebSite", Storage.getFullWebSite());
		return Freemarker.render("/web/remove-account-detail.html", model);
	}
		
	/**
	 * Notifies message to user.
	 * 
	 * GET /notify?message=You've successfully change password
	 */
	static Object notify(Context context) {
		String message = context.query("message");
		if (message == null) {
			message = "";
		}
		
		var model = createModel(context);
		model.put("message", message);
		
		return Freemarker.render("/web/notify.html", model);
	}
	
	static Object showError(Context context) {
		var model = createModel(context);
		String buffer = Freemarker.render("/web/error.html", model);
		/*
		try {
			context.response.sendError(404, "Not Found");
		} catch (Exception e) { }
		*/
		return buffer;
	}
	
	// URL: /user or /language/user
	static Object showUser(Context context) {
		String alias = context.request.getRequestURI();
		System.out.println("Alias " + alias);
		if (alias == null) return null;
		
		HashSet<String> languages = Storage.listLanguageAlias();
		
		String [] tokens = alias.split("/");
		
		User targetUser = null;
		
		// URL pattern can be /shop or /en-us/shop
		String userLanguage = Translation.defaultLanguage;
		
		if (tokens.length == 3) {
			boolean found = languages.contains(tokens[1]);
			if (found == true) {
				userLanguage = tokens[1];
				targetUser = Storage.getUserByAlias(tokens[2]);
			}
		}
		
		if (tokens.length == 2) {
			targetUser = Storage.getUserByAlias(tokens[1]);
		}
		
		if (targetUser == null) return null; // Skip this handler
		
		System.out.println("Visiting User: " + targetUser.signature);
		System.out.println("User Language: " + userLanguage);
		
		var model = createModel(context);
		model.put("target", targetUser);
		return Freemarker.render("/web/show-user.html", model);
	}
	
	// URL: /service-xxx
	static Object showServiceError(Context context) {
		String alias = context.request.getRequestURI();
		if (alias == null) return null;
		
		if (alias.startsWith("/service")) {
			return Tool.createError("service-not-found");
		}
	
		return null; // Skip this subprogram
	}
	
}

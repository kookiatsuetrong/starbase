import web.framework.Context;
import web.framework.Freemarker;
import web.framework.Redirect;
import static web.framework.Rockstar.*;

		
class SampleModule extends Module {
	
	@Override
	void start() {
		System.out.println("This is a sample module");
		
		handle("GET /test-module", SampleModule::test);
		handle("GET /test-web",    SampleModule::showHome);
	}
	
	static String VIEW_PREFIX = "/modules/sample";
	
	static Object test(Context context) {	
		return "This is a test message from the SampleModule";
	}
	
	static Object showHome(Context context) {
		
		/*
		User user = (User)context.getSession("user");
		String language = (String)context.request.getAttribute("language");
		if (valid(user)) return new Redirect("/" + language + "/profile");
		*/
		
		var model = Start.createModel(context);
		return Freemarker.render(VIEW_PREFIX + "/web/sample.html", model);
	}
	
	static boolean valid(Object e) {
		return e != null;
	}
	
}


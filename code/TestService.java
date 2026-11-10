import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import web.framework.Context;

class TestService {
	
	/*
	This is an example of file upload in JSON format.
	The data will be in the following format:

		{ "data": [{"type": "image/jpeg",
					"content": "data:image/png;base64,iVBO..."},
				   {"type": "image/jpeg",
					"content": "data:image/png;base64,iVBO..."},
				   {"type": "image/jpeg",
					"content": "data:image/png;base64,iVBO..."}
				]
		}
	
	The URL is: POST /service-test-upload-photo
	
	An example of javascript to call this web service:
	
			<script src="/uploader.js"></script>
			<script>
				var targetElement = document.querySelector(".file-upload")
				var uploader = new SinglePhotoUploader()
				uploader.setElement(targetElement)
				
				uploader.setText("Drop File(s)")
				uploader.setType("image/*")
				uploader.setMultiple(true)
				uploader.setResize(true)
				uploader.setWidth(400)
				uploader.setSquare(true)
				
				async function uploadPhoto() {
					var location = "/service-test-upload-photo"
					var result = await uploader.uploadPhoto(location)
					console.log(result)
					// window.location.href = "/test-upload.html"
				}
			</script>
	
	*/

	static Object
	handleUpload(Context context) {
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

				String path = context.request.getServletContext()
											.getRealPath("");

				// The content is starting with:
				// PNG  --> "data:image/png;base64,iVBORw0K"
				// JPG  --> "data:image/jpeg;base64,/9j/4AA"
				// WEBP --> "data:image/webp;base64,UklGRm7"
					
				String[] items = content.split(",");
				if (items.length == 2) {
					byte[] array = java.util.Base64.getDecoder()
											.decode(items[1]);
					
					String extension = ".txt";
					if ("image/jpg" .equals(type)) extension = ".jpg";
					if ("image/jpeg".equals(type)) extension = ".jpg";
					if ("image/webp".equals(type)) extension = ".webp";
					if ("image/png" .equals(type)) extension = ".png";
					
					String fileName = path + "uploaded/user-temporary-" +
												i + extension;

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
	
}

/*
String path = context.request.getServletContext().getRealPath("");

String type    = context.request.getParameter("type");
String content = context.request.getParameter("content");
// System.out.println(content);
System.out.println(type);

if (content == null) {
	content = ",";
}

// the data starts with data:image/png;base64,iVBO...
// then split with the first ","
var items = content.split(",");

byte[] data = java.util.Base64.getDecoder().decode(items[1]);

String fileName = path + "uploaded/user-temporary.png";

System.out.println("Saving " + fileName);
try {
	Files.write(Paths.get(fileName), data);
} catch (Exception e) { 
	System.out.println(e);
}
*/

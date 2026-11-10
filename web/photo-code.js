
/*

Sample Code in HTML

createPhotoCode()

async function createPhotoCode() {
	var location = "/service-create-photo-code"
	var element = document.getElementById("photo-code")
	var code = new PhotoCode(element)
	var result = await code.getPhotoCode(location)
	if (result.result == "OK") {
		var image = document.createElement("img")
		image.src = "data:image/png;base64," + result.photo
		element.appendChild(image)
	}
}

*/

class PhotoCode {
	
	constructor(element) {
		this.element = element
	}
	
	async getPhotoCode(location) {
		var response = await fetch(location)
		var result   = await response.json()
		return result
	}
}

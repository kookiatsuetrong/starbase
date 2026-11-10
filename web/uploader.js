
/*
	Sample Usage:

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
		// console.log(result)
		// window.location.href = "/test-upload.html"
	}

 */

class SinglePhotoUploader {
	
	element = document.createElement("section")
	text = "File Uploader"
	type = "image/*"
	single = true
	square = false
	resize = false
	width = 800
	
	files = [ ]

	setElement(e) {
		this.element = e
		this.element.innerText = this.text

		this.element.style.height        = "2.25rem"
		this.element.style.paddingTop    = "1rem"
		this.element.style.display       = "block"
		this.element.style.background    = "#eee"
		this.element.style.border        = ".15rem solid #eee"
		this.element.style.borderRadius  = "0.35rem"
		this.element.style.textAlign     = "center"

		this.element.ondrop      = e => this.onDrop(e)
		this.element.onclick     = e => this.onClick(e)
		this.element.ondragover  = e => this.onDragOver(e)
		this.element.ondragleave = e => this.onDragLeave(e)
		this.element.ondragend   = e => this.onDragEnd(e)
	}
	
	setText(t) {
		this.text = t
		this.element.innerText = this.text
	}
	
	setType(t) {
		this.type = t
	}
	
	setMultiple(b) {
		if (b == true) this.single = false
		if (b == false) this.single = true
	}
	
	setSquare(flag) {
		this.square = flag
	}
	
	setResize(flag) {
		this.resize = flag
	}
	
	setWidth(w) {
		this.width = w
	}
	
	onDragOver(event) {
		event.preventDefault()
		
		event.target.style.border = ".15rem dashed #888"
		event.target.style.background = "#eee"
		event.target.style.caret = "pointer"
		event.target.innerText = "Drop Here"
	}

	onDragLeave(event) {
		event.preventDefault()
		event.target.style.border = ".15rem solid #eee"
		event.target.style.background = "#eee"
		event.target.innerText = this.text
	}

	onDragEnd(event) {
		event.preventDefault()
		event.target.style.border = ".15rem solid #eee"
		event.target.style.background = "#eee"
	}

	onDrop(event) {
		event.preventDefault()
		event.target.style.border = ".15rem solid #eee"
		this.prepareFiles(event.dataTransfer.files)
	}

	onClick() {
		var chooser = document.createElement("input")
		chooser.type     = "file"
		chooser.multiple = !this.single
		chooser.accept   = this.type
		chooser.capture  = true
		chooser.onchange = event => this.prepareFiles(event.target.files)
		chooser.click()
	}
	
	prepareFiles(data) {
		var accept = true
		if (this.single == true && this.files.length > 0) {
			accept = false
		}
		for (var f of data) {
			if (accept) {
				this.files.push(f)
			}
			if (this.single == true) {
				accept = false
			}
		}
	   
		var buffer = "1 File"
		if (this.files.length > 1) {
			buffer = this.files.length + " Files"
		}
		this.setText(buffer)
	}
	
	readFile(file) {
		return new Promise( (resolve) => {
			var reader = new FileReader()
			reader.onloadend = event => {
				var content = reader.result
				resolve(content)
			}
			reader.readAsDataURL(file)
		})
	}

	resizePhoto(content, type) {
		return new Promise( (resolve) => {
			var preview = document.createElement("img")
			preview.src = content
			var buffer = document.createElement("img")
			buffer.addEventListener("load", event => {
				// event.preventDefault()
				var canvas = document.createElement("canvas")
				var context = canvas.getContext("2d")
				canvas.width = this.width
				var ratio = preview.height / preview.width
				canvas.height = canvas.width * ratio
				context.drawImage(preview, 0, 0, 
									canvas.width, canvas.height)
				
				// console.log("Scale to ", canvas.width, canvas.height)
				
				var lossy = false
				if (type == "image/jpg"  || 
					type == "image/jpeg" ||
					type == "image/webp") {
					lossy = true
				}
				
				if (lossy == true) {
					var small = canvas.toDataURL(type, 0.90)
					resolve(small)
				}
				
				if (lossy == false) {
					var small = canvas.toDataURL()
					resolve(small)
				}
				
			})
			buffer.src = preview.src
		})
	}

	async squarizePhoto(content) {
		return new Promise( resolve => {
			var preview = new Image()
			preview.onload = function() {

				if (preview.width == preview.height) {
					resolve(content)
				}

				if (preview.width < preview.height) {
					// portrait
					var size = preview.width
					var start = (preview.height - size) / 2
					start = parseInt(start)
					var canvas = document.createElement("canvas")
					canvas.width = size
					canvas.height = size
					
					// console.log("Resizing to ", size)
					
					var context = canvas.getContext("2d")
					context.drawImage(preview, 0, start,
								size, size,
								0, 0, size, size)
					var square = canvas.toDataURL()
					resolve(square)
				}

				if (preview.width > preview.height) {
					// landscape
					var size = preview.height
					var start = (preview.width - size) / 2
					start = parseInt(start)
					var canvas = document.createElement("canvas")
					canvas.width = size
					canvas.height = size
					
					// console.log("Resizing to ", size)
					
					var context = canvas.getContext("2d")
					context.drawImage(preview, start, 0,
								size, size,
								0, 0, size, size)
					var square = canvas.toDataURL()
					resolve(square)
				}

			}

			preview.src = content
		})
	}

	async upload(location) {

		var data = [ ]

		for (var f of this.files) {
			
			var detail = { }
			detail.name = f.name
			detail.size = f.size
			detail.type = f.type
			
			var content = await this.readFile(f)
			 
			if (detail.type.startsWith("image")) {
				if (this.square == true) {
					content = await this.squarizePhoto(content)
				}
				if (this.resize == true) {
					content = await this.resizePhoto(content, detail.type)
				}
			}
			
			detail.content = content
			detail.contentSize = detail.content.length
			data.push(detail)
			
			if (this.single == true) {
				// use only first photo
				break
			}
		}

		var all = { }
		all.data = data
		
		var payload = { }
		payload.method = "POST"
		payload.headers = { }
		payload.headers["Content-Type"] = "application/json"
		payload.body = JSON.stringify(all)
		
		var response = await fetch(location, payload)
		var result = await response.json()
		return result
	}
	
}

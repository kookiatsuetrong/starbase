
var displaying = [ ]

function keyCapture(event) {
	
	console.log(event)
	
	if (event.key == "Escape") {
		event.stopPropagation()
		event.preventDefault()
		document.body.removeEventListener("keydown", keyCapture)
		for (var dialog of displaying) {
			if (typeof dialog.handleClose == "function") {
				dialog.handleClose()
			}
			if (typeof dialog.cancelCallback == "function") {
				dialog.cancelCallback()
			}
		}
		
		for (var i = 0; i < displaying.length; i++) {
			displaying.pop()
		}
	}
	
	if (event.key == "Enter") {
		event.stopPropagation()
		event.preventDefault()
		document.body.removeEventListener("keydown", keyCapture)
		for (var dialog of displaying) {
			if (typeof dialog.okCallback == "function") {
				dialog.okCallback()
				continue
			}
			if (typeof dialog.handleClose == "function") {
				dialog.handleClose()
				continue
			}
		}
		
		for (var i = 0; i < displaying.length; i++) {
			displaying.pop()
		}
	}
	
	// TODO: Handle Tab key
	
}

class DialogStyle {
	static get darkBackground() {
		return "rgba(0,0,0, .75)"
		// return "rgba(255, 255, 255, .75)"
	}
}

var dialog = { }
dialog.translate = { }

dialog.translate["yes"] = { }
dialog.translate["yes"]["en-us"] = "Yes"
dialog.translate["yes"]["th-th"] = "ใช่"

dialog.translate["no"] = { }
dialog.translate["no"]["en-us"] = "No"
dialog.translate["no"]["th-th"] = "ไม่"

dialog.translate["close"] = { }
dialog.translate["close"]["en-us"] = "Close"
dialog.translate["close"]["th-th"] = "ปิด"

dialog.translate["cancel"] = { }
dialog.translate["cancel"]["en-us"] = "Cancel"
dialog.translate["cancel"]["th-th"] = "ยกเลิก"

dialog.translate["save"] = { }
dialog.translate["save"]["en-us"] = "Save"
dialog.translate["save"]["th-th"] = "บันทึก"

dialog.translate["notification"] = { }
dialog.translate["notification"]["en-us"] = "Notification"
dialog.translate["notification"]["th-th"] = "แจ้งเตือน"

dialog.translate["confirmation"] = { }
dialog.translate["confirmation"]["en-us"] = "Confirmation"
dialog.translate["confirmation"]["th-th"] = "คำถามยืนยัน"

dialog.translate["editor"] = { }
dialog.translate["editor"]["en-us"] = "Text Editor"
dialog.translate["editor"]["th-th"] = "แก้ไขข้อความ"

dialog.translate["choose-language"] = { }
dialog.translate["choose-language"]["en-us"] = "Choose Language"
dialog.translate["choose-language"]["th-th"] = "เลือกภาษา"

class Translate {
	
	static get(text, language) {

		var result = dialog.translate[text][language]
		if (result == null) {
			result = dialog.translate[text]["en-us"]
		}
		if (result == null) {
			result = text
		}
		return result
	}
}

class NotificationDialog extends HTMLElement {
	
	constructor() {
		super()
	}
	
	language = "en-us"
	
	connectedCallback() {
		var closeButton = document.createElement("button")
		closeButton.innerHTML = Translate.get("close", this.language)
		
		var buttonContainer = document.createElement("section")
		buttonContainer.id = "bottom"
		buttonContainer.appendChild(closeButton)

		var title = document.createElement("h3")
		title.innerHTML = Translate.get("notification", this.language)
		
		var text = document.createElement("p")
		text.innerHTML = ""
		
		var dialog = document.createElement("section")
		dialog.style.position     = "relative"
		dialog.style.background   = "white"
		
		dialog.appendChild(title)
		dialog.appendChild(text)
		dialog.appendChild(buttonContainer)
		
		this.appendChild(dialog)
		
		title.style.color      = "white"
		title.style.background = "var(--brand-color)"
		title.style.padding    = ".25rem .5rem .35rem .5rem"
		
		text.style.padding = "1rem"
		
		buttonContainer.style.padding = "1rem"
		buttonContainer.style.textAlign = "right"
		
		dialog.style.marginLeft   = "4rem"
		dialog.style.marginRight  = "4rem"
		dialog.style.margin       = "0 auto"
		dialog.style.marginTop    = "calc(4rem + " + window.scrollY + "px)"

		dialog.style.maxWidth     = "25rem"
		dialog.style.width        = "calc(100dvw - 8rem)"

		dialog.style.minHeight    = "6rem"
		// dialog.style.padding      = "1rem"
		dialog.style.borderRadius = ".75rem"
		dialog.style.border       = ".2rem solid var(--brand-color)"
		
		this.style.display        = "none"
		this.style.position       = "absolute"
		this.style.background     = DialogStyle.darkBackground
		this.style.width          = "100dvw"
		this.style.top            = "0"
		this.style.height         = "100%"
		this.style.bottom         = "0"
		
		dialog.addEventListener("click", e => {
			e.stopPropagation()
		})
		

		var button = this.querySelector("button")
		button.addEventListener("click", e => {
			this.handleClose(e)
		})

		this.addEventListener("click", e => {
			this.handleClose(e)
		})
	}
	
}

customElements.define("notification-dialog", NotificationDialog)

class Notification {

	element = null

	constructor() {
		this.element = document.createElement("notification-dialog")
		document.body.appendChild(this.element)
		this.element.handleClose = e => this.handleClose(e)
		
		document.body.addEventListener("keydown", keyCapture)
		displaying.push(this)
	}

	handleClose(event) {
		document.body.removeEventListener("keydown", keyCapture)
		for (var i = 0; i < displaying.length; i++) {
			displaying.pop()
		}
		
		document.body.removeChild(this.element)
		this.resolve(false)
	}
	
	resolve = null

	show() {
		this.element.style.display = "block"
		return new Promise( resolve => {
			this.resolve = resolve
		})
	}
	
	setText(message) {
		var text = this.element.querySelector("p")
		text.innerHTML = message
	}
	
	setLanguage(language) {
		this.element.language = language
		
		var title = this.element.querySelector("h3")
		title.innerHTML = Translate.get("notification", language)
		
		var closeButton = this.element.querySelector("button")
		closeButton.innerHTML = Translate.get("close", language)
	}

}

class TextEditorDialog extends HTMLElement {
	
	constructor() {
		super()
	}
	
	language = "en-us"
	
	connectedCallback() {

		var yesButton = document.createElement("button")
		yesButton.id = "yes"
		yesButton.innerHTML = Translate.get("save", this.language)
		
		var noButton = document.createElement("button")
		noButton.id = "no"
		noButton.innerHTML = Translate.get("cancel", this.language)
		
		var buttonContainer = document.createElement("section")
		buttonContainer.id = "bottom"
		buttonContainer.appendChild(yesButton)
		buttonContainer.appendChild(noButton)

		var area = document.createElement("textarea")
		
		var title = document.createElement("h3")
		title.innerHTML = Translate.get("editor", this.language)
		
		var dialog = document.createElement("section")
		dialog.style.position     = "relative"
		dialog.style.background   = "white"
		
		dialog.appendChild(title)
		dialog.appendChild(area)
		dialog.appendChild(buttonContainer)
		
		this.appendChild(dialog)

		noButton.style.marginLeft = ".5rem"
		
		// var area = this.querySelector("textarea")
		area.style.marginTop = ".5rem"
		area.style.height = "calc(100dvh - 16rem)"
		area.style.marginBottom = "1rem"
		
		dialog.style.marginLeft   = "4rem"
		dialog.style.marginRight  = "4rem"
		dialog.style.margin       = "0 auto"
		dialog.style.marginTop    = "calc(4rem + " + window.scrollY + "px)"

		dialog.style.maxWidth     = "90dvw"
		dialog.style.width        = "calc(100dvw - 8rem)"

		dialog.style.minHeight    = "6rem"
		dialog.style.padding      = "1rem"
		dialog.style.borderRadius = ".75rem"
		dialog.style.border       = ".2rem solid var(--brand-color)"
		
		this.style.display        = "none"
		this.style.position       = "absolute"
		this.style.background     = DialogStyle.darkBackground
		this.style.width          = "100dvw"
		this.style.top            = "0"
		this.style.height         = "100%"
		this.style.bottom         = "0"
		
		dialog.addEventListener("click", e => {
			e.stopPropagation()
		})

		var yes = this.querySelector("#yes")
		yes.addEventListener("click", e => {
			this.okCallback(e)
			e.stopPropagation()
		})

		var no = this.querySelector("#no")
		no.addEventListener("click", e => {
			this.cancelCallback(e)
		})

		this.addEventListener("click", e => {
			this.cancelCallback(e)
		})
	}
}

customElements.define("text-editor-dialog", TextEditorDialog)

class TextEditor {

	element = null

	constructor() {
		this.element = document.createElement("text-editor-dialog")
		document.body.appendChild(this.element)
		this.element.okCallback     = e => this.okCallback(e)
		this.element.cancelCallback = e => this.cancelCallback(e)
		
		var area = this.element.querySelector("textarea")
		area.focus()
		area.scrollIntoView()
	}

	okCallback(e) {
		this.element.style.display = "none"
		document.body.removeChild(this.element)
		this.resolve(true)
	}

	cancelCallback(e) {
		this.element.style.display = "none"
		document.body.removeChild(this.element)
		this.resolve(false)
	}

	resolve = null

	show() {
		this.element.style.display = "block"
		return new Promise( resolve => {
			this.resolve = resolve
		})
	}
	
	setText(message) {
		var area = this.element.querySelector("textarea")
		area.value = message
		area.focus()
		area.scrollIntoView()
	}
	
	getText() {
		var area = this.element.querySelector("textarea")
		return area.value
	}
	
	setLanguage(language) {
		this.element.language = language
		
		var yesButton = this.element.querySelector("#yes")
		yesButton.innerHTML = Translate.get("save", language)

		var noButton = this.element.querySelector("#no")
		noButton.innerHTML = Translate.get("close", language)

		var title = this.element.querySelector("h3")
		title.innerHTML = Translate.get("editor", language)
	}
}

class ConfirmationDialog extends HTMLElement {

	constructor() {
		super()
	}
	
	language = "en-us"

	connectedCallback() {
		var yesButton = document.createElement("button")
		yesButton.id = "yes"
		yesButton.innerHTML = Translate.get("yes", this.language)
		
		var noButton  = document.createElement("button")
		noButton.id = "no"
		noButton.innerHTML  = Translate.get("no", this.language)
		
		var buttonContainer = document.createElement("section")
		buttonContainer.appendChild(yesButton)
		buttonContainer.appendChild(noButton)
		
		var dialog = document.createElement("section")
		
		var title  = document.createElement("h3")
		title.innerHTML = Translate.get("confirmation", this.language)
		
		var text   = document.createElement("p")
		
		dialog.appendChild(title)
		dialog.appendChild(text)
		dialog.appendChild(buttonContainer)
		
		this.appendChild(dialog)

		text.style.padding   = "1rem"

		noButton.style.marginLeft = "0.5rem"
		buttonContainer.style.textAlign = "right"
		buttonContainer.style.padding = "1rem"
		
		dialog.style.position     = "relative"
		dialog.style.background   = "white"

		dialog.style.marginLeft   = "4rem"
		dialog.style.marginRight  = "4rem"
		dialog.style.margin       = "0 auto"
		dialog.style.marginTop    = "calc(4rem + " + window.scrollY + "px)"

		dialog.style.maxWidth     = "25rem"
		dialog.style.width        = "calc(100dvw - 8rem)"
		dialog.style.minHeight    = "6rem"
		dialog.style.borderRadius = ".75rem"
		dialog.style.border       = ".2rem solid var(--brand-color)"
		
		title.style.color      = "white"
		title.style.background = "var(--brand-color)"
		title.style.padding    = ".25rem .5rem .35rem .5rem"
		
		this.style.display        = "none"
		this.style.position       = "absolute"
		this.style.background     = DialogStyle.darkBackground
		this.style.width          = "100dvw"
		this.style.height         = "100%"
		this.style.bottom         = "0"


		dialog.addEventListener("click", e => {
			e.stopPropagation()
		})

		yesButton.addEventListener("click", e => {
			this.okCallback(e)
			e.stopPropagation()
		})

		noButton.addEventListener("click", e => {
			this.cancelCallback(e)
		})

		this.addEventListener("click", e => {
			this.cancelCallback(e)
		})
	}

}

customElements.define("confirmation-dialog", ConfirmationDialog)

class Confirmation {

	element = null

	constructor() {
		this.element = document.createElement("confirmation-dialog")
		document.body.appendChild(this.element)
		this.element.okCallback     = e => this.okCallback(e)
		this.element.cancelCallback = e => this.cancelCallback(e)
		
		document.body.addEventListener("keydown", keyCapture)
		displaying.push(this)
	}

	okCallback(e) {
		document.body.removeEventListener("keydown", keyCapture)
		for (var i = 0; i < displaying.length; i++) {
			displaying.pop()
		}
		this.element.style.display = "none"
		document.body.removeChild(this.element)
		this.resolve(true)
	}

	cancelCallback(e) {
		document.body.removeEventListener("keydown", keyCapture)
		for (var i = 0; i < displaying.length; i++) {
			displaying.pop()
		}
		this.element.style.display = "none"
		document.body.removeChild(this.element)
		this.resolve(false)
	}

	resolve = null

	show() {
		this.element.style.display = "block"
		return new Promise( resolve => {
			this.resolve = resolve
		})
	}

	setMessage(s) {
		var t = this.element.querySelector("p")
		t.innerHTML = s
	}
	
	setLanguage(language) {
		this.language = language
		
		var yesButton       = this.element.querySelector("#yes")
		yesButton.innerHTML = Translate.get("yes", language)

		var noButton        = this.element.querySelector("#no")
		noButton.innerHTML  = Translate.get("no", language)

		var title       = this.element.querySelector("h3")
		title.innerHTML = Translate.get("confirmation", language)
	}

}

class LanguageDialog extends HTMLElement {
	
	constructor() {
		super()
	}
	
	language = "en-us"
	
	connectedCallback() {
		var closeButton = document.createElement("button")
		closeButton.innerHTML = Translate.get("close", this.language)
		
		var buttonContainer = document.createElement("section")
		buttonContainer.id = "bottom"
		buttonContainer.appendChild(closeButton)

		var title = document.createElement("h3")
		title.innerHTML = Translate.get("choose-language", this.language)
		
		var items = document.createElement("p")
		
		var dialog = document.createElement("section")
		dialog.style.position     = "relative"
		dialog.style.background   = "white"
		
		dialog.appendChild(title)
		dialog.appendChild(items)
		dialog.appendChild(buttonContainer)
		
		this.appendChild(dialog)
		
		title.style.color      = "white"
		title.style.background = "var(--brand-color)"
		title.style.padding    = ".25rem .5rem .35rem .5rem"
		
		items.style.padding   = "1rem"
		items.style.display   = "grid"
		items.style.gridTemplateColumns = "1fr 1fr"
		items.style.columnGap = "1rem"
		items.style.height    = "8rem"
		items.style.overflowY = "scroll"
		
		buttonContainer.style.padding = "1rem"
		buttonContainer.style.textAlign = "right"
		
		dialog.style.marginLeft   = "4rem"
		dialog.style.marginRight  = "4rem"
		dialog.style.margin       = "0 auto"
		dialog.style.marginTop    = "calc(4rem + " + window.scrollY + "px)"

		dialog.style.maxWidth     = "25rem"
		dialog.style.width        = "calc(100dvw - 8rem)"

		dialog.style.minHeight    = "6rem"
		dialog.style.borderRadius = ".75rem"
		dialog.style.border       = ".2rem solid var(--brand-color)"
		
		this.style.display        = "none"
		this.style.position       = "absolute"
		this.style.background     = DialogStyle.darkBackground
		this.style.width          = "100dvw"
		this.style.top            = "0"
		this.style.height         = "100%"
		this.style.bottom         = "0"
		
		dialog.addEventListener("click", e => {
			e.stopPropagation()
		})

		var button = this.querySelector("button")
		button.addEventListener("click", e => {
			this.handleClose(e)
		})

		this.addEventListener("click", e => {
			this.handleClose(e)
		})
	}
	
}

customElements.define("language-dialog", LanguageDialog)

class LanguageSelection {

	element = null

	constructor() {
		this.element = document.createElement("language-dialog")
		document.body.appendChild(this.element)
		this.element.handleClose = e => this.handleClose(e)
		
		document.body.addEventListener("keydown", keyCapture)
		displaying.push(this)
	}
	/*
	handleClose(event) {
		document.body.removeEventListener("keydown", keyCapture)
		for (var i = 0; i < displaying.length; i++) {
			displaying.pop()
		}
		document.body.removeChild(this.element)
		this.resolve(false)
	}
	*/
	resolve = null

	show() {
		this.element.style.display = "block"
		return new Promise( resolve => {
			this.resolve = resolve
		})
	}
	
	setLanguages(languages) {
		this.languages = languages
		this.languages.sort( (a, b) => {
											if (a.name < b.name) return -1
											if (a.name > b.name) return +1
											return 0
										})
		var items = this.element.querySelector("p")
		for (var e of this.languages) {
			var a = document.createElement("a")
			var tokens = e.name.split("(")
			var buffer = ""
			buffer += tokens[0]
			buffer += "<span style='font-size:.75rem; color: #888'>"
			buffer += "(" + tokens[1]
			buffer += "</span>"
			a.innerHTML = buffer
			a.setAttribute("data-element", e.name)
			a.href = "javascript:"
			a.addEventListener("click", ev => this.chooseLanguage(ev))
			items.appendChild(a)
		}
	}
	
	language = "English (United State)"
	
	chooseLanguage(e) {
		var element = e.target
		var languageName = element.getAttribute("data-element")
		if (languageName == null) {
			element = element.parentElement
			languageName = element.getAttribute("data-element")
		}
		this.language = languageName
		this.element.style.display = "none"
		document.body.removeChild(this.element)
		this.resolve(true)
	}

	handleClose(e) {
		document.body.removeEventListener("keydown", keyCapture)
		for (var i = 0; i < displaying.length; i++) {
			displaying.pop()
		}
		// console.log("Choose Language Cancel")
		this.element.style.display = "none"
		document.body.removeChild(this.element)
		this.resolve(false)
	}
	
	setLanguage(language) {
		this.element.language = language
		
		var closeButton = this.querySelector("button")
		closeButton.innerHTML = Translate.get("close", language)

		var title = this.querySelector("h3")
		title.innerHTML = Translate.get("choose-language", language)
	}
	
}


/*
class AcceptCallDialog extends HTMLElement {

	constructor() {
		super()
	}
	
	language = "en-us"

	connectedCallback() {
		var yesButton = document.createElement("button")
		yesButton.id = "yes"
		yesButton.innerHTML = Translate.get("yes", this.language)
		
		var noButton  = document.createElement("button")
		noButton.id = "no"
		noButton.innerHTML  = Translate.get("no", this.language)
		
		var buttonContainer = document.createElement("section")
		buttonContainer.appendChild(yesButton)
		buttonContainer.appendChild(noButton)
		
		var dialog = document.createElement("section")
		
		var title       = document.createElement("h3")
		title.innerHTML = Translate.get("confirmation", this.language)
		
		var text        = document.createElement("p")
		
		dialog.appendChild(title)
		dialog.appendChild(text)
		dialog.appendChild(buttonContainer)
		
		this.appendChild(dialog)

		text.style.padding   = "1rem"

		noButton.style.marginLeft       = "0.5rem"
		buttonContainer.style.textAlign = "right"
		buttonContainer.style.padding   = "1rem"
		
		dialog.style.position     = "relative"
		dialog.style.background   = "white"

		dialog.style.marginLeft   = "4rem"
		dialog.style.marginRight  = "4rem"
		dialog.style.margin       = "0 auto"
		dialog.style.marginTop    = "calc(4rem + " + window.scrollY + "px)"

		dialog.style.maxWidth     = "25rem"
		dialog.style.width        = "calc(100dvw - 8rem)"
		dialog.style.minHeight    = "6rem"
		dialog.style.borderRadius = ".75rem"
		dialog.style.border       = ".2rem solid var(--brand-color)"
		
		title.style.color         = "white"
		title.style.background    = "var(--brand-color)"
		title.style.padding       = ".25rem .5rem .35rem .5rem"
		title.style.marginTop     = "0"
		
		this.style.display        = "none"
		this.style.position       = "absolute"
		this.style.background     = DialogStyle.darkBackground
		this.style.width          = "100dvw"
		this.style.height         = "100%"
		this.style.bottom         = "0"
		
		text.style.color          = "#333"

		dialog.addEventListener("click", e => {
			e.stopPropagation()
		})

		yesButton.addEventListener("click", e => {
			this.okCallback(e)
			e.stopPropagation()
		})

		noButton.addEventListener("click", e => {
			this.cancelCallback(e)
		})

		this.addEventListener("click", e => {
			this.cancelCallback(e)
		})
	}

}

customElements.define("accept-call-dialog", AcceptCallDialog)

class AcceptCall {

	element = null

	constructor() {
		this.element = document.createElement("accept-call-dialog")
		document.body.appendChild(this.element)
		this.element.okCallback     = e => this.okCallback(e)
		this.element.cancelCallback = e => this.cancelCallback(e)
		
		document.body.addEventListener("keydown", keyCapture)
		displaying.push(this)
	}

	okCallback(e) {
		document.body.removeEventListener("keydown", keyCapture)
		for (var i = 0; i < displaying.length; i++) {
			displaying.pop()
		}
		
		this.element.style.display = "none"
		document.body.removeChild(this.element)
		
		this.resolve(true)
	}

	cancelCallback(e) {
		document.body.removeEventListener("keydown", keyCapture)
		for (var i = 0; i < displaying.length; i++) {
			displaying.pop()
		}
		
		this.element.style.display = "none"
		document.body.removeChild(this.element)
		
		this.resolve(false)
	}

	resolve = null

	show() {
		this.element.style.display = "block"
		return new Promise( resolve => {
			this.resolve = resolve
		})
	}

	setMessage(s) {
		var t = this.element.querySelector("p")
		t.innerHTML = s
	}
	
	setLanguage(language) {
		this.language = language
		
		var yesButton       = this.element.querySelector("#yes")
		yesButton.innerHTML = Translate.get("yes", language)

		var noButton        = this.element.querySelector("#no")
		noButton.innerHTML  = Translate.get("no", language)

		var title       = this.element.querySelector("h3")
		title.innerHTML = Translate.get("confirmation", language)
	}

}
*/

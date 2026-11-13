/*

Example of the settings file:

{
	"password-minimum-length":  8,
	"password-maximum-length": 20,
	"password-numeric":        true,
	"password-uppercase":      true,
	"password-lowercase":      false,
	"password-symbol":         false,
	
	"last-update": "2026-11-01"
}


or 

var firstElement  = document.querySelector("#current")
var secondElement = document.querySelector("#password")
var thirdElement  = document.querySelector("#confirm")

var secondPassword = new Password(secondElement)
var thirdPassword = new Password(thirdElement)

secondPassword.setConfirmPassword(thirdPassword)
thirdPassword.setConfirmPassword(secondPassword)

*/

class Password {

	element     // or = document.createElement("input")
	identifier = parseInt(Math.random() * 1000000)
	rules   = { }
	
	passwordMinimumLength =  8
	passwordMaximumLength = 20
	passwordNumeric = true
	passwordUpperCase = true
	passwordLowerCase = true
	passwordSymbol = true
	
	symbol = "`~!@#$%^&*()-_+=[]{}\|;:,./<>?'\""
	
	setMinimumLength(value) {
		this.passwordMinimumLength = value
	}
	
	setMaximumLength(value) {
		this.passwordMaximumLength = value
	}
	
	setNumeric(b) {
		this.passwordNumeric = b
	}
	
	setUpperCase(b) {
		this.passwordUpperCase = b
	}
	
	setLowerCase(b) {
		this.passwordLowerCase = b
	}
	
	setSymbol(b) {
		this.passwordSymbol = b
	}
	
	setElement(e) {
		this.element = e
	}

	constructor(element, rules) {
		this.element = element
		this.rules = rules
		if (this.rules == null) {
			this.rules = { }
		}
		
		this.passwordMinimumLength = this.rules["password-minimum-length"] ||  8
		this.passwordMaximumLength = this.rules["password-maximum-length"] || 20

		this.passwordNumber    = this.rules["password-numeric"  ] || true
		this.passwordUpperCase = this.rules["password-uppercase"] || true
		this.passwordLowerCase = this.rules["password-lowercase"] || true
		this.passwordSymbol    = this.rules["password-uppercase"] || true

		this.element.addEventListener("keyup",  e => this.onKeyUp(e))
		this.element.addEventListener("change", e => this.onChange(e))
	}
	
	onKeyUp(event) {	
		this.verify()
	}
	
	onChange(event) {
		this.verify()
	}
	
	verify() {
		
		var length = this.element.value.length
		this.clearHint()
		if (length == 0) return
		
		var passed = true
		
		if (length < this.passwordMinimumLength) {
			this.createHint("password-too-short")
			passed = false
		}
		
		if (length > this.passwordMaximumLength) {
			this.createHint("password-too-long")
			passed = false
		}
		
		var text = this.element.value
		
		if (this.passwordNumber) {
			var count = 0
			for (var i = 0; i < text.length; i++) {
				if (text[i] >= '0' && text[i] <= '9') {
					count++
				}
			}
			if (count == 0) {
				this.createHint("password-has-no-digit")
				passed = false
			}
		}
		
		if (this.passwordUpperCase) {
			var count = 0
			for (var i = 0; i < text.length; i++) {
				if (text[i] >= 'A' && text[i] <= 'Z') {
					count++
				}
			}
			if (count == 0) {
				this.createHint("password-has-no-uppercase")
				passed = false
			}
		}
		
		if (this.passwordLowerCase) {
			var count = 0
			for (var i = 0; i < text.length; i++) {
				if (text[i] >= 'a' && text[i] <= 'z') {
					count++
				}
			}
			if (count == 0) {
				this.createHint("password-has-no-lowercase")
				passed = false
			}
		}
		
		if (this.passwordSymbol) {
			var count = 0
			for (var i = 0; i < text.length; i++) {
				if (this.symbol.indexOf(text[i]) >= 0) {
					count++
				}
			}
			if (count == 0) {
				this.createHint("password-has-no-symbol")
				passed = false
			}
		}
		
		if (this.confirmPassword == null) { }
		if (this.confirmPassword != null) {
			var identical = this.confirmPassword.element.value == 
							this.element.value
			
			if (identical == true) {
				// this.confirmPassword.verify()
			}
			
			if (identical == false) {
				this.createHint("password-not-identical")
				passed = false
			}			
			
			if (this.element == document.activeElement) {
				this.confirmPassword.verify()
			}
		}

		var parent = this.element.parentNode
		var correctSelector = "#password-correct-" + this.identifier
		var target = parent.querySelector(correctSelector)
		if (target == null) { }
		if (target != null) {
			this.element.parentNode.removeChild(target)
		}

		if (passed == true) {
			target = document.createElement("section")
			target.setAttribute("id", "password-correct-" + this.identifier)
			target.innerText = this.text["password-correct"]
			this.element.after(target)
			target.style.background = "#f8fff8"
			target.style.borderRadius = ".5rem"
			target.style.padding = ".5rem 1rem"
			target.style.margin = ".1rem 0 1rem 0"
			target.style.color = "#3b3"	
		}

	}
	
	clearHint() {
		var parent = this.element.parentNode
		var target = parent.querySelector("#hint-" + this.identifier)
		if (target == null) return

		var all = target.querySelectorAll("span")
		for (var e of all) {
			target.removeChild(e)
		}
		this.element.parentNode.removeChild(target)
	}
	
	createHint(text) {
		var parent = this.element.parentNode
		var target = parent.querySelector("#hint-" + this.identifier)
		if (target == null) {
			target = document.createElement("section")
			target.setAttribute("id", "hint-" + this.identifier)
			// this.element.parentNode.appendChild(target)
			this.element.after(target)
			
			target.style.background   = "#fff8f8"
			target.style.borderRadius = ".5rem"
			target.style.padding      = ".5rem 1rem"
			target.style.margin       = ".1rem 0 1rem 0"
			target.style.color        = "#b33"
		}
		
		var hint = document.createElement("span")
		hint.style.display = "block"
		hint.innerText = this.text[text]
		
		target.appendChild(hint)
	}
	
	confirmPassword = null
	
	setConfirmPassword(e) {
		this.confirmPassword = e
	}
	
	language = "identifier"
	
	setLanguage(lang) {
		this.language = lang
	}
	
	text = { }
	
	async translate() {
		this.text["password-too-short"]        = "password-too-short"
		this.text["password-too-long"]         = "password-too-long"
		this.text["password-has-no-digit"]     = "password-has-no-digit"
		this.text["password-has-no-uppercase"] = "password-has-no-uppercase"
		this.text["password-has-no-lowercase"] = "password-has-no-lowercase"
		this.text["password-has-no-symbol"]    = "password-has-no-symbol"
		this.text["password-not-identical"]    = "password-not-identical"
		this.text["password-correct"]          = "password-correct"
		
		for (var k in this.text) {
			var location = "/service-translate?language=" + this.language + 
								"&code=" + k
			var response = await fetch(location)
			var detail   = await response.json()
			if (detail.result == "OK") {
				this.text[k] = detail.text
			}
		}
	}
	
	
}

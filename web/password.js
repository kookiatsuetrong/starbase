
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

*/

class Password {

	element = document.createElement("input")
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
	
	setNumberic(b) {
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

	constructor(element, rules) {
		this.element = element
		this.rules = rules
		
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
				this.createHint("password-numeric-not-found")
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
				this.createHint("password-uppercase-not-found")
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
				this.createHint("password-lowercase-not-found")
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
				this.createHint("password-symbol-not-found")
				passed = false
			}
		}

		var target = this.element.parentNode.querySelector("#password-correct")
		if (target == null) { }
		if (target != null) {
			this.element.parentNode.removeChild(target)
		}
			
		if (passed == true) {
			target = document.createElement("section")
			target.setAttribute("id", "password-correct")
			target.innerText = "password-correct"
			this.element.after(target)
			target.style.background = "#f8fff8"
			target.style.borderRadius = ".5rem"
			target.style.padding = ".5rem 1rem"
			target.style.margin = ".1rem 0 1rem 0"
			target.style.color = "#3b3"	
		}

	}
	
	onChange(event) {
		// console.log("change", event)
	}
	
	clearHint() {
		var target = this.element.parentNode.querySelector("#hint")
		if (target == null) return

		var all = target.querySelectorAll("span")
		for (var e of all) {
			target.removeChild(e)
		}
		this.element.parentNode.removeChild(target)
	}
	
	createHint(text) {
		var target = this.element.parentNode.querySelector("#hint")
		if (target == null) {
			target = document.createElement("section")
			target.setAttribute("id", "hint")
			// this.element.parentNode.appendChild(target)
			this.element.after(target)
			
			target.style.background = "#fff8f8"
			target.style.borderRadius = ".5rem"
			target.style.padding = ".5rem 1rem"
			target.style.margin = ".1rem 0 1rem 0"
			target.style.color = "#b33"
		}
		
		var hint = document.createElement("span")
		hint.style.display = "block"
		hint.innerText = text
		
		target.appendChild(hint)
		
	}
	
}

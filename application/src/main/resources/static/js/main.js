const Toast = (function () {
    let container;

    function getContainer() {
        if (container) return container;

        container = document.createElement("div");
        container.style.cssText = `
      position: fixed;
      top: 20px;
      left: 50%;
      transform: translateX(-50%);
      z-index: 9999;
    `;

        if (document.body) {
            document.body.appendChild(container);
        } else {
            document.addEventListener("DOMContentLoaded", () => {
                document.body.appendChild(container);
            });
        }

        return container;
    }

    class ToastMessage {
        constructor(message, type) {
            this.message = message;
            this.type = type;
            this.element = null;
            this.create();
        }

        create() {
            this.element = document.createElement("div");
            this.element.textContent = this.message;
            this.element.style.cssText = `
        background-color: ${this.type === "success" ? "#4CAF50" : "#F44336"};
        color: white;
        padding: 12px 24px;
        border-radius: 4px;
        margin-bottom: 10px;
        box-shadow: 0 2px 5px rgba(0,0,0,0.2);
        opacity: 0;
        transition: opacity 0.3s ease-in-out;
      `;
            getContainer().appendChild(this.element);

            setTimeout(() => {
                this.element.style.opacity = "1";
            }, 10);

            setTimeout(() => {
                this.remove();
            }, 3000);
        }

        remove() {
            this.element.style.opacity = "0";
            setTimeout(() => {
                const parent = this.element.parentNode;
                if (parent) {
                    parent.removeChild(this.element);
                }
            }, 300);
        }
    }

    function showToast(message, type) {
        if (document.readyState === "loading") {
            document.addEventListener("DOMContentLoaded", () => {
                new ToastMessage(message, type);
            });
        } else {
            new ToastMessage(message, type);
        }
    }

    return {
        success: function (message) {
            showToast(message, "success");
        },
        error: function (message) {
            showToast(message, "error");
        },
    };
})();

function sendVerificationCode(button, sendRequest) {
    let timer;
    const countdown = 60;
    const originalButtonText = button.textContent;

    button.addEventListener("click", () => {
        button.disabled = true;
        button.textContent = i18nResources.sendVerificationCodeSending;
        sendRequest()
            .then(() => {
                startCountdown();
                Toast.success(i18nResources.sendVerificationCodeSuccess);
            })
            .catch((e) => {
                button.disabled = false;
                button.textContent = originalButtonText;
                if (e instanceof Error) {
                    Toast.error(e.message);
                } else {
                    Toast.error(i18nResources.sendVerificationCodeFailed);
                }
            });
    });

    function startCountdown() {
        let remainingTime = countdown;
        button.disabled = true;
        button.classList.add("disabled");

        timer = setInterval(() => {
            if (remainingTime > 0) {
                button.textContent = `${remainingTime}s`;
                remainingTime--;
            } else {
                clearInterval(timer);
                button.textContent = originalButtonText;
                button.disabled = false;
                button.classList.remove("disabled");
            }
        }, 1000);
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const passwordContainers = document.querySelectorAll(".toggle-password-display-flag");

    passwordContainers.forEach((container) => {
        const passwordInput = container.querySelector('input[type="password"]');
        const toggleButton = container.querySelector(".toggle-password-button");
        const displayIcon = container.querySelector(".password-display-icon");
        const hiddenIcon = container.querySelector(".password-hidden-icon");

        if (passwordInput && toggleButton && displayIcon && hiddenIcon) {
            toggleButton.addEventListener("click", () => {
                if (passwordInput.type === "password") {
                    passwordInput.type = "text";
                    displayIcon.style.display = "none";
                    hiddenIcon.style.display = "block";
                } else {
                    passwordInput.type = "password";
                    displayIcon.style.display = "block";
                    hiddenIcon.style.display = "none";
                }
            });
        }
    });
});

function setupPasswordConfirmation(passwordId, confirmPasswordId) {
    const password = document.getElementById(passwordId);
    const confirmPassword = document.getElementById(confirmPasswordId);

    function validatePasswordMatch() {
        if (password.value !== confirmPassword.value) {
            confirmPassword.setCustomValidity(i18nResources.passwordConfirmationFailed);
        } else {
            confirmPassword.setCustomValidity("");
        }
    }

    password.addEventListener("change", validatePasswordMatch);
    confirmPassword.addEventListener("input", validatePasswordMatch);
}

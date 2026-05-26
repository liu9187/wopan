const configForm = document.querySelector("#configForm");
const loginForm = document.querySelector("#loginForm");
const sendSmsButton = document.querySelector("#sendSmsButton");
const confirmLoginButton = document.querySelector("#confirmLoginButton");
const refreshCaptchaButton = document.querySelector("#refreshCaptchaButton");
const captchaImage = document.querySelector("#captchaImage");
const loginMessage = document.querySelector("#loginMessage");
const loginAccessToken = document.querySelector("#loginAccessToken");
const loginRefreshToken = document.querySelector("#loginRefreshToken");
const statusBadge = document.querySelector("#statusBadge");
const imageInput = document.querySelector("#imageInput");
const uploadButton = document.querySelector("#uploadButton");
const message = document.querySelector("#message");
const localPreview = document.querySelector("#localPreview");
const remotePreview = document.querySelector("#remotePreview");
const localBox = localPreview.closest(".preview-box");
const remoteBox = remotePreview.closest(".preview-box");
const previewLink = document.querySelector("#previewLink");

let selectedFile = null;

async function loadConfig() {
    const response = await fetch("/api/config");
    const config = await response.json();
    const saved = JSON.parse(localStorage.getItem("wopan-demo-config") || "{}");
    fillForm({...config, ...saved});
    updateStatus(config.configured);
}

async function refreshCaptcha() {
    const response = await fetch("/api/login/captcha");
    const captcha = await response.json();
    if (!response.ok) {
        showLoginMessage(captcha.error || "图形验证码加载失败", "error");
        return;
    }
    loginForm.elements.uuid.value = captcha.uuid;
    loginForm.elements.verifyCode.value = "";
    captchaImage.src = captcha.imageUrl;
}

function fillForm(config) {
    for (const element of configForm.elements) {
        if (!element.name || config[element.name] === undefined) {
            continue;
        }
        if (element.type === "checkbox") {
            element.checked = Boolean(config[element.name]);
        } else if (!String(config[element.name]).includes("******")) {
            element.value = config[element.name] || "";
        }
    }
}

function formPayload() {
    const payload = {};
    for (const element of configForm.elements) {
        if (!element.name) {
            continue;
        }
        payload[element.name] = element.type === "checkbox" ? element.checked : element.value.trim();
    }
    return payload;
}

function updateStatus(configured) {
    statusBadge.textContent = configured ? "已配置" : "未配置";
    statusBadge.classList.toggle("ready", configured);
}

function showMessage(text, type = "") {
    message.textContent = text;
    message.className = `message ${type}`.trim();
}

function showLoginMessage(text, type = "") {
    loginMessage.textContent = text;
    loginMessage.className = `message ${type}`.trim();
}

function loginPayload(includeSmsCode) {
    const payload = {};
    for (const element of loginForm.elements) {
        if (!element.name) {
            continue;
        }
        if (element.name === "smsCode" && !includeSmsCode) {
            continue;
        }
        payload[element.name] = element.value.trim();
    }
    return payload;
}

configForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const payload = formPayload();
    const response = await fetch("/api/config", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(payload)
    });
    const result = await response.json();
    if (!response.ok) {
        showMessage(result.error || "配置保存失败", "error");
        return;
    }
    localStorage.setItem("wopan-demo-config", JSON.stringify(payload));
    updateStatus(result.configured);
    showMessage("配置已保存", "ok");
});

sendSmsButton.addEventListener("click", async () => {
    sendSmsButton.disabled = true;
    showLoginMessage("正在触发短信验证码...");
    try {
        const response = await fetch("/api/login/start", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(loginPayload(false))
        });
        const result = await response.json();
        if (!response.ok) {
            showLoginMessage(result.error || "短信验证码触发失败", "error");
            if (isCaptchaError(result.error)) {
                await refreshCaptcha();
            }
            return;
        }
        showLoginMessage(result.needSmsCode ? result.message : "账号已通过第一步校验，请继续确认登录", "ok");
    } catch (error) {
        showLoginMessage(error.message || "短信验证码触发失败", "error");
    } finally {
        sendSmsButton.disabled = false;
    }
});

confirmLoginButton.addEventListener("click", async () => {
    confirmLoginButton.disabled = true;
    showLoginMessage("正在确认登录...");
    try {
        const response = await fetch("/api/login/confirm", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(loginPayload(true))
        });
        const result = await response.json();
        if (!response.ok) {
            showLoginMessage(result.error || "登录确认失败", "error");
            if (isCaptchaError(result.error)) {
                await refreshCaptcha();
            }
            return;
        }
        loginAccessToken.value = result.accessToken;
        loginRefreshToken.value = result.refreshToken;
        fillForm({
            accessToken: result.accessToken,
            refreshToken: result.refreshToken,
            ...(result.config || {})
        });
        localStorage.setItem("wopan-demo-config", JSON.stringify(formPayload()));
        updateStatus(true);
        showLoginMessage(`登录成功，token 有效期 ${result.expiresIn} 秒`, "ok");
        showMessage("token 已写入配置，可以上传图片了", "ok");
    } catch (error) {
        showLoginMessage(error.message || "登录确认失败", "error");
    } finally {
        confirmLoginButton.disabled = false;
    }
});

refreshCaptchaButton.addEventListener("click", refreshCaptcha);

imageInput.addEventListener("change", () => {
    selectedFile = imageInput.files[0] || null;
    uploadButton.disabled = !selectedFile;
    remotePreview.removeAttribute("src");
    remoteBox.classList.remove("has-image");
    previewLink.classList.remove("visible");
    previewLink.removeAttribute("href");
    if (!selectedFile) {
        localPreview.removeAttribute("src");
        localBox.classList.remove("has-image");
        return;
    }
    localPreview.src = URL.createObjectURL(selectedFile);
    localBox.classList.add("has-image");
    showMessage(`${selectedFile.name}，${formatBytes(selectedFile.size)}`);
});

uploadButton.addEventListener("click", async () => {
    if (!selectedFile) {
        return;
    }
    uploadButton.disabled = true;
    showMessage("正在上传...");
    const form = new FormData();
    form.append("image", selectedFile);
    try {
        const response = await fetch("/api/images", {
            method: "POST",
            body: form
        });
        const result = await response.json();
        if (!response.ok) {
            showMessage(result.error || "上传失败", "error");
            return;
        }
        if (result.previewUrl) {
            remotePreview.src = result.previewUrl;
            remoteBox.classList.add("has-image");
            previewLink.href = result.previewUrl;
            previewLink.classList.add("visible");
        }
        showMessage(`上传成功，fid=${result.fid}`, "ok");
    } catch (error) {
        showMessage(error.message || "上传失败", "error");
    } finally {
        uploadButton.disabled = false;
    }
});

function formatBytes(bytes) {
    if (!bytes) {
        return "0 B";
    }
    const units = ["B", "KB", "MB", "GB"];
    const index = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1);
    const value = bytes / Math.pow(1024, index);
    return `${value.toFixed(index === 0 ? 0 : 1)} ${units[index]}`;
}

function isCaptchaError(message) {
    return String(message || "").includes("6006")
        || String(message || "").includes("6007")
        || String(message || "").includes("图形验证码");
}

Promise.all([
    loadConfig(),
    refreshCaptcha()
]).catch((error) => showMessage(error.message, "error"));

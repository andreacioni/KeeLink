const INVALIDATE_TIMEOUT_SEC = 50;
const REQUEST_INTERVAL = 1500;

var _sid;
var invalidateSid = false;
var passwordReceived = false;
var pollingInterval;

function init(sid) {
	_sid = sid;
	if(!checkBrowserSupport()) {
		alertError("Your browser is up to date, please use newer browser");
	} else {
		initQrCode();
		initAsyncAjaxRequest();
	}
}

function initAsyncAjaxRequest() {
	pollingInterva = setInterval(passwordLooker, REQUEST_INTERVAL);
	setTimeout(function () {invalidateSid = true},INVALIDATE_TIMEOUT_SEC * 1000);
}

function passwordLooker() {
	if(!invalidateSid) {
		$.post("getpassforsid.php",{'sid':_sid},onSuccess,"text");
	} else if(!passwordReceived){
		invalidateSession(); 
		alertWarn("No password received...","No password was received in the last minute, reload page to start a new session");
	}
}

function initClipboardButton() {
	new Clipboard('.btn');
}

function initQrCode() {
	var qrcode = new QRCode(document.getElementById("qrcode"), {
		text: _sid,
		width: 200,
		height: 200,
		colorDark : "#000000",
		colorLight : "#ffffff",
		correctLevel : QRCode.CorrectLevel.H
	});
	
	setTimeout(function(){ $("#qrcode").css("opacity",1); }, 500);
}

function alertSuccess(title,msg) {
	swal({
	  title: title,
	  text: msg,
	  type: "success"
	});
}

function alertInfo(title,msg) {
	swal({
	  title: title,
	  text: msg,
	  type: "info"
	});
}

function alertWarn(title,msg) {
	swal({
	  title: title,
	  text: msg,
	  type: "warn"
	});
}

function alertError(title,msg) {
	swal({
	  title: title,
	  text: msg,
	  type: "error"
	});
}

function checkBrowserSupport(params) {
	return "XMLHttpRequest" in window;
}

function copyPasswordToClipboard(psw) {
}

function onSuccess(data,textStatus,jqXhr) {
	if(data != undefined && data != "ERROR") {
		passwordReceived = true;
		alertSuccess("Password received!","Your password was saved in clipboard, paste it where you want");
		console.log("Password: " + data)
		invalidateSession();
	}
}

function onFail(data,textStatus,jqXhr) {
	alertError("Comunication Failure","Are you connected to internet?")
}

function invalidateSession() {
	invalidateSid = true;
	clearInterval(pollingInterval);
	_sid = null;
	$("#sidLabel").css("text-decoration", "line-through");
	$("#qrcode").css("filter", "blur(4px)");
}
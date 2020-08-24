$(init);

var DEBUG = false;

var INVALIDATE_TIMEOUT_SEC = 50;
var REQUEST_INTERVAL = 2000;

var REMINDER_DELETE_CLIPBOARD = 10000;
var REMINDER_TITLE = "Don't forget!";
var REMINDER_BODY = "Remember to clear your clipboard, your credentials are still there!";

var DEFAULT_KEY_SIZE = 1024; //TODO test 2048 and persist on browser cache

var _sid;
var _crypt;
var invalidateSid = false;
var requestFinished = true;
var pollingInterval;
var _query_string = parseWindowURL();

function init() {
	//Hide hosting if SelfHosted
	if(!window.location.hostname.toLowerCase().endsWith("keelink.cloud")) {
		$("#hostedby").hide();
	}
	
	if(_query_string && (_query_string.onlyinfo === true || _query_string.onlyinfo === 'true')) {
		$("#qrplaceholder").hide();
	} else {
		generateKeyPair();
		requestInit();
	}

	//Enable scrolling effect on anchor clicking
	var _root = $('html, body');
	$('a.navbar-link').click(function(event){
		event.preventDefault();
		_root.animate({
			scrollTop: $( $(this).attr('href') ).offset().top
		}, 500);
		return false;
	});

	if(_query_string && _query_string.show) {
		$('a.navbar-link[href$="' + _query_string.show + '"').trigger('click');
		//window.location.hash = "#" + _query_string.show;
	}
}

function requestInit() {
	$("#sidLabel").text("Receiving...");
	log(PEMtoBase64(_crypt.getPublicKey()));
	log(toSafeBase64(PEMtoBase64(_crypt.getPublicKey())))
	$.post("init.php",{PUBLIC_KEY : toSafeBase64(PEMtoBase64(_crypt.getPublicKey()))},"json").done(
		function(data) {
			if(data.status === true) {
				_sid = data['message'];
				
				if(!checkBrowserSupport()) {
					alertError("Your browser is up to date, please use newer browser");
				} else {
					$("#sidLabel").text(_sid);
					initQrCode();
					initAsyncAjaxRequest();
				}
			} else {
				alertError("Cannot initialize KeeLink",data.message);
			}
		}
	).fail(
		function() {
			alertError("Error","Cannot initilize this service");
		}
	);
}

function generateKeyPair() {
	$("#sidLabel").text("Generating key pair...");
	_crypt = new JSEncrypt({default_key_size: DEFAULT_KEY_SIZE});
	_crypt.getKey();
	log(_crypt.getPublicKey());
}

function parseWindowURL() {
	var query_string = {};
	var query = window.location.href.split("?")[1];
	if(query) {
		var vars = query.split("&");
		for (var i=0;i<vars.length;i++) {
			var pair = vars[i].split("=");
				// If first entry with this name
			if (typeof query_string[pair[0]] === "undefined") {
			query_string[pair[0]] = decodeURIComponent(pair[1]);
				// If second entry with this name
			} else if (typeof query_string[pair[0]] === "string") {
			var arr = [ query_string[pair[0]],decodeURIComponent(pair[1]) ];
			query_string[pair[0]] = arr;
				// If third or later entry with this name
			} else {
			query_string[pair[0]].push(decodeURIComponent(pair[1]));
			}
		} 
		return query_string;
	}

}

function initAsyncAjaxRequest() {
	pollingInterval = setInterval(passwordLooker, REQUEST_INTERVAL);
	setTimeout(function () {
		invalidateSid = true;
	},1000 * INVALIDATE_TIMEOUT_SEC);
}

function passwordLooker() {
	if(!invalidateSid) {
		if(requestFinished) {
			requestFinished = false;
			$.get("getcredforsid.php",{'sid':_sid},onSuccess,"json").always(function() {requestFinished = true;});
		}
	} else {
		invalidateSession(); 
		alertWarnReload("No credentials received...","No credential was received in the last minute, reload page to start a new session");
	}
}

function initClipboardButtons(username,password) {
	$("#copyUserBtn").show();
	$("#copyPswBtn").show();
	$("#clearBtn").show();
	
	$("#copyUserBtn,.swal-button").attr("data-clipboard-text",username);
	$("#copyPswBtn,.swal-button").attr("data-clipboard-text",password);
	
	//Copy username to clipboard button
	var clipCopyUser = new ClipboardJS('#copyUserBtn,.swal-button');
	clipCopyUser.on('success', function() {
		remindDelete();
	});
	
	//Copy password to clipboard button
	var clipCopyPsw = new ClipboardJS('#copyPswBtn,.swal-button');
	clipCopyPsw.on('success', function() {
		remindDelete();
	});

	//Clear clipboard button
	new ClipboardJS('#clearBtn');

	$("#clearBtn").click(function() { 
		alertSuccess("Ok","Clipboard cleared!");
	});
}

function initQrCode() {
	var qrcode = new QRCode(document.getElementById("qrcode"), {
		text: "ksid://" + _sid,
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
	icon: "success"
	});
}

function alertInfo(title,msg) {
	swal({
	title: title,
	text: msg,
	icon: "info"
	});
}

function alertWarn(title,msg) {
	swal({
	title: title,
	text: msg,
	icon: "warning"
	});
}

function alertWarnReload(title,msg) {
	swal({
	title: title,
	text: msg,
	icon: "warning",
	button: "Reload",
	}).then((value)=>{
		if(value)
			location.reload();
	});
}

function alertError(title,msg) {
	swal({
	title: title,
	text: msg,
	icon: "error"
	});
}

function checkBrowserSupport() {
	return "XMLHttpRequest" in window;
}

function checkNotificationSupport() {
	return "Notification" in window;
}

function remindDelete() {
	setTimeout(function() {
		if (Notification.permission === "granted") {
			var notification = new Notification(REMINDER_TITLE,{"body":REMINDER_BODY});
		}
		else if (Notification.permission !== 'denied') {
			Notification.requestPermission(function (permission) {
			if (permission === "granted") {
				var notification = new Notification(REMINDER_TITLE,{"body":REMINDER_BODY});
			}
			});
		}
	},REMINDER_DELETE_CLIPBOARD);
}

function onSuccess(data,textStatus,jqXhr) {
	if(data != undefined && data.status === true) {
		log("Encoded username: " + data.username);
		data.username = fromSafeBase64(data.username);
		log("Decoded username: " + data.username);
		decryptedUsername = _crypt.decrypt(data.username);
		log("Encoded password: " + data.password);
		data.password = fromSafeBase64(data.password);
		log("Decoded password: " + data.password);
		decryptedPsw = _crypt.decrypt(data.password);
		if(decryptedUsername && decryptedPsw) {
			log("Decrypted username: " + decryptedUsername);
			log("Decrypted password: " + decryptedPsw);
			alertSuccess("Credentials received!","Would you copy password on clipboard? (Also remember to clear your clipboard after usage!)");
			initClipboardButtons(decryptedUsername, decryptedPsw);
			$.post("removeentry.php",{'sid':_sid},function(){},"json");
			invalidateSession();
		} else {
			alertError("Error", "There was an error, can't decrypt your credentials. Try again...");
			invalidateSession();
		}
	}
}

function onFail(data,textStatus,jqXhr) {
	errorMsg = (data != undefined && data.status === false) ? "Error: " + data.message : "Are you connected to Internet?"
	alertError("Comunication Failure",errorMsg)
}

function invalidateSession() {
	invalidateSid = true;
	clearInterval(pollingInterval);
	_sid = null;
	$("#sidLabel").css("text-decoration", "line-through");
	$("#qrcode").css("filter", "blur(2px)");
	$("#qrcode").css("-webkit-filter", "blur(2px)");
}

function PEMtoBase64(pem) {
	return pem.replace(new RegExp("\\n","g"), "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
}

function toSafeBase64(notSafe) {
	return notSafe.replace(new RegExp("\\n","g"), "").replace(new RegExp("\\+","g"),"-").replace(new RegExp("\/","g"), "_");
}

function fromSafeBase64(safe) {
	return safe.replace(new RegExp("\\n","g"), "").replace(new RegExp("-","g"),"+").replace(new RegExp("_","g"), "/");
}

function log(str) {
	if(DEBUG)
		console.log(str);
}
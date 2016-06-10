const INVALIDATE_TIMEOUT_SEC = 50;
const REQUEST_INTERVAL = 2000;

var _sid;
var invalidateSid = false;
var requestFinished = true;
var pollingInterval;

function init() {
	$.post("init.php",{},
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
	,"json");
	
	//Enable scrolling effect on anchor clicking
	var _root = $('html, body');
	$('a').click(function(event){
		event.preventDefault();
		_root.animate({
			scrollTop: $( $(this).attr('href') ).offset().top
		}, 500);
		return false;
	});
}

function initAsyncAjaxRequest() {
	pollingInterval = setInterval(passwordLooker, REQUEST_INTERVAL);
	setTimeout(function () {invalidateSid = true},INVALIDATE_TIMEOUT_SEC * 1000);
}

function passwordLooker() {
	if(!invalidateSid && requestFinished) {
		requestFinished = false;
		$.post("getpassforsid.php",{'sid':_sid},onSuccess,"json").always(function() {requestFinished = true;});
	} else {
		invalidateSession(); 
		alertWarn("No password received...","No password was received in the last minute, reload page to start a new session");
	}
}

function initClipboardButton(password) {
	$("#copyBtn").show();
	$("#moreBtn").show().click(
		function(){
			if($("#hiddenMoreButtonField").is(":hidden"))
				$("#hiddenMoreButtonField").slideDown();
			else
				$("#hiddenMoreButtonField").slideUp();
		}
	);
	$("#clearBtn").show();
	
	$("#copyBtn").attr("data-clipboard-text",password);
	
	new Clipboard('#copyBtn');
	new Clipboard('#moreBtn');
	new Clipboard('#clearBtn');
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
	  type: "success"
	},function() {
		$("#copyBtn").click();
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
	  type: "warning"
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

function onSuccess(data,textStatus,jqXhr) {
	if(data != undefined && data.status === true) {
		initClipboardButton(data.message);
		alertSuccess("Password received!","Would you copy password on clipboard?");
		$.post("removeentry.php",{'sid':_sid},function(){},"json");
		invalidateSession();
	}
}

function onFail(data,textStatus,jqXhr) {
	alertError("Comunication Failure","Are you connected to Internet?")
}

function invalidateSession() {
	invalidateSid = true;
	clearInterval(pollingInterval);
	_sid = null;
	$("#sidLabel").css("text-decoration", "line-through");
	$("#qrcode").css("filter", "blur(2px)");
}
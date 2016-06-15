$(document).load(
	function () {
		const INVALIDATE_TIMEOUT_SEC = 50;
		const REQUEST_INTERVAL = 2000;

		var _sid;
		var invalidateSid = false;
		var requestFinished = true;
		var pollingInterval;

		var _query_string = parseWindowURL();

		init();

		function init() {

			detectHttpProtocol();

			if(_query_string && (_query_string.onlyinfo === true || _query_string.onlyinfo === 'true')) {
				$("#qrplaceholder").hide();
			} else {
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
			
		}

		function detectHttpProtocol() {
			if (window.location.protocol != "https:") {
				swal({
					title: "Are you sure?",
					text: "This connection is using HTTP protocol so is not secure, would you like to use HTTPS secure protocol?",
					type: "warning",
					showCancelButton: true,
					confirmButtonText: "Go safe!",
					cancelButtonText: "Stay here...",
					closeOnConfirm: false
					},
					function(){
					window.location.href = "https:" + window.location.href.substring(window.location.protocol.length);
				});
			}
		}

		function parseWindowURL() {
			// This function is anonymous, is executed immediately and 
			// the return value is assigned to QueryString!
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
					if($("#clearBtn").is(":hidden"))
						$("#clearBtn").slideDown();
					else
						$("#clearBtn").slideUp();
				}
			);
			
			$("#copyBtn").attr("data-clipboard-text",password);
			
			new Clipboard('#copyBtn');
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
			$("#qrcode").css("-webkit-filter", "blur(2px)");
		}
	}
);
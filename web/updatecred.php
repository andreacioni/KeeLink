<?php 
	require("keelink.php");
	
	if(isset($_POST["sid"]) and isset($_POST["username"]) and isset($_POST["password"])) {
		$sid = $_POST["sid"];
		$username = $_POST["username"];
		$psw = $_POST["password"];
		echo KeeLink::setCredentialsForSid($sid, $username, $psw);
	} else {
		$jresp["status"] = false;
		$jresp["message"] = "Invalid Sid or Credentials!";
		echo json_encode($jresp);
	}
?>
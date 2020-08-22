<?php 
	require('keelink.php');
	
	if(isset($_POST['sid']) and isset($_POST['key'])) {
		$sid = $_POST["sid"];
		$psw = $_POST["key"];
		echo KeeLink::setPasswordForSid($sid,$psw);
	} else {
		$jresp['status'] = false;
		$jresp['message'] = "Invalid Sid or Key!";
		echo json_encode($jresp);
	}
?>
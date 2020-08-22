<?php
	require('keelink.php');
	
	if(isset($_GET['sid'])) {
		$sid = $_GET['sid'];
		echo KeeLink::getPublicKeyForSid($sid);
	} else {
		$jresp['status'] = false;
		$jresp['message'] = "Invalid Sid!";
		echo json_encode($jresp);
	}
?>
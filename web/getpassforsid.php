<?php
	require('keelink.php');
	
	if(isset($_GET['sid'])) {
		$sid = $_GET['sid'];
		echo KeeLink::getPasswordForSid($sid);
	} else {
		$jresp['status'] = false;
		$jresp['message'] = "Invalid Sid!";
		echo json_encode($jresp);
	}
?>
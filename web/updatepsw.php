<?php 
    //Left for those who have an older version of the Android app
	require('keelink.php');
	
	$sid = $_POST["sid"];
	$psw = $_POST["key"];
	
	echo KeeLink::setCredentialsForSid($sid, NULL, $psw);
?>
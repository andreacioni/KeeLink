<?php 
	require('keelink.php');
	
	$sid = $_POST["sid"];
	$psw = $_POST["key"];
	
	echo KeeLink::setPasswordForSid($sid,$psw);
?>
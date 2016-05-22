<?php 
	require('lib/php/keelink.php');
	
	$sid = $_POST["sid"];
	$psw = $_POST["key"];
	
	KeeLink::setPasswordForSid($sid,$psw);

?>
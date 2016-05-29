<?php
	require('keelink.php');
	$sid = $_POST["sid"];
	
	KeeLink::getPasswordForSid($sid);

?>
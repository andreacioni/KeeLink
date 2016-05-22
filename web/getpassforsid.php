<?php
	require('lib/php/keelink.php');
	$sid = $_POST["sid"];
	
	KeeLink::getPasswordForSid($sid);

?>
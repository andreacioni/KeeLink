<?php
	require('keelink.php');
	
	$sid = $_GET['sid'];
	echo KeeLink::getPublicKeyForSid($sid);
?>
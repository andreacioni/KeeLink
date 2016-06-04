<?php 
    require('keelink.php');
	$sid = $_POST["sid"];
	
    echo KeeLink::removeEntry($sid);
?>
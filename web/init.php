<?php 
    require('keelink.php');
    $publickey = $_POST['PUBLIC_KEY'];

	echo KeeLink::initNewSession($publickey);
?>
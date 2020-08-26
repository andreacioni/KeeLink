<?php 
    require('keelink.php');
	
	if(isset($_POST['PUBLIC_KEY'])) {
		$publickey = $_POST['PUBLIC_KEY'];
		echo KeeLink::initNewSession($publickey);
	} else {
		$jresp['status'] = false;
		$jresp['message'] = "Invalid Public Key!";
		echo json_encode($jresp);
	}
?>
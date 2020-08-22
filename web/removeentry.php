<?php 
    require('keelink.php');
	
	if(isset($_POST['sid'])) {
		$sid = $_POST['sid'];
		echo KeeLink::removeEntry($sid);
	} else {
		$jresp['status'] = false;
		$jresp['message'] = "Invalid Sid!";
		echo json_encode($jresp);
	}
?>
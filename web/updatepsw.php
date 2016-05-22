<?php 
	$sid = $_POST["sid"];
	$psw = $_POST["key"];
	
	if($sid === NULL || $psw === NULL) {
		echo "Invalid parameter passed";
	} else {
		$servername = "localhost";
		$username = "andysite";
		$password = "";
		$dbname = "my_andysite";

		// Create connection
		$conn = new mysqli($servername, $username, $password, $dbname);
		// Check connection
		if ($conn->connect_error) {
			die("Connection failed: " . $conn->connect_error);
		}

		$sql = "UPDATE KEEPASS set PSW ='".$psw."' where SESSION_ID='".$sid."' and PSW is null";
		
		if (($conn->query($sql) === TRUE) && ($conn->affected_rows == 1)) {
			echo "OK";
		} else {
			echo "Error: " . $sql . "<br>" . $conn->error;
		}

		$conn->close();
	}

	

?>
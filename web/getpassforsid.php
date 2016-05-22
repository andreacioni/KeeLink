<?php
	$sid = $_POST["sid"];
	
	if($sid === NULL) {
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

		$sql = "SELECT PSW FROM KEEPASS WHERE SESSION_ID='".$sid."' and PSW is not null";
		$result = $conn->query($sql);
		
		if ($result->num_rows > 0) {
			$row = $result->fetch_assoc();
			echo $row['PSW'];
		} else {
			echo "ERROR";
		}

		$conn->close();	
	}

?>
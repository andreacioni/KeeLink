<?php 

class KeeLink {
    
    const _servername = "localhost";
	const _username = "andysite";
	const _password = "";
	const _dbname = "my_andysite";
    
    static public function initNewSession() {
        $sid = KeeLink::generateSid();
        
        $conn = KeeLink::getConnection();
        
        $sqlInsert = "INSERT INTO `KEEPASS`(`SESSION_ID`) VALUES ('" . $sid . "')";
		$sqlDelete = "DELETE FROM `KEEPASS` WHERE DATE_ADD(`CREATION_DATE`,INTERVAL 2 MINUTE) < NOW() ";
		
		if ($conn->query($sqlDelete) === FALSE) {
			echo "Error: " . $conn->error;
        }
		if ($conn->query($sqlInsert) === FALSE) {
			echo "Error: " . $conn->error;
        }

		$conn->close();
        
        return $sid;
    }
    
    static public function getPasswordForSid($sid) {
        if($sid === NULL) {
            echo "Invalid parameter passed";
        } else {
            $conn = KeeLink::getConnection();

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
    }
    
    static public function setPasswordForSid($sid,$psw) {
        if($sid === NULL || $psw === NULL) {
            echo "Invalid parameter passed";
        } else {
            $conn = KeeLink::getConnection();

            $sql = "UPDATE KEEPASS set PSW ='".$psw."' where SESSION_ID='".$sid."' and PSW is null";
            
            if (($conn->query($sql) === TRUE) && ($conn->affected_rows == 1)) {
                echo "OK";
            } else {
                echo "Error: " . $sql . "<br>" . $conn->error;
            }

            $conn->close();
        }
    }
    
    static private function generateSid() {
        return hash("md5",openssl_random_pseudo_bytes(256));
    }
    
    static private function getConnection() {
        // Create connection
		$conn = new mysqli(self::_servername, self::_username, self::_password, self::_dbname);
		// Check connection
		if ($conn->connect_error)
			die("Connection failed: " . $conn->connect_error);
		else {
            return $conn;
        }
    }

}

?>
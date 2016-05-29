<?php
session_start();

header("Content Type: application/json");

class KeeLink {
    
    const JSON_RESPONSE = "{
        'status':false,
        'message':'null',
        'chaptaRequired`:false
    }";
    
    static public function initNewSession() {
        
        $jresp = json_decode(self::JSON_RESPONSE);
        
        $sid = KeeLink::generateSid();
        
        $conn = KeeLink::getConnection();
        
        $chaptaRequired = KeeLink::needChapta($conn);
        
        if($chaptaRequired == TRUE)
        
        $sqlInserUser = "INSERT INTO `USER`(`USER_ID`) VALUES ('" . $_SERVER['REMOTE_ADDR'] . "') ON DUPLICATE KEY UPDATE USER.SID_CREATED=USER.SID_CREATED+1 and USER.LAST_ACCESS=CURRENT_TIMESTAMP";
        $sqlInsertSID = "INSERT IGNORE INTO `KEEPASS`(`SESSION_ID`,`USER_ID`) VALUES ('" . $sid . "','". $_SERVER['REMOTE_ADDR'] ."')";
		$sqlDelete = "DELETE FROM `KEEPASS` WHERE DATE_ADD(`CREATION_DATE`,INTERVAL 2 MINUTE) < NOW() ";
		
		if ($conn->query($sqlDelete) === TRUE) {
			if ($conn->query($sqlInserUser) === TRUE) {
                if ($conn->query($sqlInsertSID) === TRUE) {
                    $jresp['message'] = "Error: " . $conn->error;
                    $jresp['status'] = true;
                } else {
                     $jresp['message'] = "Error: " . $conn->error;
                }
            } else {
                 $jresp['message'] = "Error: " . $conn->error;
            }
        } else {
             $jresp['message'] = "Error: " . $conn->error;
        }
       

		$conn->close();
        
        return json_encode($jresp);
    }
    
    static public function getPasswordForSid($sid) {
        if($sid === NULL && $_SESSION['generatedSid'] != $sid) {
            echo "Invalid parameter passed";
        } else {
            $conn = KeeLink::getConnection();
            
            $sql = "SELECT PSW FROM KEEPASS WHERE SESSION_ID='".$sid."' and PSW is not null";
            $result = $conn->query($sql);
            
            if ($result->num_rows > 0) {
                $row = $result->fetch_assoc();
                $_SESSION["generatedSid"] = NULL;
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
                echo "Error: " . $sql . " " . $conn->error;
            }

            $conn->close();
        }
    }
    
    static private function needChapta($conn) {
        $sqlAccessAttempt = "select SID_CREATED from USER where USER_ID='".$_SERVER['REMOTE_ADDR']."'";
        $result = $conn->query($sqlAccessAttempt);
        if ( === TRUE) 
    }
    
    static private function generateSid() {
        if($_SESSION["generatedSid"] == NULL)
            $_SESSION["generatedSid"] = hash("md5",openssl_random_pseudo_bytes(256));
        
        return $_SESSION["generatedSid"];
    }
    
    static private function getConnection() {
        // Create connection
        $CONFIG_INI = parse_ini_file('private/config.ini');
        
        if($CONFIG_INI == FALSE)
        echo "erroo";
        
		$conn = new mysqli($CONFIG_INI['host'], $CONFIG_INI['username'], $CONFIG_INI['password'], $CONFIG_INI['dbname']);
		// Check connection
		if ($conn->connect_error)
			die("Connection failed: " . $conn->connect_error);
		else {
            return $conn;
        }
    }

}

?>
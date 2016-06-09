<?php
session_start();

header("Content-Type: application/json");

class KeeLink {
    
    static public function initNewSession() {
        
        $jresp['status'] = false;
        
        $sid = KeeLink::generateSid();
        
        $conn = KeeLink::getConnection();
        
        $jresp['chaptaRequired'] = FALSE;//TODO KeeLink::needChapta($conn);

        if($jresp['chaptaRequired'] === TRUE) {
            $jresp['message'] = "Error(4): Chapta required";
        } else {
            $sqlInserUser = "INSERT INTO `USER`(`USER_ID`) VALUES ('" . $_SERVER['REMOTE_ADDR'] . "') ON DUPLICATE KEY UPDATE USER.SID_CREATED=USER.SID_CREATED+1, USER.LAST_ACCESS=CURRENT_TIMESTAMP";
            $sqlInsertSID = "INSERT IGNORE INTO `KEEPASS`(`SESSION_ID`,`USER_ID`) VALUES ('" . $sid . "','". $_SERVER['REMOTE_ADDR'] ."')";
            
            if ($conn->query($sqlInserUser) === TRUE) {
                if ($conn->query($sqlInsertSID) === TRUE) {
                    $jresp['status'] = TRUE;
                    $jresp['message'] = $sid;
                } else {
                    $jresp['message'] = "Error(2): " . $conn->error;
                }
            } else {
                $jresp['message'] = "Error(3): " . $conn->error;
            }
        }

		$conn->close();
        return json_encode($jresp);
    }
    
    static public function getPasswordForSid($sid) {
        $jresp['status'] = FALSE;
        
        if($sid === NULL && $_SESSION['generatedSid'] != $sid) {
            $jresp['message'] = "Invalid parameter passed (1)";
        } else {
            $conn = KeeLink::getConnection();
            
            $sql = "SELECT PSW FROM KEEPASS WHERE SESSION_ID='".$sid."' and PSW is not null";
            $result = $conn->query($sql);
            
            if ($result->num_rows > 0) {
                $row = $result->fetch_assoc();
                $_SESSION["generatedSid"] = NULL;
                
                $jresp['message'] = $row['PSW'];
                $jresp['status'] = TRUE;
            } else {
                $jresp['message'] = "Error fetching password (4)";
            }

            $conn->close();	
        }
        
        return json_encode($jresp);
    }
    
    static public function removeEntry($sid) {
        $jresp['status'] = FALSE;
        
        if($sid === NULL && $_SESSION['generatedSid'] != $sid) {
            $jresp['message'] = "Invalid parameter passed (7)";
        } else {
            $conn = KeeLink::getConnection();
            
            $sql = "DELETE FROM KEEPASS WHERE SESSION_ID='".$sid."'";
            
            if ($conn->query($sql) === TRUE) {
                $jresp['message'] = "OK";
                $jresp['status'] = TRUE;
            } else {
                $jresp['message'] = "Error fetching password (8)";
            }

            $conn->close();	
        }
        
        return json_encode($jresp);
    }
    
    static public function setPasswordForSid($sid,$psw) {
        $jresp['status'] = FALSE;
        
        if($sid === NULL || $psw === NULL) {
            $jresp['message'] = "Invalid parameter passed (5)";
        } else {
            $conn = KeeLink::getConnection();
            
            $sql = "UPDATE KEEPASS set PSW ='".$psw."' where SESSION_ID='".$sid."' and PSW is null";
            
            if (($conn->query($sql) === TRUE) && ($conn->affected_rows == 1)) {
                $jresp['message'] = "OK";
                $jresp['status'] = TRUE;
            } else {
                $jresp['message'] = "SQL Error (6): " . $conn->error;
            }

            $conn->close();
        }
        
        return json_encode($jresp);
    }
    
    static private function needChapta($conn) {
        $sqlAccessAttempt = "select SID_CREATED from USER where USER_ID='".$_SERVER['REMOTE_ADDR']."'";
        $result = $conn->query($sqlAccessAttempt);
        if ($result->num_rows == 1) {
            $attempts = $result->fetch_assoc()["SID_CREATED"];
            if($attempts > 5)
                return TRUE;
        } 
        
        return FALSE;
    }
    
    static private function generateSid() {
        if($_SESSION["generatedSid"] == NULL)
            $_SESSION["generatedSid"] = hash("md5",openssl_random_pseudo_bytes(256));
        
        return $_SESSION["generatedSid"];
    }
    
    static private function getConnection() {
        // Create connection
        $CONFIG_INI = parse_ini_file('private/config.ini');
        
        if($CONFIG_INI == FALSE) {
            $CONFIG_INI['host'] = getenv("OPENSHIFT_MYSQL_DB_HOST");
            $CONFIG_INI['username'] = getenv("OPENSHIFT_MYSQL_DB_USERNAME");
            $CONFIG_INI['password'] = getenv("OPENSHIFT_MYSQL_DB_PASSWORD");
            $CONFIG_INI['dbname'] = "keelink";
            $CONFIG_INI['port'] = getenv("OPENSHIFT_MYSQL_DB_PORT");
        } else {
            $conn = new mysqli($CONFIG_INI['host'], $CONFIG_INI['username'], $CONFIG_INI['password'], $CONFIG_INI['dbname'],$CONFIG_INI['port']);
            // Check connection
            if ($conn->connect_error)
                die("Connection failed: " . $conn->connect_error);
            else {
                return $conn;
            }
        }
    }

}

?>
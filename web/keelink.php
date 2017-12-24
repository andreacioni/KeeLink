<?php
error_reporting(E_ERROR); //This disable unwanted warning when config file not found

session_start();

header("Content-Type: application/json");

//Cloudflare need this!
if (isset($_SERVER["HTTP_CF_CONNECTING_IP"])) {
  $_SERVER['REMOTE_ADDR'] = $_SERVER["HTTP_CF_CONNECTING_IP"];
}

class KeeLink {
    
    static public function initNewSession($publickey) {
        
        $jresp['status'] = false;
        
        $sid = KeeLink::generateSid();
        
        $inputok = KeeLink::validateBase64Input($publickey);
        
        $jresp['chaptaRequired'] = FALSE;//TODO KeeLink::needChapta($conn);

        if($inputok) {
            $conn = KeeLink::getConnection();
            
            if($jresp['chaptaRequired'] === TRUE) {
                $jresp['message'] = "Error(4): Chapta required";
            } else {
                $sqlInserUser = "insert into `USER`(USER_ID) values ('" . $_SERVER['REMOTE_ADDR'] . "') on duplicate key update USER.SID_CREATED=USER.SID_CREATED+1, USER.LAST_ACCESS=CURRENT_TIMESTAMP";
                $sqlInsertSID = "insert into KEEPASS(SESSION_ID,USER_ID,PUBLIC_KEY) values ('" . $sid . "','". $_SERVER['REMOTE_ADDR'] ."','". $publickey ."') on duplicate key update PUBLIC_KEY='".$publickey."'";
                
                if ($conn->query($sqlInserUser) === TRUE) {
                    if ($conn->query($sqlInsertSID) === TRUE) {
                        $jresp['status'] = TRUE;
                        $jresp['message'] = $sid;
                    } else {
                        $jresp['message'] = "Error(2): ".mysqli_error($conn);
                    }
                } else {
                    $jresp['message'] = "Error(3): ".mysqli_error($conn);
                }
            }
    
            $conn->close();
        } else {
            $jresp['message'] = "Error(1): Invalid public key received";
        }
        
        return json_encode($jresp);
    }
    
    static public function getPasswordForSid($sid) {
        $jresp['status'] = FALSE;
        
        $inputok = KeeLink::validateMD5($sid);

        if($inputok) {
            if($sid === NULL && $_SESSION['generatedSid'] != $sid) {
                $jresp['message'] = "Invalid parameter passed (2)";
            } else {
                $conn = KeeLink::getConnection();
                
                $sql = "select PSW from KEEPASS where SESSION_ID='".$sid."' and PSW is not null";
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
        } else {
            $jresp['message'] = "Invalid sid passed (1)";
        }
        
        
        return json_encode($jresp);
    }
    
    static public function removeEntry($sid) {
        $jresp['status'] = FALSE;
        
        $inputok = KeeLink::validateMD5($sid);

        if($inputok) {
            if($sid === NULL && $_SESSION['generatedSid'] != $sid) {
                $jresp['message'] = "Invalid parameter passed (7)";
            } else {
                $conn = KeeLink::getConnection();
                
                $sql = "delete from KEEPASS where SESSION_ID='".$sid."'";
                
                if ($conn->query($sql) === TRUE) {
                    $jresp['message'] = "OK";
                    $jresp['status'] = TRUE;
                } else {
                    $jresp['message'] = "Error fetching password (8)";
                }
    
                $conn->close();	
            }
        } else {
            $jresp['message'] = "Invalid sid passed (1)";
        }
        
        return json_encode($jresp);
    }
    
    static public function setPasswordForSid($sid,$psw) {
        $jresp['status'] = FALSE;
        
        $inputok = KeeLink::validateMD5($sid);

        if($inputok) {
            if($sid === NULL || $psw === NULL) {
                $jresp['message'] = "Invalid parameter passed (5)";
            } else {
                $conn = KeeLink::getConnection();
                
                $sql = "update KEEPASS set PSW ='".$conn->real_escape_string($psw)."' where SESSION_ID='".$sid."' and PSW is null";
                
                if (($conn->query($sql) === TRUE) && ($conn->affected_rows == 1)) {
                    $jresp['message'] = "OK";
                    $jresp['status'] = TRUE;
                } else {
                    $jresp['message'] = "SQL Error (6): ".mysqli_error($conn);
                }
    
                $conn->close();
            }
        } else {
            $jresp['message'] = "Invalid sid passed (1)";
        }
        
        return json_encode($jresp);
    }

    static public function getPublicKeyForSid($sid) {
        $jresp['status'] = FALSE;
        
        $inputok = KeeLink::validateMD5($sid);

        if($inputok) {
            if($sid === NULL && $_SESSION['generatedSid'] != $sid) {
                $jresp['message'] = "Invalid parameter passed (2)";
            } else {
                $conn = KeeLink::getConnection();
                
                $sql = "select PUBLIC_KEY from KEEPASS where SESSION_ID='".$sid."'";
                $result = $conn->query($sql);
                
                if ($result->num_rows > 0) {
                    $row = $result->fetch_assoc();
                    
                    $jresp['message'] = $row['PUBLIC_KEY'];
                    $jresp['status'] = TRUE;
                } else {
                    $jresp['message'] = "Error fetching public key (4)";
                }
    
                $conn->close();	
            }
        } else {
            $jresp['message'] = "Invalid sid passed (1)";
        }
        
        
        return json_encode($jresp);
    }

    static public function removeOldSids() {
        $jresp['status'] = TRUE;

        $conn = KeeLink::getConnection();
        $sql = "delete from KEEPASS where PSW is not null";
        $result = $conn->query($sql);

        $jresp['message'] = "Removed ".$conn->affected_rows." old record/s";

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

    static private function validateBase64Input($str) { 
        return preg_match("/^[A-Za-z0-9-_]*={0,4}$/", $str) === 1;
    }

    static private function validateMD5($str) {
        return (preg_match("/^[A-Za-z0-9]{32}$/", $str) === 1);
    }
    
    static private function getConnection() {
        // Create connection
        $CONFIG_INI = parse_ini_file('private/config.ini');
        
        if($CONFIG_INI == FALSE) {
            error_log("Configuration file not found");
            die;
        }
        
        $conn = new mysqli($CONFIG_INI['host'], $CONFIG_INI['username'], $CONFIG_INI['password'], $CONFIG_INI['dbname'],$CONFIG_INI['port']) or die("Error: ".mysqli_error($conn));
        $conn->set_charset("utf8");
        
        return $conn;
    }

}

?>
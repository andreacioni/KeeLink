<?php
//error_reporting(E_ALL); //DEBUG

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
        $jresp['captchaRequired'] = FALSE;//TODO KeeLink::needCaptcha($conn);

        if($inputok) {
            if($jresp['captchaRequired'] === TRUE) {
                $jresp['message'] = "Error(1): Captcha required";
            } else {
                $conn = KeeLink::getConnection();
                $sqlInserUser = $conn->prepare("REPLACE INTO User (User_Id, Last_Access, Sid_Created) VALUES (:User_Id, CURRENT_TIMESTAMP, IFNULL((SELECT COUNT (User_Id) FROM User WHERE User_Id = :User_Id), 0) + 1)");
                    $sqlInserUser->bindParam(":User_Id", $_SERVER['REMOTE_ADDR']);
                $sqlInsertSID = $conn->prepare("REPLACE INTO Keepass (Session_Id, User_Id, Public_Key) VALUES (:Session_Id, :User_Id, :Public_Key)");
                    $sqlInsertSID->bindParam(":Session_Id", $sid);
                    $sqlInsertSID->bindParam(":User_Id", $_SERVER['REMOTE_ADDR']);
                    $sqlInsertSID->bindParam(":Public_Key", $publickey);
                
                if ($sqlInserUser->execute() === TRUE) {
                    if ($sqlInsertSID->execute() === TRUE) {
                        $jresp['status'] = TRUE;
                        $jresp['message'] = $sid;
                    } else {
                        error_log($sqlInsertSID->errorInfo()[2]);
                        $jresp['message'] = "SQL Error(1): ".$sqlInsertSID->errorCode();
                    }
                } else {
                    error_log($sqlInserUser->errorInfo()[2]);
                    $jresp['message'] = "SQL Error(1): ".$sqlInserUser->errorCode();
                }
            }
            $conn = null;
        } else {
            $jresp['message'] = "Error(1): Invalid public key provided";
        }
        
        return json_encode($jresp);
    }
    
    static public function getPasswordForSid($sid) {
        $jresp['status'] = FALSE;
        $inputok = KeeLink::validateMD5($sid);

        if($inputok) {
            if($sid === NULL && $_SESSION['generatedSid'] != $sid) {
                $jresp['message'] = "Error(2): Invalid parameter provided";
            } else {
                $conn = KeeLink::getConnection();
                $sql = $conn->prepare("SELECT Psw FROM Keepass WHERE Session_Id = :Session_Id AND Psw IS NOT NULL");
                    $sql->bindParam(":Session_Id", $sid);
                $sql->execute();
                $result = $sql->fetchAll(PDO::FETCH_ASSOC);

                if ($result && count($result) == 1) {
                    $_SESSION['generatedSid'] = NULL;
                    $jresp['message'] = $result[0]['Psw'];
                    $jresp['status'] = TRUE;
                } else {
                    $jresp['message'] = "Error(2): Error fetching password";
                }
                $conn = null;
            }
        } else {
            $jresp['message'] = "Error(2): Invalid sid provided";
        }
        
        return json_encode($jresp);
    }
    
    static public function removeEntry($sid) {
        $jresp['status'] = FALSE;
        $inputok = KeeLink::validateMD5($sid);

        if($inputok) {
            if($sid === NULL && $_SESSION['generatedSid'] != $sid) {
                $jresp['message'] = "Error(3): Invalid parameter provided";
            } else {
                $conn = KeeLink::getConnection();
                $sql = $conn->prepare("DELETE FROM Keepass WHERE Session_Id = :Session_Id");
                    $sql->bindParam(":Session_Id", $sid);
                
                if ($sql->execute() === TRUE) {
                    $jresp['message'] = "OK";
                    $jresp['status'] = TRUE;
                } else {
                    $jresp['message'] = "SQL Error(3): Error removing entry";
                }
                $conn = null;	
            }
        } else {
            $jresp['message'] = "Error(3): Invalid sid provided";
        }
        
        return json_encode($jresp);
    }
    
    static public function setPasswordForSid($sid,$psw) {
        $jresp['status'] = FALSE;
        
        $inputok = KeeLink::validateMD5($sid);

        if($inputok) {
            if($sid === NULL || $psw === NULL) {
                $jresp['message'] = "Error(4): Invalid parameter provided";
            } else {
                $conn = KeeLink::getConnection();
                $sql = $conn->prepare("UPDATE Keepass SET Psw = :Psw WHERE Session_Id = :Session_Id AND Psw IS NULL");
                    $sql->bindParam(":Psw", $conn->real_escape_string($psw));
                    $sql->bindParam(":Session_Id", $sid);
                
                if (($sql->execute() === TRUE) && ($sql->rowCount() == 1)) {
                    $jresp['message'] = "OK";
                    $jresp['status'] = TRUE;
                } else {
                    error_log($sqlInsertSID->errorInfo()[2]);
                    $jresp['message'] = "SQL Error (4): ".$sql->errorCode();
                }
                $conn = null;
            }
        } else {
            $jresp['message'] = "Error(4): Invalid sid provided";
        }
        
        return json_encode($jresp);
    }

    static public function getPublicKeyForSid($sid) {
        $jresp['status'] = FALSE;
        $inputok = KeeLink::validateMD5($sid);

        if($inputok) {
            if($sid === NULL && $_SESSION['generatedSid'] != $sid) {
                $jresp['message'] = "Error(5): Invalid parameter provided";
            } else {
                $conn = KeeLink::getConnection();
                $sql = $conn->prepare("SELECT PublicKey FROM Keepass WHERE Session_Id = :Session_Id");
                    $sql->bindParam(":Session_Id", $sid);
                $sql->execute();
                $result = $sql->fetchAll(PDO::FETCH_ASSOC);
                
                if ($result && count($result) == 1) {                   
                    $jresp['message'] = $result[0]['Public_Key'];
                    $jresp['status'] = TRUE;
                } else {
                    $jresp['message'] = "Error(5): Error fetching public key";
                }
                $conn = null;
            }
        } else {
            $jresp['message'] = "Error(5): Invalid sid provided";
        }

        return json_encode($jresp);
    }

    static public function removeOldSids() {
        $jresp['status'] = TRUE;

        $conn = KeeLink::getConnection();
        $result = $conn->exec("DELETE FROM Keepass WHERE Psw IS NOT NULL");

        $jresp['message'] = "Removed ".$result." old record/s";
        
        $conn = null;
        return json_encode($jresp);
    }
    
    static private function needCaptcha($conn) {
        $sql = $conn->prepare("SELECT Sid_Created FROM User WHERE User_Id = :User_Id");
            $sql->bindParam(":UserId", $_SERVER['REMOTE_ADDR']);
        $sql->execute();
        $result = $sql->fetchAll(PDO::FETCH_ASSOC);
        
        if ($result && count($result) == 1) {
            $attempts = $result[0]["Sid_Created"];
            if($attempts > 5)
                return TRUE;
        } 
        
        return FALSE;
    }
    
    static private function generateSid() {
        if(!isset($_SESSION["generatedSid"]) or $_SESSION["generatedSid"] == NULL)
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
        $isMySql = file_exists("private/config.ini");
        $dbSqliteExists = file_exists("private/KeeLink.db");
        
        try
        {
            if($isMySql) {
                $CONFIG_INI = parse_ini_file("private/config.ini");
                if($CONFIG_INI == FALSE) {
                    error_log("MySQL configuration file not found!");
                    die;
                }
            $conn = new PDO("mysql:host=" . $CONFIG_INI['host'] . ";port=" . $CONFIG_INI['port'] . ";dbname=" . $CONFIG_INI['dbname'] . ";charset=utf8", $CONFIG_INI['username'], $CONFIG_INI['password']); 
            } else {
                //SQLite
                $conn = new PDO("sqlite:private/KeeLink.db");
            }
        }
        catch(PDOException $e)
        {
            error_log($e->getMessage());
            die('Exception: unable to open Database!');
        }
        
        //Create SQLite DB if not exists
        if (!$isMySql and !$dbSqliteExists) {
            try
            {
                $conn -> exec ("CREATE TABLE IF NOT EXISTS [User] (
                                User_Id TEXT PRIMARY KEY NOT NULL,
                                Last_Access DATETIME DEFAULT (CURRENT_TIMESTAMP),
                                Sid_Created INTEGER NOT NULL DEFAULT (1) 
                            );");
                $conn -> exec ("CREATE TABLE IF NOT EXISTS [Keepass] (
                                Session_Id TEXT NOT NULL PRIMARY KEY,
                                Username TEXT,
                                Psw TEXT,
                                Creation_Date DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL,
                                User_Id TEXT NOT NULL,
                                Public_Key TEXT NOT NULL,
                                FOREIGN KEY (User_Id) REFERENCES User (User_Id)
                            );");
                $conn -> exec ("PRAGMA encoding = 'UTF-8';");  
            }
            catch(PDOException $e)
            {
                error_log($e->getMessage());
                die('Exception: unable to create SQlite DB!');
            }
        }
        return $conn;
    }
}
?>
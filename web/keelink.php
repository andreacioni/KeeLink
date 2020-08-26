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
        $conn = KeeLink::getConnection();
        $jresp['captchaRequired'] = KeeLink::needCaptcha($conn);

        if($inputok) {
            if($jresp['captchaRequired'] === TRUE) {
                $jresp['message'] = "Too many connections from your address, please try again later!";
            } else {
                $sqlInserUser = $conn->prepare("REPLACE INTO Users (UserId, LastAccess, SidCreated) VALUES (:UserId, CURRENT_TIMESTAMP, IFNULL((SELECT COUNT(UC.UserId) FROM Users AS UC WHERE UC.UserId = :UserId), 0) + 1)");
                    $sqlInserUser->bindValue(":UserId", $_SERVER['REMOTE_ADDR']);
                $sqlInsertSID = $conn->prepare("REPLACE INTO Keepass (SessionId, UserId, PublicKey) VALUES (:SessionId, :UserId, :PublicKey)");
                    $sqlInsertSID->bindValue(":SessionId", $sid);
                    $sqlInsertSID->bindValue(":UserId", $_SERVER['REMOTE_ADDR']);
                    $sqlInsertSID->bindValue(":PublicKey", $publickey);

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
    
    static public function getCredentialsForSid($sid) {
        $jresp['status'] = FALSE;
        $inputok = KeeLink::validateMD5($sid);

        if($inputok) {
            if($sid === NULL && $_SESSION['generatedSid'] != $sid) {
                $jresp['message'] = "Error(2): Invalid parameter provided";
            } else {
                $conn = KeeLink::getConnection();
                $sql = $conn->prepare("SELECT Username, Psw FROM Keepass WHERE SessionId = :SessionId AND (Username IS NOT NULL OR Psw IS NOT NULL)");
                    $sql->bindValue(":SessionId", $sid);
                $sql->execute();
                $result = $sql->fetchAll(PDO::FETCH_ASSOC);

                if ($result && count($result) == 1) {
                    $_SESSION['generatedSid'] = NULL;
                    $jresp['username'] = $result[0]['Username'];
                    $jresp['password'] = $result[0]['Psw'];
                    $jresp['status'] = TRUE;
                } else {
                    $jresp['message'] = "Error(2): Error fetching credentials";
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
                $sql = $conn->prepare("DELETE FROM Keepass WHERE SessionId = :SessionId");
                    $sql->bindValue(":SessionId", $sid);
                
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
    
    static public function setCredentialsForSid($sid,$username,$psw) {
        $jresp['status'] = FALSE;
        
        $inputok = KeeLink::validateMD5($sid);

        if($inputok) {
            if($sid === NULL || $psw === NULL) {
                $jresp['message'] = "Error(4): Invalid parameter provided";
            } else {
                $conn = KeeLink::getConnection();
                $sql = $conn->prepare("UPDATE Keepass SET Username = :Username, Psw = :Psw WHERE SessionId = :SessionId AND Username IS NULL AND Psw IS NULL");
                    $sql->bindValue(":Username", $username);
                    $sql->bindValue(":Psw", $psw);
                    $sql->bindValue(":SessionId", $sid);
                
                if (($sql->execute() === TRUE) && ($sql->rowCount() == 1)) {
                    $jresp['message'] = "OK";
                    $jresp['status'] = TRUE;
                } else {
                    error_log($sql->errorInfo()[2]);
                    $jresp['message'] = "Error (4): Unknown or already used session ID - ".$sql->errorCode();
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
                $sql = $conn->prepare("SELECT PublicKey FROM Keepass WHERE SessionId = :SessionId");
                    $sql->bindValue(":SessionId", $sid);
                $sql->execute();
                $result = $sql->fetchAll(PDO::FETCH_ASSOC);
                
                if ($result && count($result) == 1) {                   
                    $jresp['message'] = $result[0]['PublicKey'];
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
        $sql = $conn->prepare("SELECT * FROM Users WHERE UserId = :UserId");
            $sql->bindValue(":UserId", $_SERVER['REMOTE_ADDR']);
        $sql->execute();
        $result = $sql->fetchAll(PDO::FETCH_ASSOC);
        if ($result && count($result) == 1) {
            if($result[0]["SidCreated"] > 4) {
                if(strtotime($result[0]["LastAccess"]) > strtotime("-10 minutes")) {
                    return TRUE;
                } else {
                    $sqlReset = $conn->prepare("UPDATE Users SET SidCreated=1, LastAccess=CURRENT_TIMESTAMP WHERE UserId = :UserId");
                        $sql->bindValue(":UserId", $_SERVER['REMOTE_ADDR']);
                    $sqlReset->execute();
                }
            }
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
                    error_log("MySQL configuration file found but looks wrong!");
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
                $conn -> exec ("CREATE TABLE IF NOT EXISTS [Users] (
                                UserId TEXT PRIMARY KEY NOT NULL,
                                LastAccess DATETIME DEFAULT (CURRENT_TIMESTAMP),
                                SidCreated INTEGER NOT NULL DEFAULT (1) 
                            );");
                $conn -> exec ("CREATE TABLE IF NOT EXISTS [Keepass] (
                                SessionId TEXT NOT NULL PRIMARY KEY,
                                Username TEXT,
                                Psw TEXT,
                                CreationDate DATETIME DEFAULT (CURRENT_TIMESTAMP) NOT NULL,
                                UserId TEXT NOT NULL,
                                PublicKey TEXT NOT NULL,
                                FOREIGN KEY (UserId) REFERENCES Users (UserId)
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
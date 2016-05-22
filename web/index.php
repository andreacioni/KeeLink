<html>
 <head>
  <!-- Basic Page Needs
  –––––––––––––––––––––––––––––––––––––––––––––––––– -->
  <meta charset="utf-8">
  <title>Keepass Test Page</title>
  <meta name="Keepass Test Page" content="">
  <meta name="Andrea Cioni" content="">
    
  <!-- Mobile Specific Metas
  –––––––––––––––––––––––––––––––––––––––––––––––––– -->
  <meta name="viewport" content="width=device-width, initial-scale=1">

  <!-- FONT
  –––––––––––––––––––––––––––––––––––––––––––––––––– -->
  <link href="//fonts.googleapis.com/css?family=Raleway:400,300,600" rel="stylesheet" type="text/css">

  <!-- CSS
  –––––––––––––––––––––––––––––––––––––––––––––––––– -->
  <link rel="stylesheet" href="css/normalize.css">
  <link rel="stylesheet" href="css/skeleton.css">
  <link rel="stylesheet" href="css/sweetalert.css">
  <link rel="stylesheet" href="css/pace.css">
  
  <style>
  	#qrcode {
    	transition-property: opacity;
    	transition-duration: 1.15s;
    	transition-timing-function: ease;
    	transition-delay: 0s;
		transition-property: opacity;
		transition-duration: 1.15s;
		transition-timing-function: ease;
		transition-delay: 0s;
		opacity: 0;
		z-index: 1; 
	}
	
	
	#qrcode img {
		border-style: solid;
		border-color: white;
		border-width: 8px;
	}
	
	.content-font {
		color:  #363533;
		font-size: 30px;
	}
  
  </style>

  <!-- Favicon
  –––––––––––––––––––––––––––––––––––––––––––––––––– -->
  <link rel="icon" type="image/ico" href="images/favicon.ico">
  
  <!-- JS LIBARY -->
  <script type="text/javascript" src="lib/pace.js"></script>
  <script type="text/javascript" src="lib/pace.js"></script>
  <script type="text/javascript" src="lib/jquery.min.js"></script>
  <script type="text/javascript" src="lib/qrcode.js"></script>
  <script type="text/javascript" src="lib/sweetalert.min.js"></script>
  <script type="text/javascript" src="lib/clipboard.min.js"></script>

  <script type="text/javascript" src="lib/main.js"></script>
  
  
  <?php
  
  	$randomsid = getSid();
	
	function getSid() {
		return hash("md5",openssl_random_pseudo_bytes(256));
	}
	
	function saveSid($sid) {
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
		
		$sqlInsert = "INSERT INTO `KEEPASS`(`SESSION_ID`) VALUES ('" . $sid . "')";
		$sqlDelete = "DELETE FROM `KEEPASS` WHERE DATE_ADD(`CREATION_DATE`,INTERVAL 2 MINUTE) < NOW() ";
		
		if ($conn->query($sqlDelete) === TRUE) {
			//echo "<br>Old records deleted";
		} else {
			echo "<br>Error: " . $sqlDelete . "<br>" . $conn->error;
		}

		if ($conn->query($sqlInsert) === TRUE) {
			//echo "<br>New record created successfully";
		} else {
			echo "<br>Error: " . $sqlInsert . "<br>" . $conn->error;
		}

		$conn->close();
	}
	
	saveSid($randomsid);
  ?>
  
 </head>
 <body onload="init('<?php echo $randomsid ?>')" >
 	
	<!-- Top -->
	<div style="height:7%;"></div>
 	
	 <!-- Center -->
	 <div>
		<div style="height:15%"></div>
		<div class="container">
		<div class="row">
			<div class="twelve columns"><p class="content-font" align="center"><b>Use this QR code to share a password from Keepass to this device</b></p></div>
			<div class="twelve columns">&nbsp;</div>
			<div class="twelve columns"><div align="center" id="qrcode"></div></div>
			<div class="twelve columns">&nbsp;</div>
			<div class="twelve columns"><p class="content-font" style="font-size:small" align="center"><b>Your Session ID: <span id="sidLabel"><?php echo $randomsid; ?></span></b></p></div>
		</div>
		</div>
	</div>
	
	<!-- Footer -->
	<div class="container">
		<div class="row" style="height: 200px;">
			<div class="twelve columns"><p style="font-size: 58px;font-weight: bold;color: red;"align="center"></p></div>
		</div>
	</div>

 </body>
</html>
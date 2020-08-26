# KeeLink

<p align="center">
  <img height="140" src="https://github.com/andreacioni/KeeLink/raw/master/misc/images/1464190636_flat_icons-graficheria.it-01.png">
</p>

<p align="center"><i>a Keepass2Android Plug-In</i></p>

<p>More info and <b>online service</b>: <a href="https://keelink.cloud?onlyinfo=true">https://keelink.cloud</a>

<p>Based on work of:
<ul>
  <li>PhilipC - <a>http://keepass2android.codeplex.com/</a></li>
  <li>XZing - <a>https://github.com/zxing/zxing</a></li>
</ul>

</p>
<br/>
<br/>
<p><b>HOW TO SELFHOST</b> (Requirements: PHP and SQLITE or MYSQL)</p>
<ul>
  <li>Copy the content of the `web` folder on your webserver</li>
  <li>By default it starts with SQLite (it's enough for few users)</li>
  <li>In order to switch to MySQL (for many users):</li>
  <ul>
    <li>Copy the file `private/config.default.ini` to `private/config.ini` and edit it with MySQL settings</li>
    <li>Manually initialize the MySQL DB (IE with phpMyAdmin) with the file `private/InitMySQL.sql`</li>
  </ul>
  <li>If you are not using Apache, make sure to deny access to the "private" folder from web because it contains your SQLite DB and your MySQL credentials</li>
</ul>
</p>

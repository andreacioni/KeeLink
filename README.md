# KeeLink
<p align="center">
  <img height="140" src="https://github.com/andreacioni/KeeLink/raw/master/misc/images/1464190636_flat_icons-graficheria.it-01.png">
</p>

<p align="center"><i>a Keepass2Android Plug-In</i></p>

More info and **online service** => [https://keelink.cloud](https://keelink.cloud?onlyinfo=true)

---

**HOW TO SELFHOST** (Requirements: PHP and SQLITE or MYSQL)
* Copy the content of the `web` folder on your webserver</li>
* By default it starts with SQLite (it's enough for few users)</li>
* In order to switch to MySQL (for many users):
    * Copy the file `private/config.default.ini` to `private/config.ini` and edit it with MySQL settings
    * Manually initialize the MySQL DB (IE with phpMyAdmin) with the file `private/InitMySQL.sql`
* If you are not using Apache, make sure to deny access to the "private" folder from web because it contains your SQLite DB and your MySQL credentials

---

Based on work of:
* PhilipC - https://github.com/PhilippC/keepass2android
* XZing - https://github.com/zxing/zxing

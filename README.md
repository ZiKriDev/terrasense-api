<p align="center">
  <img src="https://i.imgur.com/HnBxH4L.png" alt="Logo">
</p>
<br>

<h2 align="center">TerraSense API</h2>

<p align="center">TerraSense is a desktop application responsible for making it easier for end users to monitor environments and equipment with temperature and humidity sensors. The TerraSense API is just the part of the app responsible for managing access to microcontroller data and the temperature and humidity measurements they send.</p>

## 💻 Prerequisites

Before you begin, make sure you've met the following requirements:

- You have installed `Java` version v17+ - [Java download](https://www.azul.com/downloads/?package=jdk#zulu)
- You have installed the latest version of `Cassandra DB` - [Cassandra download](https://cassandra.apache.org/_/download.html)
- Your device is already configured in Tasmota (or other firmware) to send measurements using the WebQuery feature - [See this](https://tasmota.github.io/docs/Commands/#control)
- (OPTIONAL) Your device (SONOFF, Smart Switch or any Expressif chipset) has `Tasmota` v14+ firmware if you want to use the device control features - [Tasmota install](https://tasmota.github.io/docs/Getting-Started/)
- (OPTIONAL) You have installed `Maven` v3.9+ if you want to build your own artifact - [Maven download](https://maven.apache.org/download.cgi)
- (OPTIONAL) You have the ``WhatsApp API`` if you want to use the reporting features through the WhatsApp app - [WhatsApp API download](https://github.com/chrishubert/whatsapp-api)

## 🚀 Installation and Running

To run the TerraSense API, follow the steps below:

- Download the latest jar artifact from the "Actions" section.
- Navigate to the folder where the artifact is located.
- In the application folder, define a script like this:

> 
> Windows (.bat):
> ```
> @ECHO OFF
> SET BINDIR=%~dp0
> CD /D "%BINDIR%"
>
> REM (spring.mail.username) Email to send password recovery and registration 
> messages
> REM (spring.mail.password) Email app password (not your personal password)
> REM (api.security.token.secret) Secret for JWT token 
> REM (api.whatsapp.token.secret) WhatsApp API Secret
> REM (api.whatsapp.session.id) WhatsApp Session ID
> REM (api.whatsapp.url) WhatsApp API URL (for requests)
> REM (img.storage.path) Folder path to store images (like signatures)
> REM (spring.cassandra.contact-points) Contact Points (for Cassandra DB)
> REM (spring.cassandra.port) Cassandra Port (for Cassandra DB)
> REM (spring.cassandra.keyspace-name) Keyspace Name (for Cassandra DB)
> REM (spring.cassandra.local-datacenter) Local Datacenter (for Cassandra DB)
> REM (spring.cassandra.username) Login user (for Cassandra DB)
> REM (spring.cassandra.password) Login password (for Cassandra DB)
>
> :start
> java -Dspring.mail.username=testemail@gmail.com ^
>      -Dspring.mail.password=my-pass ^
>      -Dapi.security.token.secret=my-secret-key ^
>      -Dapi.whatsapp.token.secret=my-whatsapp-secret ^
>      -Dapi.whatsapp.session.id=myWhatsAppSessionID ^
>      -Dapi.whatsapp.url=http://yourUrlToWpp/api ^
>      -Dimg.storage.path=/path/to/storage ^
>      -Dspring.cassandra.contact-points=localhost ^
>      -Dspring.cassandra.port=9042 ^
>      -Dspring.cassandra.keyspace-name=sensordata ^
>      -Dspring.cassandra.local-datacenter=datacenter1 ^
>      -Dspring.cassandra.username=root ^
>      -Dspring.cassandra.password=admin ^
> -jar terrasense-api.jar
> ```
> ⚠️ Modify the environment variables in the file according to your needs! ⚠️

> Linux and macOS (.sh):
> ```
> #!/bin/bash
>
> BINDIR=$(dirname "$0")
> cd "$BINDIR" || exit 1
>
> # (spring.mail.username) Email to send password recovery and registration messages
> # (spring.mail.password) Email app password (not your personal password)
> # (api.security.token.secret) Secret for JWT token
> # (api.whatsapp.token.secret) WhatsApp API Secret
> # (api.whatsapp.session.id) WhatsApp Session ID
> # (api.whatsapp.url) WhatsApp API URL (for requests)
> # (img.storage.path) Folder path to store images (like signatures)
> # (spring.cassandra.contact-points) Contact Points (for Cassandra DB)
> # (spring.cassandra.port) Cassandra Port (for Cassandra DB)
> # (spring.cassandra.keyspace-name) Keyspace Name (for Cassandra DB)
> # (spring.cassandra.local-datacenter) Local Datacenter (for Cassandra DB)
> # (spring.cassandra.username) Login user (for Cassandra DB)
> # (spring.cassandra.password) Login password (for Cassandra DB)
>
> java -Dspring.mail.username=testemail@gmail.com \
>      -Dspring.mail.password=my-pass \
>      -Dapi.security.token.secret=my-secret-key \
>      -Dapi.whatsapp.token.secret=my-whatsapp-secret \
>      -Dapi.whatsapp.session.id=myWhatsAppSessionID \
>      -Dapi.whatsapp.url=http://yourUrlToWpp/api \
>      -Dimg.storage.path=/path/to/storage \
>      -Dspring.cassandra.contact-points=localhost \
>      -Dspring.cassandra.port=9042 \
>      -Dspring.cassandra.keyspace-name=sensordata \
>      -Dspring.cassandra.local-datacenter=datacenter1 \
>      -Dspring.cassandra.username=root \
>      -Dspring.cassandra.password=admin \
> -jar terrasense-api.jar
> ```
> ⚠️ Modify the environment variables in the file according to your needs! ⚠️
> 

- Run the script and you're done! The application will be available at _http://localhost:8087_ 😃

## 🔥 Features

(Coming soon!)

## 🔧 Tests

(Coming soon!)

## 📚 Resources

(Coming soon!)

## 🤝 Contributions

(Coming soon!)

## 📝 License

(Coming soon!)

## 💰 Donate

(Coming soon!)
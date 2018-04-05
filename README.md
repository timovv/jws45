DiscordJava
===========

Work in progress, nowhere near complete
---------------------------------------

This is a personal project (not intended for production use) to create a chat client for the 
[Discord](https://discord.gg/) service. My goal is to implement this using the Java standard library (i.e. no third
party libraries at all).

Planned features
----------------
###Working
* Implementation of a WebSocket client compatible with RFC 6455
    * Client is based on the NIO library, particularly `java.nio.SocketChannel`, and is fully asynchronous.
    * SSL support based on `javax.net.SSLEngine` (mostly working, untested)
    


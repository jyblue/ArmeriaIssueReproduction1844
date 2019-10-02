# Armeria issue#1844 reproduction
## Issue
https://github.com/line/armeria/issues/1844
## Test key generation command
### JKS Key
```
keytool -genkeypair -keyalg RSA -keysize 2048 -storetype JKS -keystore keyStore01.jks
```
### PKCS Key
```
keytool -genkeypair -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keyStore01.p12
```
## Result
### Case 01
* Key Store Type : "JKS"
* Key Store Password : **given**
* Key Password : given
* Server Start : pass
* Https Request : pass
* Result : no target exception occurs
### Case 02
* Key Store Type : "JKS"
* Key Store Password : **null**
* Key Password : given
* Server Start : pass
* Https Request : pass
* Result : **no target exception occurs**
### Case 03
* Key Store Type : "PKCS12"
* Key Store Password : **given**
* Key Password : given
* Server Start : pass
* Https Request : pass
* Result : no target exception occurs
### Case 04
* Key Store Type : "PKCS12"
* Key Store Password : **null**
* Key Password : given
* Server Start : pass
* Https Request : fail
* Result : **target exception occurs**
``` java
[armeria-common-worker-epoll-2-1] WARN com.linecorp.armeria.server.HttpServerPipelineConfigurator - [id: 0x3b82c32a, L:/127.0.0.1:8081 - R:/127.0.0.1:41596] TLS handshake failed:
javax.net.ssl.SSLHandshakeException: error:100000ae:SSL routines:OPENSSL_internal:NO_CERTIFICATE_SET
	at io.netty.handler.ssl.ReferenceCountedOpenSslEngine.shutdownWithError(ReferenceCountedOpenSslEngine.java:965)
	at io.netty.handler.ssl.ReferenceCountedOpenSslEngine.sslReadErrorResult(ReferenceCountedOpenSslEngine.java:1231)
	at io.netty.handler.ssl.ReferenceCountedOpenSslEngine.unwrap(ReferenceCountedOpenSslEngine.java:1185)
	at io.netty.handler.ssl.ReferenceCountedOpenSslEngine.unwrap(ReferenceCountedOpenSslEngine.java:1256)
	at io.netty.handler.ssl.ReferenceCountedOpenSslEngine.unwrap(ReferenceCountedOpenSslEngine.java:1299)
	at io.netty.handler.ssl.SslHandler$SslEngineType$1.unwrap(SslHandler.java:204)
	at io.netty.handler.ssl.SslHandler.unwrap(SslHandler.java:1329)
	at io.netty.handler.ssl.SslHandler.decodeNonJdkCompatible(SslHandler.java:1236)
	at io.netty.handler.ssl.SslHandler.decode(SslHandler.java:1273)
	at io.netty.handler.codec.ByteToMessageDecoder.decodeRemovalReentryProtection(ByteToMessageDecoder.java:505)
	at io.netty.handler.codec.ByteToMessageDecoder.callDecode(ByteToMessageDecoder.java:444)
	at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:283)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:374)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:360)
	at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:352)
	at io.netty.handler.codec.ByteToMessageDecoder.handlerRemoved(ByteToMessageDecoder.java:256)
	at io.netty.handler.codec.ByteToMessageDecoder.decodeRemovalReentryProtection(ByteToMessageDecoder.java:510)
	at io.netty.handler.codec.ByteToMessageDecoder.callDecode(ByteToMessageDecoder.java:444)
	at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:283)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:374)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:360)
	at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:352)
	at io.netty.handler.flush.FlushConsolidationHandler.channelRead(FlushConsolidationHandler.java:154)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:374)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:360)
	at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:352)
	at io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1421)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:374)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:360)
	at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:930)
	at io.netty.channel.epoll.AbstractEpollStreamChannel$EpollStreamUnsafe.epollInReady(AbstractEpollStreamChannel.java:794)
	at io.netty.channel.epoll.EpollEventLoop.processReady(EpollEventLoop.java:424)
	at io.netty.channel.epoll.EpollEventLoop.run(EpollEventLoop.java:326)
	at io.netty.util.concurrent.SingleThreadEventExecutor$5.run(SingleThreadEventExecutor.java:918)
	at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
	at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
	at java.base/java.lang.Thread.run(Thread.java:834)
```

## Internal cause
Exception occurs when netty SslHander is trying to decode(unwrap) clinet's first record(packet)  
https://github.com/netty/netty/blob/d8b1a2d93f556a08270e6549bf7f91b3b09f24bb/handler/src/main/java/io/netty/handler/ssl/SslHandler.java#L1329


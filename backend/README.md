首先需要自己准备一个mysql数据库和redis，在application.yaml文件中填写对应的服务器地址信息和密码即可。

当我在服务端使用openJDK8运行时，会出现`javax.net.ssl.SSLHandshakeException: No appropriate protocol`的错误，

> 解决问题在于，修改java自己的文件，jre/lib/security/java.security
>
> ```
> # Example:
> #   jdk.tls.disabledAlgorithms=MD5, SSLv3, DSA, RSA keySize < 2048
> jdk.tls.disabledAlgorithms=SSLv3, TLSv1, TLSv1.1, RC4, DES, MD5withRSA, \
>     DH keySize < 1024, EC keySize < 224, 3DES_EDE_CBC, anon, NULL, \
>     include jdk.disabled.namedCurves
> ```
>
> 移除`SSLv3, TLSv1, TLSv1.1,`

如果是官方的JDK应该就不会存在问题。

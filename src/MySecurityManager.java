import java.security.Permission;
//classe temporanea per testare il resto
class MySecurityManager extends SecurityManager {
    @Override
    public void checkConnect(String host, int port, Object context)
    { }
    @Override
    public void checkPermission(Permission perm)
    { }
}

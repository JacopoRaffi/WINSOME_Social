import java.security.Permission;

class MySecurityManager extends SecurityManager {
    @Override
    public void checkConnect(String host, int port, Object context)
    { }
    @Override
    public void checkPermission(Permission perm)
    { }
}

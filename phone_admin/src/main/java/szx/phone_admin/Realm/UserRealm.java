package szx.phone_admin.Realm;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import szx.phone_admin.dao.ModuleMapper;
import szx.phone_admin.dao.RoleMapper;
import szx.phone_admin.dao.UserMapper;
import szx.phone_admin.entity.Module;
import szx.phone_admin.entity.Role;
import szx.phone_admin.entity.User;

import java.util.List;
@Repository
public class UserRealm extends AuthorizingRealm {
    @Autowired(required = false)
    private UserMapper userMapper;
    @Autowired(required = false)
    private RoleMapper roleMapper;
    @Autowired(required = false)
    private ModuleMapper moduleMapper;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        //获得登录的账号
        String username = (String) principalCollection.getPrimaryPrincipal();
        //创建授权信息对象
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        //通过账号查询角色，添加角色
        List<Role> roles = roleMapper.selectRoleByAccount(username);
        roles.forEach((role -> info.addRole(role.getRoleName())));
        //通过账号查询权限，添加权限
        List<Module> modules = moduleMapper.selectModuleByAccount(username);
        modules.forEach((module -> info.addStringPermission(module.getModuleName())));
        return info;
    }

    //身份验证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        //获得账号和密码
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        String username = token.getUsername();
        String password = new String(token.getPassword());
        //通过用户名查询用户
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("user_login_account ", username));
        //如果用户名不存在就抛出异常
        if(user == null){
            throw new UnknownAccountException("不存在的用户");
        }
        //返回验证信息,1）用户名 2）数据库的正确密码 3) 盐 4) Realm名字
        return new SimpleAuthenticationInfo(username,user.getUserLoginPwd(),
                ByteSource.Util.bytes(user.getUserLoginAccount()),getName());
    }
}

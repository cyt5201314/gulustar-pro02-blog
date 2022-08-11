package gulustar.service.impl;

import gulustar.mapper.UserMapper;
import gulustar.pojo.User;
import gulustar.service.UserService;
import gulustar.util.SqlSessionFactoryUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

public class UserServiceImpl implements UserService {

    SqlSessionFactory sqlSessionFactory = SqlSessionFactoryUtils.getSqlSessionFactory();

    /**
     * 登录方法：根据账号密码查询用户，并封装成对象返回
     * @param account
     * @param password
     * @return
     */
    @Override
    public User login(String account, String password) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

        User user = userMapper.selectByAccAndPwd(account, password);
        sqlSession.close();
        return user;
    }

    /**
     * 注册方法：把用户信息封装为对象调用mapper方法添加到数据库
     * @return
     */
    @Override
    public boolean registe(User user) {
        //获取userMapper对象 调用其方法
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

        //获取用户信息
        String account = user.getAccount();
        String username = user.getUsername();

        //确认同名用户是否存在
        User isExsitsAcc = userMapper.selectByAccount(account);
        User isExsitsNikename = userMapper.selectByUsername(username);
        if (isExsitsAcc == null){
            if (isExsitsNikename == null){
                //如果不存在同名同账号用户、将user对象存到数据库
                user.init();
                boolean insertRes = userMapper.addUser(user);
                sqlSession.commit();
                sqlSession.close();

                return insertRes;
            }
            else {
                //如果昵称存在，则注册失败
                sqlSession.close();
                return false;
            }
        }else{
            //如果账户存在，则注册失败
            sqlSession.close();
            return false;
        }
    }
}

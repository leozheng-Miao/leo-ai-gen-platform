package com.leo.leoaigenplatform.service;

import com.leo.leoaigenplatform.model.dto.user.LoginUser;
import com.leo.leoaigenplatform.model.dto.user.UserAddRequest;
import com.leo.leoaigenplatform.model.dto.user.UserQueryRequest;
import com.leo.leoaigenplatform.model.dto.user.UserUpdateRequest;
import com.leo.leoaigenplatform.model.vo.LoginUserVO;
import com.leo.leoaigenplatform.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.leo.leoaigenplatform.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author Leo
 * @since 1.0.1
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return 返回注册好的用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 获取用户脱敏后的信息
     * 把数据库 User -》 前端展示 LoginUserVO
     *
     * @param user
     * @return
     */
    LoginUserVO convertToLoginUserVO(User user);

    /**
     * 用户登录
     *
     * @param userAccount
     * @param userPassword
     * @param request
     * @return 返回脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 从 Session 中取出当前用户的 ‘登录态’
     *
     * @param request
     * @return 返回最新的用户信息
     */
    LoginUser getLoginUser(HttpServletRequest request);

    /**
     * 基于当前登陆态，返回当前用户的展示信息
     *
     * @param request
     * @return
     */
    LoginUserVO getCurrentUserVO(HttpServletRequest request);

    /**
     * 用户退出登录
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);

    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);


    boolean updateUser(UserUpdateRequest userUpdateRequest);

    long addUser(UserAddRequest userAddRequest);
}

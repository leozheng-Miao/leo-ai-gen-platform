package com.leo.leoaigenplatform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.leo.leoaigenplatform.constant.UserConstant;
import com.leo.leoaigenplatform.exception.BusinessException;
import com.leo.leoaigenplatform.exception.ErrorCode;
import com.leo.leoaigenplatform.model.dto.user.LoginUser;
import com.leo.leoaigenplatform.model.dto.user.UserAddRequest;
import com.leo.leoaigenplatform.model.dto.user.UserQueryRequest;
import com.leo.leoaigenplatform.model.dto.user.UserUpdateRequest;
import com.leo.leoaigenplatform.model.vo.LoginUserVO;
import com.leo.leoaigenplatform.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.leo.leoaigenplatform.model.entity.User;
import com.leo.leoaigenplatform.mapper.UserMapper;
import com.leo.leoaigenplatform.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 用户 服务层实现。
 *
 * @author Leo
 * @since 1.0.1
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {

        //1. 校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号长度不能小于4");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码长度不能小于8");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }
        //2. 检查是否已经注册
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("isDelete", 0);
        long res = this.mapper.selectCountByQuery(queryWrapper);
        if (res > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号已存在");
        }
        //3. 密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        //3. 注册用户
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean save = this.save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败, 数据库错误");
        }
        //4. 返回id
        return user.getId();
    }

    @Override
    public LoginUserVO convertToLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1. 校验参数
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号长度不能小于4");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码长度不能小于8");
        }
        //2. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        //2. 检查用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        //3. 登录
        LoginUser loginUser = new LoginUser();
        BeanUtil.copyProperties(user, loginUser);
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, loginUser);
        //4. 返回用户id
        return this.convertToLoginUserVO(user);
    }

    @Override
    public LoginUser getLoginUser(HttpServletRequest request) {
        LoginUser currentLoginUser = (LoginUser) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (currentLoginUser == null || currentLoginUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        User currUser = this.getById(currentLoginUser.getId());
        if (currUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户不存在");
        }
        BeanUtil.copyProperties(currUser, currentLoginUser);
        return currentLoginUser;
    }

    @Override
    public LoginUserVO getCurrentUserVO(HttpServletRequest request) {
        LoginUser loginUser = getLoginUser(request); // 只负责“是否登录”
        User user = this.getById(loginUser.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return convertToLoginUserVO(user);
    }


    @Override
    public boolean userLogout(HttpServletRequest request) {
        Object user = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        QueryWrapper queryWrapper = QueryWrapper.create();
        // ===== 查询条件 =====
        queryWrapper
                .eq("id", id, id != null)
                .eq("userRole", userRole, StrUtil.isNotBlank(userRole))
                .like("userAccount", userAccount, StrUtil.isNotBlank(userAccount))
                .like("userName", userName, StrUtil.isNotBlank(userName))
                .like("userProfile", userProfile, StrUtil.isNotBlank(userProfile))
                .orderBy(sortField, "ascend".equals(sortOrder));
        // 排序（白名单）
        if (StrUtil.isNotBlank(sortField) && isValidSortField(sortField)) {
            boolean isAsc = "ascend".equalsIgnoreCase(sortOrder);
            queryWrapper.orderBy(sortField, isAsc);
        }
        return queryWrapper;

    }

    @Override
    public boolean updateUser(UserUpdateRequest userUpdateRequest) {

        if (userUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        String currRole = getById(userUpdateRequest.getId()).getUserRole();
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        user.setUserRole(Objects.equals(userUpdateRequest.getUserRole(), "") ? currRole : userUpdateRequest.getUserRole());
        return this.updateById(user);
    }

    @Override
    public long addUser(UserAddRequest userAddRequest) {
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        user.setUserPassword(getEncryptPassword(UserConstant.DEFAULT_PASSWORD));
        boolean save = this.save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加用户失败");
        }
        return user.getId();
    }

    /**
     * 密码加密
     *
     * @param userPassword
     * @return
     */
    private String getEncryptPassword(String userPassword) {
        final String SALT = "leozzz";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    /**
     * sql 白名单方法
     *
     * @param sortField
     * @return
     */
    private boolean isValidSortField(String sortField) {
        return List.of(
                "id",
                "userAccount",
                "userName",
                "userRole",
                "createTime"
        ).contains(sortField);
    }

}

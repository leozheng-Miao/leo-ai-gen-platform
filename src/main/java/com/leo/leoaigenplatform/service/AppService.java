package com.leo.leoaigenplatform.service;

import com.leo.leoaigenplatform.model.dto.app.*;
import com.leo.leoaigenplatform.model.dto.user.LoginUser;
import com.leo.leoaigenplatform.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.leo.leoaigenplatform.model.entity.App;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author Leo
 * @since 1.0.1
 */
public interface AppService extends IService<App> {

    /**
     * 聊天
     * @param userMessage
     * @param appId
     * @param loginUser
     * @return
     */
    Flux<String> chatToGenCode(String userMessage, Long appId, LoginUser loginUser);

    /**
     * 将本地项目部署
     * @param appId
     * @param loginUser
     * @return
     */
    String deployApp(Long appId, LoginUser loginUser);

    /**
     * 创建应用
     *
     * @param appAddRequest 应用添加请求
     * @param request HTTP请求
     * @return 应用id
     */
    Long addApp(AppAddRequest appAddRequest, HttpServletRequest request);

    /**
     * 用户更新应用（只支持修改应用名称）
     *
     * @param appUpdateRequest 应用更新请求
     * @param request HTTP请求
     * @return 是否成功
     */
    Boolean updateApp(AppUpdateRequest appUpdateRequest, HttpServletRequest request);

    /**
     * 用户删除应用
     *
     * @param id 应用id
     * @param request HTTP请求
     * @return 是否成功
     */
    Boolean deleteApp(Long id, HttpServletRequest request);

    /**
     * 用户根据id查看应用详情
     *
     * @param id 应用id
     * @param request HTTP请求
     * @return 应用视图对象
     */
    AppVO getAppVOById(Long id, HttpServletRequest request);

    /**
     * 用户分页查看自己的应用列表（支持根据名称查询，每页最多20个）
     *
     * @param appQueryRequest 应用查询请求
     * @param request HTTP请求
     * @return 分页结果
     */
    com.mybatisflex.core.paginate.Page<AppVO> listMyAppByPage(AppQueryRequest appQueryRequest, HttpServletRequest request);

    /**
     * 用户分页查询精选应用列表（支持根据名称查询，每页最多20个）
     *
     * @param appQueryRequest 应用查询请求
     * @return 分页结果
     */
    com.mybatisflex.core.paginate.Page<AppVO> listFeaturedAppByPage(AppQueryRequest appQueryRequest);

    /**
     * 管理员根据id删除任意应用
     *
     * @param id 应用id
     * @return 是否成功
     */
    Boolean adminDeleteApp(Long id);

    /**
     * 管理员根据id查询任意应用详情
     *
     * @param id 应用id
     * @return 应用视图对象
     */
    AppVO adminGetAppVOById(Long id);

    /**
     * 管理员根据id更新任意应用（支持更新应用名称、应用封面、优先级）
     *
     * @param appAdminUpdateRequest 管理员应用更新请求
     * @return 是否成功
     */
    Boolean adminUpdateApp(AppAdminUpdateRequest appAdminUpdateRequest);

    /**
     * 管理员分页查询应用列表（支持根据除时间外的任何字段查询，每页数量不限）
     *
     * @param appAdminQueryRequest 管理员应用查询请求
     * @return 分页结果
     */
    com.mybatisflex.core.paginate.Page<AppVO> adminListAppByPage(AppAdminQueryRequest appAdminQueryRequest);

    /**
     * 获取应用视图对象
     *
     * @param app 应用实体
     * @return 应用视图对象
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用视图对象列表
     *
     * @param appList 应用实体列表
     * @return 应用视图对象列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 获取用户查询条件包装器
     *
     * @param appQueryRequest 应用查询请求
     * @param userId 用户id
     * @return 查询条件包装器
     */
    QueryWrapper getUserQueryWrapper(AppQueryRequest appQueryRequest, Long userId);

    /**
     * 获取精选应用查询条件包装器
     *
     * @param appQueryRequest 应用查询请求
     * @return 查询条件包装器
     */
    QueryWrapper getFeaturedQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获取管理员查询条件包装器
     *
     * @param appAdminQueryRequest 管理员应用查询请求
     * @return 查询条件包装器
     */
    QueryWrapper getAdminQueryWrapper(AppAdminQueryRequest appAdminQueryRequest);
}
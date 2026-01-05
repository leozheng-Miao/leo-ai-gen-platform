package com.leo.leoaigenplatform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.leo.leoaigenplatform.constant.AppConstant;
import com.leo.leoaigenplatform.constant.UserConstant;
import com.leo.leoaigenplatform.core.AiGenCodeFacade;
import com.leo.leoaigenplatform.core.builder.VueProjectBuilder;
import com.leo.leoaigenplatform.core.handler.StreamHandlerExecutor;
import com.leo.leoaigenplatform.exception.BusinessException;
import com.leo.leoaigenplatform.exception.ErrorCode;
import com.leo.leoaigenplatform.exception.ThrowUtils;
import com.leo.leoaigenplatform.manager.CosManager;
import com.leo.leoaigenplatform.mapper.AppMapper;
import com.leo.leoaigenplatform.model.dto.app.AppAddRequest;
import com.leo.leoaigenplatform.model.dto.app.AppAdminQueryRequest;
import com.leo.leoaigenplatform.model.dto.app.AppAdminUpdateRequest;
import com.leo.leoaigenplatform.model.dto.app.AppQueryRequest;
import com.leo.leoaigenplatform.model.dto.app.AppUpdateRequest;
import com.leo.leoaigenplatform.model.dto.user.LoginUser;
import com.leo.leoaigenplatform.model.entity.App;
import com.leo.leoaigenplatform.model.entity.User;
import com.leo.leoaigenplatform.model.enums.CodeGenType;
import com.leo.leoaigenplatform.model.enums.MessageType;
import com.leo.leoaigenplatform.model.vo.AppVO;
import com.leo.leoaigenplatform.model.vo.UserVO;
import com.leo.leoaigenplatform.service.AppService;
import com.leo.leoaigenplatform.service.ChatHistoryService;
import com.leo.leoaigenplatform.service.ScreenshotService;
import com.leo.leoaigenplatform.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author Leo
 * @since 1.0.1
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Value("${code.deploy-host:http://localhost:8090}")
    private String deployHost;
    @Resource
    private UserService userService;

    @Resource
    private AiGenCodeFacade aiGenCodeFacade;

    @Resource
    private ChatHistoryService chatHistoryService;
    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;
    @Resource
    private VueProjectBuilder vueProjectBuilder;
    @Resource
    private ScreenshotService screenshotService;
    @Resource
    private CosManager cosManager;

    @Override
    public Flux<String> chatToGenCode(String userMessage, Long appId, LoginUser loginUser) {
        ThrowUtils.throwIf(StrUtil.isBlank(userMessage), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID必须大于0");
        App currApp = this.getById(appId);
        ThrowUtils.throwIf(currApp == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        ThrowUtils.throwIf(!currApp.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限访问此应用");
        String codeGenType = currApp.getCodeGenType();
        CodeGenType type = CodeGenType.of(codeGenType);
        ThrowUtils.throwIf(type == null, ErrorCode.PARAMS_ERROR, "不支持的代码生成类型");

        // 存储用户消息 userMessage
        chatHistoryService.addChatMessage(appId, userMessage, MessageType.USER.getValue(), loginUser.getId(), null);

        // 存储 AI 消息 aiMessage
        Flux<String> AIMessageStream = aiGenCodeFacade.generateAndSaveCodeStream(userMessage, type, appId);
        return streamHandlerExecutor.doExecute(AIMessageStream, chatHistoryService, appId, loginUser, type);
    }

    @Override
    public String deployApp(Long appId, LoginUser loginUser) {
        //1. 参数校验
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID必须大于0");
        //2. 查询应用信息
        App currApp = this.getById(appId);
        ThrowUtils.throwIf(currApp == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        //3. 权限校验
        ThrowUtils.throwIf(!currApp.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限部署此应用");
        //4. 检查是否已有 deployKey
        String deployKey = currApp.getDeployKey();
        // 若没有 deployKey 则生成
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }

        //5. 获取代码生成类型， 获取原始代码生成路径 （应用访问目录）
        String codeGenType = currApp.getCodeGenType();
        String originCodePath = AppConstant.FILE_SAVE_ROOT_DIR + File.separator + codeGenType + "_" + appId;

        //6. 检查路径是否存在
        File originCodeFile = new File(originCodePath);
        if (!originCodeFile.exists()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用代码不存在");
        }
        //7. 检查是否是 vue 项目 - 执行构建
        CodeGenType codeGenTypeEnum = CodeGenType.of(codeGenType);
        // 若是，则使用 vueProjectBuilder 部署项目，并将文件改为 dist 目录下
        if (codeGenTypeEnum == CodeGenType.VUE_PROJECT) {
            boolean buildSuccess = vueProjectBuilder.buildProject(originCodePath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue项目构建失败");
            File dist = new File(originCodePath, "dist");
            ThrowUtils.throwIf(!dist.exists(), ErrorCode.SYSTEM_ERROR, "Vue 构建项目完成但 未生成 dist 目录");
            originCodeFile = dist;
            log.info("Vue 项目构建成功， 将部署 dist目录：{}", dist.getAbsolutePath());
        }
        //8. 复制文件到部署目录
        String localCodePath = AppConstant.FILE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(originCodeFile, new File(localCodePath), true);
        } catch (IORuntimeException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "部署应用转移文件失败" + e.getMessage());
        }
        //9. 更新数据库
        App app = new App();
        app.setId(appId);
        app.setDeployKey(deployKey);
        app.setDeployedTime(LocalDateTime.now());
        boolean result = this.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        //10. 构建 应用访问 url
        String appDeployUrl = String.format("%s/%s/", deployHost, deployKey);
        //11. 异步生成应用封面
        generateAppScreenshotAsync(appId, appDeployUrl);
        return appDeployUrl;
    }

    /**
     * 虚拟线程生成应用封面并更新封面
     * @param appId
     * @param url
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String url) {
        Thread.startVirtualThread(() -> {
            String cosUrl = screenshotService.generateAddUploadScreenshot(url);
            ThrowUtils.throwIf(StrUtil.isBlank(cosUrl), ErrorCode.OPERATION_ERROR, "生成截图服务失败");
            // 更新 app 信息到数据库
            App app = new App();
            app.setCover(cosUrl);
            app.setId(appId);
            boolean result = this.updateById(app);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新应用截图信息失败");
        });
    }


    @Override
    public Long addApp(AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        String appName = appAddRequest.getAppName();
        String initPrompt = appAddRequest.getInitPrompt();
        if (StrUtil.isBlank(appName)) {
            appName = aiGenCodeFacade.generateAppName(initPrompt);
        }

        // 校验参数
        if (StrUtil.isBlank(initPrompt)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "initPrompt不能为空");
        }

        // 获取当前登录用户
        LoginUser loginUser = userService.getLoginUser(request);

        // 创建应用
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        app.setCodeGenType(CodeGenType.VUE_PROJECT.getCode());
        app.setAppName(appName);
        boolean save = this.save(app);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "创建应用失败");
        return app.getId();
    }

    @Override
    public Boolean updateApp(AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);

        // 获取当前登录用户
        LoginUser loginUser = userService.getLoginUser(request);

        // 查询应用
        App app = this.getById(appUpdateRequest.getId());
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 验证是否为应用所有者
        if (!app.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改此应用");
        }

        // 更新应用名称
        if (StrUtil.isNotBlank(appUpdateRequest.getAppName())) {
            app.setAppName(appUpdateRequest.getAppName());
        }
        app.setEditTime(LocalDateTime.now());
        boolean result = this.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        return true;
    }

    @Override
    public Boolean deleteApp(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        // 获取当前登录用户
        LoginUser loginUser = userService.getLoginUser(request);

        // 查询应用
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 验证是否为应用所有者
        if (!app.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除此应用");
        }
        // 获取 应用封面图访问路径
        String coverUrl = app.getCover();
        // 删除应用
        boolean deleteAppResult = this.removeById(id);
        ThrowUtils.throwIf(!deleteAppResult, ErrorCode.OPERATION_ERROR);
        // 删除 cos 封面图
        if (StrUtil.isNotBlank(coverUrl)) {
            String objectKeyName = coverUrl.substring(coverUrl.indexOf("screenshots"));
            cosManager.deleteObject(objectKeyName);
        }
        return true;
    }

    @Override
    public AppVO getAppVOById(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        // 获取当前登录用户
        LoginUser loginUser = userService.getLoginUser(request);

        // 查询应用
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 验证是否为应用所有者
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()) || !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()), ErrorCode.NO_AUTH_ERROR, "无权限查看此应用");

        return getAppVO(app);
    }

    @Override
    public Page<AppVO> listMyAppByPage(AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);

        // 获取当前登录用户
        LoginUser loginUser = userService.getLoginUser(request);

        // 限制每页最多20个
        int pageSize = appQueryRequest.getPageSize();
        if (pageSize > 20) {
            pageSize = 20;
        }
        int pageNum = appQueryRequest.getPageNum();

        // 构建查询条件
        QueryWrapper queryWrapper = getUserQueryWrapper(appQueryRequest, loginUser.getId());

        // 分页查询
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);

        return appVOPage;
    }

    @Override
    public Page<AppVO> listFeaturedAppByPage(AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);

        // 限制每页最多20个
        int pageSize = appQueryRequest.getPageSize();
        if (pageSize > 20) {
            pageSize = 20;
        }
        int pageNum = appQueryRequest.getPageNum();

        // 构建查询条件（精选应用：priority > 0，按优先级降序）
        QueryWrapper queryWrapper = getFeaturedQueryWrapper(appQueryRequest);

        // 分页查询
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);

        return appVOPage;
    }

    @Override
    public Boolean adminDeleteApp(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        // 查询应用
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 获取 应用封面图访问路径
        String coverUrl = app.getCover();
        // 删除应用
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 删除 cos 封面图
        if (StrUtil.isNotBlank(coverUrl)) {
            String objectKeyName = coverUrl.substring(coverUrl.indexOf("screenshots"));
            cosManager.deleteObject(objectKeyName);
        }
        return true;
    }

    @Override
    public AppVO adminGetAppVOById(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);

        // 查询应用
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        return getAppVO(app);
    }

    @Override
    public Boolean adminUpdateApp(AppAdminUpdateRequest appAdminUpdateRequest) {
        ThrowUtils.throwIf(appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);

        // 查询应用
        App app = this.getById(appAdminUpdateRequest.getId());
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 更新应用信息
        BeanUtil.copyProperties(appAdminUpdateRequest, app);
//        if (StrUtil.isNotBlank(appAdminUpdateRequest.getAppName())) {
//            app.setAppName(appAdminUpdateRequest.getAppName());
//        }
//        if (StrUtil.isNotBlank(appAdminUpdateRequest.getCover())) {
//            app.setCover(appAdminUpdateRequest.getCover());
//        }
//        if (appAdminUpdateRequest.getPriority() != null) {
//            app.setPriority(appAdminUpdateRequest.getPriority());
//        }
        app.setEditTime(LocalDateTime.now());
        boolean result = this.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        return true;
    }

    @Override
    public Page<AppVO> adminListAppByPage(AppAdminQueryRequest appAdminQueryRequest) {
        ThrowUtils.throwIf(appAdminQueryRequest == null, ErrorCode.PARAMS_ERROR);

        int pageSize = appAdminQueryRequest.getPageSize();
        int pageNum = appAdminQueryRequest.getPageNum();

        // 构建查询条件
        QueryWrapper queryWrapper = getAdminQueryWrapper(appAdminQueryRequest);

        // 分页查询
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);

        return appVOPage;
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUserVO(userVO);
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUserVO(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getUserQueryWrapper(AppQueryRequest appQueryRequest, Long userId) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        String appName = appQueryRequest.getAppName();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        QueryWrapper queryWrapper = QueryWrapper.create();
        // 查询条件：只查询当前用户的应用
        queryWrapper.eq("userId", userId);
        if (StrUtil.isNotBlank(appName)) {
            queryWrapper.like("appName", appName);
        }
        // 排序（白名单）
        if (StrUtil.isNotBlank(sortField) && isValidSortField(sortField)) {
            boolean isAsc = "ascend".equalsIgnoreCase(sortOrder);
            queryWrapper.orderBy(sortField, isAsc);
        } else {
            // 默认按创建时间降序
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    @Override
    public QueryWrapper getFeaturedQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        String appName = appQueryRequest.getAppName();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        QueryWrapper queryWrapper = QueryWrapper.create();
        // 查询条件：精选应用（priority > 0）
        queryWrapper.gt("priority", 0);
        if (StrUtil.isNotBlank(appName)) {
            queryWrapper.like("appName", appName);
        }
        // 排序（白名单）
        if (StrUtil.isNotBlank(sortField) && isValidSortField(sortField)) {
            boolean isAsc = "ascend".equalsIgnoreCase(sortOrder);
            queryWrapper.orderBy(sortField, isAsc);
        } else {
            // 默认按优先级降序，然后按创建时间降序
            queryWrapper.orderBy("priority", false);
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    @Override
    public QueryWrapper getAdminQueryWrapper(AppAdminQueryRequest appAdminQueryRequest) {
        if (appAdminQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        Long id = appAdminQueryRequest.getId();
        String appName = appAdminQueryRequest.getAppName();
        String cover = appAdminQueryRequest.getCover();
        String initPrompt = appAdminQueryRequest.getInitPrompt();
        String codeGenType = appAdminQueryRequest.getCodeGenType();
        String deployKey = appAdminQueryRequest.getDeployKey();
        Integer priority = appAdminQueryRequest.getPriority();
        Long userId = appAdminQueryRequest.getUserId();
        String sortField = appAdminQueryRequest.getSortField();
        String sortOrder = appAdminQueryRequest.getSortOrder();

        QueryWrapper queryWrapper = QueryWrapper.create();
        // 查询条件（支持根据除时间外的任何字段查询）
        queryWrapper
                .eq("id", id, id != null)
                .eq("userId", userId, userId != null)
                .eq("priority", priority, priority != null)
                .eq("codeGenType", codeGenType, StrUtil.isNotBlank(codeGenType))
                .eq("deployKey", deployKey, StrUtil.isNotBlank(deployKey))
                .like("appName", appName, StrUtil.isNotBlank(appName))
                .like("cover", cover, StrUtil.isNotBlank(cover))
                .like("initPrompt", initPrompt, StrUtil.isNotBlank(initPrompt));
        // 排序（白名单）
        if (StrUtil.isNotBlank(sortField) && isValidSortField(sortField)) {
            boolean isAsc = "ascend".equalsIgnoreCase(sortOrder);
            queryWrapper.orderBy(sortField, isAsc);
        } else {
            // 默认按创建时间降序
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            return false;
        }
        Long appId = Long.valueOf(id.toString());
        // 删除应用时，同时删除对应的对话历史记录
        try {
            chatHistoryService.deleteChatHistoryByAppId(appId);
        } catch (Exception e) {
            log.error("删除应用时，删除对应的对话历史记录失败", e);
        }
        return super.removeById(id);
    }

    /**
     * SQL 白名单方法
     *
     * @param sortField 排序字段
     * @return 是否为有效的排序字段
     */
    private boolean isValidSortField(String sortField) {
        return List.of(
                "id",
                "appName",
                "priority",
                "userId",
                "createTime",
                "updateTime",
                "editTime"
        ).contains(sortField);
    }
}
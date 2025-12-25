<template>
  <div id="userEditPage">
    <a-card title="编辑用户信息" :bordered="false">
      <a-form
        :model="formState"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
        @finish="handleSubmit"
      >
        <!-- 用户头像 -->
        <a-form-item label="用户头像" name="userAvatar">
          <div class="avatar-upload-container">
            <a-avatar :size="120" :src="formState.userAvatar" class="avatar-preview">
              <template #icon>
                <UserOutlined />
              </template>
            </a-avatar>
            <div class="avatar-upload-actions">
              <a-input
                v-model:value="formState.userAvatar"
                placeholder="请输入头像 URL"
                style="margin-bottom: 8px"
              />
              <div class="avatar-tips">
                <a-typography-text type="secondary" style="font-size: 12px">
                  请输入图片 URL 地址，或上传图片后复制 URL
                </a-typography-text>
              </div>
            </div>
          </div>
        </a-form-item>

        <!-- 用户名 -->
        <a-form-item label="用户名" name="userName">
          <a-input
            v-model:value="formState.userName"
            placeholder="请输入用户名"
            :maxlength="50"
            show-count
          />
        </a-form-item>

        <!-- 用户简介 -->
        <a-form-item label="用户简介" name="userProfile">
          <a-textarea
            v-model:value="formState.userProfile"
            placeholder="请输入用户简介"
            :rows="4"
            :maxlength="500"
            show-count
          />
        </a-form-item>

        <!-- 提交按钮 -->
        <a-form-item :wrapper-col="{ offset: 6, span: 16 }">
          <a-space>
            <a-button type="primary" html-type="submit" :loading="loading"> 提交 </a-button>
            <a-button @click="handleReset">重置</a-button>
            <a-button @click="handleCancel">取消</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined } from '@ant-design/icons-vue'
import { updateUser } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import type { Rule } from 'ant-design-vue/es/form'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const loading = ref(false)

// 表单数据
const formState = reactive<{
  userName: string
  userAvatar: string
  userProfile: string
}>({
  userName: '',
  userAvatar: '',
  userProfile: '',
})

// 原始数据，用于重置
const originalData = reactive<{
  userName: string
  userAvatar: string
  userProfile: string
}>({
  userName: '',
  userAvatar: '',
  userProfile: '',
})

// 表单验证规则
const rules: Record<string, Rule[]> = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度应在 2-50 个字符之间', trigger: 'blur' },
  ],
  userAvatar: [
    {
      validator: (_rule, value) => {
        if (!value) {
          return Promise.resolve()
        }
        // 简单的 URL 验证
        const urlPattern = /^(https?:\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w \.-]*)*\/?$/
        if (urlPattern.test(value)) {
          return Promise.resolve()
        }
        return Promise.reject(new Error('请输入有效的图片 URL 地址'))
      },
      trigger: 'blur',
    },
  ],
  userProfile: [{ max: 500, message: '用户简介不能超过 500 个字符', trigger: 'blur' }],
}

// 初始化表单数据
const initFormData = () => {
  const user = loginUserStore.loginUser
  if (user && user.id) {
    formState.userName = user.userName || ''
    formState.userAvatar = user.userAvatar || ''
    formState.userProfile = user.userProfile || ''

    // 保存原始数据
    originalData.userName = user.userName || ''
    originalData.userAvatar = user.userAvatar || ''
    originalData.userProfile = user.userProfile || ''
  }
}

// 提交表单
const handleSubmit = async (values: {
  userName: string
  userAvatar?: string
  userProfile?: string
}) => {
  loading.value = true
  try {
    const user = loginUserStore.loginUser
    if (!user || !user.id) {
      message.error('用户未登录')
      router.push('/user/login')
      return
    }

    const updateData: API.UserUpdateRequest = {
      id: user.id,
      userName: values.userName,
      userAvatar: values.userAvatar || undefined,
      userProfile: values.userProfile || undefined,
    }

    const res = await updateUser(updateData)
    if (res.data.code === 0) {
      message.success('更新成功')
      // 更新本地用户信息
      await loginUserStore.fetchLoginUser()
      // 更新原始数据
      originalData.userName = values.userName
      originalData.userAvatar = values.userAvatar || ''
      originalData.userProfile = values.userProfile || ''
    } else {
      message.error('更新失败：' + res.data.message)
    }
  } catch (error) {
    message.error('更新失败，请稍后重试')
    console.error('更新用户信息失败：', error)
  } finally {
    loading.value = false
  }
}

// 重置表单
const handleReset = () => {
  formState.userName = originalData.userName
  formState.userAvatar = originalData.userAvatar
  formState.userProfile = originalData.userProfile
  message.info('已重置为原始数据')
}

// 取消编辑
const handleCancel = () => {
  router.back()
}

// 页面加载时初始化
onMounted(() => {
  // 检查是否已登录
  const user = loginUserStore.loginUser
  if (!user || !user.id) {
    message.warning('请先登录')
    router.push('/user/login')
    return
  }
  initFormData()
})
</script>

<style scoped>
#userEditPage {
  max-width: 800px;
  margin: 24px auto;
  padding: 0 24px;
}

.avatar-upload-container {
  display: flex;
  align-items: flex-start;
  gap: 24px;
}

.avatar-preview {
  flex-shrink: 0;
  border: 2px solid #d9d9d9;
  border-radius: 8px;
}

.avatar-upload-actions {
  flex: 1;
  min-width: 0;
}

.avatar-tips {
  margin-top: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  #userEditPage {
    padding: 0 16px;
  }

  .avatar-upload-container {
    flex-direction: column;
    align-items: center;
  }

  .avatar-upload-actions {
    width: 100%;
  }
}
</style>

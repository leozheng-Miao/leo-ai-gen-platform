// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 POST /chatHistory/add */
export async function addChatMessage(
  body: API.ChatHistoryAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseLong>('/chatHistory/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /chatHistory/admin/list/page/vo */
export async function adminListChatHistoryByPage(
  body: API.ChatHistoryQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageChatHistory>('/chatHistory/admin/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /chatHistory/app/list/vo/${param0} */
export async function listChatHistoryByPage(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listChatHistoryByPageParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<API.BaseResponsePageChatHistoryVO>(`/chatHistory/app/list/vo/${param0}`, {
    method: 'POST',
    params: {
      // pageSize has a default value: 10
      pageSize: '10',
      ...queryParams,
    },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 POST /chatHistory/export/markdown */
export async function exportChatHistory(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.exportChatHistoryParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>('/chatHistory/export/markdown', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

import request from '@/utils/request'
import type { ApiResponse } from '@/types'

/**
 * 帮助中心分类
 */
export interface HelpCategory {
  id: number
  name: string
  children?: HelpCategory[]
}

/**
 * 帮助中心文章
 */
export interface HelpArticle {
  id: number
  categoryId: number
  title: string
  content: string // HTML内容，支持图片和文字
  images?: string[] // 图片URL数组
  sort: number
  status: number
  createTime?: string
  updateTime?: string
}

/**
 * 获取帮助中心分类列表
 */
export const getHelpCategories = (): Promise<HelpCategory[]> => {
  return request.get('/api/common/help/categories').then((categories: any[]) => {
    // 将后端返回的树形结构转换为前端需要的格式
    const convertCategory = (cat: any): HelpCategory => {
      const result: HelpCategory = {
        id: cat.id,
        name: cat.name
      }
      if (cat.children && cat.children.length > 0) {
        result.children = cat.children.map(convertCategory)
      }
      return result
    }
    return categories.map(convertCategory)
  })
  
  // 临时返回模拟数据（已注释，使用真实API）
  /* return Promise.resolve([
    {
      id: 1,
      name: '新手上路',
      children: [
        { id: 11, name: '顾客必读' },
        { id: 12, name: '会员等级折扣' },
        { id: 13, name: '订单的几种状态' },
        { id: 14, name: '积分奖励计划' },
        { id: 15, name: '商品退货保障' }
      ]
    },
    {
      id: 2,
      name: '购物指南',
      children: [
        { id: 21, name: '体贴的售后服务' },
        { id: 22, name: '网站使用条款' },
        { id: 23, name: '网站免责声明' },
        { id: 24, name: '简单的购物流程' }
      ]
    },
    {
      id: 3,
      name: '支付/配送方式',
      children: [
        { id: 31, name: '支付方式' },
        { id: 32, name: '配送方式' },
        { id: 33, name: '订单何时出库?' },
        { id: 34, name: '网上支付小贴士' },
        { id: 35, name: '关于送货和验货' }
      ]
    },
    {
      id: 4,
      name: '购物条款',
      children: [
        { id: 41, name: '会员注册协议' },
        { id: 42, name: '隐私保护政策' }
      ]
    },
    {
      id: 5,
      name: '代销会员使用帮助',
      children: [
        { id: 51, name: '淘宝卖家订单同步软件' },
        { id: 52, name: '抓抓的下载安装' },
        { id: 53, name: '同步淘宝订单' },
        { id: 54, name: '检查我们的库存' },
        { id: 55, name: '发货信息同步' }
      ]
    },
    {
      id: 6,
      name: '批发会员使用帮助',
      children: [
        { id: 61, name: '如何进行线下交易' },
        { id: 62, name: '如何快速订货' },
        { id: 63, name: '如何货号订货' },
        { id: 64, name: '如何补货' },
        { id: 65, name: '批发会员如何进行代销行为' }
      ]
    }
  ]) */
}


/**
 * 根据分类ID获取帮助文章列表
 */
export const getHelpArticlesByCategory = (categoryId: number): Promise<HelpArticle[]> => {
  return request.get(`/api/common/help/articles`, { params: { categoryId } })
}

/**
 * 根据文章ID获取帮助文章详情
 */
export const getHelpArticleById = (id: number): Promise<HelpArticle> => {
  return request.get(`/api/common/help/article/${id}`).then((article: any) => {
    return {
      id: article.id,
      categoryId: article.categoryId,
      title: article.title,
      content: article.content,
      images: article.images || [],
      sort: article.sort,
      status: article.status,
      createTime: article.createTime,
      updateTime: article.updateTime
    }
  })
  
  // 临时返回模拟数据（已注释，使用真实API）
  /* const mockArticles: Record<number, HelpArticle> = {
    // 新手上路
    11: {
      id: 11,
      categoryId: 1,
      title: '顾客必读',
      content: `
        <h3>如何订购商品?</h3>
        <p>您可以通过网站浏览商品并直接下单，也可以联系客服进行订购。</p>
        
        <h3>我通过网站看到你们的商品后觉得不错,但是我不是经常上网,你可以寄一些商品的图片和介绍给我吗?</h3>
        <p>网站会定期向会员发送商品信息邮件。如果您还不是会员，可以在网站注册成为会员。</p>
        
        <h3>请告诉我在这里购物的理由好吗?</h3>
        <ol>
          <li>拥有长期零售经验的网站</li>
          <li>提供优质商品，价格更优惠</li>
          <li>多种支付方式，全国快速配送</li>
          <li>人性化的退换货政策</li>
          <li>贴心的会员积分制度</li>
          <li>所有商品均为原厂正品包装</li>
        </ol>
        
        <h3>你们的商品我都非常喜欢,已经购买了很多,但是有些怎么一直没货?会不会订不到?</h3>
        <p>由于客户购买量较大，商品可能随时缺货。您可以使用网站上的"到货通知"功能进行预订，或联系客服进行预订。</p>
        
        <h3>所有的产品都能够在网站上购买?</h3>
        <p>目前所有可搜索到的商品都可以订购，但需要根据仓库库存确认。部分热销商品可以通过网站进行预订，到货后会通过电话或邮件通知您。</p>
        
        <h3>为什么要注册会员?</h3>
        <ol>
          <li>只有注册用户才能在线下单并享受优惠价格</li>
          <li>只有注册用户才能登录"会员中心"使用更多购物功能和管理个人数据</li>
          <li>只有注册用户才能给其他注册好友留言</li>
          <li>只有注册用户才能收到我们赠送的礼品</li>
        </ol>
        
        <h3>忘记了密码怎么办?</h3>
        <p>为了客户利益，我们无法查看您的密码。当您忘记密码时，请前往注册页面，点击"忘记密码"，系统会自动将密码发送到您的邮箱。然后您可以登录"会员中心"修改密码，确保账户安全。</p>
        
        <h3>积分是怎么回事?有什么作用?</h3>
        <p>积分等级反映了您对我们的关注和支持程度。我们的积分是通过订购商品产生的。对于高积分客户，我们会有一定的奖励，如积分兑换商品、积分抵扣价格、赠送商品、购买商品更优惠的价格等，以回馈我们的忠实客户。</p>
      `,
      images: [
        'https://via.placeholder.com/600x300/E4393C/ffffff?text=购物流程示意图1',
        'https://via.placeholder.com/600x300/E4393C/ffffff?text=购物流程示意图2'
      ],
      sort: 1,
      status: 1
    },
    12: {
      id: 12,
      categoryId: 1,
      title: '会员等级折扣',
      content: `
        <h3>会员等级说明</h3>
        <p>我们的会员分为多个等级，不同等级享受不同的折扣优惠：</p>
        <ul>
          <li><strong>普通会员：</strong>享受9.5折优惠</li>
          <li><strong>银卡会员：</strong>享受9折优惠</li>
          <li><strong>金卡会员：</strong>享受8.5折优惠</li>
          <li><strong>钻石会员：</strong>享受8折优惠</li>
        </ul>
        <p>会员等级根据您的累计消费金额自动升级，消费越多，等级越高，享受的折扣越大。</p>
      `,
      sort: 2,
      status: 1
    },
    13: {
      id: 13,
      categoryId: 1,
      title: '订单的几种状态',
      content: `
        <h3>订单状态说明</h3>
        <ul>
          <li><strong>待付款：</strong>订单已创建，等待您完成支付</li>
          <li><strong>待发货：</strong>订单已支付，等待仓库发货</li>
          <li><strong>已发货：</strong>商品已发出，正在配送中</li>
          <li><strong>已完成：</strong>订单已完成，商品已送达</li>
          <li><strong>已取消：</strong>订单已取消</li>
          <li><strong>已退款：</strong>订单已退款</li>
        </ul>
        <p>您可以在"会员中心"的"我的订单"中查看所有订单的详细状态。</p>
      `,
      sort: 3,
      status: 1
    },
    // 购物指南
    24: {
      id: 24,
      categoryId: 2,
      title: '简单的购物流程',
      content: `
        <h3>购物流程</h3>
        <p>我们的购物流程非常简单，只需几个步骤即可完成：</p>
        <ol>
          <li>浏览商品，选择您喜欢的商品</li>
          <li>将商品加入购物车</li>
          <li>进入购物车，确认商品信息</li>
          <li>填写收货地址和联系方式</li>
          <li>选择支付方式并完成支付</li>
          <li>等待商品发货和配送</li>
          <li>收到商品后确认收货</li>
        </ol>
        <p>如果您在购物过程中遇到任何问题，可以随时联系我们的客服。</p>
      `,
      images: [
        'https://via.placeholder.com/600x400/E4393C/ffffff?text=购物流程步骤图'
      ],
      sort: 1,
      status: 1
    },
    21: {
      id: 21,
      categoryId: 2,
      title: '体贴的售后服务',
      content: `
        <h3>售后服务承诺</h3>
        <p>我们致力于为您提供最优质的售后服务：</p>
        <ul>
          <li>7天无理由退换货</li>
          <li>商品质量问题免费退换</li>
          <li>专业客服团队，快速响应</li>
          <li>完善的售后保障体系</li>
        </ul>
        <p>如有任何售后问题，请及时联系我们的客服团队。</p>
      `,
      sort: 2,
      status: 1
    },
    // 支付/配送方式
    31: {
      id: 31,
      categoryId: 3,
      title: '支付方式',
      content: `
        <h3>支持的支付方式</h3>
        <ul>
          <li><strong>在线支付：</strong>支持支付宝、微信支付、银联在线支付</li>
          <li><strong>预存款支付：</strong>使用账户预存款余额支付</li>
          <li><strong>货到付款：</strong>部分区域支持货到付款（具体以配送时为准）</li>
        </ul>
        <p>我们采用安全的支付系统，保障您的资金安全。</p>
      `,
      sort: 1,
      status: 1
    },
    32: {
      id: 32,
      categoryId: 3,
      title: '配送方式',
      content: `
        <h3>配送方式说明</h3>
        <p>我们提供多种配送方式，满足您的不同需求：</p>
        <ul>
          <li><strong>快递配送：</strong>全国大部分地区支持快递配送，一般3-7个工作日送达</li>
          <li><strong>同城配送：</strong>部分城市支持同城配送，当日或次日送达</li>
          <li><strong>自提：</strong>部分城市支持到店自提</li>
        </ul>
        <p>具体配送方式和费用，请在下单时查看。</p>
      `,
      sort: 2,
      status: 1
    }
  }
  
  // 返回对应的模拟数据，如果没有则返回默认数据
  const article = mockArticles[id]
  if (article) {
    return Promise.resolve(article)
  }
  
  // 默认返回（已注释，使用真实API）
  /* return Promise.resolve({
    id,
    categoryId: 0,
    title: `帮助文章 ${id}`,
    content: `<p>这是帮助文章 ${id} 的内容。内容将在管理后台配置。</p>`,
    sort: 0,
    status: 1
  }) */
}


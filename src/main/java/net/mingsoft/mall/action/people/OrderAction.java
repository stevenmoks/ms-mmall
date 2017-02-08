package net.mingsoft.mall.action.people;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.mingsoft.base.filter.DateValueFilter;
import com.mingsoft.base.filter.DoubleValueFilter;
import com.mingsoft.util.StringUtil;

import net.mingsoft.basic.util.BasicUtil;
import net.mingsoft.mall.biz.ICartBiz;
import net.mingsoft.mall.entity.CartEntity;
import net.mingsoft.order.biz.IOrderBiz;
import net.mingsoft.order.constant.ModelCode;
import net.mingsoft.order.constant.e.OrderStatusEnum;
import net.mingsoft.order.entity.GoodsEntity;
import net.mingsoft.order.entity.OrderEntity;

/**
 * 商城订单管理控制层
 * 
 * @author 铭飞开发团队
 * @version 版本号：1.0.0<br/>
 *          创建日期：<br/>
 *          历史修订：<br/>
 */
@Controller("peopleMallOrderAction")
@RequestMapping("/people/mall/order")
public class OrderAction extends net.mingsoft.people.action.BaseAction {

	/**
	 * 注入订单评价业务层
	 */
	@Autowired
	private IOrderBiz orderBiz;
	
	/**
	 * 注入购物车信息
	 */
	@Resource(name = "mallCartBiz")
	private ICartBiz cartBiz;

	/**
	 * 订单详情
	 * 
	 * @param order
	 *            <i>order参数包含字段信息参考：</i><br/>
	 *            "orderNo": "订单号", <br/>
	 *            <dt><span class="strong">返回</span></dt><br/>
	 *            <i>order参数包含字段信息参考：</i><br/>
	 *            "orderUserName": "收货人", <br/>
	 *            "orderPhone": "联系电话", <br/>
	 *            "orderAddress": "收货地址", <br/>
	 *            "orderDescription": "订单描述留言", <br/>
	 *            "orderExpress": 快递方式, <br/>
	 *            "orderExpressTitle": 快递派送描述, <br/>
	 *            "orderInvoiceName": 发票抬头, <br/>
	 *            "orderInvoiceDefinite": 发票明细, <br/>
	 *            "orderInvoiceType": 发票类型，暂时不用, <br/>
	 *            "orderPayment": 支付方式, <br/>
	 *            "orderStatus": 订单状态－数值, <br/>
	 *            "orderExpressInfo":订单物流信息，没有返回null
	 *            格式参考http://m.kuaidi100.com/query?type=yuantong&postid=
	 *            882595201560601055
	 */
	@RequestMapping("/detail")
	@ResponseBody
	public void detail(@ModelAttribute OrderEntity order, HttpServletRequest request, HttpServletResponse response) {
		if (order == null) {
			this.outJson(response, ModelCode.ORDER, false);
			return;
		}

		if (StringUtil.isBlank(order.getOrderNo())) {
			this.outJson(response, ModelCode.ORDER, false);
			return;
		}

		OrderEntity _order = (OrderEntity) orderBiz.getByOrderNo(order.getOrderNo());
		if (_order.getOrderPeopleId() != this.getPeopleBySession().getPeopleId()) {
			this.outJson(response, ModelCode.ORDER, false);
			return;
		}
		this.outJson(response, net.mingsoft.base.util.JSONObject.toJSONString(_order, new DoubleValueFilter(),
				new DateValueFilter("yyyy-MM-dd")));
	}

	/**
	 * 提交订单
	 * <i>参数：</i><br/>
	 * cartIds 购物车编号，多个用逗号隔开<br/>
	 * cartProductDetailIds 规格编号，多个用逗号隔开<br/>
	 * @param order
	 *            <i>order参数包含字段信息参考：</i><br/>
	 *            "orderUserName": "收货人", <br/>
	 *            "orderPhone": "联系电话", <br/>
	 *            "orderAddress": "收货地址", <br/>
	 *            "orderDescription": "订单描述留言", <br/>
	 *            "orderExpress": 快递方式, <br/>
	 *            "orderExpressTitle": 快递派送描述, <br/>
	 *            "orderInvoiceName": 发票抬头, <br/>
	 *            "orderInvoiceDefinite": 发票明细, <br/>
	 *            "orderInvoiceType": 发票类型，暂时不用, <br/>
	 *            "orderPayment": 支付方式, <br/>
	 *            "orderStatus": 订单状态－数值, <br/>
	 *            cartId: 购物车编号，多个编号用逗号隔开<br/>
	 */
	@RequestMapping("/submit")
	@ResponseBody
	public void submit(@ModelAttribute OrderEntity order, HttpServletRequest request, HttpServletResponse response) {
		int[] cartIds = BasicUtil.getInts("cartIds", ",");
		int[] cartProductDetailIds = BasicUtil.getInts("cartProductDetailIds", ",");
		if (order == null) {
			this.outJson(response, ModelCode.ORDER, false);
			return;
		}

		if (StringUtil.isBlank(order.getOrderAddress()) || StringUtil.isBlank(order.getOrderUserName())
				|| StringUtil.isBlank(order.getOrderPhone())) {
			this.outJson(response, ModelCode.ORDER, false);
			return;
		}

		// 再次确认购物车的数据
		List list = cartBiz.query(cartIds, cartProductDetailIds, this.getPeopleBySession().getPeopleId());
		
		if (list != null) {
			for (int i=0;i<list.size();i++) {
						CartEntity _cart = (CartEntity)list.get(i);
						GoodsEntity goods = new GoodsEntity();
						goods.setGoodsAppId(BasicUtil.getAppId());
						goods.setGoodsBasicId(_cart.getCartBasicId());
						goods.setGoodsName(_cart.getCartTitle());
						goods.setGoodsNum(_cart.getCartNum());
						goods.setGoodsPrice(_cart.getCartPrice());
						goods.setGoodsRebate(_cart.getCartDiscount());
						goods.setGoodsThumbnail(_cart.getCartThumbnail());
						goods.setGoodsUrl(_cart.getCartUrl());
						order.getGoods().add(goods);
			}
		}

		// 设置订单号
		order.setOrderNo(StringUtil.getDateSimpleStr() + StringUtil.randomNumber(4));
		order.setOrderAppId(BasicUtil.getAppId());
		order.setOrderPeopleId(this.getPeopleBySession().getPeopleId());
		order.setOrderStatus(OrderStatusEnum.UNPAY);
		orderBiz.saveEntity(order);
		this.outJson(response, order);
	}

}
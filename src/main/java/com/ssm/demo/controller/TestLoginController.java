package com.ssm.demo.controller;

import com.ssm.demo.common.Constants;
import com.ssm.demo.common.Result;
import com.ssm.demo.common.ResultGenerator;
import com.ssm.demo.controller.annotation.TokenToUser;
import com.ssm.demo.entity.AdminUser;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/testLogin")
//测试登陆验证的接口
public class TestLoginController {

    @RequestMapping(value = "/test1", method = RequestMethod.GET)
    public Result test1(@TokenToUser AdminUser user) {
        //此接口含有@TokenToUser注解，即需要登陆验证的接口。
        Result result = null;
        if (user == null) {
            //如果通过请求header中的token未查询到用户的话即token无效，登陆验证失败，返回未登录错误码。
            result = ResultGenerator.genErrorResult(Constants.RESULT_CODE_NOT_LOGIN, "未登录！");
        } else {
            //登陆验证通过。
            result = ResultGenerator.genSuccessResult("登陆验证通过");
        }
        return result;
    }

    @RequestMapping(value = "/test2", method = RequestMethod.GET)
    public Result test2() {
        //此接口不含@TokenToUser注解，即访问此接口无需登陆验证，此类接口在实际开发中应该很少，为了安全起见应该所有接口都会做登陆验证。
        Result result = ResultGenerator.genSuccessResult("此接口无需登陆验证，请求成功");
        //直接返回业务逻辑返回的数据即可。
        return result;
    }
}

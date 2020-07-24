package io.github.talelin.latticy.controller.cms;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.talelin.core.annotation.GroupRequired;
import io.github.talelin.core.annotation.PermissionMeta;
import io.github.talelin.core.annotation.PermissionModule;
import io.github.talelin.latticy.common.util.PageUtil;
import io.github.talelin.latticy.model.LogDO;
import io.github.talelin.latticy.service.LogService;
import io.github.talelin.latticy.vo.PageResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;

/**
 * @author pedro@TaleLin
 * @author Juzi@TaleLin
 */
@RestController
@RequestMapping("/cms/log")
@PermissionModule(value = "日志")
@Validated
public class LogController {
	@Autowired
	private LogService logService;

	/**
	 * 
	 * @RequestParam 将请求参数绑定到你控制器的方法参数上（是springmvc中接收普通参数的注解）
	 * 
	 *               语法：@RequestParam(value=”参数名”,required=”true/false”,defaultValue=””)
	 * 
	 *               value：参数名 required：是否包含该参数，默认为true，表示该请求路径中必须包含该参数，如果不包含就报错。
	 *               defaultValue：默认参数值，如果设置了该值，required=true将失效，自动为false,如果没有传该参数，就使用默认值
	 * 
	 */

	/**
	 * 
	 * @DateTimeFormat 时间格式化参数
	 * 
	 *                 语法:@DateTimeFormat(pattern="yyyy/MM/dd HH:mm:ss")
	 * 
	 *                 pattern：时间格式
	 * 
	 */

	@GetMapping("")
	@GroupRequired
	@PermissionMeta(value = "查询所有日志")
	public PageResponseVO<LogDO> getLogs(
			@RequestParam(name = "start", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date start,
			@RequestParam(name = "end", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date end,
			@RequestParam(name = "name", required = false) String name,
			@RequestParam(name = "count", required = false, defaultValue = "15") @Min(value = 1, message = "{page.count.min}") @Max(value = 30, message = "{page.count.max}") Integer count,
			@RequestParam(name = "page", required = false, defaultValue = "0") @Min(value = 0, message = "{page.number.min}") Integer page) {
		IPage<LogDO> iPage = logService.getLogPage(page, count, name, start, end);
		return PageUtil.build(iPage);
	}

	@GetMapping("/search")
	@GroupRequired
	@PermissionMeta(value = "搜索日志")
	public PageResponseVO<LogDO> searchLogs(
			@RequestParam(name = "start", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date start,
			@RequestParam(name = "end", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date end,
			@RequestParam(name = "name", required = false) String name,
			@RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
			@RequestParam(name = "count", required = false, defaultValue = "15") @Min(value = 1, message = "{page.count.min}") @Max(value = 30, message = "{page.count.max}") Integer count,
			@RequestParam(name = "page", required = false, defaultValue = "0") @Min(value = 0, message = "{page.number.min}") Integer page) {
		IPage<LogDO> iPage = logService.searchLogPage(page, count, name, keyword, start, end);
		return PageUtil.build(iPage);
	}

	@GetMapping("/users")
	@GroupRequired
	@PermissionMeta(value = "查询日志记录的用户")
	public PageResponseVO<String> getUsers(
			@RequestParam(name = "count", required = false, defaultValue = "15") @Min(value = 1, message = "{page.count.min}") @Max(value = 30, message = "{page.count.max}") Integer count,
			@RequestParam(name = "page", required = false, defaultValue = "0") @Min(value = 0, message = "{page.number.min}") Integer page) {
		IPage<String> iPage = logService.getUserNamePage(page, count);
		return PageUtil.build(iPage);
	}
}

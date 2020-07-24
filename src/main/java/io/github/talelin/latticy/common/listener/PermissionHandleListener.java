package io.github.talelin.latticy.common.listener;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.talelin.autoconfigure.bean.MetaInfo;
import io.github.talelin.autoconfigure.bean.PermissionMetaCollector;
import io.github.talelin.latticy.model.PermissionDO;
import io.github.talelin.latticy.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author pedro@TaleLin
 * @author colorful@TaleLin
 */
@Component
public class PermissionHandleListener implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private PermissionMetaCollector metaCollector;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		addNewPermissions();
		removeUnusedPermissions();
	}

	/**
	 * 添加新的
	 * 
	 */
	private void addNewPermissions() {
		
		metaCollector.getMetaMap().values().forEach(meta -> {
			String module = meta.getModule();
			String permission = meta.getPermission();
			createPermissionIfNotExist(permission, module);
		});
	}

	/**
	 * 删除未使用的权限
	 * 
	 */
	private void removeUnusedPermissions() {
		List<PermissionDO> allPermissions = permissionService.list();
		Map<String, MetaInfo> metaMap = metaCollector.getMetaMap();
		for (PermissionDO permission : allPermissions) {
			boolean stayedInMeta = metaMap.values().stream()
					.anyMatch(meta -> meta.getModule().equals(permission.getModule())
							&& meta.getPermission().equals(permission.getName()));
			if (!stayedInMeta) {
				permission.setMount(false);
				permissionService.updateById(permission);
			}
		}
	}

	/**
	 * 创建权限(权限开启)
	 * 
	 * @param name   权限名
	 * @param module 模块名
	 */
	private void createPermissionIfNotExist(String name, String module) {
		QueryWrapper<PermissionDO> wrapper = new QueryWrapper<>();

		wrapper.lambda().eq(PermissionDO::getName, name).eq(PermissionDO::getModule, module);

		// 查询权限是否存在
		PermissionDO permission = permissionService.getOne(wrapper);

		if (permission == null) {
			// 插入一条权限
			permissionService.save(PermissionDO.builder().module(module).name(name).build());
		}
		if (permission != null && !permission.getMount()) {
			permission.setMount(true);
			// 开启权限
			permissionService.updateById(permission);
		}
	}
}

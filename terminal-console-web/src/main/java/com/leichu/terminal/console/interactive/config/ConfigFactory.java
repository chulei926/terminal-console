package com.leichu.terminal.console.interactive.config;

import com.google.common.base.CaseFormat;
import com.leichu.terminal.console.interactive.model.Vendor;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ConfigFactory {

	private static final Logger logger = LoggerFactory.getLogger(ConfigFactory.class);

	private static final Map<Vendor, InteractiveConfig> INTERACTIVE_CONFIG_MAP = new ConcurrentHashMap<>();
	private static final String INTERACTIVE_CONFIG_PREFIX = "interactive.config.";

	private static final List<Field> FIELDS = FieldUtils.getAllFieldsList(InteractiveConfig.class);
	private static final Map<String, Method> SET_METHOD = new HashMap<>();


	private ConfigFactory() {
		try {
			for (Vendor vendor : Vendor.values()) {
				INTERACTIVE_CONFIG_MAP.put(vendor, new InteractiveConfig());
			}

			PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(InteractiveConfig.class);
			for (PropertyDescriptor pd : propertyDescriptors) {
				SET_METHOD.put(pd.getName(), pd.getWriteMethod());
			}

			String classPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
			initInteractiveConfig(Paths.get(classPath, "config", "application.properties"));
			initInteractiveConfig(Paths.get(classPath, "config", "interactive.properties"));
		} catch (Exception e) {
			logger.error("Interactive config init error!", e);
		}
		if (logger.isDebugEnabled()) {
			logger.info("Interactive config init success. {}", INTERACTIVE_CONFIG_MAP);
		}
	}

	private void initInteractiveConfig(Path path) {
		try {
			File file = path.toFile();
			if (!file.exists()) {
				logger.warn("{} file not found!", path);
				return;
			}
			PropertiesConfiguration propertiesConfiguration = new Configurations().properties(file);
			for (Map.Entry<Vendor, InteractiveConfig> entry : INTERACTIVE_CONFIG_MAP.entrySet()) {
				Vendor vendor = entry.getKey();
				InteractiveConfig interactiveConfig = entry.getValue();
				String fieldKeyPrefix = String.format("%s%s", INTERACTIVE_CONFIG_PREFIX, vendor.name().toLowerCase());
				Iterator<String> iterator = propertiesConfiguration.getKeys(fieldKeyPrefix);
				List<String> keys = new ArrayList<>();
				while (iterator.hasNext()) {
					keys.add(iterator.next());
				}
				if (keys.size() == 0) {
					continue;
				}
				setConfigAttr(FIELDS, keys, fieldKeyPrefix, null, SET_METHOD, propertiesConfiguration, interactiveConfig);
				logger.info("[initInteractiveConfig] {} {}", vendor, interactiveConfig);
			}
		} catch (Exception e) {
			logger.error("Interactive config init error! Path:{}", path, e);
		}
	}

	private static void setConfigAttr(List<Field> fields, List<String> keys, String fieldKeyPrefix, Integer listStart, Map<String, Method> setMethodMap,
	                                  PropertiesConfiguration propertiesConfiguration, Configuration interactiveConfig) throws Exception {

		for (Field field : fields) {
			String lowerHyphen = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, field.getName());
			String prefix = listStart == null ? String.format("%s.%s", fieldKeyPrefix, lowerHyphen) : String.format("%s[%d].%s", fieldKeyPrefix, listStart, lowerHyphen);
			if (List.class.getName().equals(field.getType().getName())) {
				List<String> fieldKeys = keys.stream().filter(k -> k.startsWith(prefix)).collect(Collectors.toList());
				ParameterizedType pt = (ParameterizedType) field.getGenericType();
				Class<?> actualType = (Class<?>) pt.getActualTypeArguments()[0]; // 得到泛型里的class类型对象
				List<Object> values = new ArrayList<>();
				if (isBaseType(actualType) || isRefType(actualType)) {
					for (String fieldKey : fieldKeys) {
						Object val = propertiesConfiguration.get(actualType, fieldKey);
						if (Objects.nonNull(val)) {
							values.add(val);
						}
					}
					setMethodMap.get(field.getName()).invoke(interactiveConfig, values);
				} else {
					String fieldPrefix = String.format("%s.%s", fieldKeyPrefix, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, field.getName()));
					Set<Integer> idxList = new TreeSet<>();
					for (String key : keys) {
						if (key.startsWith(fieldPrefix)) {
							String idx = key.replaceAll("^.*\\[(\\d+)\\].*$", "$1");
							idxList.add(Integer.parseInt(idx));
						}
					}
					List<Configuration> list = new ArrayList<>();
					for (Integer idx : idxList) {
						Object fieldInstance = actualType.newInstance();
						if (fieldInstance instanceof Configuration) { // 自定义配置类
							Configuration instance = (Configuration) fieldInstance;
							List<Field> fieldFields = FieldUtils.getAllFieldsList(actualType);
							PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(actualType);
							Map<String, Method> fieldSetMethod = new HashMap<>();
							for (PropertyDescriptor pd : propertyDescriptors) {
								fieldSetMethod.put(pd.getName(), pd.getWriteMethod());
							}
							setConfigAttr(fieldFields, keys, fieldPrefix, idx, fieldSetMethod, propertiesConfiguration, instance);
							list.add(instance);
						}
					}
					setMethodMap.get(field.getName()).invoke(interactiveConfig, list);
				}
			} else if (isBaseType(field.getType()) || isRefType(field.getType())) {
				Object val = propertiesConfiguration.get(field.getType(), prefix);
				if (val instanceof String) {
					val = ((String) val).replaceAll("^\"(.*)\"$", "$1");
				}
				setMethodMap.get(field.getName()).invoke(interactiveConfig, val);
			} else {
				Object fieldInstance = field.getType().newInstance();
				if (fieldInstance instanceof Configuration) { // 自定义配置类
					Configuration instance = (Configuration) fieldInstance;
					List<Field> fieldFields = FieldUtils.getAllFieldsList(field.getType());
					PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(field.getType());
					Map<String, Method> fieldSetMethod = new HashMap<>();
					for (PropertyDescriptor pd : propertyDescriptors) {
						fieldSetMethod.put(pd.getName(), pd.getWriteMethod());
					}
					String fieldPrefix = String.format("%s.%s", fieldKeyPrefix, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, field.getName()));
					setConfigAttr(fieldFields, keys, fieldPrefix, null, fieldSetMethod, propertiesConfiguration, instance);
					setMethodMap.get(field.getName()).invoke(interactiveConfig, fieldInstance);
				}
			}
		}
	}


	private static boolean isBaseType(Class<?> clazz) {
		boolean base = false;
		try {
			base = clazz.isPrimitive();
		} catch (Exception e) {
			// DO NOTHING
		}
		return base;
	}

	private static boolean isRefType(Class<?> clazz) {
		List<String> refs = new ArrayList<String>() {{
			add(String.class.getName());
			add(Integer.class.getName());
			add(Double.class.getName());
			add(Float.class.getName());
			add(Long.class.getName());
			add(Short.class.getName());
		}};
		return refs.contains(clazz.getName());
	}

	private static class ConfigFactoryHolder {
		private static final ConfigFactory INSTANCE = new ConfigFactory();
	}

	public static ConfigFactory getInstance() {
		return ConfigFactoryHolder.INSTANCE;
	}

	public InteractiveConfig getConfig(Vendor vendor) {
		return INTERACTIVE_CONFIG_MAP.get(vendor);
	}


	public SoapConfig getSoapConfig() {
		return new SoapConfig();
	}
}

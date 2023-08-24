package com.hupubao.collections.tree;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 *
 */
public class TreeUtils {


	private static final String DEAFULT_ROOT_ID = "0";
	public static final String OTHER_ROOT_ID = "-1";

	public static final String DEFAULT_FIELD_NAME_ID = "id";
	public static final String DEFAULT_FIELD_NAME_PID = "pId";
	public static final String DEFAULT_FIELD_NAME_CHILDREN = "children";
	public static final String DEFAULT_HAS_CHILDREN_FIELD_NAME = "hasChildren";



	/**
     * 把列表转换为树结构
     *
     * @param originalList 原始list数据
     * @return 组装后的集合
     */
	public static <T> List<T> getTree(List<T> originalList) {
		return getTree(originalList, DEFAULT_FIELD_NAME_ID, DEFAULT_FIELD_NAME_PID, DEFAULT_FIELD_NAME_CHILDREN);
	}

	/**
	 * 构建树结构
	 * @param <T>
	 * @param rootId
	 * @param originalList
	 * @return
	 */
	public static <T> T buildTree(String rootId, List<T> originalList) {
		if (CollectionUtil.isEmpty(originalList)) {
			return null;
		}
		return buildTree(rootId,originalList, DEFAULT_FIELD_NAME_ID, DEFAULT_FIELD_NAME_PID, DEFAULT_FIELD_NAME_CHILDREN);
	}

	/**
	 * 构建以rootId为根节点的树结构对象
	 * @param <T>
	 * @param rootId
	 * @param originalList
	 * @return
	 */
	public static <T> T buildTree(String rootId, List<T> originalList, String keyName, String parentFieldName, String childrenFieldName) {
		try {
			T root = null;
	        for (int i = 0; i < originalList.size(); i++) {
	            T t = originalList.get(i);
	            if (rootId.equals(BeanUtil.getProperty(t, keyName)) ) {
	            	root = t;
	            	break ;
	            }
	        }
	        buildTree(root, originalList, keyName, parentFieldName, childrenFieldName);
	        return root;
		} catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("转换为树形机构异常！" + e.getMessage());
        }
	}

	/**
	 * 构建单颗树
	 * @param <T>
	 * @param root
	 * @param originalList
	 * @param keyName
	 * @param parentFieldName
	 * @param childrenFieldName
	 * @throws Exception
	 */
	private static <T> void buildTree(T root, List<T> originalList, String keyName, String parentFieldName, String childrenFieldName) throws Exception {
		List<T> children = fillChildren(root, originalList, keyName, parentFieldName, childrenFieldName);
		originalList.removeAll(children);
		if(ArrayUtil.isEmpty(children)) {
			return ;
		}
        for (T child : children) {
            buildTree(child, originalList, keyName, parentFieldName, childrenFieldName);
        }
	}

    /**
     * 把列表转换为树结构
     *
     * @param originalList 原始list数据
     * @param keyName id字段名称
     * @param parentFieldName 父id的字段名称
     * @param childrenFieldName 名称字段名称
     * @return 组装后的集合
     */
    public static <T> List<T> getTree(List<T> originalList, String keyName, String parentFieldName, String childrenFieldName) {
        // 获取根节点，即找出父节点为空的对象
        try {
            List<T> topList = new ArrayList<>();
            for (T t : originalList) {
                String parentId = BeanUtil.getProperty(t, parentFieldName);
                if (StringUtils.isBlank(parentId) || parentId.equals(DEAFULT_ROOT_ID)) {
                    topList.add(t);
                }
            }
            // 将根节点从原始list移除，减少下次处理数据
            originalList.removeAll(topList);
            // 递归封装树
            fillTree(topList, originalList, keyName, parentFieldName, childrenFieldName);
            return topList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("转换为树形机构异常！" + e.getMessage());
        }
    }


	public static <T> List<T> getTree(List<T> originalList, String keyName, String parentFieldName, String appointParentId, String childrenFieldName) {
		// 获取根节点，即找出父节点为空的对象
		try {
			List<T> topList = new ArrayList<>();
			for (T t : originalList) {
				String parentId = BeanUtil.getProperty(t, parentFieldName);
				if (parentId.equals(appointParentId)) {
					topList.add(t);
				}
			}
			// 将根节点从原始list移除，减少下次处理数据
			originalList.removeAll(topList);
			// 递归封装树
			fillTree(topList, originalList, keyName, parentFieldName, childrenFieldName);
			return topList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("转换为树形机构异常！" + e.getMessage());
		}
	}




    /**
     * 封装树
     *
     * @param parentList 要封装为树的父对象集合
     * @param originalList 原始list数据
     * @param keyName 作为唯一标示的字段名称
     * @param parentFieldName 模型中作为parent字段名称
     * @param childrenFieldName 模型中作为children的字段名称
     */
    private static <T> void fillTree(List<T> parentList, List<T> originalList, String keyName, String parentFieldName, String childrenFieldName) throws Exception {
        for (T t : parentList) {
            List<T> children = fillChildren(t, originalList, keyName, parentFieldName, childrenFieldName);
            if (children.isEmpty()) {
                continue;
            }
            originalList.removeAll(children);
            fillTree(children, originalList, keyName, parentFieldName, childrenFieldName);
        }
    }

    /**
     * 封装子对象
     *
     * @param parent 父对象
     * @param originalList 待处理对象集合
     * @param keyName 作为唯一标示的字段名称
     * @param parentFieldName 模型中作为parent字段名称
     * @param childrenFieldName 模型中作为children的字段名称
     */
    private static <T> List<T> fillChildren(T parent, List<T> originalList, String keyName, String parentFieldName, String childrenFieldName) throws Exception {
        List<T> childList = new ArrayList<>();
        String parentId = BeanUtil.getProperty(parent, keyName);
        for (T t : originalList) {
            String childParentId = BeanUtil.getProperty(t, parentFieldName);
            if (parentId.equals(childParentId)) {
                childList.add(t);
            }
        }
		boolean hasChildrenFieldExists = FieldUtils.getDeclaredField(parent.getClass(), DEFAULT_HAS_CHILDREN_FIELD_NAME, true) != null;
        boolean hasChildren = false;
		if (!childList.isEmpty()) {
            FieldUtils.writeDeclaredField(parent, childrenFieldName, childList, true);
			hasChildren = true;
        }

		if (hasChildrenFieldExists) {
			FieldUtils.writeDeclaredField(parent, DEFAULT_HAS_CHILDREN_FIELD_NAME, hasChildren, true);
		}
        return childList;
    }

	/**
	 * 把子目列表转换为树形接口
	 * @param originalList 列表
	 * @param keyName 关键字段名
	 * @param parentFieldName 父级字段名
	 * @param childrenFieldName 子集名
	 * @return
	 * @param <T>
	 */
	public static <T> List<T> getTreeCyclic(List<T> originalList, String keyName, String parentFieldName, String childrenFieldName) {
		List<T> topList = new ArrayList<>();
		Map<String, List<T>> tMap = new HashMap<>();
		for (int i = 0; i < originalList.size(); i++) {
			T t = originalList.get(i);
			String parentId = BeanUtil.getProperty(t, parentFieldName);
			List<T> pList = tMap.get(parentId);
			if (pList == null) {
				pList = new ArrayList<>();
			}
			pList.add(t);
			tMap.put(parentId, pList);
			if (StringUtils.isBlank(parentId) || parentId.equals(DEAFULT_ROOT_ID) || parentId.equals(OTHER_ROOT_ID)) {
				topList.add(t);
			}
		}

		tMap.forEach((pid, children) -> {
			for (T child : children) {
				List<T> tList = tMap.get(BeanUtil.getProperty(child, keyName));
				if (CollUtil.isEmpty(tList)) {
					tList = new ArrayList<>();
				}
				BeanUtil.setFieldValue(child, childrenFieldName, tList);
			}
		});
		return topList;
	}


    /**
     * 根据子目号筛选树
     * @param clazz
     * @param childrenField
     * @param searchField
     * @param data
     * @param keyword
     * @return
     */
	public static <T> List<T> search(Class<T> clazz, String childrenField, String searchField, List<T> data, String keyword) {

		if (CollectionUtil.isEmpty(data)) {
			return data;
		}
		filterByKeyword(clazz, childrenField, searchField, data, keyword);
		return data;
	}

    /**
     * 根据子目号筛选
     * @param clazz
     * @param childrenField
     * @param searchField
     * @param data
     * @param keyword
     */
    private static <T> void filterByKeyword(Class<T> clazz, String childrenField, String searchField, List<T> data, String keyword) {
        String getChildrenStr = "get" + firstCharToUpperCase(childrenField);
        String getSearchStr = "get" + firstCharToUpperCase(searchField);
        Method getChildrenMethod;
        Method getSearchMethod;
        try {
            getChildrenMethod = clazz.getDeclaredMethod(getChildrenStr);
            getSearchMethod = clazz.getDeclaredMethod(getSearchStr);


            Iterator<T> parent = data.iterator();
            while (parent.hasNext()) {
                //当前节点
                T treeNode = parent.next();
                List<T> children = (List<T>) getChildrenMethod.invoke(treeNode);
                if (StrUtil.isNotEmpty(keyword) && !getSearchMethod.invoke(treeNode).toString().contains(keyword)) {
                    //当前节点不包含关键字，继续遍历下一级
                    // 取出下一级节点
                    // 递归
                    if (!CollectionUtil.isEmpty(children)) {
                        filterByKeyword(clazz, childrenField, searchField, children, keyword);
                    }
                    //下一级节点都被移除了，那么父节点也移除，因为父节点也不包含关键字
                    if (CollectionUtil.isEmpty(children)) {
                        parent.remove();
                    }
                } else {
                    //当前节点包含关键字，继续递归遍历
                    //子节点递归如果不包含关键字则会进入if分支被删除
                    // 递归
                    if (!CollectionUtil.isEmpty(children)) {
                        filterByKeyword(clazz, childrenField, searchField, children, keyword);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	public static String firstCharToUpperCase(String str) {
		if (str == null) {
			return null;
		}
		if (str.isEmpty()) {
			return "";
		}
		char[] cs = str.toCharArray();
		cs[0] -= 32;
		return String.valueOf(cs);
	}

}

package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.Category;
import cc.ryanc.halo.repository.CategoryRepository;
import cc.ryanc.halo.service.CategoryService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     分类业务逻辑实现类
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/11/30
 */
@Service
public class CategoryServiceImpl extends AbstractCrudService<Category, Long> implements CategoryService {

    private static final String POSTS_CACHE_NAME = "posts";

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        super(categoryRepository);
        this.categoryRepository = categoryRepository;
    }

    /**
     * 保存/修改分类目录
     *
     * @param category 分类目录
     * @return Category
     */
    @Override
    @CacheEvict(value = POSTS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public Category create(Category category) {
        return super.create(category);
    }

    /**
     * 根据编号移除分类目录
     *
     * @param cateId 分类目录编号
     * @return Category
     */
    @Override
    @CacheEvict(value = POSTS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public Category removeById(Long cateId) {
        return super.removeById(cateId);
    }

    /**
     * 根据分类目录路径查询，用于验证是否已经存在该路径
     *
     * @param cateUrl cateUrl
     * @return Category
     */
    @Override
    public Category findByCateUrl(String cateUrl) {
        return categoryRepository.findCategoryByCateUrl(cateUrl);
    }

    /**
     * 根据分类名称查询
     *
     * @param cateName 分类名称
     * @return Category
     */
    @Override
    public Category findByCateName(String cateName) {
        return categoryRepository.findCategoryByCateName(cateName);
    }

    /**
     * 将分类字符串集合转化为Category泛型集合
     *
     * @param strings strings
     * @return List
     */
    @Override
    public List<Category> strListToCateList(List<String> strings) {
        if (null == strings) {
            return null;
        }
        final List<Category> categories = new ArrayList<>();
        for (String str : strings) {
            // TODO There maybe cause NoSuchElementException
            categories.add(fetchById(Long.parseLong(str)).get());
        }
        return categories;
    }
}

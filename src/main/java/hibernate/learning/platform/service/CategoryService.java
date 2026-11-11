package hibernate.learning.platform.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hibernate.learning.platform.domain.entity.Category;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;
import hibernate.learning.platform.domain.exception.DuplicateResourceException;
import hibernate.learning.platform.domain.exception.ResourceNotFoundException;
import hibernate.learning.platform.repository.CategoryRepository;
import hibernate.learning.platform.repository.CourseRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;

    public Category create(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new DuplicateResourceException("Category with name already exists");
        }

        Category saved = categoryRepository.save(Category.create(name));
        LOGGER.info("event=category_created id={}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Category getById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @Transactional(readOnly = true)
    public List<Category> getAll() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    public Category update(Long id, String name) {
        Category category = getById(id);

        if (!category.getName().equals(name) && categoryRepository.existsByName(name)) {
            throw new DuplicateResourceException("Category with name already exists");
        }

        category.updateName(name);
        Category saved = categoryRepository.save(category);

        LOGGER.info("event=category_updated id={}", saved.getId());
        return saved;
    }

    public void delete(Long id) {
        Category category = getById(id);

        if (courseRepository.existsByCategoryId(id)) {
            throw new BusinessRuleViolationException("Category has assigned courses");
        }

        categoryRepository.delete(category);
        LOGGER.info("event=category_deleted id={}", id);
    }
}

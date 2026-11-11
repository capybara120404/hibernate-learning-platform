package hibernate.learning.platform.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hibernate.learning.platform.domain.entity.Tag;
import hibernate.learning.platform.domain.exception.BusinessRuleViolationException;
import hibernate.learning.platform.domain.exception.DuplicateResourceException;
import hibernate.learning.platform.domain.exception.ResourceNotFoundException;
import hibernate.learning.platform.repository.TagRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional
public class TagService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TagService.class);

    private final TagRepository tagRepository;

    public Tag create(String name) {
        if (tagRepository.existsByName(name)) {
            throw new DuplicateResourceException("Tag with name already exists");
        }

        Tag saved = tagRepository.save(Tag.create(name));
        LOGGER.info("event=tag_created id={}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Tag getById(Long id) {
        return tagRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
    }

    @Transactional(readOnly = true)
    public List<Tag> getAll() {
        return tagRepository.findAllByOrderByNameAsc();
    }

    public Tag update(Long id, String name) {
        Tag tag = getById(id);

        if (!tag.getName().equals(name) && tagRepository.existsByName(name)) {
            throw new DuplicateResourceException("Tag with name already exists");
        }

        tag.updateName(name);
        Tag saved = tagRepository.save(tag);

        LOGGER.info("event=tag_updated id={}", saved.getId());
        return saved;
    }

    public void delete(Long id) {
        Tag tag = getById(id);

        if (tagRepository.existsCourseAssigned(id)) {
            throw new BusinessRuleViolationException("Tag is assigned to courses");
        }

        tagRepository.delete(tag);
        LOGGER.info("event=tag_deleted id={}", id);
    }
}

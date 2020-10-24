package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import run.halo.app.model.entity.AttachmentGroup;
import run.halo.app.repository.AttachmentGroupRepository;
import run.halo.app.service.AttachmentGroupService;
import run.halo.app.service.AttachmentService;
import run.halo.app.service.base.AbstractCrudService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Attachment group service implementation.
 *
 * @author guqing
 * @date 2020-10-24
 */
@Slf4j
@Service
public class AttachmentGroupServiceImpl extends AbstractCrudService<AttachmentGroup, Integer> implements AttachmentGroupService {

    private final AttachmentGroupRepository attachmentGroupRepository;

    private final AttachmentService attachmentService;

    public AttachmentGroupServiceImpl(AttachmentGroupRepository attachmentGroupRepository,
                                      AttachmentService attachmentService) {
        super(attachmentGroupRepository);
        this.attachmentGroupRepository = attachmentGroupRepository;
        this.attachmentService = attachmentService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeGroupAndAttachmentBy(List<Integer> parentGroupIds) {
        List<Integer> groupIdsToDelete = listGroupIdsRecursivelyByParentIds(parentGroupIds);
        // delete attachment that group id in groupIds
        attachmentService.removeByGroupIdsPermanently(groupIdsToDelete);
        // remove attachment group by ids
        removeInBatch(groupIdsToDelete);
    }

    private List<Integer> listGroupIdsRecursivelyByParentIds(List<Integer> groupIds) {
        List<Integer> list = new ArrayList<>();
        listGroupIdsRecursivelyByParentIds(groupIds, list);
        // high level group id is added to the last
        list.addAll(groupIds);
        return list;
    }

    private void listGroupIdsRecursivelyByParentIds(List<Integer> groupIds, List<Integer> collect) {
        List<AttachmentGroup> attachmentGroups = attachmentGroupRepository.findByParentIdIn(groupIds);

        if (CollectionUtils.isEmpty(attachmentGroups)) {
            // termination condition of recursion
            return;
        }

        List<Integer> groupIdList = attachmentGroups.stream()
                .map(AttachmentGroup::getId)
                .collect(Collectors.toList());
        // add to collect
        collect.addAll(groupIdList);

        // find Recursively
        listGroupIdsRecursivelyByParentIds(groupIdList, collect);
    }
}

package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import run.halo.app.exception.BadRequestException;
import run.halo.app.model.dto.AttachmentDTO;
import run.halo.app.model.dto.AttachmentGroupDTO;
import run.halo.app.model.dto.AttachmentViewDTO;
import run.halo.app.model.entity.Attachment;
import run.halo.app.model.entity.AttachmentGroup;
import run.halo.app.repository.AttachmentGroupRepository;
import run.halo.app.service.AttachmentGroupService;
import run.halo.app.service.AttachmentService;
import run.halo.app.service.base.AbstractCrudService;

import java.util.*;
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

    @NonNull
    @Override
    public AttachmentViewDTO listBy(@NonNull Integer groupId) {
        // List attachments and groups by groupId
        List<AttachmentGroup> attachmentGroups = attachmentGroupRepository.findByParentId(groupId);
        List<Attachment> attachments = attachmentService.listByGroupId(groupId);

        List<AttachmentGroupDTO> attachmentGroupDtoList = attachmentGroups.stream()
                .sorted(Comparator.comparing(AttachmentGroup::getCreateTime).reversed())
                .map(attachmentGroup -> (AttachmentGroupDTO) new AttachmentGroupDTO().convertFrom(attachmentGroup))
                .collect(Collectors.toList());

        List<AttachmentDTO> attachmentDtoList = attachments.stream()
                .sorted(Comparator.comparing(Attachment::getCreateTime).reversed())
                .map(attachmentService::convertToDto)
                .collect(Collectors.toList());

        AttachmentViewDTO attachmentViewDTO = new AttachmentViewDTO();
        attachmentViewDTO.setGroups(attachmentGroupDtoList);
        attachmentViewDTO.setAttachments(attachmentDtoList);
        return attachmentViewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttachmentGroup createBy(AttachmentGroup attachmentGroup) {
        Optional<AttachmentGroup> groupOptional = attachmentGroupRepository
                .findByNameAndParentId(attachmentGroup.getName(), attachmentGroup.getParentId());
        if (groupOptional.isPresent()) {
            return groupOptional.get();
        }
        this.create(attachmentGroup);
        return attachmentGroup;
    }

    @Override
    public List<AttachmentGroup> listByParentId(Integer parentId) {
        return attachmentGroupRepository.findByParentId(parentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<AttachmentGroup> batchMoveTo(List<Integer> ids, Integer parentId) {
        List<Integer> groupIdsToBeMoved = listGroupIdsRecursivelyByParentIds(ids);
        if (groupIdsToBeMoved.contains(parentId)) {
            throw new BadRequestException("不能将目录移动到自身或其子目录下");
        }

        List<AttachmentGroup> attachmentGroups = listAllByIds(groupIdsToBeMoved);

        List<AttachmentGroup> result = new LinkedList<>();
        attachmentGroups.forEach(attachmentGroup -> {
            attachmentGroup.setParentId(parentId);
            // update
            this.update(attachmentGroup);
            // collect result
            if (ids.contains(attachmentGroup.getId())) {
                result.add(attachmentGroup);
            }
        });
        return result;
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

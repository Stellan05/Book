package org.example.baozi.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.baozi.book.entity.ReasonTemplate;
import org.example.baozi.book.mapper.ReasonTemplateMapper;
import org.example.baozi.book.service.ReasonTemplateService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 理由模板服务实现类
 */
@Service
@RequiredArgsConstructor
public class ReasonTemplateServiceImpl extends ServiceImpl<ReasonTemplateMapper, ReasonTemplate> implements ReasonTemplateService {

    private final ReasonTemplateMapper reasonTemplateMapper;

    /**
     * 添加理由模板
     * @param creatorId 创建者ID
     * @param content 理由内容
     * @param type 理由类型
     * @return 是否添加成功
     */
    @Override
    public boolean addTemplate(String creatorId, String content, Integer type) {
        ReasonTemplate template = new ReasonTemplate();
        template.setCreatorId(creatorId);
        template.setContent(content);
        template.setType(type);
        return save(template);
    }

    /**
     * 更新理由模板
     * @param id 模板ID
     * @param content 理由内容
     * @return 是否更新成功
     */
    @Override
    public boolean updateTemplate(Integer id, String content) {
        ReasonTemplate template = getById(id);
        if (template == null) {
            return false;
        }
        
        template.setContent(content);
        return updateById(template);
    }

    /**
     * 删除理由模板
     * @param id 模板ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteTemplate(Integer id) {
        return removeById(id);
    }


    /**
     * 根据类型获取理由模板列表
     * @param type 理由类型
     * @param creatorId 创建者ID
     * @return 理由模板列表
     */
    @Override
    public List<ReasonTemplate> getTemplatesByType(Integer type, String creatorId) {
        return reasonTemplateMapper.getTemplatesByType(type, creatorId);
    }
} 
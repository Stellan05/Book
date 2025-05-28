package org.example.baozi.book.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.baozi.book.entity.ReasonTemplate;

import java.util.List;

/**
 * 理由模板服务接口
 */
public interface ReasonTemplateService extends IService<ReasonTemplate> {
    /**
     * 添加理由模板
     * @param creatorId 创建者ID
     * @param content 理由内容
     * @param type 理由类型
     * @return 是否添加成功
     */
    boolean addTemplate(String creatorId, String content, Integer type);
    
    /**
     * 更新理由模板
     * @param id 模板ID
     * @param content 理由内容
     * @return 是否更新成功
     */
    boolean updateTemplate(Integer id, String content);
    
    /**
     * 删除理由模板
     * @param id 模板ID
     * @return 是否删除成功
     */
    boolean deleteTemplate(Integer id);

    
    /**
     * 根据类型获取理由模板列表
     * @param type 理由类型
     * @param creatorId 创建者ID
     * @return 理由模板列表
     */
    List<ReasonTemplate> getTemplatesByType(Integer type, String creatorId);
} 
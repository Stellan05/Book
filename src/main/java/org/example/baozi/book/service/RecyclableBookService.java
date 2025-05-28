package org.example.baozi.book.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.baozi.book.entity.RecyclableBook;
import org.example.baozi.book.response.PageResult;
import org.example.baozi.book.vo.RecyclableBookVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 可回收书籍服务接口
 */
public interface RecyclableBookService extends IService<RecyclableBook> {
    /**
     * 添加可回收书籍
     * @param bookId 书籍ID
     * @param bookName 书名
     * @param printingEdition 印刷版次
     * @param publisher 出版商
     * @return 添加的书籍ID
     */
    Integer addRecyclableBook(Long bookId, String bookName, String printingEdition, String publisher);
    
    /**
     * 分页查询可回收书籍列表
     * @param pages 分页参数
     * @param keyword 关键词（用于书名搜索）
     * @return 分页结果
     */
    PageResult<RecyclableBookVO> getRecyclableBooks(Page<RecyclableBook> pages, String keyword);

    /**
     * 获取可回收书籍详情
     * @param rId 可回收书籍ID
     * @return 可回收书籍对象
     */
    RecyclableBook getRecyclableBookInfo(Integer rId);
} 
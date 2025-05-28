package org.example.baozi.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.baozi.book.entity.RecyclableBook;
import org.example.baozi.book.mapper.RecyclableBookMapper;
import org.example.baozi.book.response.PageResult;
import org.example.baozi.book.service.RecyclableBookService;
import org.example.baozi.book.vo.RecyclableBookVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 可回收书籍服务实现类
 */
@Service
@RequiredArgsConstructor
public class RecyclableBookServiceImpl extends ServiceImpl<RecyclableBookMapper, RecyclableBook> implements RecyclableBookService {

    private final RecyclableBookMapper recyclableBookMapper;


    /**
     * 添加可回收书籍
     * @param bookId 书籍ID
     * @param bookTitle 书名
     * @param printingEdition 印刷版次
     * @param publisher 出版商
     * @return 添加的书籍ID
     */
    @Override
    public Integer addRecyclableBook(Long bookId, String bookTitle, String printingEdition, String publisher) {
        // 创建可回收书籍对象
        RecyclableBook recyclableBook = new RecyclableBook();
        recyclableBook.setBookId(bookId);
        recyclableBook.setBookTitle(bookTitle);
        recyclableBook.setPrintingEdition(printingEdition);
        recyclableBook.setPublisher(publisher);
        
        // 保存可回收书籍信息
        save(recyclableBook);
        
        return recyclableBook.getRId();
    }
    
    /**
     * 分页查询可回收书籍列表
     * @param pages 分页参数
     * @param keyword 关键词（用于书名搜索）
     * @return 分页结果
     */
    @Override
    public PageResult<RecyclableBookVO> getRecyclableBooks(Page<RecyclableBook> pages, String keyword) {
        // 方案1: 直接使用自定义查询方法进行分页
        Page<RecyclableBookVO> resultPage = recyclableBookMapper.getRBookByPage(pages);
        
        // 如果需要关键词过滤，可以在XML中添加条件
        
        return new PageResult<>(
                resultPage.getRecords(),// 当前页数据
                resultPage.getTotal(), //总记录数
                resultPage.getSize(), //每页条数
                resultPage.getCurrent() //当前页码
        );
    }


    /**
     * 获取可回收书籍详情
     * @param rId 可回收书籍ID
     * @return 可回收书籍对象
     */
    @Override
    public RecyclableBook getRecyclableBookInfo(Integer rId) {
        return getById(rId);
    }
} 
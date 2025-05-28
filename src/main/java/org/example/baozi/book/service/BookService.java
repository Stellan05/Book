package org.example.baozi.book.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.baozi.book.entity.Book;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 书籍服务接口
 */
public interface BookService extends IService<Book> {
    /**
     * 创建一本新书
     * @param ownerId 所有者学号
     * @param campus 校区
     * @param bookImage 书籍图片
     * @return 创建的书籍ID
     * @throws IOException 如果图片处理失败
     */
    Long createBook(String ownerId, String campus, MultipartFile bookImage) throws IOException;
    
    /**
     * 获取书籍信息
     * @param bookId 书籍ID
     * @return 书籍对象
     */
    Book getBookInfo(Long bookId);
} 
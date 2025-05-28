package org.example.baozi.book.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.baozi.book.entity.Book;
import org.example.baozi.book.mapper.BookMapper;
import org.example.baozi.book.service.BookService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 书籍服务实现类
 */
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {
    
    /**
     * 创建一本新书
     * @param ownerId 所有者学号
     * @param campus 校区
     * @param bookImage 书籍图片
     * @return 创建的书籍ID
     * @throws IOException 如果图片处理失败
     */
    @Override
    public Long createBook(String ownerId, String campus, MultipartFile bookImage) throws IOException {
        // 创建新的书籍对象
        Book book = new Book();
        book.setOwnerId(ownerId);
        book.setCampus(campus);
        
        // 如果提供了图片，处理并保存图片数据
        if (bookImage != null && !bookImage.isEmpty()) {
            book.setBookData(bookImage.getBytes());
        }
        
        // 保存书籍信息
        save(book);
        
        return book.getId();
    }
    
    /**
     * 获取书籍信息
     * @param bookId 书籍ID
     * @return 书籍对象
     */
    @Override
    public Book getBookInfo(Long bookId) {
        return getById(bookId);
    }
} 
package org.example.baozi.book.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.baozi.book.entity.Book;
import org.example.baozi.book.entity.RecyclableBook;
import org.example.baozi.book.entity.SealedBook;
import org.example.baozi.book.entity.Student;
import org.example.baozi.book.response.PageResult;
import org.example.baozi.book.response.ResponseMessage;
import org.example.baozi.book.service.BookService;
import org.example.baozi.book.service.RecyclableBookService;
import org.example.baozi.book.service.SealedBookService;
import org.example.baozi.book.service.StudentService;
import org.example.baozi.book.util.JWTUtil;
import org.example.baozi.book.vo.RecyclableBookVO;
import org.example.baozi.book.vo.SealedBookVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 书籍控制器
 * 处理书籍相关的请求
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    private final RecyclableBookService recyclableBookService;
    
    private final SealedBookService sealedBookService;

    private final StudentService studentService;
    
    /**
     * 添加待售书籍---同时创建订单
     * @param request token请求
     * @param bookImage 书籍图片
     * @param weight 书籍重量
     * @return 添加结果
     */
    @PostMapping("/sealed")
    public ResponseMessage<?> addSealedBook(
            HttpServletRequest request,
            @RequestParam(value = "bookImage", required = false) MultipartFile bookImage,
            @RequestParam("weight") Double weight) {

            String token = JWTUtil.getTokenFromAuthorization(request);

        try {
            // 获取当前学生信息
            String studentId = JWTUtil.getUsernameFromToken(token);
            Student student = studentService.getStudentById(studentId);
            
            if (student == null) {
                return ResponseMessage.error("学生信息不存在");
            }
            
            // 创建新书籍
            Long bookId = bookService.createBook(studentId, student.getCampus(), bookImage);
            
            // 添加待售书籍
            Integer sealedBookId = sealedBookService.addSealedBook(bookId, weight);

            // 创建订单
            Integer orderId = sealedBookService.createSealedOrder(sealedBookId);


            // 返回结果
            Map<String, Object> response = new HashMap<>();
            response.put("message", "添加待售书籍成功");
            response.put("sealedBookId", sealedBookId);
            response.put("orderId", orderId);
            return ResponseMessage.success(response);
        } catch (IOException e) {
            return ResponseMessage.error("图片处理失败");
        } catch (Exception e) {
            return ResponseMessage.error("添加待售书籍失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加可回收书籍
     * @param request 浏览器请求
     * @param bookCover 书籍封面图片
     * @param bookName 书名
     * @param printingEdition 印刷版次
     * @param publisher 出版商
     * @return 添加结果
     */
    @PostMapping("/recycle")
    public ResponseMessage<?> addRecyclableBook(
            HttpServletRequest request,
            @RequestParam("bookCover") MultipartFile bookCover,
            @RequestParam("bookName") String bookName,
            @RequestParam("printingEdition") String printingEdition,
            @RequestParam("publisher") String publisher) {

        String token = JWTUtil.getTokenFromAuthorization(request);


        try {
            // 获取当前学生信息
            String studentId = JWTUtil.getUsernameFromToken(token);
            Student student = studentService.getStudentById(studentId);
            
            if (student == null) {
                return ResponseMessage.info(403,"学生信息不存在");
            }
            
            // 创建新书籍
            Long bookId = bookService.createBook(studentId, student.getCampus(), bookCover);
            
            // 添加可回收书籍
            Integer recyclableBookId = recyclableBookService.addRecyclableBook(bookId, bookName, printingEdition, publisher);
            
            // 返回结果
            Map<String, Object> response = new HashMap<>();
            response.put("message", "添加可回收书籍成功");
            response.put("recyclableBookId", recyclableBookId);
            
            return ResponseMessage.success(response);
        } catch (IOException e) {
            return ResponseMessage.error("图片处理失败");
        } catch (Exception e) {

            return ResponseMessage.error("添加可回收书籍失败: " + e.getMessage());
        }
    }
    
    /**
     * 分页查询可回收书籍
     * @param page 页码
     * @param size 每页大小
     * @param keyword 关键词
     * @return 分页结果
     */
    @GetMapping("/recyclable")
    public PageResult<RecyclableBookVO> listRecyclableBooks(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword) {
        
        // 创建分页对象
        Page<RecyclableBook> pageParam = new Page<>(page, size);
        
        // 查询可回收书籍列表
        return recyclableBookService.getRecyclableBooks(pageParam, keyword);
    }
    
    /**
     * 获取可回收书籍详情---文本信息
     * @param id 可回收书籍ID
     * @return 书籍详情
     */
    @GetMapping("/recyclable/{id}")
    public ResponseMessage<?> getRecyclableBook(@PathVariable Integer id) {
        RecyclableBook recyclableBook = recyclableBookService.getRecyclableBookInfo(id);
        
        if (recyclableBook == null) {
            return ResponseMessage.notfound("图书未找到");
        }
        
        // 获取书籍主体信息
        Book book = bookService.getBookInfo(recyclableBook.getBookId());
        
        if (book == null) {
            return ResponseMessage.notfound("图书未找到");
        }
        
        // 组装返回数据
        Map<String, Object> response = new HashMap<>();
        response.put("id", recyclableBook.getRId());
        response.put("bookName", recyclableBook.getBookTitle());
        response.put("printingEdition", recyclableBook.getPrintingEdition());
        response.put("publisher", recyclableBook.getPublisher());
        response.put("campus", book.getCampus());
        response.put("createTime", book.getCreateTime());
        response.put("ownerId", book.getOwnerId());
        return ResponseMessage.success(response);
    }



    /**
     * 获取书籍图片信息
     * 将书籍图片和文本信息分开获取可以优化性能，对图片进行懒加载
     * @param id 书籍ID
     * @return 书籍封面图片
     */
    @GetMapping(value = "/cover/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<?> getBookCover(@PathVariable Long id) {
        Book book = bookService.getBookInfo(id);
        
        if (book == null || book.getBookData() == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(book.getBookData());
    }

    /**
     * 获取学生的待售书籍列表
     * @param request 浏览器请求
     * @return 待售书籍列表
     */
    @GetMapping("/sealed/student")
    public ResponseMessage<?> getStudentSealedBooks(HttpServletRequest request) {
        // 获取当前学生ID
        String token = JWTUtil.getTokenFromAuthorization(request);
        String studentId = JWTUtil.getUsernameFromToken(token);
        
        // 查询学生的待售书籍
        List<SealedBookVO> sealedBooks = sealedBookService.getStudentSealedBooks(studentId);
        
        return ResponseMessage.success(sealedBooks);
    }
}
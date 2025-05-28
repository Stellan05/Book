package org.example.baozi.book.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.baozi.book.entity.SealedBook;
import org.example.baozi.book.vo.SealedBookVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 待售书籍服务接口
 */
public interface SealedBookService extends IService<SealedBook> {
    /**
     * 添加待售书籍
     * @param bookId 书籍ID
     * @param bookWeight 书籍重量
     * @return 添加的待售书籍ID
     */
    Integer addSealedBook(Long bookId, Double bookWeight);
    
    /**
     * 获取学生所有待售书籍
     * @param ownerId 学生ID
     * @return 待售书籍列表
     */
    List<SealedBookVO> getStudentSealedBooks(String ownerId);
    
    /**
     * 收书员接单
     * @param sId 待售书籍ID
     * @return 是否接单成功
     */
    boolean acceptBook(Integer sId);
    
    /**
     * 计算书籍价格（重量*1.6）
     * @param bookWeight 书籍重量
     * @return 计算得到的价格
     */
    Double calculatePrice(Double bookWeight);

    /**
     * 添加售卖书籍订单
     * @param sId sealedBookId
     * @return 是否更新订单
     */
    Integer createSealedOrder(Integer sId);
} 
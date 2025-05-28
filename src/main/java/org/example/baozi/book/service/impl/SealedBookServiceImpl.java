package org.example.baozi.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.baozi.book.entity.Book;
import org.example.baozi.book.entity.CollectOrder;
import org.example.baozi.book.entity.SealedBook;
import org.example.baozi.book.entity.Student;
import org.example.baozi.book.mapper.BookMapper;
import org.example.baozi.book.mapper.CollectOrderMapper;
import org.example.baozi.book.mapper.SealedBookMapper;
import org.example.baozi.book.mapper.StudentMapper;
import org.example.baozi.book.service.SealedBookService;
import org.example.baozi.book.vo.SealedBookVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 待售书籍服务实现类
 */
@Service
@RequiredArgsConstructor
public class SealedBookServiceImpl extends ServiceImpl<SealedBookMapper, SealedBook> implements SealedBookService {

    private final BookMapper bookMapper;
    private final SealedBookMapper sealedBookMapper;
    private final CollectOrderMapper collectOrderMapper;

    /**
     * 添加待售书籍
     * @param bookId 书籍ID
     * @param bookWeight 书籍重量
     * @return 添加的待售书籍ID
     */
    @Override
    public Integer addSealedBook(Long bookId, Double bookWeight) {
        // 创建待售书籍对象
        SealedBook sealedBook = new SealedBook();
        sealedBook.setBookId(bookId);
        sealedBook.setBookWeight(bookWeight);
        
        // 计算价格（重量 * 1.6）
        Double price = calculatePrice(bookWeight);
        sealedBook.setPrice(price);
        
        // 设置初始状态为未接单
        sealedBook.setIsAccept(false);
        
        // 保存待售书籍信息
        save(sealedBook);
        
        return sealedBook.getSId();
    }
    
    /**
     * 获取学生所有待售书籍
     * @param ownerId 学生ID
     * @return 待售书籍列表
     */
    @Override
    public List<SealedBookVO> getStudentSealedBooks(String ownerId) {
        // 首先查询属于该学生的所有书籍
        return sealedBookMapper.sealedBookList(ownerId);
    }
    
    /**
     * 收书员接单
     * @param sId 待售书籍ID
     * @return 是否接单成功
     */
    @Override
    public boolean acceptBook(Integer sId) {
        // 查询待售书籍是否存在
        SealedBook sealedBook = getById(sId);
        if (sealedBook == null) {
            return false;
        }
        
        // 修改接单状态
        sealedBook.setIsAccept(true);
        
        // 保存修改
        return updateById(sealedBook);
    }
    
    /**
     * 计算书籍价格（重量*1.6）
     * @param bookWeight 书籍重量
     * @return 计算得到的价格
     */
    @Override
    public Double calculatePrice(Double bookWeight) {
        if (bookWeight == null || bookWeight <= 0) {
            return 0.0;
        }
        
        // 价格计算公式：重量 * 1.6
        return bookWeight * 1.6;
    }

    /**
     * 创建售卖书籍订单
     * @param sealedId sealedBookId
     * @return 是否创建成功
     */
    @Override
    public Integer createSealedOrder(Integer sealedId) {
        SealedBook sealedBook = getById(sealedId);
        if (sealedBook == null) {
            return -1;
        }
        else{
            CollectOrder order = new CollectOrder();
            Book book = bookMapper.selectById(sealedBook.getBookId());

            order.setStudentId(book.getOwnerId());
            order.setSealedBookId(sealedBook.getSId());
            order.setCampus(book.getCampus());
            int result=collectOrderMapper.insert(order);
            return result==1?order.getId():-1;
        }

    }
} 
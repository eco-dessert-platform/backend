package com.bbangle.bbangle.board.admin.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.EditStockFlag;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

@DisplayName("[동시성 테스트] AdminProductService - 상품 재고 상태 변경")
@SpringBootTest
@ActiveProfiles("test")
class AdminProductServiceConcurrencyTest {

    @Autowired
    private AdminProductService adminProductService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Store store;
    private Board board;
    private Product product;

    @BeforeEach
    void setUp() {
        resetTable();
        initializeTestData();
    }

    private void resetTable() {
        transactionTemplate.executeWithoutResult(status -> {
            em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE store").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE product_board").executeUpdate();
            em.createNativeQuery("TRUNCATE TABLE product").executeUpdate();
            em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
        });
    }

    private void initializeTestData() {
        transactionTemplate.executeWithoutResult(status -> {
            store = storeRepository.save(StoreFixture.defaultStore());
            board = boardRepository.save(BoardFixture.defaultBoardWithStore(store, "테스트 상품"));
            product = productRepository.save(ProductFixture.create(board, "테스트 상품"));
        });
    }

    @Test
    @DisplayName("동시에 2개 스레드가 재고를 감소시킬 때 Lost Update가 발생하지 않는다")
    void concurrentDecrease_twoThreads_preventsLostUpdate() throws InterruptedException {
        // given
        int initialStock = 100;
        transactionTemplate.executeWithoutResult(status -> {
            product.editStock(initialStock - product.getStock(), EditStockFlag.INCREASE);
            productRepository.save(product);
        });

        int decreaseAmount = 30;
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ConcurrentHashMap<String, Exception> errors = new ConcurrentHashMap<>();

        // when
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    adminProductService.editProductStock(
                        product.getId(), decreaseAmount, EditStockFlag.DECREASE);
                } catch (Exception e) {
                    errors.put("Thread-" + threadId, e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        // then
        // - 모든 스레드가 성공 (에러 없음)
        assertThat(errors).isEmpty();

        // - stock = 100 - 30 - 30 = 40
        Product result = productRepository.findById(product.getId()).get();
        assertThat(result.getStock()).isEqualTo(initialStock - (decreaseAmount * threadCount));
    }

    @Test
    @DisplayName("동시에 2개 스레드가 재고를 증가시킬 때 Lost Update가 발생하지 않는다")
    void concurrentIncrease_twoThreads_preventsLostUpdate() throws InterruptedException {
        // given
        int initialStock = product.getStock();
        int increaseAmount = 50;
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ConcurrentHashMap<String, Exception> errors = new ConcurrentHashMap<>();

        // when
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    adminProductService.editProductStock(
                        product.getId(), increaseAmount, EditStockFlag.INCREASE);
                } catch (Exception e) {
                    errors.put("Thread-" + threadId, e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        // then
        // - 모든 스레드가 성공 (에러 없음)
        assertThat(errors).isEmpty();

        // - stock = initial + 50 + 50 = initial + 100
        Product result = productRepository.findById(product.getId()).get();
        assertThat(result.getStock()).isEqualTo(initialStock + (increaseAmount * threadCount));
    }

    @Test
    @DisplayName("동시에 증가와 감소 작업이 혼합될 때 정확한 결과를 보장한다")
    void concurrentMixedOperations_preventsLostUpdate() throws InterruptedException {
        // given
        int initialStock = 100;
        transactionTemplate.executeWithoutResult(status -> {
            product.editStock(initialStock - product.getStock(), EditStockFlag.INCREASE);
            productRepository.save(product);
        });

        int threadCount = 4;  // 2개는 증가, 2개는 감소
        int amountPerThread = 25;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ConcurrentHashMap<String, Exception> errors = new ConcurrentHashMap<>();

        // when
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            final EditStockFlag flag = (i % 2 == 0) ? EditStockFlag.INCREASE : EditStockFlag.DECREASE;
            executor.submit(() -> {
                try {
                    adminProductService.editProductStock(
                        product.getId(), amountPerThread, flag);
                } catch (Exception e) {
                    errors.put("Thread-" + threadId, e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        // then
        // - 모든 스레드가 성공 (에러 없음)
        assertThat(errors).isEmpty();

        // - stock = 100 + 25 - 25 + 25 - 25 = 100 (2개 증가, 2개 감소)
        Product result = productRepository.findById(product.getId()).get();
        assertThat(result.getStock()).isEqualTo(initialStock);
    }

    @Test
    @DisplayName("동시 감소 시 재고가 음수가 되지 않는다")
    void concurrentDecrease_preventNegativeStock() throws InterruptedException {
        // given
        int initialStock = 50;
        transactionTemplate.executeWithoutResult(status -> {
            product.editStock(initialStock - product.getStock(), EditStockFlag.INCREASE);
            productRepository.save(product);
        });

        int decreaseAmount = 30;
        int threadCount = 2;  // 2개 스레드가 각각 30씩 감소 시도 (총 60, 초기값 50)
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ConcurrentHashMap<Integer, Exception> errors = new ConcurrentHashMap<>();

        // when
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    adminProductService.editProductStock(
                        product.getId(), decreaseAmount, EditStockFlag.DECREASE);
                } catch (Exception e) {
                    errors.put(threadId, e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        // then - 두 번째 스레드는 예외 발생해야 함
        assertThat(errors).hasSize(1);

        // - stock은 음수가 아님 (첫 번째만 감소: 50 - 30 = 20)
        Product result = productRepository.findById(product.getId()).get();
        assertThat(result.getStock()).isGreaterThanOrEqualTo(0);
        assertThat(result.getStock()).isEqualTo(20);
    }

    @Test
    @DisplayName("부하 테스트: 100개 스레드가 동시에 재고를 감소시킬 때 정확한 결과를 보장한다")
    void loadTest_concurrentDecrease_hundredThreads() throws InterruptedException {
        // given
        int initialStock = 10000;
        transactionTemplate.executeWithoutResult(status -> {
            product.editStock(initialStock - product.getStock(), EditStockFlag.INCREASE);
            productRepository.save(product);
        });

        int threadCount = 100;
        int decreaseAmount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ConcurrentHashMap<String, Exception> errors = new ConcurrentHashMap<>();

        // when
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    adminProductService.editProductStock(
                        product.getId(), decreaseAmount, EditStockFlag.DECREASE);
                } catch (Exception e) {
                    errors.put("Thread-" + threadId, e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        long endTime = System.currentTimeMillis();
        executor.shutdown();

        // then
        // - 모든 스레드가 성공 (에러 없음)
        assertThat(errors).isEmpty();

        // - stock = 10000 - (10 * 100) = 9000
        Product result = productRepository.findById(product.getId()).get();
        assertThat(result.getStock()).isEqualTo(initialStock - (decreaseAmount * threadCount));

        // - 성능 기록 (5초 이내 완료)
        long duration = endTime - startTime;
        System.out.println("100 threads completed in " + duration + "ms");
        assertThat(duration).isLessThan(5000);
    }

    @Test
    @DisplayName("부하 테스트: 100개 스레드가 동시에 재고를 증가시킬 때 정확한 결과를 보장한다")
    void loadTest_concurrentIncrease_hundredThreads() throws InterruptedException {
        // given
        int initialStock = product.getStock();
        int threadCount = 100;
        int increaseAmount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ConcurrentHashMap<String, Exception> errors = new ConcurrentHashMap<>();

        // when
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    adminProductService.editProductStock(
                        product.getId(), increaseAmount, EditStockFlag.INCREASE);
                } catch (Exception e) {
                    errors.put("Thread-" + threadId, e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        // then
        // - 모든 스레드가 성공 (에러 없음)
        assertThat(errors).isEmpty();

        // - stock = initial + (10 * 100) = initial + 1000
        Product result = productRepository.findById(product.getId()).get();
        assertThat(result.getStock()).isEqualTo(initialStock + (increaseAmount * threadCount));
    }
}

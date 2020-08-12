package br.com.josehamilton.library.api.model.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.josehamilton.library.api.model.entity.Book;
import br.com.josehamilton.library.api.model.entity.Loan;

import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

	@Query(value = "SELECT CASE WHEN ( COUNT(l.id) > 0 ) THEN true ELSE false END"
			+ " FROM Loan l WHERE l.book = :book AND ( l.returned is null or l.returned is false )")
	boolean existsByBookAndNotReturned(@Param("book") Book book);

	@Query(value = "SELECT l FROM Loan as l JOIN l.book as b WHERE b.isbn = :isbn or l.customer = :customer")
	Page<Loan> findByBookIsbnOrCustomer(@Param("isbn") String isbn, @Param("customer") String customer,
			Pageable pageRequest);

	Page<Loan> findByBook(Book book, Pageable pageable);

    @Query(value = "SELECT l FROM Loan as l WHERE l.loanDate <= :threeDaysAgo AND ( l.returned is null or l.returned is false )")
	List<Loan> findByLoanDateLessThanAndNotReturned(@Param("threeDaysAgo") LocalDate threeDaysAgo);
}
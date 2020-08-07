package br.com.josehamilton.library.api.model.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.josehamilton.library.api.model.entity.Book;
import br.com.josehamilton.library.api.model.entity.Loan;
import br.com.josehamilton.library.api.model.repositories.LoanRepository;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private LoanRepository repository;

	@Test
	@DisplayName("Deve verificar se existe empréstimo não devolvido para o livro.")
	public void existsByBookAndNotReturned() {
		// Cenário
		Loan loan = createAndPersistLoan();
		Book book = loan.getBook();
		// Execução
		boolean exists = this.repository.existsByBookAndNotReturned(book);
		// Verificação
		assertThat(exists).isTrue();
	}

	@Test
	@DisplayName("Deve buscar empréstimo pelo isbn do livro ou customer.")
	public void findByBookIsbnOrCustomerTest() {
		// Cenário
		Loan loan = createAndPersistLoan();
		// Execução
		Page<Loan> result = repository.findByBookIsbnOrCustomer("123", "Fulano", PageRequest.of(0, 10));
		// Verificações
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent()).contains(loan);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getPageable().getPageSize()).isEqualTo(10);
		assertThat(result.getTotalElements()).isEqualTo(1);
	}

	public Loan createAndPersistLoan() {
		Book book = BookRepositoryTest.createNewBook("123");
		entityManager.persist(book);
		Loan loan = Loan.builder().customer("Fulano").book(book).loanDate(LocalDate.now()).build();
		entityManager.persist(loan);
		return loan;
	}

}
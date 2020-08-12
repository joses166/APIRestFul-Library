package br.com.josehamilton.library.api.model.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

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
		Loan loan = createAndPersistLoan(LocalDate.now());
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
		Loan loan = createAndPersistLoan(LocalDate.now());
		// Execução
		Page<Loan> result = repository.findByBookIsbnOrCustomer("123", "Fulano", PageRequest.of(0, 10));
		// Verificações
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent()).contains(loan);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getPageable().getPageSize()).isEqualTo(10);
		assertThat(result.getTotalElements()).isEqualTo(1);
	}

	@Test
	@DisplayName("Deve obter empréstimos cuja data empréstimo for menor ou igual a três dias e não retornados.")
	public void findByLoanDateLessThanAndNotReturnedTest() {
		// Cenário
		Loan loan = createAndPersistLoan(LocalDate.now().minusDays(5));
		// Execução
		List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));
		// Verificações
		assertThat(result).hasSize(1).contains(loan);
	}

	@Test
	@DisplayName("Deve retornar vazio quando não houver empréstimos atrasados.")
	public void notFindByLoanDateLessThanAndNotReturnedTest() {
		// Cenário
		Loan loan = createAndPersistLoan(LocalDate.now());
		// Execução
		List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));
		// Verificações
		assertThat(result).isEmpty();
	}

	public Loan createAndPersistLoan(LocalDate loanDate) {
		Book book = BookRepositoryTest.createNewBook("123");
		entityManager.persist(book);
		Loan loan = Loan.builder().customer("Fulano").book(book).loanDate(loanDate).build();
		entityManager.persist(loan);
		return loan;
	}

}
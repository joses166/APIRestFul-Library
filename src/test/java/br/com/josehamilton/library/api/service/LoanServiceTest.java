package br.com.josehamilton.library.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.josehamilton.library.api.dtos.LoanFilterDTO;
import br.com.josehamilton.library.api.model.entity.Book;
import br.com.josehamilton.library.api.model.entity.Loan;
import br.com.josehamilton.library.api.model.repositories.LoanRepository;
import br.com.josehamilton.library.api.services.LoanService;
import br.com.josehamilton.library.api.services.impl.LoanServiceImpl;
import br.com.josehamilton.library.exception.BusinessException;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

	private LoanService service;

	@MockBean
	private LoanRepository repository;

	@BeforeEach
	public void setUp() {
		this.service = new LoanServiceImpl(repository);
	}

	@Test
	@DisplayName("Deve salvar um empréstimo.")
	public void saveLoanTest() {
		// Cenário
		Book book = Book.builder().id(1l).build();
		String customer = "Fulano";
		Loan savingLoan = Loan.builder().id(1l).book(book).customer(customer).loanDate(LocalDate.now()).build();

		Loan savedLoan = Loan.builder().id(1l).book(book).customer(customer).loanDate(LocalDate.now()).build();
		when(this.repository.existsByBookAndNotReturned(book)).thenReturn(false);
		when(repository.save(savingLoan)).thenReturn(savedLoan);
		// Execução
		Loan loan = this.service.save(savingLoan);
		// Verificações
		assertThat(loan.getId()).isEqualTo(savedLoan.getId());
		assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
		assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
		assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
	}

	@Test
	@DisplayName("Deve salvar um empréstimo.")
	public void loanedBookSaveTest() {
		// Cenário
		Book book = Book.builder().id(1l).build();
		String customer = "Fulano";
		Loan savingLoan = createLoan();
		when(this.repository.existsByBookAndNotReturned(book)).thenReturn(true);
		// Execução
		Throwable exception = catchThrowable(() -> this.service.save(savingLoan));
		// Verificações
		assertThat(exception).isInstanceOf(BusinessException.class).hasMessage("Book already loaned.");
		verify(repository, never()).save(savingLoan);
	}

	@Test
	@DisplayName("Deve retornar as informações de um empréstimo pelo ID.")
	public void getLoanDetailsTest() {
		// Cenário
		Long id = 1l;
		Loan loan = createLoan();
		loan.setId(id);
		Mockito.when(repository.findById(id)).thenReturn(Optional.of(loan));
		// Execução
		Optional<Loan> foundedLoan = this.service.getById(id);
		// Verificações
		assertThat(foundedLoan.isPresent()).isTrue();
		assertThat(foundedLoan.get().getId()).isEqualTo(loan.getId());
		assertThat(foundedLoan.get().getCustomer()).isEqualTo(loan.getCustomer());
		assertThat(foundedLoan.get().getBook()).isEqualTo(loan.getBook());
		assertThat(foundedLoan.get().getLoanDate()).isEqualTo(loan.getLoanDate());
		verify(repository, times(1)).findById(id);
	}

	@Test
	@DisplayName("Deve atualizar um empréstimo.")
	public void updateLoanTest() {
		// Cenário
		Long id = 1l;
		Loan loan = createLoan();
		loan.setId(id);
		loan.setReturned(true);
		Mockito.when(this.repository.save(loan)).thenReturn(loan);
		// Execução
		Loan updatedLoan = this.service.update(loan);
		// Verificações
		assertThat(updatedLoan.getReturned()).isEqualTo(loan.getReturned());
		verify(repository).save(loan);
	}

	@Test
	@DisplayName("Deve filtrar empréstimos pelas propriedades.")
	public void findLoanTest() {
		// Cenário
		LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().customer("Fulano").isbn("321").build();
		Long id = 1l;
		Loan loan = createLoan();
		loan.setId(id);

		PageRequest pageRequest = PageRequest.of(0, 10);
		List<Loan> list = Arrays.asList(loan);
		Page<Loan> page = new PageImpl<Loan>(list, pageRequest, list.size());

		when(repository.findByBookIsbnOrCustomer(Mockito.anyString(), Mockito.anyString(), Mockito.any(Pageable.class)))
				.thenReturn(page);

		// Execução
		Page<Loan> result = service.find(loanFilterDTO, pageRequest);

		// Verificação
		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent()).isEqualTo(list);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getPageable().getPageSize()).isEqualTo(10);
	}

	@Test
	@DisplayName("Deve retornar empréstimos de um livro.")
	public void getLoansByBook() {
		// Cenário
		Book book = Book.builder().title("some title").author("some author").isbn("123ABC").build();
		Loan loan = Loan.builder().book(book).loanDate(LocalDate.now()).customer("Fulano").build();
		PageRequest pageRequest = PageRequest.of(0, 20);
		List<Loan> list = Arrays.asList(loan);
		Page<Loan> page = new PageImpl<Loan>( list, pageRequest, 1 );
		Mockito.when( repository.findByBook(Mockito.any(Book.class), Mockito.any(Pageable.class)) ).thenReturn( page );
		// Execução
		Page<Loan> result = service.getLoansByBook(book, pageRequest);
		// Verificações
		assertThat( result.getTotalElements() ).isEqualTo( 1 );
		assertThat( result.getContent() ).isEqualTo( list );
		assertThat( result.getPageable().getPageNumber() ).isEqualTo( 0 );
		assertThat( result.getPageable().getPageSize() ).isEqualTo( 20 );
	}

	public static Loan createLoan() {
		Book book = Book.builder().id(1l).build();
		String customer = "Fulano";
		return Loan.builder().book(book).customer(customer).loanDate(LocalDate.now()).build();
	}

}

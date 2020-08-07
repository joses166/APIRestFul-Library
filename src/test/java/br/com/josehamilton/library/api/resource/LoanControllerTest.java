package br.com.josehamilton.library.api.resource;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.josehamilton.library.api.dtos.LoanDTO;
import br.com.josehamilton.library.api.dtos.LoanFilterDTO;
import br.com.josehamilton.library.api.dtos.ReturnedLoanDTO;
import br.com.josehamilton.library.api.model.entity.Book;
import br.com.josehamilton.library.api.model.entity.Loan;
import br.com.josehamilton.library.api.resources.LoanController;
import br.com.josehamilton.library.api.service.LoanServiceTest;
import br.com.josehamilton.library.api.services.BookService;
import br.com.josehamilton.library.api.services.LoanService;
import br.com.josehamilton.library.exception.BusinessException;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {

	static final String LOAN_API = "/api/loans";

	@Autowired
	MockMvc mvc;

	@MockBean
	private BookService bookService;

	@MockBean
	private LoanService loanService;

	@Test
	@DisplayName("Deve realizar um empréstimo.")
	public void createLoanTest() throws Exception {
		// Cenário
		LoanDTO dto = LoanDTO.builder().isbn("123").email("customer@gmail.com").customer("Fulano").build();
		String json = new ObjectMapper().writeValueAsString(dto);

		Book book = Book.builder().id(1l).isbn("123").build();
		BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book));

		Loan loan = Loan.builder().id(1l).customer("Fulano").book(book).loanDate(LocalDate.now()).build();
		BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);
		// Execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json);
		// Verificação
		mvc.perform(request).andExpect(status().isCreated()).andExpect(content().string("1"));
	}

	@Test
	@DisplayName("Deve retornar erro ao tentar fazer emprestimo de livro inexistente.")
	public void invalidIsbnCreateLoanTest() throws Exception {
		// Cenário
		LoanDTO dto = LoanDTO.builder().isbn("123").email("customer@gmail.com").customer("Fulano").build();
		String json = new ObjectMapper().writeValueAsString(dto);
		BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.empty());
		// Execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json);
		// Verificações
		mvc.perform(request).andExpect(status().isBadRequest()).andExpect(jsonPath("errors", hasSize(1)))
				.andExpect(jsonPath("errors[0]").value("Book not found for passed isbn."));
	}

	@Test
	@DisplayName("Deve retornar erro ao tentar fazer emprestimo de livro emprestado.")
	public void loanedBookErrorOnCreateLoanTest() throws Exception {
		// Cenário
		LoanDTO dto = LoanDTO.builder().email("customer@gmail.com").isbn("123").customer("Fulano").build();
		String json = new ObjectMapper().writeValueAsString(dto);
		Book book = Book.builder().id(1l).isbn("123").build();
		BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book));
		BDDMockito.given(loanService.save(Mockito.any(Loan.class)))
				.willThrow(new BusinessException("Book already loaned."));
		// Execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json);
		// Verificações
		mvc.perform(request).andExpect(status().isBadRequest()).andExpect(jsonPath("errors", hasSize(1)))
				.andExpect(jsonPath("errors[0]").value("Book already loaned."));
	}

	@Test
	@DisplayName("Deve retornar um livro.")
	public void returnBookTest() throws Exception {
		ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
		Loan loan = Loan.builder().id(1l).build();
		BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.of(loan));

		String json = new ObjectMapper().writeValueAsString(dto);

		mvc.perform(patch(LOAN_API.concat("/1")).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isOk());

		verify(loanService, times(1)).update(loan);
	}

	@Test
	@DisplayName("Deve retornar um livro inexistente.")
	public void returnInexistentBookTest() throws Exception {
		ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();

		BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.empty());

		String json = new ObjectMapper().writeValueAsString(dto);

		mvc.perform(patch(LOAN_API.concat("/1")).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isNotFound());

		verify(loanService, never()).update(Loan.builder().build());
	}

	@Test
	@DisplayName("Deve filtrar empréstimos.")
	public void findLoansTest() throws Exception {
		Long id = 1l;

		Loan loan = LoanServiceTest.createLoan();
		loan.setId(id);
		Book book = Book.builder().id(1l).isbn("321").build();
		loan.setBook(book);

		BDDMockito.given(loanService.find(Mockito.any(LoanFilterDTO.class), Mockito.any(Pageable.class)))
				.willReturn(new PageImpl<Loan>(Arrays.asList(loan), PageRequest.of(0, 10), 1));

		String queryString = String.format("?isbn=%s&customer=%s&page=0&size=10", book.getIsbn(), loan.getCustomer());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(LOAN_API.concat(queryString))
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request).andExpect(status().isOk()).andExpect(jsonPath("content", hasSize(1)))
				.andExpect(jsonPath("totalElements").value(1)).andExpect(jsonPath("pageable.pageSize").value(10))
				.andExpect(jsonPath("pageable.pageNumber").value(0));

	}

}

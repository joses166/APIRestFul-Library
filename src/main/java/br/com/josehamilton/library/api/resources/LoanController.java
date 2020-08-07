package br.com.josehamilton.library.api.resources;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.josehamilton.library.api.dtos.BookDTO;
import br.com.josehamilton.library.api.dtos.LoanDTO;
import br.com.josehamilton.library.api.dtos.LoanFilterDTO;
import br.com.josehamilton.library.api.dtos.ReturnedLoanDTO;
import br.com.josehamilton.library.api.model.entity.Book;
import br.com.josehamilton.library.api.model.entity.Loan;
import br.com.josehamilton.library.api.services.BookService;
import br.com.josehamilton.library.api.services.LoanService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

	private final LoanService service;
	private final BookService bookService;
	private final ModelMapper modelMapper;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Long create(@RequestBody @Valid LoanDTO dto) {
		Book book = bookService.getBookByIsbn(dto.getIsbn()).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for passed isbn."));
		Loan entity = Loan.builder().book(book).customer(dto.getCustomer()).loanDate(LocalDate.now()).build();
		entity = service.save(entity);
		return entity.getId();
	}

	@PatchMapping("{id}")
	@ResponseStatus(HttpStatus.OK)
	public void returnBook(@PathVariable Long id, @RequestBody ReturnedLoanDTO dto) {
		Loan loan = this.service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		loan.setReturned(dto.getReturned());
		this.service.update(loan);
	}

	@GetMapping
	public Page<LoanDTO> find(LoanFilterDTO dto, Pageable pageRequest) {
		Page<Loan> result = service.find(dto, pageRequest);
		List<LoanDTO> loans = result.getContent().stream().map(entity -> {
			Book book = entity.getBook();
			BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
			LoanDTO loanDTO = modelMapper.map(entity, LoanDTO.class);
			loanDTO.setBook(bookDTO);
			return loanDTO;
		}).collect(Collectors.toList());
		return new PageImpl<LoanDTO>(loans, pageRequest, result.getTotalElements());
	}

}

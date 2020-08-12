package br.com.josehamilton.library.api.resources;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.josehamilton.library.api.dtos.BookDTO;
import br.com.josehamilton.library.api.dtos.LoanDTO;
import br.com.josehamilton.library.api.model.entity.Book;
import br.com.josehamilton.library.api.model.entity.Loan;
import br.com.josehamilton.library.api.services.BookService;
import br.com.josehamilton.library.api.services.LoanService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Api("Book API")
public class BookController {

	private final BookService service;
	private final ModelMapper modelMapper;
	private final LoanService loanService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Creates a book.")
	@ApiResponses({ @ApiResponse(code = 201, message = "Book succesfully created.") })
	public BookDTO create(@RequestBody @Valid BookDTO dto) {
		// converte a classe BookDTO em Book
		Book entity = modelMapper.map(dto, Book.class);
		// método para salva um novo livro
		entity = service.save(entity);
		// converte a classe Book em BookDTO
		return modelMapper.map(entity, BookDTO.class);
	}

	@GetMapping("{id}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Obtains a book details by id.")
	@ApiResponses({ @ApiResponse(code = 200, message = "Book succesfully finded.") })
	public BookDTO get(@PathVariable Long id) {
		return service.getById(id).map(book -> modelMapper.map(book, BookDTO.class))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	@DeleteMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation("Deletes a book by id.")
	@ApiResponses({@ApiResponse(code = 204, message = "Book succesfully deleted.")})
	public void delete(@PathVariable Long id) {
		Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		service.delete(book);
	}

	@PutMapping("{id}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Updates a book.")
	@ApiResponses({ @ApiResponse(code = 200, message = "Book succesfully updated.") })
	public BookDTO update(@PathVariable Long id, BookDTO dto) {
		return service.getById(id).map(book -> {
			book.setAuthor(dto.getAuthor());
			book.setTitle(dto.getTitle());
			book = service.update(book);
			return modelMapper.map(book, BookDTO.class);
		}).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	@GetMapping
	@ApiOperation("Find books by params.")
	@ApiResponses({ @ApiResponse(code = 200, message = "Books succesfully finded.") })
	public Page<BookDTO> find(BookDTO dto, Pageable pageRequest) {
		Book filter = modelMapper.map(dto, Book.class);
		Page<Book> result = service.find(filter, pageRequest);
		List<BookDTO> list = result.getContent().stream().map(entity -> modelMapper.map(entity, BookDTO.class))
				.collect(Collectors.toList());
		return new PageImpl<BookDTO>(list, pageRequest, result.getTotalElements());
	}

	@GetMapping("{id}/loans")
	@ApiOperation("Find loans by book.")
	@ApiResponses({ @ApiResponse(code = 200, message = "Loans succesfully finded.") })
	public Page<LoanDTO> loansByBook(@PathVariable Long id, Pageable pageable) {
		Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		Page<Loan> result = loanService.getLoansByBook(book, pageable);
		List<LoanDTO> list = result.getContent()
				.stream()
				.map(entity -> {
					Book loanBook = entity.getBook();
					BookDTO bookDTO = modelMapper.map(loanBook, BookDTO.class);
					LoanDTO loanDTO = modelMapper.map(entity, LoanDTO.class);
					loanDTO.setBook(bookDTO);
					return loanDTO;
				}).collect(Collectors.toList());
		return new PageImpl<LoanDTO>(list, pageable, result.getTotalElements());
	}

}
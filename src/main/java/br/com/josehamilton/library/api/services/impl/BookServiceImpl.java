package br.com.josehamilton.library.api.services.impl;

import br.com.josehamilton.library.api.model.entity.Book;
import br.com.josehamilton.library.api.model.repositories.BookRepository;
import br.com.josehamilton.library.api.services.BookService;
import br.com.josehamilton.library.exception.BusinessException;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {

    private BookRepository repository;

    public BookServiceImpl(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {
        if ( repository.existsByIsbn( book.getIsbn() ) ) {
            throw new BusinessException("Isbn já cadastrado.");
        }
        return repository.save(book);
    }
}

package br.com.josehamilton.library.api.model.repositories;

import br.com.josehamilton.library.api.model.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);

}

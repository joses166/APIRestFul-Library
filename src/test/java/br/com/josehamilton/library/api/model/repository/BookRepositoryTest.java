package br.com.josehamilton.library.api.model.repository;

import br.com.josehamilton.library.api.model.entity.Book;
import br.com.josehamilton.library.api.model.repositories.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base com o isbn informado.")
    public void returnTrueWhenIsbnExists() {
        // Cenário
        String isbn = "123";
        Book book = createNewBook(isbn);
        entityManager.persist( book );
        // Execução
        boolean exists = this.repository.existsByIsbn(isbn);
        // Verificação
        assertThat(exists).isTrue();
    }

    private Book createNewBook(String isbn) {
        return Book.builder().title("Aventuras").author("Fulano").isbn(isbn).build();
    }

    @Test
    @DisplayName("Deve retornar falso quando não existir um livro na base com o isbn informado.")
    public void returnFalseWhenIsbnDoesNotExist() {
        // Cenário
        String isbn = "123";
        // Execução
        boolean exists = this.repository.existsByIsbn(isbn);
        // Verificação
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve obter um livro por id.")
    public void findByIdTest() {
        // Cenário
        Book book = createNewBook("123");
        entityManager.persist(book);
        // Execução
        Optional<Book> foundBook = this.repository.findById(book.getId());
        // Verificação
        assertThat( foundBook.isPresent() ).isTrue();
    }

    @Test
    @DisplayName("Deve salvar um livro.")
    public void saveBookTest() {
        // Cenário
        Book book = createNewBook("123");
        // Execução
        Book savedBook = this.repository.save( book );
        // Verificação
        assertThat( savedBook.getId() ).isNotNull();
    }

    @Test
    @DisplayName("Deve deletar um livro.")
    public void deleteBookTest() {
        // Cenário
        Book book = createNewBook("123");
        entityManager.persist(book);
        Book foundBook = entityManager.find(Book.class, book.getId());
        // Execução
        this.repository.delete( foundBook );
        // Verificação
        Book deletedBook = entityManager.find(Book.class, book.getId());
        assertThat( deletedBook ).isNull();
    }

}

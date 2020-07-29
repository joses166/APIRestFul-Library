package br.com.josehamilton.library.api.service;

import br.com.josehamilton.library.api.model.entity.Book;
import br.com.josehamilton.library.api.model.repositories.BookRepository;
import br.com.josehamilton.library.api.services.impl.BookServiceImpl;
import br.com.josehamilton.library.api.services.BookService;
import br.com.josehamilton.library.exception.BusinessException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp() {
        this.service = new BookServiceImpl( repository );
    }

    @Test
    @DisplayName("Deve salvar um livro.")
    public void saveBookTest() {

        // Cenário
        Book book = createValidBook();
        Mockito.when( repository.existsByIsbn(Mockito.anyString()) ).thenReturn( false );
        Mockito
                .when( repository.save(book) )
                .thenReturn(
                    Book
                            .builder()
                            .id(1l)
                            .title("As Aventuras")
                            .author("Fulano")
                            .isbn("123")
                            .build()
                );

        // Execução
        Book savedBook = service.save(book);

        // Verificação
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getAuthor()).isEqualTo(book.getAuthor());
        assertThat(savedBook.getTitle()).isEqualTo(book.getTitle());
        assertThat(savedBook.getIsbn()).isEqualTo(book.getIsbn());

    }

    @Test
    @DisplayName("Deve lançar erro de regra de negócio ao tentar salvar um livro com isbn duplicado.")
    public void shouldNotSaveABookWithDuplicatedIsbn() {

        // Cenario
        Book book = createValidBook();
        Mockito.when( repository.existsByIsbn(Mockito.anyString()) ).thenReturn( true );

        // Execução
        Throwable exception = Assertions.catchThrowable( () -> service.save(book) );

        // Verificação
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já cadastrado.");

        Mockito.verify( repository, Mockito.never() ).save(book);

    }

    // Metodo para criação de um livro válido
    private Book createValidBook() {
        return Book.builder().title("As Aventuras").author("Fulano").isbn("123").build();
    }

}

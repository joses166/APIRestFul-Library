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
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    @Test
    @DisplayName("Deve obter um livro por id.")
    public void getByIdTest() {
        // Cenário
        Long id = 1l;
        Book book = createValidBook();
        book.setId(id);
        Mockito.when( repository.findById(id) ).thenReturn( Optional.of(book) );
        // Execução
        Optional<Book> foundBook = service.getById(id);
        // Verificação
        assertThat( foundBook.isPresent() ).isTrue();
        assertThat( foundBook.get().getId() ).isEqualTo( id );
        assertThat( foundBook.get().getAuthor() ).isEqualTo( book.getAuthor() );
        assertThat( foundBook.get().getTitle() ).isEqualTo( book.getTitle() );
        assertThat( foundBook.get().getIsbn() ).isEqualTo( book.getIsbn() );
    }

    @Test
    @DisplayName("Deve retornar vazio ao obter um livro por Id quando ele não existe na base de dados.")
    public void bookNotFoundByIdTest() {
        // Cenário
        Long id = 1l;
        Mockito.when( repository.findById(id) ).thenReturn( Optional.empty() );
        // Execução
        Optional<Book> foundBook = service.getById(id);
        // Verificação
        assertThat( foundBook.isPresent() ).isFalse();
    }

    @Test
    @DisplayName("Deve excluir um livro a partir do seu Id.")
    public void deleteBookTest() {
        // Cenário
        Book book = Book.builder().id(1l).build();
        // Execução
        org.junit.jupiter.api.Assertions.assertDoesNotThrow( () -> service.delete( book ) );
        // Verificação
        Mockito.verify( repository, Mockito.times(1) ).delete(book);
    }

    @Test
    @DisplayName("Deve retornar um erro ao tentar excluir um livro a partir de um Id inexistente.")
    public void deleteInexistentBookTest() {
        // Cenário
        Book book = new Book();
        // Execução
        org.junit.jupiter.api.Assertions.assertThrows( IllegalArgumentException.class, () -> service.delete( book ) );
        // Verificação
        Mockito.verify( repository, Mockito.never() ).delete(book);
    }

    @Test
    @DisplayName("Deve alterar um livro.")
    public void updateBookTest() {
        // Cenário
        Long id = 1l;
        Book updatingBook = Book.builder().id(id).build();
        Book updatedBook = createValidBook();
        updatedBook.setId(id);
        Mockito.when( repository.save( updatingBook ) ).thenReturn( updatedBook );
        // Execução
        Book book = service.update(updatingBook);
        // Verificação
        assertThat( book.getId() ).isEqualTo( id );
        assertThat( book.getAuthor() ).isEqualTo( updatedBook.getAuthor() );
        assertThat( book.getTitle() ).isEqualTo( updatedBook.getTitle() );
        assertThat( book.getIsbn() ).isEqualTo( updatedBook.getIsbn() );
    }

    @Test
    @DisplayName("Deve retornar um erro ao tentar alterar um livro inexistente.")
    public void updateInexistentBookTest() {
        // Cenário
        Book updatedBook = new Book();
        // Execução
        org.junit.jupiter.api.Assertions.assertThrows( IllegalArgumentException.class, () -> service.update(updatedBook) );
        // Verificação
        Mockito.verify( repository, Mockito.never() ).delete(updatedBook);
    }

    @Test
    @DisplayName("Deve filtrar livros pelas propriedades.")
    public void findBookTest() {
        // Cenário
        Book book = createValidBook();
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Book> list = Arrays.asList(book);
        Page<Book> page = new PageImpl<>( list, pageRequest, 1);
        Mockito.when( repository.findAll( Mockito.any(Example.class), Mockito.any(Pageable.class) ) )
                .thenReturn( page );
        // Execução
        Page<Book> result = service.find( book, pageRequest );
        // Verificação
        assertThat( result.getTotalElements() ).isEqualTo( 1 );
        assertThat( result.getContent() ).isEqualTo( list );
        assertThat( result.getPageable().getPageNumber() ).isEqualTo( 0 );
        assertThat( result.getPageable().getPageSize() ).isEqualTo( 10 );
    }

    // Metodo para criação de um livro válido
    private Book createValidBook() {
        return Book.builder().title("As Aventuras").author("Fulano").isbn("123").build();
    }

}

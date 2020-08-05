package br.com.josehamilton.library.api.dtos;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDTO {

    private String isbn;
    private String customer;

}
